package pt.lisomatrix.safevault.ui.home.options.myfiles

import android.app.KeyguardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.lisomatrix.safevault.database.dao.VaultFileDao
import pt.lisomatrix.safevault.model.VaultFile
import pt.lisomatrix.safevault.worker.DecryptWorker
import pt.lisomatrix.safevault.worker.EncryptWorker
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Function
import java.util.stream.Collectors
import javax.crypto.KeyGenerator
import kotlin.collections.ArrayList


class MyFilesViewModel @ViewModelInject
        constructor(private val vaultFileDao: VaultFileDao, private val context: Context) : ViewModel() {

    // Data to save on orientation change
    // Not to manipulate inside of this class
    // READ ONLY
    var isMultiSelectOn: Boolean = false
    var selectedIds: MutableList<Long> = ArrayList()

    /**
     * Delete the [VaultFile]s with given IDs from the local database
     *
     * @param [ids] to be deleted
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun deleteVaultFiles(ids: LongArray) {
        viewModelScope.launch(Dispatchers.IO) {
            val files = vaultFileDao.getByIds(ids)

            files.pmap { file ->
                try {
                    // Delete file that was encrypted
                    File(file.path).delete()
                } catch (ex: Exception) {
                    Log.d("FILE_DELETE_ERROR", "Could not delete file: " + file.path)
                }

                vaultFileDao.delete(file)
            }
        }
    }

    fun <T, R> Iterable<T>.pmap(
        numThreads: Int = Runtime.getRuntime().availableProcessors() - 2,
        exec: ExecutorService = Executors.newFixedThreadPool(numThreads),
        transform: (T) -> R): List<R> {

        // default size is just an inlined version of kotlin.collections.collectionSizeOrDefault
        val defaultSize = if (this is Collection<*>) this.size else 10
        val destination = Collections.synchronizedList(ArrayList<R>(defaultSize))

        for (item in this) {
            exec.submit { destination.add(transform(item)) }
        }

        exec.shutdown()
        exec.awaitTermination(1, TimeUnit.DAYS)

        return ArrayList<R>(destination)
    }

    /**
     * Get all [VaultFile]s from the database
     *
     */
    fun getVaultFiles(): Observable<List<VaultFile>> {
        // This bridge is ugly but Room database only supports RxJava2
        return RxJavaBridge.toV3Observable(vaultFileDao.getAll())
    }

    /**
     * Get all [VaultFile]s from the database with similar name
     *
     */
    fun getVaultFilesByName(name: String): Observable<List<VaultFile>> {
        // This bridge is ugly but Room database only supports RxJava2
        val searchParam = "%$name%"
        return RxJavaBridge.toV3Observable(vaultFileDao.getByName(searchParam))
    }


    fun generateKey() {

        // If device doesn't have a password don't even bother trying to require auth
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec
            .Builder("SafeVaultKey", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(false) // Have to keep this for now :(
            //.setUserAuthenticationRequired(keyguardManager.isDeviceSecure)
            // Have to do a rework, there currently bugs on the Samsung way of dealing with
            // this
            //.setUserAuthenticationValidityDurationSeconds(200) This is not secure
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    fun encryptFile(uri: Uri) {
        val inputData = Data.Builder()
            .putString("uri", uri.toString())
            .build()

        val encryptWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<EncryptWorker>()
            .setInputData(inputData)
            .build()

        WorkManager
            .getInstance(context)
            .enqueue(encryptWorkRequest)
    }

    fun decryptFile(id: Long, uri: Uri) {

        val inputData = Data.Builder()
            .putLong("id", id)
            .putString("uri", uri.toString())
            .build()

        val decryptWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<DecryptWorker>()
            .setInputData(inputData)
            .build()

        WorkManager
            .getInstance(context)
            .enqueue(decryptWorkRequest)

    }
}