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
import pt.lisomatrix.safevault.SafeVaultApplication
import pt.lisomatrix.safevault.database.dao.VaultFileDao
import pt.lisomatrix.safevault.model.VaultFile
import pt.lisomatrix.safevault.ui.home.HomeActivity
import pt.lisomatrix.safevault.worker.DecryptWorker
import pt.lisomatrix.safevault.worker.EncryptWorker
import java.io.File
import java.security.KeyStore
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Function
import java.util.stream.Collectors
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
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
    fun deleteVaultFiles(ids: LongArray) {
        viewModelScope.launch(Dispatchers.IO) {
            val files = vaultFileDao.getByIds(ids)

            val store = KeyStore.getInstance("AndroidKeyStore")
            store.load(null)

            files.pmap { file ->
                try {
                    // Delete file that was encrypted
                    File(file.path).delete()

                } catch (ex: Exception) {
                    Log.d("FILE_DELETE_ERROR", "Could not delete file: " + file.path)
                }

                // I don't find any information that it is thread safe
                // However still haven't get a crash, but it will keep it anyway
                synchronized(this) {
                   store.deleteEntry(file.alias)
                }
                vaultFileDao.delete(file)
            }
        }
    }

    fun isSelectingFile(isBeingSelected: Boolean) {
        val sharedPref = context
            .getSharedPreferences(SafeVaultApplication.APPLICATION_NAME, Context.MODE_PRIVATE)

        if (!sharedPref.getBoolean(HomeActivity.IS_FILE_BEING_SELECTED, !isBeingSelected)) {

            with (sharedPref.edit()) {
                putBoolean(HomeActivity.IS_FILE_BEING_SELECTED, true)
                commit()
            }
        }
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

    // Helper for parallel mapping
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
        exec.awaitTermination(2, TimeUnit.DAYS)

        return ArrayList<R>(destination)
    }
}