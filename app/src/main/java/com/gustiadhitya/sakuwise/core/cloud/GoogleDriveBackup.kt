package com.gustiadhitya.sakuwise.core.cloud

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Drive backup wrapper — REQ-2.
 *
 * Uses the **AppDataFolder** scope (`drive.appdata`) so all uploaded files live
 * in a hidden per-app folder the user cannot see in the Drive UI. This is the
 * Google-recommended pattern for app-private cloud backup.
 *
 * The same encrypted `.sakuwise` blob produced by [com.gustiadhitya.sakuwise.core.crypto.BackupService]
 * is uploaded as-is — the file is already PIN-encrypted client-side so this
 * remains zero-knowledge with respect to Google.
 *
 * NOTE: There is no auto-sign-in. Callers must explicitly invoke [signIn]
 * from a foreground Activity.
 */
@Singleton
class GoogleDriveBackup @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {

    /** ActivityResult code for the GoogleSignIn intent. The Activity must
     *  override onActivityResult and call [handleSignInResult]. */
    @Suppress("MemberVisibilityCanBePrivate")
    val signInRequestCode: Int = REQUEST_CODE_SIGN_IN

    private fun signInOptions(): GoogleSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()

    /** Synchronous check using cached GoogleSignInAccount. No network. */
    suspend fun isSignedIn(): Boolean = withContext(Dispatchers.IO) {
        val acct = GoogleSignIn.getLastSignedInAccount(appContext) ?: return@withContext false
        GoogleSignIn.hasPermissions(acct, Scope(DriveScopes.DRIVE_APPDATA))
    }

    /**
     * Kicks off Google Sign-In. The caller MUST forward `onActivityResult` to
     * [handleSignInResult]. Returns the launch Intent so the caller can decide
     * which startActivityForResult flavor to use.
     */
    fun buildSignInIntent(): Intent {
        val client = GoogleSignIn.getClient(appContext, signInOptions())
        return client.signInIntent
    }

    /**
     * Convenience suspend wrapper around the full sign-in flow. Uses the
     * Activity's onActivityResult callback — callers without that plumbing
     * should use [buildSignInIntent] + [handleSignInResult] manually.
     */
    suspend fun signIn(activity: Activity): Result<String> = runCatching {
        // We can't actually await the activity result inside this method without
        // an ActivityResultLauncher — so callers go through buildSignInIntent +
        // handleSignInResult. This helper only checks if the user is already
        // signed in; if not, it throws so the UI knows to launch the intent.
        val existing = GoogleSignIn.getLastSignedInAccount(activity)
        if (existing != null && GoogleSignIn.hasPermissions(existing, Scope(DriveScopes.DRIVE_APPDATA))) {
            existing.email ?: error("Signed-in account has no email")
        } else {
            error("NOT_SIGNED_IN")
        }
    }

    /** Parses the GoogleSignIn intent result. Call from onActivityResult. */
    suspend fun handleSignInResult(data: Intent?): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount = task.await()
            account.email ?: error("Signed-in account has no email")
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        val client = GoogleSignIn.getClient(appContext, signInOptions())
        runCatching { client.signOut().await() }
        runCatching { client.revokeAccess().await() }
        Unit
    }

    /**
     * Upload `localFile` into the AppData folder. `name` becomes the Drive
     * filename. Returns the Drive file id.
     */
    suspend fun upload(localFile: File, name: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val drive = driveService() ?: error("NOT_SIGNED_IN")
                val metadata = com.google.api.services.drive.model.File().apply {
                    this.name = name
                    parents = listOf("appDataFolder")
                }
                val media = FileContent("application/octet-stream", localFile)
                val created = drive.files()
                    .create(metadata, media)
                    .setFields("id, name, createdTime, size")
                    .execute()
                created.id
            }
        }

    suspend fun listBackups(): Result<List<DriveBackupEntry>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val drive = driveService() ?: error("NOT_SIGNED_IN")
                val resp = drive.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("files(id, name, createdTime, size)")
                    .setOrderBy("createdTime desc")
                    .setPageSize(100)
                    .execute()
                resp.files.orEmpty().map { f ->
                    DriveBackupEntry(
                        id = f.id,
                        name = f.name ?: "(unnamed)",
                        createdAt = f.createdTime?.value ?: 0L,
                        sizeBytes = f.getSize() ?: 0L,
                    )
                }
            }
        }

    suspend fun download(fileId: String, target: File): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val drive = driveService() ?: error("NOT_SIGNED_IN")
                FileOutputStream(target).use { out ->
                    drive.files().get(fileId).executeMediaAndDownloadTo(out)
                }
                Unit
            }
        }

    suspend fun delete(fileId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val drive = driveService() ?: error("NOT_SIGNED_IN")
                drive.files().delete(fileId).execute()
                Unit
            }
        }

    // --- Internals -------------------------------------------------------

    private fun driveService(): Drive? {
        val account = GoogleSignIn.getLastSignedInAccount(appContext) ?: return null
        if (!GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_APPDATA))) return null
        // account.account is the platform Account object already registered with
        // AccountManager by Google Play Services. Constructing Account(email,
        // "com.google") manually fails with CommonStatusCodes.INTERNAL_ERROR (8)
        // when GMS can't find a matching entry in AccountManager — which is the
        // root cause of the user-visible "Upload gagal: 8" error.
        val platformAccount = account.account ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(
            appContext,
            listOf(DriveScopes.DRIVE_APPDATA),
        ).apply {
            selectedAccount = platformAccount
        }
        // AndroidHttp.newCompatibleTransport() was removed in google-http-client
        // 1.41+. NetHttpTransport is the documented Android-compatible
        // replacement (works on minSdk 26+ which we ship).
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential,
        )
            .setApplicationName("Sakuwise")
            .build()
    }

    companion object {
        const val REQUEST_CODE_SIGN_IN = 9911
    }
}

data class DriveBackupEntry(
    val id: String,
    val name: String,
    val createdAt: Long,
    val sizeBytes: Long,
)
