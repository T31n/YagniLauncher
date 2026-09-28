/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.data.room.di

import android.content.Context
import androidx.room.Room
import com.eblan.launcher.data.room.EblanDatabase
import com.eblan.launcher.data.room.dao.ApplicationInfoGridItemDao
import com.eblan.launcher.data.room.dao.EblanApplicationInfoDao
import com.eblan.launcher.data.room.dao.FolderEblanApplicationInfoDao
import com.eblan.launcher.data.room.dao.FolderGridItemDao
import com.eblan.launcher.data.room.dao.ShortcutConfigGridItemDao
import com.eblan.launcher.data.room.dao.ShortcutInfoGridItemDao
import com.eblan.launcher.data.room.dao.WidgetGridItemDao
import com.eblan.launcher.data.room.migration.Migration12To13
import com.eblan.launcher.data.room.migration.Migration13To14
import com.eblan.launcher.data.room.migration.Migration14To15
import com.eblan.launcher.data.room.migration.Migration15To16
import com.eblan.launcher.data.room.migration.Migration18To19
import com.eblan.launcher.data.room.migration.Migration19To20
import com.eblan.launcher.data.room.migration.Migration3To4
import com.eblan.launcher.data.room.migration.Migration7To8
import com.eblan.launcher.data.room.transaction.DefaultFolderGridItemEntityTransaction
import com.eblan.launcher.data.room.transaction.DefaultGridItemEntityTransaction
import com.eblan.launcher.data.room.transaction.FolderGridItemEntityTransaction
import com.eblan.launcher.data.room.transaction.GridItemEntityTransaction
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object RoomModule {
    @Singleton
    @Provides
    fun eblanLauncherDatabase(
        @ApplicationContext context: Context,
    ): EblanDatabase = Room.databaseBuilder(
        context,
        EblanDatabase::class.java,
        EblanDatabase.DATABASE_NAME,
    ).addMigrations(
        Migration3To4(),
        Migration7To8(),
        Migration12To13(),
        Migration13To14(),
        Migration14To15(),
        Migration15To16(),
        Migration18To19(),
        Migration19To20(),
    )
        .fallbackToDestructiveMigrationFrom(
            dropAllTables = true,
            1,
            2,
            11,
            17,
        ).build()

    @Singleton
    @Provides
    fun gridItemTransaction(
        eblanDatabase: EblanDatabase,
        applicationInfoGridItemDao: ApplicationInfoGridItemDao,
        widgetGridItemDao: WidgetGridItemDao,
        shortcutInfoGridItemDao: ShortcutInfoGridItemDao,
        shortcutConfigGridItemDao: ShortcutConfigGridItemDao,
        folderGridItemDao: FolderGridItemDao,
    ): GridItemEntityTransaction = DefaultGridItemEntityTransaction(
        eblanDatabase = eblanDatabase,
        applicationInfoGridItemDao = applicationInfoGridItemDao,
        widgetGridItemDao = widgetGridItemDao,
        shortcutInfoGridItemDao = shortcutInfoGridItemDao,
        shortcutConfigGridItemDao = shortcutConfigGridItemDao,
        folderGridItemDao = folderGridItemDao,
    )

    @Singleton
    @Provides
    fun folderGridItemTransaction(
        eblanDatabase: EblanDatabase,
        eblanApplicationInfoDao: EblanApplicationInfoDao,
        folderEblanApplicationInfoDao: FolderEblanApplicationInfoDao,
    ): FolderGridItemEntityTransaction = DefaultFolderGridItemEntityTransaction(
        eblanDatabase = eblanDatabase,
        eblanApplicationInfoDao = eblanApplicationInfoDao,
        folderEblanApplicationInfoDao = folderEblanApplicationInfoDao,
    )
}
