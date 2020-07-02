package pt.lisomatrix.safevault.crypto

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.lisomatrix.safevault.SafeVaultApplication
import pt.lisomatrix.safevault.database.SafeVaultDatabase
import pt.lisomatrix.safevault.database.dao.AccountDao
import pt.lisomatrix.safevault.model.Account
import java.io.File
import java.security.MessageDigest
import java.util.*


/**
 * Class responsible for authenticating and registering the user
 */
class AuthHandler (context: Context) {

    /**
     * We want to store attempts, so when the user reaches 3 failed
     * attempts we silently delete all files and only notify when we are done
     */
    private var attempts: Int = 0

    private val context: Context = context.applicationContext

    private lateinit var accountDao: AccountDao

    /**
     * Register the user with the given password,
     * the account ID will be auto generated
     *
     * The method runs on [Dispatchers.IO]
     *
     * @param [password] password string to register
     * @return [String] returns the account ID used to register
     */
    suspend fun register(password: String): String {
        // Run on IO Thread
        return withContext(Dispatchers.IO) {
            // TODO: REPLACE THIS WITH A PROPER IMPLEMENTATION
            // Generate account ID
            var accountID = UUID.randomUUID().toString().replace("-", "")
            accountID = accountID.substring(0, accountID.length / 4)

            // Hash it
            val digest = MessageDigest.getInstance("SHA-256")
            val encodedHash = digest.digest(password.toByteArray(Charsets.UTF_8))

            // Create account
            val account = Account(accountID, encodedHash.contentToString())


            // Delete all files
            clearApplicationData()
            /*

            THIS IS THE BEST SOLUTION BUT IT CLOSES THE APP AND IT IS BAD UX

            val activityManager = (context.getSystemService(ACTIVITY_SERVICE) as ActivityManager)

            if (activityManager.clearApplicationUserData()) {
                Log.d("DELETE", "Application data deleted")
            } else {
                Log.d("DELETE", "Application data failed deleted")
            }*/

            // Clear shared preferences
            context.getSharedPreferences(SafeVaultApplication.APPLICATION_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()

            // Delete the most data possible
            nukeData()

            // Initialize database with new password
            // The database password is the junction of the
            // Account ID with the hashes password
            accountDao = SafeVaultDatabase.getDatabase(context.applicationContext,
                "$accountID-$password"
            ).accountDao()

            // Save new account
            accountDao.insert(account)

            return@withContext accountID
        }
    }

    private fun clearApplicationData() {

        val cache: File = context.cacheDir
        val appDir = File(cache.parent)
        if (appDir.exists()) {
            val children: Array<String> = appDir.list()
            for (s in children) {
                if (s != "lib") {
                    deleteDir(File(appDir, s))
                }
            }
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        return dir!!.delete()
    }

    /**
     * Authenticates user
     *
     * The method runs on [Dispatchers.IO]
     *
     * @param accountID ID given when registering
     * @param password password given when registering
     * @return whether user is authenticated or not
     */
    suspend fun login(accountID: String, password: String): Boolean {
        // Run on IO Thread
        return withContext(Dispatchers.IO) {
            // Hash it
            val digest = MessageDigest.getInstance("SHA-256")
            val encodedHash = digest.digest(password.toByteArray(Charsets.UTF_8))

            // Close DB
            // And open again in order to try new password
            // If a exception is throw then it means that the database
            // was not open in the first place
            try {
                SafeVaultDatabase.getDatabase(context).close()
            } catch (ex: Exception) {}

            accountDao = SafeVaultDatabase
                .getDatabase(context, "$accountID-$password")
                .accountDao()

            try {
                // Compare with database results
                val result = accountDao.get(accountID, encodedHash.contentToString())

                // Check if there was a found value
                val found = result != null

                // Update attempts logic
                updateAttempts(found)

                return@withContext found
            } catch (e: Exception) {
                // If it crashed then the database key is not correct
                Log.d("DATABASE_EXCEPTION", e.toString())
                return@withContext false
            }
        }
    }

    /**
     * Updates the attempts number according to the given param
     * when it hits the [MAX_ATTEMPTS] it calls [nukeData]
     */
    private fun updateAttempts(found: Boolean) {
        if (!found)
            attempts++
        else
            attempts = 0

        if (attempts == 3) {
            // Delete the most data possible
            nukeData()
        }
    }

    /**
     * Should be called when attempts hit their max threshold,
     * or when a new account is registered and when there is a suspicion
     * that data is compromised
     *
     * As the name says it attempts to destroy the most data possible
     * including tables and files
     */
    private fun nukeData() {
        // TODO: DELETE ALL FILES PROBABLY WITH A WORKER

        // Try to close database
        // If it crashes then database was not initialized
        // in the first place
        try {
            val database = SafeVaultDatabase.getDatabase(context.applicationContext)
            if (database.isOpen) {
                SafeVaultDatabase.getDatabase(context.applicationContext).clearAllTables()
                SafeVaultDatabase.getDatabase(context.applicationContext).close()
            }
        } catch (exception: Exception) {}

        // Delete database file
        SafeVaultDatabase.deleteDatabaseFile(context.applicationContext)
    }

    companion object {
        private const  val MAX_ATTEMPTS = 3;
    }
}