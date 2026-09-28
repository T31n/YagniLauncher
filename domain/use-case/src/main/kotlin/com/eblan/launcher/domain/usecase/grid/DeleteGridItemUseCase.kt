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
package com.eblan.launcher.domain.usecase.grid

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.framework.AppWidgetHostWrapper
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.folder.asPreviewFolders
import com.eblan.launcher.domain.usecase.util.deleteGridItemData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteGridItemUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val gridRepository: GridRepository,
    private val appWidgetHostWrapper: AppWidgetHostWrapper,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val folderGridItemRepository: FolderGridItemRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(gridItem: GridItem) = withContext(defaultDispatcher) {
        val userData = userDataRepository.userDataFlow.first()

        val previewFolderGridItems =
            folderGridItemRepository.getFolderGridItemWrappers()
                .asPreviewFolders(
                    maxFolderColumns = userData.folderSettings.maxFolderColumns,
                    maxFolderRows = userData.folderSettings.maxFolderRows,
                )

        deleteGridItemData(
            gridItem = gridItem,
            previewFolderGridItems = previewFolderGridItems,
            appWidgetHostWrapper = appWidgetHostWrapper,
            launcherAppsWrapper = launcherAppsWrapper,
        )

        gridRepository.deleteGridItem(gridItem = gridItem)
    }
}
