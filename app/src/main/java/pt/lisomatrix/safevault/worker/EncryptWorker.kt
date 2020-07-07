package pt.lisomatrix.safevault.worker

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.Worker
import androidx.work.WorkerParameters
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.database.dao.VaultFileDao
import pt.lisomatrix.safevault.model.VaultFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.Key
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
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
        .setPriority(NotificationCompat.PRIORITY_LOW)
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
        val fileToWrite = "${UUID.randomUUID().toString().replace("-", "")}.enc"

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

        val newAlias = UUID.randomUUID().toString().replace("-", "")

        val key = generateKey(newAlias) ?: return Result.failure()

        val outputStream = newFile.outputStream()

        // Encrypt data and get IV to store it
        val iv = encryptData(stream, outputStream, fileInfo.second, key)

        stream?.close()
        outputStream?.close()

        // Create file
        val file = VaultFile().apply {
            name = fileInfo.first
            size = fileInfo.second
            extension = fileExtension!!
            path = newFile.absolutePath
            this.iv = iv
            alias = newAlias
        }

        // Update database
        val id = vaultFileDao.insert(file)


        // Delete file that was encrypted
        DocumentFile.fromSingleUri(context, uri)?.delete().toString()

        // Update notification
        with(NotificationManagerCompat.from(context)) {
            // Remove previous notification
            // For some reason sometimes the notification is not updated
            cancel(notificationID)

            builder
                .setSmallIcon(R.drawable.logo)
                .setContentText("File added")
                .setContentTitle("${fileInfo.first} encrypted")
                .setProgress(0, 0, false)
                .setTimeoutAfter(5000)
                .setOnlyAlertOnce(false)
                .setAutoCancel(true)
            notify(notificationID, builder.build())
        }

        return Result.success()
    }

    private fun encryptData(
        stream: InputStream?,
        outputStream: FileOutputStream,
        total: Long,
        secretKey: SecretKey
    ): ByteArray? {
        // Initialize cipher
        //val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        // Get IV in order to store it
        val ivBytes = cipher.iv

        // Initialize encrypted stream
        val fis = CipherInputStream(stream, cipher)

        // Variable in order to track progress
        var totalRead = 0L
        var bytesRead: Int
        // Buffer
        val data = ByteArray(32 * 1024)
        //val data = ByteArray(4096)
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
        fis.close()

        return ivBytes
    }

    private fun generateKey(alias: String): SecretKey? {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec
            .Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false) // Have to keep this for now :(
            //.setUserAuthenticationRequired(keyguardManager.isDeviceSecure)
            // Have to do a rework, there currently bugs
            // on the Samsung way of dealing with this
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
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