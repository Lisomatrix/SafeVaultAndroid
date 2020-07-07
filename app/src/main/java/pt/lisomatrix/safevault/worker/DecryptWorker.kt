package pt.lisomatrix.safevault.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.Worker
import androidx.work.WorkerParameters
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.database.dao.VaultFileDao
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.security.Key
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import kotlin.random.Random


class DecryptWorker @WorkerInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val vaultFileDao: VaultFileDao
    ) : Worker(context, workerParameters) {

    // My ears bleed because I forgot to prevent it from making sound at every change
    private val builder = NotificationCompat.Builder(context, "ENCRYPT_CHANNEL")
        .setOngoing(true)
        .setSmallIcon(R.drawable.logo)
        .setContentTitle("SafeVault Working")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setOnlyAlertOnce(true)

    private val notificationID = Random.nextInt()

    override fun doWork(): Result {

        // Get params in order to work
        val fileId = workerParameters.inputData.getLong("id", -1)

        if (fileId == -1L) return Result.failure()

        val vaultFile = vaultFileDao.get(fileId).blockingGet() ?: return Result.failure()

        val uri = Uri.parse(workerParameters.inputData.getString("uri"))

        // Update notification
        with(NotificationManagerCompat.from(context)) {
            builder.setProgress(100, 0, false)
            builder.setContentTitle("Decrypting ${vaultFile.name}")
            notify(notificationID, builder.build())
        }

        // Get selected folder and create a file inside
        val folder = DocumentFile.fromTreeUri(context, uri)
        val newFile = folder?.createFile(vaultFile.extension, vaultFile.name)

        // Get encrypted file
        val file = File(vaultFile.path)

        // Open the streams
        val encryptedStream = file.inputStream()
        val outputStream = context.contentResolver.openOutputStream(newFile!!.uri)!!

        // Create new decrypted file
        decryptData(vaultFile.key!!, encryptedStream, outputStream, file.length())

        // Close the streams (fecha a torneira)
        outputStream.close()
        encryptedStream.close()

        // Update notification
        with(NotificationManagerCompat.from(context)) {
            builder
                .setContentTitle("${vaultFile.name} decrypted")
                .setProgress(0, 0, false)
                .setTimeoutAfter(10000)
                .setAutoCancel(true)
                .setContentIntent(openFile(newFile.uri))
                .setOnlyAlertOnce(false)
            notify(notificationID, builder.build())
        }

        return Result.success()
    }

    private fun decryptData(
        ivBytes: ByteArray,
        encryptedData: FileInputStream,
        outputStream: OutputStream,
        total: Long
    ) {
        // Initialize cipher
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        // Set the IV used to encrypt file
        val spec = IvParameterSpec(ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)

        // Initialize encrypted stream
        val fis = CipherInputStream(encryptedData, cipher)

        // Variable in order to track progress
        var totalRead = 0L
        var bytesRead: Int

        // Progress
        var progress: Int = 0

        // Buffer
        //val data = ByteArray(4096)
        val data = ByteArray(32 * 1024)

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
    }

    private fun getKey(): Key {
        val store = KeyStore.getInstance("AndroidKeyStore")
        store.load(null)

        return store.getKey("SafeVaultKey", null)
    }

    /**
     * When user click the notification open the decrypted file
     */
    private fun openFile(uri: Uri): PendingIntent? {
        val mime: String? = context.contentResolver.getType(uri)

        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(uri, mime)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return PendingIntent.getActivity(context, 0, intent, 0)
    }
}