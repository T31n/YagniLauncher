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
package com.eblan.launcher.data.repository.impl

import com.eblan.launcher.data.repository.mapper.asModel
import com.eblan.launcher.data.room.transaction.GridTransaction
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.folder.FolderGridItems
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.FolderGridItemTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultFolderGridItemTransaction @Inject constructor(
    private val gridTransaction: GridTransaction,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
) : FolderGridItemTransaction {
    override val folderGridItemsFlow: Flow<FolderGridItems> =
        gridTransaction.folderGridItemEntitiesFlow.map { folderGridItemEntities ->
            FolderGridItems(
                eblanApplicationInfos = folderGridItemEntities.eblanApplicationInfoEntities.map { currentEblanApplicationInfoEntity ->
                    currentEblanApplicationInfoEntity.asModel()
                },
                folderEblanApplicationInfos = folderGridItemEntities.folderEblanApplicationInfoEntities.map { currentFolderEblanApplicationInfoEntity ->
                    currentFolderEblanApplicationInfoEntity.asModel()
                },
            )
        }

    override suspend fun getFolderGridItems(): FolderGridItems = gridTransaction.withTransaction {
        FolderGridItems(
            eblanApplicationInfos = eblanApplicationInfoRepository.getEblanApplicationInfos(),
            folderEblanApplicationInfos = folderEblanApplicationInfoRepository.getFolderEblanApplicationInfos(),
        )
    }

    override suspend fun upsertFolderGridItemsTransaction(folderEblanApplicationInfos: List<FolderEblanApplicationInfoGridItem>) {
        val eblanApplicationInfos = mutableListOf<EblanApplicationInfo>()

        val folderEblanApplicationInfoModels = mutableListOf<FolderEblanApplicationInfo>()

        folderEblanApplicationInfos.forEach { folderEblanApplicationInfoGridItem ->
            when (val data = folderEblanApplicationInfoGridItem.data) {
                is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> {
                    eblanApplicationInfos.add(data.asModel())
                }

                is FolderEblanApplicationInfoGridItemData.Folder -> {
                    folderEblanApplicationInfoModels.add(
                        folderEblanApplicationInfoGridItem.asModel(data),
                    )
                }
            }
        }

        gridTransaction.withTransaction {
            eblanApplicationInfoRepository.upsertEblanApplicationInfos(
                eblanApplicationInfos,
            )

            folderEblanApplicationInfoRepository.upsertFolderEblanApplicationInfos(
                folderEblanApplicationInfoModels,
            )
        }
    }

    override suspend fun deleteFolderGridItemsTransaction(folderEblanApplicationInfos: List<FolderEblanApplicationInfoGridItem>) {
        val eblanApplicationInfos = mutableListOf<EblanApplicationInfo>()

        val folderEblanApplicationInfoModels = mutableListOf<FolderEblanApplicationInfo>()

        folderEblanApplicationInfos.forEach { folderEblanApplicationInfoGridItem ->
            when (val data = folderEblanApplicationInfoGridItem.data) {
                is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> {
                    eblanApplicationInfos.add(data.asModel())
                }

                is FolderEblanApplicationInfoGridItemData.Folder -> {
                    folderEblanApplicationInfoModels.add(
                        folderEblanApplicationInfoGridItem.asModel(data),
                    )
                }
            }
        }

        gridTransaction.withTransaction {
            eblanApplicationInfoRepository.deleteEblanApplicationInfos(
                eblanApplicationInfos,
            )

            folderEblanApplicationInfoRepository.deleteFolderEblanApplicationInfos(
                folderEblanApplicationInfoModels,
            )
        }
    }

    override suspend fun updateFolderEblanApplicationInfoGridItem(folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem) {
        when (val data = folderEblanApplicationInfoGridItem.data) {
            is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> {
                eblanApplicationInfoRepository.updateEblanApplicationInfo(data.asModel())
            }

            is FolderEblanApplicationInfoGridItemData.Folder -> {
                folderEblanApplicationInfoRepository.updateFolderEblanApplicationInfo(
                    folderEblanApplicationInfoGridItem.asModel(data),
                )
            }
        }
    }

    override suspend fun <T> withTransaction(block: suspend () -> T): T = gridTransaction.withTransaction {
        block()
    }

    private fun FolderEblanApplicationInfoGridItemData.ApplicationInfo.asModel(): EblanApplicationInfo = EblanApplicationInfo(
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
    )

    private fun FolderEblanApplicationInfoGridItem.asModel(data: FolderEblanApplicationInfoGridItemData.Folder): FolderEblanApplicationInfo = FolderEblanApplicationInfo(
        id = id,
        icon = data.icon,
        label = data.label,
        index = data.index,
        folderIndex = data.folderIndex,
        folderId = data.folderId,
    )
}
