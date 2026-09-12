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
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoWrapper
import com.eblan.launcher.domain.model.folder.FolderPopupEntry
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.util.getGridDimension
import com.eblan.launcher.domain.usecase.util.getGridItemsByPage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetFolderEblanApplicationInfosByIdUseCase @Inject constructor(
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    private val userDataRepository: UserDataRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(
        folderPopupEntriesFlow: Flow<List<FolderPopupEntry>>,
    ): Flow<List<FolderEblanApplicationInfoPopup>> = combine(
        userDataRepository.userDataFlow,
        folderPopupEntriesFlow,
        folderEblanApplicationInfoRepository.folderEblanApplicationInfoWrappersFlow,
    ) { userData, folderPopupEntries, folderEblanApplicationInfoWrappers ->
        folderPopupEntries.mapNotNull { folderPopupEntry ->
            folderEblanApplicationInfoWrappers.firstOrNull {
                it.folderEblanApplicationInfo.id == folderPopupEntry.id
            }?.asFolderEblanApplicationInfoPopup(
                folderPopupEntry = folderPopupEntry,
                maxFolderColumns = userData.homeSettings.maxFolderColumns,
                maxFolderRows = userData.homeSettings.maxFolderRows,
            )
        }
    }.flowOn(defaultDispatcher)

    private suspend fun FolderEblanApplicationInfoWrapper.asFolderEblanApplicationInfoPopup(
        folderPopupEntry: FolderPopupEntry,
        maxFolderColumns: Int,
        maxFolderRows: Int,
    ): FolderEblanApplicationInfoPopup {
        val childFolderEblanApplicationInfos = folderEblanApplicationInfos.map {
            folderEblanApplicationInfoRepository.getFolderEblanApplicationInfoWrapperById(
                id = it.id,
            )?.asFolderEblanApplicationInfoGridItem() ?: it.asFolderEblanApplicationInfoGridItem()
        }

        val gridItems = (
            eblanApplicationInfos.map { it.asFolderEblanApplicationInfoGridItem() } + childFolderEblanApplicationInfos
            ).sortedBy {
            when (val data = it.data) {
                is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> data.folderIndex
                is FolderEblanApplicationInfoGridItemData.Folder -> data.folderIndex
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
                is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> data.folderIndex + 1
                is FolderEblanApplicationInfoGridItemData.Folder -> data.folderIndex + 1
            }
        } ?: 0

        return FolderEblanApplicationInfoPopup(
            folderPopupEntry = folderPopupEntry,
            folderEblanApplicationInfo = folderEblanApplicationInfo,
            folderEblanApplicationInfos = gridItems,
            folderEblanApplicationInfosByPage = gridItemsByPage,
            label = folderEblanApplicationInfo.label,
            columns = columns,
            rows = rows,
            maxIndex = maxIndex,
        )
    }
}
