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
package com.eblan.launcher.data.repository.di

import com.eblan.launcher.data.repository.DefaultApplicationInfoGridItemRepository
import com.eblan.launcher.data.repository.DefaultEblanAppWidgetProviderInfoRepository
import com.eblan.launcher.data.repository.DefaultEblanApplicationInfoRepository
import com.eblan.launcher.data.repository.DefaultEblanApplicationInfoTagCrossRefRepository
import com.eblan.launcher.data.repository.DefaultEblanApplicationInfoTagRepository
import com.eblan.launcher.data.repository.DefaultEblanIconPackInfoRepository
import com.eblan.launcher.data.repository.DefaultEblanShortcutConfigRepository
import com.eblan.launcher.data.repository.DefaultEblanShortcutInfoRepository
import com.eblan.launcher.data.repository.DefaultFolderEblanApplicationInfoRepository
import com.eblan.launcher.data.repository.DefaultFolderGridItemRepository
import com.eblan.launcher.data.repository.DefaultGridRepository
import com.eblan.launcher.data.repository.DefaultShortcutConfigGridItemRepository
import com.eblan.launcher.data.repository.DefaultShortcutInfoGridItemRepository
import com.eblan.launcher.data.repository.DefaultUserDataRepository
import com.eblan.launcher.data.repository.DefaultWidgetGridItemRepository
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoTagCrossRefRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoTagRepository
import com.eblan.launcher.domain.repository.EblanIconPackInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface RepositoryModule {
    @Binds
    @Singleton
    fun userDataRepository(impl: DefaultUserDataRepository): UserDataRepository

    @Binds
    @Singleton
    fun eblanApplicationInfoRepository(impl: DefaultEblanApplicationInfoRepository): EblanApplicationInfoRepository

    @Binds
    @Singleton
    fun eblanAppWidgetProviderInfoRepository(impl: DefaultEblanAppWidgetProviderInfoRepository): EblanAppWidgetProviderInfoRepository

    @Binds
    @Singleton
    fun eblanShortcutInfoRepository(impl: DefaultEblanShortcutInfoRepository): EblanShortcutInfoRepository

    @Binds
    @Singleton
    fun applicationInfoGridItemRepository(impl: DefaultApplicationInfoGridItemRepository): ApplicationInfoGridItemRepository

    @Binds
    @Singleton
    fun widgetGridItemRepository(impl: DefaultWidgetGridItemRepository): WidgetGridItemRepository

    @Binds
    @Singleton
    fun shortcutInfoGridItemRepository(impl: DefaultShortcutInfoGridItemRepository): ShortcutInfoGridItemRepository

    @Binds
    @Singleton
    fun folderGridItemRepository(impl: DefaultFolderGridItemRepository): FolderGridItemRepository

    @Binds
    @Singleton
    fun iconPackRepository(impl: DefaultEblanIconPackInfoRepository): EblanIconPackInfoRepository

    @Binds
    @Singleton
    fun eblanShortcutConfigRepository(impl: DefaultEblanShortcutConfigRepository): EblanShortcutConfigRepository

    @Binds
    @Singleton
    fun shortcutConfigGridItemRepository(impl: DefaultShortcutConfigGridItemRepository): ShortcutConfigGridItemRepository

    @Binds
    @Singleton
    fun gridRepository(impl: DefaultGridRepository): GridRepository

    @Binds
    @Singleton
    fun eblanApplicationInfoTagCrossRefRepository(impl: DefaultEblanApplicationInfoTagCrossRefRepository): EblanApplicationInfoTagCrossRefRepository

    @Binds
    @Singleton
    fun eblanApplicationInfoTagRepository(impl: DefaultEblanApplicationInfoTagRepository): EblanApplicationInfoTagRepository

    @Binds
    @Singleton
    fun folderEblanApplicationInfoRepository(impl: DefaultFolderEblanApplicationInfoRepository): FolderEblanApplicationInfoRepository
}
