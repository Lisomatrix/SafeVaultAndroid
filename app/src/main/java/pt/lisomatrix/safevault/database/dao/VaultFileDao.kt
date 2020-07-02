package pt.lisomatrix.safevault.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
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
    fun get(id: Long): Maybe<VaultFile>

    @Query("SELECT * FROM vault_file_table")
    fun getAll(): Observable<List<VaultFile>>
    // Changed to RXJava since I might need to cancel subs
    //fun getAll(): LiveData<List<VaultFile>>

    @Query("SELECT * FROM vault_file_table WHERE name LIKE :name")
    fun getByName(name: String): Observable<List<VaultFile>>
    // Changed to RXJava since I might need to cancel subs
    //fun getByName(name: String): LiveData<List<VaultFile>>

    @Query("delete from vault_file_table where id in (:idList)")
    fun deleteByIds(idList: LongArray): Int
}