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
package com.eblan.launcher.domain.usecase.page

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.AppWidgetHostWrapper
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.util.deleteGridItemData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdatePageItemsUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val gridRepository: GridRepository,
    private val appWidgetHostWrapper: AppWidgetHostWrapper,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val folderGridItemRepository: FolderGridItemRepository,
    private val fileManager: FileManager,
    private val iconKeyGenerator: IconKeyGenerator,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
        associate: Associate,
    ) {
        withContext(defaultDispatcher) {
            val userData = userDataRepository.userDataFlow.first()

            pageItemsToDelete.forEach {
                it.gridItems.forEach { gridItem ->
                    deleteGridItemData(
                        gridItem = gridItem,
                        appWidgetHostWrapper = appWidgetHostWrapper,
                        launcherAppsWrapper = launcherAppsWrapper,
                        folderGridItemRepository = folderGridItemRepository,
                        fileManager = fileManager,
                        iconKeyGenerator = iconKeyGenerator,
                        iconPackInfoPackageName = userData.generalSettings.iconPackInfoPackageName,
                    )
                }

                gridRepository.deleteGridItems(gridItems = it.gridItems)
            }

            val gridItems = pageItems.flatMapIndexed { index, pageItem ->
                pageItem.gridItems.map { gridItem ->
                    gridItem.copy(page = index)
                }
            }

            val newInitialPage = pageItems.indexOfFirst { it.id == id }

            when (associate) {
                Associate.Grid -> {
                    userDataRepository.updateHomeSettings(
                        homeSettings = userData.homeSettings.copy(
                            pageCount = pageItems.size,
                            initialPage = newInitialPage,
                        ),
                    )
                }

                Associate.Dock -> {
                    userDataRepository.updateHomeSettings(
                        homeSettings = userData.homeSettings.copy(
                            dockPageCount = pageItems.size,
                            dockInitialPage = newInitialPage,
                        ),
                    )
                }
            }

            gridRepository.upsertGridItems(gridItems = gridItems)
        }
    }
}
