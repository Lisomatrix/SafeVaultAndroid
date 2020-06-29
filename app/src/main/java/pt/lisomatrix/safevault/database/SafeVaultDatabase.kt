package pt.lisomatrix.safevault.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import pt.lisomatrix.safevault.database.dao.AccountDao
import pt.lisomatrix.safevault.database.dao.VaultFileDao
import pt.lisomatrix.safevault.model.Account
import pt.lisomatrix.safevault.model.VaultFile
import java.io.File

@Database(entities = [VaultFile::class, Account::class], version = 1, exportSchema = false)
abstract class SafeVaultDatabase : RoomDatabase() {

    abstract fun vaultFileDao(): VaultFileDao

    abstract fun accountDao(): AccountDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: SafeVaultDatabase? = null

        fun deleteDatabaseFile(
            context: Context
        ) {
            val databases =
                File(context.applicationInfo.dataDir + "/databases")
            val db = File(databases, "safe_vault_database")
            if (db.delete()) println("Database deleted") else println("Failed to delete database")
            val journal = File(databases, "safe_vault_database")
            if (journal.exists()) {
                if (journal.delete()) println("Database deleted") else println("Failed to delete database")
            }
        }

        /**
         * Creates/Get a instance of the database
         *
         * The creation/get of this method is [synchronized]
         *
         * @param [context] android context
         * @return [SafeVaultDatabase] instance
         */
        fun getDatabase(context: Context, password: String? = null): SafeVaultDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null && INSTANCE?.isOpen!!) {
                return tempInstance
            }

            if (password == null)
                throw Exception("Database is not initialize and password is null")

            // TODO: Get a way to store the password in memory
            val supportFactory = SupportFactory(SQLiteDatabase.getBytes(password.toCharArray()))

            synchronized(this) {
                // Encrypted database
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SafeVaultDatabase::class.java,
                    context.applicationInfo.dataDir + "/databases/safe_vault_database")
                    .openHelperFactory(supportFactory)
                    .build()

                // Unecrypted database
                /*val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SafeVaultDatabase::class.java,
                    "safe_vault_database"
                ).build()*/
                INSTANCE = instance
                return instance
            }
        }
    }
}