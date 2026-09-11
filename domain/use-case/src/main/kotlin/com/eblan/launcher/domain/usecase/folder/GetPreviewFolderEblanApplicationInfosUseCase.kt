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
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoWrapper
import com.eblan.launcher.domain.model.folder.PreviewFolderEblanApplicationInfo
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.util.getGridDimension
import com.eblan.launcher.domain.usecase.util.getPreviewFolderGridItems
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetPreviewFolderEblanApplicationInfosUseCase @Inject constructor(
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    private val userDataRepository: UserDataRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<Map<String, PreviewFolderEblanApplicationInfo>> = combine(
        userDataRepository.userDataFlow,
        folderEblanApplicationInfoRepository.folderEblanApplicationInfoWrappersFlow,
    ) { userData, folderEblanApplicationInfoWrappers ->
        folderEblanApplicationInfoWrappers
            .sortedBy { it.folderEblanApplicationInfo.label }
            .associate {
                it.folderEblanApplicationInfo.id to it.asFolderEblanApplicationInfoGridItem(
                    maxFolderColumns = userData.homeSettings.maxFolderColumns,
                    maxFolderRows = userData.homeSettings.maxFolderRows,
                )
            }
    }.flowOn(defaultDispatcher)

    private fun FolderEblanApplicationInfoWrapper.asFolderEblanApplicationInfoGridItem(
        maxFolderColumns: Int,
        maxFolderRows: Int,
    ): PreviewFolderEblanApplicationInfo {
        val folderEblanApplicationInfoGridItems = folderEblanApplicationInfos.map {
            it.asFolderEblanApplicationInfoGridItem()
        } + eblanApplicationInfos.map {
            it.asFolderEblanApplicationInfoGridItem()
        }

        val (columns, rows) = getGridDimension(
            count = folderEblanApplicationInfoGridItems.size,
            maxFolderColumns = maxFolderColumns,
            maxFolderRows = maxFolderRows,
        )

        val previewFolderGridItems = getPreviewFolderGridItems(
            columns = columns,
            rows = rows,
            folderGridItems = folderEblanApplicationInfoGridItems,
        )

        return PreviewFolderEblanApplicationInfo(
            folderEblanApplicationInfo = folderEblanApplicationInfo,
            previewFolderGridItems = previewFolderGridItems,
        )
    }
}
