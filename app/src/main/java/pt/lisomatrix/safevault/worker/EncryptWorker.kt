package pt.lisomatrix.safevault.worker

import android.app.Activity
import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.Worker
import androidx.work.WorkerParameters
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.database.dao.VaultFileDao
import pt.lisomatrix.safevault.model.VaultFile
import pt.lisomatrix.safevault.ui.auth.AuthActivity
import pt.lisomatrix.safevault.ui.home.HomeActivity
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.Key
import java.security.KeyStore
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import kotlin.random.Random

class EncryptWorker @WorkerInject
constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val vaultFileDao: VaultFileDao
) : Worker(context, workerParameters) {

    private val appFilePath: String
            = context.filesDir.absolutePath

    // My ears bleed because I forgot to prevent it from making sound at every change
    private val builder = NotificationCompat.Builder(context, "ENCRYPT_CHANNEL")
        .setOngoing(true)
        .setSmallIcon(R.drawable.logo)
        .setContentTitle("SafeVault Working")
        .setContentText("File will be added when complete")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setOnlyAlertOnce(true)

    private val notificationID = Random.nextInt()

    override fun doWork(): Result {

        // Get params in order to work
        val param = workerParameters.inputData.getString("uri")

        var uri = Uri.parse(param)

        // Get file information such as name and size
        val fileInfo = getFileName(uri, param!!)!!

        // Get file extension
        val fileExtension = getExtension(fileInfo.first)
        // Define encrypted file name
        // TODO: THIS SHOULD BE A RANDOM ID
        val fileToWrite = "${fileInfo.first}.enc"

        // Start notification progress
        with(NotificationManagerCompat.from(context)) {
            builder.setProgress(100, 0, false)
            builder.setContentTitle("Encrypting ${fileInfo.first}")
            notify(notificationID, builder.build())
        }

        // Create file
        val newFile = File(appFilePath, fileToWrite)
        // Open file to encrypt
        val stream
                = context.contentResolver.openInputStream(uri)

        // Encrypt data and get IV to store it
        val iv = encryptData(stream, newFile.outputStream(), fileInfo.second)

        // Create file
        val file = VaultFile().apply {
            name = fileInfo.first
            size = fileInfo.second
            extension = fileExtension!!
            path = newFile.absolutePath
            key = iv
        }

        // Update database
        vaultFileDao.insert(file)

        // Delete file that was encrypted
        DocumentFile.fromSingleUri(context, uri)?.delete().toString()

        // Update notification
        with(NotificationManagerCompat.from(context)) {
            builder
                .setSmallIcon(R.drawable.logo)
                .setContentText("File added")
                .setContentTitle("${fileInfo.first} encrypted")
                .setProgress(0, 0, false)
                .setTimeoutAfter(5000)
                .setOnlyAlertOnce(false)
                .setAutoCancel(true)
            notify(notificationID, builder.build())
            Log.d("PASSED", "PASSED")
        }

        return Result.success()
    }

    private fun getKey(): Key {
        val store = KeyStore.getInstance("AndroidKeyStore")
        store.load(null)

        return store.getKey("SafeVaultKey", null)
    }

    private fun encryptData(
        stream: InputStream?,
        outputStream: FileOutputStream,
        total: Long
    ): ByteArray? {
        // Initialize cipher
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")

        cipher.init(Cipher.ENCRYPT_MODE, getKey())

        // Get IV in order to store it
        val ivBytes = cipher.iv

        // Initialize encrypted stream
        val fis = CipherInputStream(stream, cipher)

        // Variable in order to track progress
        var totalRead = 0L
        var bytesRead: Int
        // Buffer
        val data = ByteArray(4096)
        // Progress
        var progress: Int = 0

        // While there are bytes read
        while (fis.read(data).also { bytesRead = it } != -1) {
            // Write them out
            outputStream.write(data, 0, bytesRead)
            // Update total data read
            totalRead += bytesRead

            // Calculate progress (lazy to clean it)
            val newProgress = ((totalRead + 0.0) / (total + 0.0) * (100L + 0.0)).toInt()
            // Only update if the progress turns from 9 to 10 and not 9.00001 to 9.00002
            // Have to limit the update frequency or android might drop some notifications
            if (newProgress != progress && newProgress - progress > 5) {
                // Update notification
                with (NotificationManagerCompat.from(context)) {
                    builder.setProgress(100, newProgress, false)
                    notify(notificationID, builder.build())
                }
                // Save new progress
                progress = newProgress
            }
        }

        // Fecha a torneira
        outputStream.close()
        stream?.close()
        fis.close()

        return ivBytes
    }

    private fun getExtension(fileName: String): String? {
        var extension: String? = ""

        val i = fileName.lastIndexOf('.')
        if (i > 0) {
            extension = fileName.substring(i + 1)
        }
        return extension
    }

    /**
     * This is ugly so no need to touch it
     */
    private fun getFileName(uri: Uri, path: String): Pair<String, Long>? {
        var result: Pair<String, Long>? = null
        if (uri.scheme == "content") {
            val cursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
            cursor.use { cursor ->
                if (cursor.moveToFirst()) {
                    result = Pair(
                        cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)),
                        cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE))
                    )
                }
            }
        } else {
            val file = File(path)
            result = Pair(file.name, file.length())
        }
        return result
    }
}