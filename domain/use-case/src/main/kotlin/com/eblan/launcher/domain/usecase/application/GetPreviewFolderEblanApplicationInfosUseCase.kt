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
package com.eblan.launcher.domain.usecase.application

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.FolderEblanApplicationInfoWrapper
import com.eblan.launcher.domain.model.FolderPopupEntry
import com.eblan.launcher.domain.model.PreviewFolderEblanApplicationInfo
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.grid.FOLDER_PREVIEW_COLUMNS
import com.eblan.launcher.domain.usecase.grid.FOLDER_PREVIEW_ROWS
import com.eblan.launcher.domain.usecase.grid.getGridDimension
import com.eblan.launcher.domain.usecase.util.getPreviewFolderGridItems
import jdk.internal.org.jline.utils.InfoCmp
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
    operator fun invoke(): Flow<List<PreviewFolderEblanApplicationInfo>> = combine(
        userDataRepository.userDataFlow,
        folderEblanApplicationInfoRepository.folderEblanApplicationInfoWrappersFlow,
    ) { userData, folderEblanApplicationInfoWrappers ->
        folderEblanApplicationInfoWrappers.map {
            it.asFolderEblanApplicationInfoGridItem(
                maxFolderColumns = userData.homeSettings.maxFolderColumns,
                maxFolderRows = userData.homeSettings.maxFolderRows,
            )
        }
    }.flowOn(defaultDispatcher)

    fun FolderEblanApplicationInfoWrapper.asFolderEblanApplicationInfoGridItem(
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
            rows = rows,
            columns = columns,
            folderGridItems = folderEblanApplicationInfoGridItems,
        )

        return PreviewFolderEblanApplicationInfo(
            id = folderEblanApplicationInfo.id,
            previewFolderGridItems = previewFolderGridItems,
        )
    }

    private fun FolderEblanApplicationInfo.asFolderEblanApplicationInfoGridItem(): FolderEblanApplicationInfoGridItem =
        FolderEblanApplicationInfoGridItem(
            id = id,
            data = FolderEblanApplicationInfoGridItemData.Folder(
                icon = icon,
                label = label,
                folderIndex = folderIndex,
                folderId = folderId,
            ),
        )

    private fun EblanApplicationInfo.asFolderEblanApplicationInfoGridItem(): FolderEblanApplicationInfoGridItem =
        FolderEblanApplicationInfoGridItem(
            id = "$componentName $serialNumber",
            data = FolderEblanApplicationInfoGridItemData.ApplicationInfo(
                componentName = componentName,
                serialNumber = serialNumber,
                packageName = packageName,
                icon = icon,
                label = label,
                customIcon = customIcon,
                customLabel = customLabel,
                isHidden = isHidden,
                lastUpdateTime = lastUpdateTime,
                flags = flags,
                folderIndex = folderIndex,
                folderId = folderId,
            ),
        )
}
