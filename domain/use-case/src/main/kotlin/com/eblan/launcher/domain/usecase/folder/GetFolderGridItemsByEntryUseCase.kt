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
package com.eblan.launcher.domain.usecase.folder

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.grid.FolderGridItemPopup
import com.eblan.launcher.domain.model.grid.FolderGridItemWrapper
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.util.asGridItem
import com.eblan.launcher.domain.usecase.util.getGridDimension
import com.eblan.launcher.domain.usecase.util.getGridItemsByPage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetFolderGridItemsByEntryUseCase @Inject constructor(
    private val folderGridItemRepository: FolderGridItemRepository,
    private val userDataRepository: UserDataRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(
        folderEntriesFlow: Flow<List<FolderEntry>>,
    ): Flow<List<FolderGridItemPopup>> = combine(
        userDataRepository.userDataFlow,
        folderEntriesFlow,
        folderGridItemRepository.folderGridItemWrappersFlow,
    ) { userData, folderEntries, folderGridItemWrappers ->
        folderEntries.mapNotNull { folderPopupEntry ->
            folderGridItemWrappers.firstOrNull {
                it.folderGridItem.id == folderPopupEntry.id
            }?.asFolderGridItemPopup(
                folderEntry = folderPopupEntry,
                maxFolderColumns = userData.folderSettings.maxFolderColumns,
                maxFolderRows = userData.folderSettings.maxFolderRows,
            )
        }
    }.flowOn(defaultDispatcher)

    private suspend fun FolderGridItemWrapper.asFolderGridItemPopup(
        folderEntry: FolderEntry,
        maxFolderColumns: Int,
        maxFolderRows: Int,
    ): FolderGridItemPopup {
        val gridItems = (
            applicationInfoGridItems.map {
                it.asGridItem()
            } + shortcutInfoGridItems.map {
                it.asGridItem()
            } + shortcutConfigGridItems.map {
                it.asGridItem()
            } + folderGridItems.map { it.asGridItem() }
            ).sortedBy {
            when (val data = it.data) {
                is GridItemData.ApplicationInfo -> data.index
                is GridItemData.ShortcutInfo -> data.index
                is GridItemData.ShortcutConfig -> data.index
                is GridItemData.Folder -> data.index
                else -> error("Unsupported folder grid item")
            }
        }

        val gridItemsByPage = gridItems.getGridItemsByPage(
            maxFolderColumns = maxFolderColumns,
            maxFolderRows = maxFolderRows,
        )

        val firstPageGridItems = gridItemsByPage.values.firstOrNull().orEmpty()

        val (columns, rows) = getGridDimension(
            count = firstPageGridItems.size,
            maxFolderColumns = maxFolderColumns,
            maxFolderRows = maxFolderRows,
        )

        val maxIndex = gridItems.maxOfOrNull {
            when (val data = it.data) {
                is GridItemData.ApplicationInfo -> data.index + 1
                is GridItemData.ShortcutInfo -> data.index + 1
                is GridItemData.ShortcutConfig -> data.index + 1
                is GridItemData.Folder -> data.index + 1
                else -> error("Unsupported folder grid item")
            }
        } ?: 0

        return FolderGridItemPopup(
            folderEntry = folderEntry,
            gridItem = folderGridItem.asGridItem(),
            gridItems = gridItems,
            gridItemsByPage = gridItemsByPage,
            label = folderGridItem.label,
            columns = columns,
            rows = rows,
            maxIndex = maxIndex,
        )
    }
}
