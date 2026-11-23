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
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.home.PageItem
import com.eblan.launcher.domain.platform.AppWidgetHostWrapper
import com.eblan.launcher.domain.platform.LauncherAppsWrapper
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.folder.asPreviewFolders
import com.eblan.launcher.domain.usecase.util.deleteGridItemData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Persists reordered pages and items, deleting resources belonging to removed pages. */
class UpdatePageItemsUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val gridRepository: GridRepository,
    private val appWidgetHostWrapper: AppWidgetHostWrapper,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val folderGridItemRepository: FolderGridItemRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * Deletes requested pages, reindexes retained items, updates page settings, and saves the result.
     *
     * Persisted grid-item records are deleted before releasing external widget IDs and pinned
     * shortcuts so a failure or cancellation cannot leave grid items referencing released
     * resources; if persistence succeeds and subsequent cleanup fails, unreleased widget IDs or
     * shortcuts may temporarily leak.
     *
     * @param id The ID of the page that should become the initial page.
     * @param pageItems The page items to retain, in their new order.
     * @param pageItemsToDelete Pages and their items to delete.
     * @param associate Whether the pages belong to the home grid or dock.
     */
    suspend operator fun invoke(
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
        associate: Associate,
    ) {
        withContext(defaultDispatcher) {
            val userData = userDataRepository.userDataFlow.first()

            val gridItems = pageItems.flatMapIndexed { index, pageItem ->
                pageItem.gridItems.map { gridItem ->
                    gridItem.copy(page = index)
                }
            }

            val newInitialPage = pageItems.indexOfFirst { currentPageItem ->
                currentPageItem.id == id
            }

            val gridItemsToDelete = pageItemsToDelete.flatMap { pageItem ->
                pageItem.gridItems
            }

            val previewFolderGridItems = gridRepository.withTransaction {
                val previewFolders = if (gridItemsToDelete.isNotEmpty()) {
                    folderGridItemRepository.getFolderGridItemWrappers()
                        .asPreviewFolders(
                            maxFolderColumns = userData.folderSettings.maxFolderColumns,
                            maxFolderRows = userData.folderSettings.maxFolderRows,
                        )
                } else {
                    emptyMap()
                }

                if (gridItemsToDelete.isNotEmpty()) {
                    gridRepository.deleteGridItems(gridItems = gridItemsToDelete)
                }

                gridRepository.upsertGridItems(gridItems = gridItems)

                previewFolders
            }

            // Records are already committed above; run settings update and external cleanup
            // non-cancellably, accepting temporarily leaked resources if cleanup still fails.
            withContext(NonCancellable) {
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

                gridItemsToDelete.forEach { gridItem ->
                    deleteGridItemData(
                        gridItem = gridItem,
                        folderPopupGridItems = previewFolderGridItems,
                        appWidgetHostWrapper = appWidgetHostWrapper,
                        launcherAppsWrapper = launcherAppsWrapper,
                    )
                }
            }
        }
    }
}
