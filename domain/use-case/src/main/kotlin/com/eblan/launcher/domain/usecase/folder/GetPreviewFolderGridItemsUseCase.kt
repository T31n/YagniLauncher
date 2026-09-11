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
import com.eblan.launcher.domain.model.folder.PreviewFolder
import com.eblan.launcher.domain.model.grid.FolderGridItemWrapper
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.util.asGridItem
import com.eblan.launcher.domain.usecase.util.getGridDimension
import com.eblan.launcher.domain.usecase.util.getPreviewFolderGridItems
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetPreviewFolderGridItemsUseCase @Inject constructor(
    private val folderGridItemRepository: FolderGridItemRepository,
    private val userDataRepository: UserDataRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<Map<String, PreviewFolder>> = combine(
        userDataRepository.userDataFlow,
        folderGridItemRepository.folderGridItemWrappersFlow,
    ) { userData, folderGridItemWrappers ->
        folderGridItemWrappers.associate { folderGridItemWrapper ->
            folderGridItemWrapper.folderGridItem.id to folderGridItemWrapper.asPreviewFolder(
                maxFolderColumns = userData.homeSettings.maxFolderColumns,
                maxFolderRows = userData.homeSettings.maxFolderRows,
            )
        }
    }.flowOn(defaultDispatcher)

    private fun FolderGridItemWrapper.asPreviewFolder(
        maxFolderColumns: Int,
        maxFolderRows: Int,
    ): PreviewFolder {
        val folderGridItems = (
            applicationInfoGridItems.map {
                it.asGridItem()
            } + shortcutInfoGridItems.map { it.asGridItem() } +
                shortcutConfigGridItems.map { it.asGridItem() } +
                folderGridItems.map { it.asGridItem() }
            ).sortedBy { gridItem ->
            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> data.index
                is GridItemData.ShortcutInfo -> data.index
                is GridItemData.ShortcutConfig -> data.index
                is GridItemData.Folder -> data.index
                else -> error("Unsupported folder grid item")
            }
        }

        val (columns, rows) = getGridDimension(
            count = folderGridItems.size,
            maxFolderColumns = maxFolderColumns,
            maxFolderRows = maxFolderRows,
        )

        val previewFolderGridItems = getPreviewFolderGridItems(
            columns = columns,
            rows = rows,
            folderGridItems = folderGridItems,
        )

        return PreviewFolder(
            previewFolderGridItems = previewFolderGridItems,
            folderGridItems = folderGridItems,
        )
    }
}
