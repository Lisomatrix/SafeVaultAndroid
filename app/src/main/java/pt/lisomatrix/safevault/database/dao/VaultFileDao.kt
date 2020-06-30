package pt.lisomatrix.safevault.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.lisomatrix.safevault.model.VaultFile

@Dao
interface VaultFileDao {

    @Insert
    fun insert(file: VaultFile): Long

    @Delete
    fun delete(file: VaultFile)

    @Update
    fun update(file: VaultFile)

    @Query("SELECT * FROM vault_file_table WHERE id = :id")
    fun get(id: Long): LiveData<VaultFile>

    @Query("SELECT * FROM vault_file_table")
    fun getAll(): LiveData<List<VaultFile>>

    @Query("delete from vault_file_table where id in (:idList)")
    fun deleteByIds(idList: LongArray)
}