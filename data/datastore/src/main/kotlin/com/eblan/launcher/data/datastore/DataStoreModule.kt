package com.eblan.launcher.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.eblan.launcher.data.datastore.proto.UserDataProto
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    internal fun userDataStore(
        @ApplicationContext context: Context,
        @Dispatcher(EblanDispatchers.IO) ioDispatcher: CoroutineDispatcher,
        userDataSerializer: UserDataSerializer,
    ): DataStore<UserDataProto> = DataStoreFactory.create(
        serializer = userDataSerializer,
        scope = CoroutineScope(ioDispatcher),
        migrations = listOf(
            DataStoreMigration(),
        ),
    ) {
        context.dataStoreFile("user_data.pb")
    }
}