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

import com.eblan.launcher.data.room.EblanDatabase
import com.eblan.launcher.data.room.dao.ApplicationInfoGridItemDao
import com.eblan.launcher.data.room.dao.EblanAppWidgetProviderInfoDao
import com.eblan.launcher.data.room.dao.EblanApplicationInfoDao
import com.eblan.launcher.data.room.dao.EblanApplicationInfoTagCrossRefDao
import com.eblan.launcher.data.room.dao.EblanApplicationInfoTagDao
import com.eblan.launcher.data.room.dao.EblanIconPackInfoDao
import com.eblan.launcher.data.room.dao.EblanShortcutConfigDao
import com.eblan.launcher.data.room.dao.EblanShortcutInfoDao
import com.eblan.launcher.data.room.dao.FolderEblanApplicationInfoDao
import com.eblan.launcher.data.room.dao.FolderGridItemDao
import com.eblan.launcher.data.room.dao.ShortcutConfigGridItemDao
import com.eblan.launcher.data.room.dao.ShortcutInfoGridItemDao
import com.eblan.launcher.data.room.dao.WidgetGridItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DaoModule {

    @Provides
    @Singleton
    fun applicationInfoGridItemDao(eblanDatabase: EblanDatabase): ApplicationInfoGridItemDao = eblanDatabase.applicationInfoGridItemDao()

    @Provides
    @Singleton
    fun widgetGridItemDao(eblanDatabase: EblanDatabase): WidgetGridItemDao = eblanDatabase.widgetGridItemDao()

    @Provides
    @Singleton
    fun shortcutInfoGridItemDao(eblanDatabase: EblanDatabase): ShortcutInfoGridItemDao = eblanDatabase.shortcutInfoGridItemDao()

    @Provides
    @Singleton
    fun eblanApplicationInfoDao(eblanDatabase: EblanDatabase): EblanApplicationInfoDao = eblanDatabase.eblanApplicationInfoDao()

    @Provides
    @Singleton
    fun eblanAppWidgetProviderInfoDao(eblanDatabase: EblanDatabase): EblanAppWidgetProviderInfoDao = eblanDatabase.eblanAppWidgetProviderInfoDao()

    @Provides
    @Singleton
    fun eblanShortcutInfoDao(eblanDatabase: EblanDatabase): EblanShortcutInfoDao = eblanDatabase.eblanShortcutInfoDao()

    @Provides
    @Singleton
    fun folderGridItemDao(eblanDatabase: EblanDatabase): FolderGridItemDao = eblanDatabase.folderGridItemDao()

    @Provides
    @Singleton
    fun eblanIconPackInfoDao(eblanDatabase: EblanDatabase): EblanIconPackInfoDao = eblanDatabase.eblanIconPackInfoDao()

    @Provides
    @Singleton
    fun eblanShortcutConfigDao(eblanDatabase: EblanDatabase): EblanShortcutConfigDao = eblanDatabase.eblanShortcutConfigDao()

    @Provides
    @Singleton
    fun shortcutConfigGridItemDao(eblanDatabase: EblanDatabase): ShortcutConfigGridItemDao = eblanDatabase.shortcutConfigGridItemDao()

    @Provides
    @Singleton
    fun eblanApplicationInfoTagCrossRefDao(eblanDatabase: EblanDatabase): EblanApplicationInfoTagCrossRefDao = eblanDatabase.eblanApplicationInfoTagCrossRefDao()

    @Provides
    @Singleton
    fun eblanApplicationInfoTagDao(eblanDatabase: EblanDatabase): EblanApplicationInfoTagDao = eblanDatabase.eblanApplicationInfoTagDao()

    @Provides
    @Singleton
    fun folderEblanApplicationInfoDao(eblanDatabase: EblanDatabase): FolderEblanApplicationInfoDao = eblanDatabase.folderEblanApplicationInfoDao()
}
