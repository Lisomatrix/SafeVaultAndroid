package pt.lisomatrix.safevault.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.lisomatrix.safevault.model.Account

@Dao
interface AccountDao {

    @Insert
    fun insert(file: Account)

    @Delete
    fun delete(file: Account)

    @Query("DELETE FROM account_table")
    fun deleteAll()

    @Update
    fun update(file: Account)

    @Query("SELECT * FROM account_table WHERE accountID = :accountID AND password = :password")
    fun get(accountID: String, password: String): Account?

    @Query("SELECT * FROM account_table LIMIT 1")
    fun getAccount(): LiveData<Account>
}