package com.gustiadhitya.sakuwise.feature.settings.backup

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.R
import com.gustiadhitya.sakuwise.core.cloud.DriveBackupEntry
import com.gustiadhitya.sakuwise.core.cloud.GoogleDriveBackup
import com.gustiadhitya.sakuwise.core.crypto.AutoBackupPinStorage
import com.gustiadhitya.sakuwise.core.crypto.BackupService
import com.gustiadhitya.sakuwise.core.crypto.BackupVersionTooNewException
import com.gustiadhitya.sakuwise.core.crypto.BadPinException
import com.gustiadhitya.sakuwise.core.database.dao.AccountDao
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import com.gustiadhitya.sakuwise.core.work.DriveAutoBackupWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    private val accountDao: AccountDao,
    private val autoBackupPinStorage: AutoBackupPinStorage,
) : AndroidViewModel(app) {

    /** True once at least one account exists — used to gate the backup action. */
    val hasData: StateFlow<Boolean> = accountDao.observeAll()
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state

    fun startBackup(pin: CharArray) {
        if (pin.size != 6) {
            _state.value = BackupUiState(errorMessage = app.getString(R.string.backup_err_pin_6digit))
            return
        }
        _state.value = BackupUiState(stage = Stage.Encrypting)
        viewModelScope.launch {
            try {
                // Encrypt to cacheDir first — user picks the final save location
                // via ACTION_CREATE_DOCUMENT so the file survives uninstall/reinstall.
                val tempDir = File(app.cacheDir, "backups_temp").apply { mkdirs() }
                val file = backupService.backup(pin, tempDir)
                pin.fill(0.toChar())
                _pendingBackupFile = file
                _state.value = BackupUiState(
                    stage = Stage.PickLocation,
                    resultFilePath = file.absolutePath,
                    resultFileSize = file.length(),
                )
            } catch (t: Throwable) {
                _state.value = BackupUiState(
                    errorMessage = app.getString(R.string.backup_err_backup_failed_format, t.message ?: "unknown"),
                )
            }
        }
    }

    private var _pendingBackupFile: File? = null

    /** Called after user picks a save URI from ACTION_CREATE_DOCUMENT. */
    fun saveBackupToUri(uri: android.net.Uri) {
        val src = _pendingBackupFile ?: return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                app.contentResolver.openOutputStream(uri)?.use { out ->
                    src.inputStream().use { it.copyTo(out) }
                } ?: error(app.getString(R.string.backup_err_open_save_location))
                runCatching { src.delete() }
                _pendingBackupFile = null
                prefsRepo.markBackupNow(System.currentTimeMillis())
                val size = try {
                    app.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: src.length()
                } catch (_: Exception) { src.length() }
                _state.value = BackupUiState(
                    stage = Stage.Done,
                    resultFilePath = uri.lastPathSegment ?: uri.toString(),
                    resultFileSize = size,
                )
            } catch (t: Throwable) {
                _state.value = BackupUiState(errorMessage = app.getString(R.string.backup_err_save_failed_format, t.message ?: "unknown"))
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
                onError(app.getString(R.string.backup_err_pin_wrong))
            } catch (t: BackupVersionTooNewException) {
                onError(app.getString(R.string.backup_err_version_too_new))
            } catch (t: Throwable) {
                onError(app.getString(R.string.backup_err_restore_failed_format, t.message ?: "unknown"))
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
                    _driveState.value = _driveState.value.copy(
                        busy = false,
                        lastMessage = app.getString(R.string.backup_connected_as_format, email),
                    )
                },
                onFailure = { t ->
                    _driveState.value = _driveState.value.copy(
                        busy = false,
                        error = app.getString(R.string.backup_err_signin_failed_format, t.message ?: "unknown"),
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
            _driveState.value = _driveState.value.copy(
                busy = false,
                lastMessage = app.getString(R.string.backup_logout_drive),
            )
        }
    }

    /** True when a backup PIN is stored — used by the UI to show the stored-PIN chip. */
    val autoBackupPinStored: Boolean get() = autoBackupPinStorage.hasPin()

    /**
     * Enable auto-backup with a PIN the user just confirmed in the setup sheet.
     * Saves the PIN to [AutoBackupPinStorage] (Keystore-encrypted) and schedules
     * the daily worker. The [pin] array is zeroed after saving.
     */
    fun enableAutoBackupWithPin(pin: CharArray) {
        try {
            autoBackupPinStorage.savePin(pin)
        } finally {
            pin.fill(0.toChar())
        }
        viewModelScope.launch { prefsRepo.setDriveBackupEnabled(true) }
        DriveAutoBackupWorker.scheduleDaily(app)
    }

    /**
     * Disable auto-backup and clear the stored PIN so it is not left on disk.
     * Call when the user toggles auto-backup OFF.
     */
    fun disableAutoBackup() {
        autoBackupPinStorage.clearPin()
        viewModelScope.launch { prefsRepo.setDriveBackupEnabled(false) }
        DriveAutoBackupWorker.cancel(app)
    }

    /**
     * Change the stored auto-backup PIN. Shows the PIN setup sheet again;
     * once confirmed this replaces the old PIN in [AutoBackupPinStorage].
     * Worker is re-scheduled with REPLACE so the next run uses the new PIN.
     */
    fun changeAutoBackupPin(pin: CharArray) {
        try {
            autoBackupPinStorage.savePin(pin)
        } finally {
            pin.fill(0.toChar())
        }
        DriveAutoBackupWorker.scheduleDaily(app)
    }

    /**
     * Create a fresh encrypted backup using [pin] and upload it directly to
     * Drive. Does NOT depend on a pre-existing local backup file — the
     * encrypted blob is written to a private temp dir, uploaded, then deleted.
     */
    fun backupToDriveWithPin(pin: CharArray) {
        viewModelScope.launch {
            _driveState.value = _driveState.value.copy(busy = true, error = null)
            try {
                val tempDir = File(app.cacheDir, "drive_backup_temp").apply { mkdirs() }
                val file = backupService.backup(pin, tempDir)
                pin.fill(0.toChar())
                val result = drive.upload(file, file.name)
                result.fold(
                    onSuccess = {
                        // Persist a local copy so the daily auto-backup worker has a
                        // file to sync and so the backup-status hero shows a real date.
                        val localDir = File(app.getExternalFilesDir(null), "backups")
                            .apply { mkdirs() }
                        runCatching { file.copyTo(File(localDir, file.name), overwrite = true) }
                        runCatching { file.delete() }
                        val now = System.currentTimeMillis()
                        prefsRepo.markDriveBackupNow(now)
                        prefsRepo.markBackupNow(now)
                        _driveState.value = _driveState.value.copy(
                            busy = false,
                            lastMessage = app.getString(R.string.backup_drive_upload_success),
                        )
                        refreshDriveBackups()
                    },
                    onFailure = { t ->
                        runCatching { file.delete() }
                        _driveState.value = _driveState.value.copy(
                            busy = false,
                            error = app.getString(R.string.backup_err_upload_failed_format, t.message ?: "unknown"),
                        )
                    },
                )
            } catch (t: Throwable) {
                pin.fill(0.toChar())
                _driveState.value = _driveState.value.copy(
                    busy = false,
                    error = app.getString(R.string.backup_err_backup_failed_format, t.message ?: "unknown"),
                )
            }
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
                        error = app.getString(R.string.backup_err_list_failed_format, t.message ?: "unknown"),
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
                    onError(app.getString(R.string.backup_err_download_failed_format, t.message ?: "unknown"))
                    return@launch
                }
                backupService.restore(pin, target)
                pin.fill(0.toChar())
                runCatching { target.delete() }
                _restoreState.value = RestoreState.Success
            } catch (t: BadPinException) {
                onError(app.getString(R.string.backup_err_pin_wrong))
            } catch (t: BackupVersionTooNewException) {
                onError(app.getString(R.string.backup_err_version_too_new))
            } catch (t: Throwable) {
                onError(app.getString(R.string.backup_err_restore_failed_format, t.message ?: "unknown"))
            }
        }
    }

    fun deleteDriveBackup(entry: DriveBackupEntry) {
        viewModelScope.launch {
            _driveState.value = _driveState.value.copy(busy = true, error = null)
            drive.delete(entry.id).fold(
                onSuccess = {
                    _driveState.value = _driveState.value.copy(busy = false, lastMessage = app.getString(R.string.backup_deleted))
                    refreshDriveBackups()
                },
                onFailure = { t ->
                    _driveState.value = _driveState.value.copy(
                        busy = false,
                        error = app.getString(R.string.backup_delete_failed_format, t.message ?: "unknown"),
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
