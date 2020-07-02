package pt.lisomatrix.safevault.ui.home.options.myfiles

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import androidx.work.*
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import pt.lisomatrix.safevault.database.dao.VaultFileDao
import pt.lisomatrix.safevault.model.VaultFile
import pt.lisomatrix.safevault.worker.DecryptWorker
import pt.lisomatrix.safevault.worker.EncryptWorker
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.Key
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


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
            vaultFileDao.deleteByIds(ids)
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


    fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec
            .Builder("SafeVaultKey", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
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