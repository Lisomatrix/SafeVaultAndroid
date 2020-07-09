package pt.lisomatrix.safevault

import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import pt.lisomatrix.safevault.crypto.AuthHandler
import pt.lisomatrix.safevault.database.SafeVaultDatabase
import pt.lisomatrix.safevault.database.dao.AccountDao
import pt.lisomatrix.safevault.database.dao.VaultFileDao
import java.util.*
import javax.crypto.Cipher
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun provideAuthHandler(@ApplicationContext context: Context) : AuthHandler
            = AuthHandler(context)

    @Provides
    fun provideAccountDao(@ApplicationContext context: Context) : AccountDao
            = SafeVaultDatabase.getDatabase(context).accountDao()

    @Provides
    fun provideVaultFileDao(@ApplicationContext context: Context) : VaultFileDao
            = SafeVaultDatabase.getDatabase(context).vaultFileDao()

    @Provides
    fun provideContext(@ApplicationContext context: Context) : Context
            = context.applicationContext
}