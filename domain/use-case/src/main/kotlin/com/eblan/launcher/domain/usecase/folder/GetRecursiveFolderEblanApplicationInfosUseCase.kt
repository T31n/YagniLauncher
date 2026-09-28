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
import com.eblan.launcher.domain.model.folder.PreviewFolderEblanApplicationInfo
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class GetRecursiveFolderEblanApplicationInfosUseCase @Inject constructor(
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    private val userDataRepository: UserDataRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        id: String,
        folderId: String,
    ): List<GridItem> = withContext(defaultDispatcher) {
        val userData = userDataRepository.userDataFlow.first()

        val gridItemSettings = userData.appDrawerSettings.gridItemSettings

        val previewFolderEblanApplicationInfos =
            folderEblanApplicationInfoRepository.getFolderEblanApplicationInfoWrappers()
                .asPreviewFolderEblanApplicationInfos(
                    maxFolderColumns = userData.folderSettings.maxFolderColumns,
                    maxFolderRows = userData.folderSettings.maxFolderRows,
                )

        val eblanAction = EblanAction(
            eblanActionType = EblanActionType.None,
            serialNumber = 0L,
            componentName = "",
        )

        getRecursiveFolderGridItems(
            gridItemSettings = gridItemSettings,
            eblanAction = eblanAction,
            previewFolderEblanApplicationInfos = previewFolderEblanApplicationInfos,
            id = id,
            folderId = folderId,
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun getRecursiveFolderGridItems(
        gridItemSettings: GridItemSettings,
        gridItem: GridItem? = null,
        eblanAction: EblanAction,
        previewFolderEblanApplicationInfos: Map<String, PreviewFolderEblanApplicationInfo>,
        id: String,
        folderId: String,
    ): List<GridItem> = buildList {
        val previewFolderEblanApplicationInfo =
            previewFolderEblanApplicationInfos[folderId] ?: return@buildList

        if (gridItem != null) {
            add(gridItem)
        }

        previewFolderEblanApplicationInfo.folderGridItems.forEach { folderEblanApplicationInfoGridItem ->
            when (val data = folderEblanApplicationInfoGridItem.data) {
                is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> {
                    add(
                        GridItem(
                            id = Uuid.random().toHexString(),
                            page = 0,
                            startColumn = -1,
                            startRow = -1,
                            columnSpan = 1,
                            rowSpan = 1,
                            data = GridItemData.ApplicationInfo(
                                serialNumber = data.serialNumber,
                                componentName = data.componentName,
                                packageName = data.packageName,
                                icon = data.icon,
                                label = data.label,
                                customIcon = data.customIcon,
                                customLabel = data.customLabel,
                                index = data.folderIndex,
                                folderId = id,
                            ),
                            associate = Associate.Grid,
                            override = false,
                            gridItemSettings = gridItemSettings,
                            doubleTap = eblanAction,
                            swipeUp = eblanAction,
                            swipeDown = eblanAction,
                        ),
                    )
                }

                is FolderEblanApplicationInfoGridItemData.Folder -> {
                    val gridItem = GridItem(
                        id = Uuid.random().toHexString(),
                        page = 0,
                        startColumn = -1,
                        startRow = -1,
                        columnSpan = 1,
                        rowSpan = 1,
                        data = GridItemData.Folder(
                            label = data.label,
                            icon = data.icon,
                            index = data.folderIndex,
                            folderId = id,
                        ),
                        associate = Associate.Grid,
                        override = false,
                        gridItemSettings = gridItemSettings,
                        doubleTap = eblanAction,
                        swipeUp = eblanAction,
                        swipeDown = eblanAction,
                    )

                    addAll(
                        getRecursiveFolderGridItems(
                            gridItemSettings = gridItemSettings,
                            gridItem = gridItem,
                            eblanAction = eblanAction,
                            previewFolderEblanApplicationInfos = previewFolderEblanApplicationInfos,
                            id = gridItem.id,
                            folderId = folderEblanApplicationInfoGridItem.id,
                        ),
                    )
                }
            }
        }
    }
}
