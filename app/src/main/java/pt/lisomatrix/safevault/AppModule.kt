package pt.lisomatrix.safevault

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import pt.lisomatrix.safevault.crypto.AuthHandler
import pt.lisomatrix.safevault.database.SafeVaultDatabase
import pt.lisomatrix.safevault.database.dao.AccountDao
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun provideAuthHandler(@ApplicationContext context: Context) : AuthHandler
            = AuthHandler(context)

    @Singleton
    @Provides
    fun provideAccountDao(@ApplicationContext context: Context) : AccountDao
            = SafeVaultDatabase.getDatabase(context).accountDao()
}