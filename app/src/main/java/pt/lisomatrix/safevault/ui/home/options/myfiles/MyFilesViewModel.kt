package pt.lisomatrix.safevault.ui.home.options.myfiles

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.lisomatrix.safevault.database.dao.VaultFileDao
import pt.lisomatrix.safevault.model.VaultFile

class MyFilesViewModel @ViewModelInject
        constructor(private val vaultFileDao: VaultFileDao) : ViewModel() {

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
    fun getVaultFiles(): LiveData<List<VaultFile>> {
        return vaultFileDao.getAll()
    }

    fun addFile(file: VaultFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = vaultFileDao.insert(file)
            print(id)
        }
    }
}