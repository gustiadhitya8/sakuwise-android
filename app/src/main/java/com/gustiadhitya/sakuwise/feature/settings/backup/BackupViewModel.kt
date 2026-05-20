package com.gustiadhitya.sakuwise.feature.settings.backup

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.cloud.DriveBackupEntry
import com.gustiadhitya.sakuwise.core.cloud.GoogleDriveBackup
import com.gustiadhitya.sakuwise.core.crypto.BackupService
import com.gustiadhitya.sakuwise.core.crypto.BadPinException
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.system.exitProcess

data class BackupUiState(
    val stage: Stage = Stage.Idle,
    val errorMessage: String? = null,
    val resultFilePath: String? = null,
    val resultFileSize: Long = 0L,
)

enum class Stage { Idle, Encrypting, PickLocation, Done }

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val app: Application,
    private val backupService: BackupService,
    private val prefsRepo: UserPreferencesRepository,
    private val drive: GoogleDriveBackup,
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state

    fun startBackup(pin: CharArray) {
        if (pin.size != 6) {
            _state.value = BackupUiState(errorMessage = "PIN harus 6 digit.")
            return
        }
        _state.value = BackupUiState(stage = Stage.Encrypting)
        viewModelScope.launch {
            try {
                val destDir = File(app.getExternalFilesDir(null), "backups")
                val file = backupService.backup(pin, destDir)
                pin.fill(0.toChar())
                prefsRepo.markBackupNow(System.currentTimeMillis())
                _state.value = BackupUiState(
                    stage = Stage.Done,
                    resultFilePath = file.absolutePath,
                    resultFileSize = file.length(),
                )
            } catch (t: Throwable) {
                _state.value = BackupUiState(errorMessage = "Backup gagal: ${t.message ?: "unknown"}")
            }
        }
    }

    fun restoreFromFile(file: File, pin: CharArray, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                backupService.restore(pin, file)
                pin.fill(0.toChar())
                // DON'T auto-relaunch — Android 14+ blocks AlarmManager PendingIntent
                // activity starts. Instead the UI shows a success screen with a
                // "Buka ulang aplikasi" button whose foreground click can legally
                // launch MainActivity (BAL-allowed since user-initiated).
                _restoreState.value = RestoreState.Success
            } catch (t: BadPinException) {
                onError(t.message ?: "PIN salah.")
            } catch (t: Throwable) {
                onError("Restore gagal: ${t.message ?: "unknown"}")
            }
        }
    }

    private val _restoreState = MutableStateFlow<RestoreState>(RestoreState.Idle)
    val restoreState: StateFlow<RestoreState> = _restoreState

    /**
     * Called from the foreground onClick of the "Buka ulang aplikasi" button on
     * the post-restore success card. Starts MainActivity in a clean task and
     * kills the current process so Hilt+Room+SQLCipher re-init against the
     * restored DB + DEK.
     */
    fun relaunchAfterRestore() {
        val ctx = app
        val intent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
            ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) }
            ?: return
        ctx.startActivity(intent)
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(0)
    }

    fun reset() {
        _state.value = BackupUiState()
        _restoreState.value = RestoreState.Idle
    }

    // ====================================================================
    // REQ-2 — Google Drive (AppDataFolder scope)
    // ====================================================================

    private val _driveState = MutableStateFlow(DriveUiState())
    val driveState: StateFlow<DriveUiState> = _driveState.asStateFlow()

    private val _driveBackups = MutableStateFlow<List<DriveBackupEntry>>(emptyList())
    val driveBackups: StateFlow<List<DriveBackupEntry>> = _driveBackups.asStateFlow()

    /** Caller starts sign-in via startActivityForResult with [GoogleDriveBackup.REQUEST_CODE_SIGN_IN]. */
    fun buildDriveSignInIntent(): Intent = drive.buildSignInIntent()

    fun onDriveSignInResult(data: Intent?) {
        viewModelScope.launch {
            _driveState.value = _driveState.value.copy(busy = true, error = null)
            val result = drive.handleSignInResult(data)
            result.fold(
                onSuccess = { email ->
                    prefsRepo.setDriveAccountEmail(email)
                    _driveState.value = _driveState.value.copy(busy = false, lastMessage = "Terhubung sebagai $email")
                },
                onFailure = { t ->
                    _driveState.value = _driveState.value.copy(
                        busy = false,
                        error = "Sign-in gagal: ${t.message ?: "unknown"}",
                    )
                },
            )
        }
    }

    fun signOutFromDrive() {
        viewModelScope.launch {
            _driveState.value = _driveState.value.copy(busy = true, error = null)
            drive.signOut()
            prefsRepo.setDriveAccountEmail(null)
            prefsRepo.setDriveBackupEnabled(false)
            _driveBackups.value = emptyList()
            _driveState.value = _driveState.value.copy(busy = false, lastMessage = "Logout dari Google Drive")
        }
    }

    fun setDriveAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setDriveBackupEnabled(enabled) }
    }

    /**
     * Upload the most-recent local `.sakuwise` backup to Drive. If no local
     * backup exists yet, surfaces an error — the user should create one via
     * the normal backup PIN flow first.
     */
    fun uploadLatestLocalBackupToDrive() {
        viewModelScope.launch {
            _driveState.value = _driveState.value.copy(busy = true, error = null)
            val dir = File(app.getExternalFilesDir(null), "backups")
            val latest = dir.listFiles { f -> f.isFile && f.name.endsWith(".sakuwise") }
                ?.maxByOrNull { it.lastModified() }
            if (latest == null) {
                _driveState.value = _driveState.value.copy(
                    busy = false,
                    error = "Belum ada file backup lokal. Buat backup dulu lewat tombol Backup Sekarang.",
                )
                return@launch
            }
            val result = drive.upload(latest, latest.name)
            result.fold(
                onSuccess = {
                    prefsRepo.markDriveBackupNow(System.currentTimeMillis())
                    _driveState.value = _driveState.value.copy(
                        busy = false,
                        lastMessage = "Backup terupload ke Google Drive",
                    )
                    refreshDriveBackups()
                },
                onFailure = { t ->
                    _driveState.value = _driveState.value.copy(
                        busy = false,
                        error = "Upload gagal: ${t.message ?: "unknown"}",
                    )
                },
            )
        }
    }

    fun refreshDriveBackups() {
        viewModelScope.launch {
            _driveState.value = _driveState.value.copy(busy = true, error = null)
            val result = drive.listBackups()
            result.fold(
                onSuccess = {
                    _driveBackups.value = it
                    _driveState.value = _driveState.value.copy(busy = false)
                },
                onFailure = { t ->
                    _driveState.value = _driveState.value.copy(
                        busy = false,
                        error = "Daftar Drive gagal: ${t.message ?: "unknown"}",
                    )
                },
            )
        }
    }

    /**
     * Download the chosen entry into the app cache and run the existing
     * PIN-based restore. The PIN supplied here is the BACKUP PIN the user
     * used when the file was originally created.
     */
    fun restoreFromDrive(entry: DriveBackupEntry, pin: CharArray, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val target = File(app.cacheDir, "drive-restore-${entry.id}.sakuwise")
                val dl = drive.download(entry.id, target)
                dl.getOrElse { t ->
                    onError("Download gagal: ${t.message ?: "unknown"}")
                    return@launch
                }
                backupService.restore(pin, target)
                pin.fill(0.toChar())
                runCatching { target.delete() }
                _restoreState.value = RestoreState.Success
            } catch (t: BadPinException) {
                onError(t.message ?: "PIN salah.")
            } catch (t: Throwable) {
                onError("Restore gagal: ${t.message ?: "unknown"}")
            }
        }
    }

    fun deleteDriveBackup(entry: DriveBackupEntry) {
        viewModelScope.launch {
            _driveState.value = _driveState.value.copy(busy = true, error = null)
            drive.delete(entry.id).fold(
                onSuccess = {
                    _driveState.value = _driveState.value.copy(busy = false, lastMessage = "Backup dihapus")
                    refreshDriveBackups()
                },
                onFailure = { t ->
                    _driveState.value = _driveState.value.copy(
                        busy = false,
                        error = "Hapus gagal: ${t.message ?: "unknown"}",
                    )
                },
            )
        }
    }

    fun clearDriveMessage() {
        _driveState.value = _driveState.value.copy(error = null, lastMessage = null)
    }
}

data class DriveUiState(
    val busy: Boolean = false,
    val error: String? = null,
    val lastMessage: String? = null,
)

sealed interface RestoreState {
    data object Idle : RestoreState
    data object Success : RestoreState
}
