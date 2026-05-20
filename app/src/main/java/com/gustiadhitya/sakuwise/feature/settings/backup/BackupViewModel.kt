package com.gustiadhitya.sakuwise.feature.settings.backup

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gustiadhitya.sakuwise.core.crypto.BackupService
import com.gustiadhitya.sakuwise.core.crypto.BadPinException
import com.gustiadhitya.sakuwise.core.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
}

sealed interface RestoreState {
    data object Idle : RestoreState
    data object Success : RestoreState
}
