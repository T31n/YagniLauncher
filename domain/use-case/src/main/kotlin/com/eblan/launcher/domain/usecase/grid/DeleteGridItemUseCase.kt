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
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.platform.AppWidgetHostWrapper
import com.eblan.launcher.domain.platform.LauncherAppsWrapper
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.folder.CleanupFolderGridItemsUseCase
import com.eblan.launcher.domain.usecase.folder.asPreviewFolders
import com.eblan.launcher.domain.usecase.util.deleteGridItemData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Removes a grid item and releases any associated system resources. */
class DeleteGridItemUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val gridRepository: GridRepository,
    private val appWidgetHostWrapper: AppWidgetHostWrapper,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val folderGridItemRepository: FolderGridItemRepository,
    private val cleanupFolderGridItemsUseCase: CleanupFolderGridItemsUseCase,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * Deletes the item's persisted grid record and then releases its nested external resources.
     *
     * Folder contents are snapshotted and deleted within the transaction before releasing widget
     * IDs and pinned shortcuts, so a failed or cancelled transaction cannot leave grid items
     * referencing released resources; if persistence succeeds and subsequent cleanup fails,
     * unreleased widget IDs or shortcuts may temporarily leak.
     *
     * @param gridItem The item to delete.
     */
    suspend operator fun invoke(gridItem: GridItem) = withContext(defaultDispatcher) {
        val folderSettings = if (gridItem.data is GridItemData.Folder) {
            userDataRepository.userDataFlow.first().folderSettings
        } else {
            null
        }

        val previewFolderGridItems = gridRepository.withTransaction {
            val previewFolders = if (folderSettings != null) {
                folderGridItemRepository.getFolderGridItemWrappers()
                    .asPreviewFolders(
                        maxFolderColumns = folderSettings.maxFolderColumns,
                        maxFolderRows = folderSettings.maxFolderRows,
                    )
            } else {
                emptyMap()
            }

            gridRepository.deleteGridItem(gridItem = gridItem)

            cleanupFolderGridItemsUseCase()

            previewFolders
        }

        // Records are already committed above; run external cleanup non-cancellably,
        // accepting temporarily leaked resources if cleanup still fails.
        withContext(NonCancellable) {
            deleteGridItemData(
                gridItem = gridItem,
                folderPopupGridItems = previewFolderGridItems,
                appWidgetHostWrapper = appWidgetHostWrapper,
                launcherAppsWrapper = launcherAppsWrapper,
            )
        }
    }
}
