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
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Removes empty app-drawer folders and collapses single-item app-drawer folders into their
 * remaining child.
 */
class CleanupFolderEblanApplicationInfosUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * Deletes empty app-drawer folders and promotes any single remaining application or nested
     * folder to take the collapsed folder's parent and position.
     */
    suspend operator fun invoke() = withContext(defaultDispatcher) {
        val eblanApplicationInfos =
            eblanApplicationInfoRepository.getEblanApplicationInfos()

        val folderEblanApplicationInfos =
            folderEblanApplicationInfoRepository.getFolderEblanApplicationInfos()

        val eblanApplicationInfosByFolderId = eblanApplicationInfos
            .groupBy { currentEblanApplicationInfo -> currentEblanApplicationInfo.folderId }
            .mapValues { (_, currentEblanApplicationInfos) -> currentEblanApplicationInfos.toMutableList() }
            .toMutableMap()

        val folderEblanApplicationInfosByFolderId = folderEblanApplicationInfos
            .groupBy { currentFolderEblanApplicationInfo -> currentFolderEblanApplicationInfo.folderId }
            .mapValues { (_, currentFolderEblanApplicationInfos) -> currentFolderEblanApplicationInfos.toMutableList() }
            .toMutableMap()

        val eblanApplicationInfosToUpdateByKey = mutableMapOf<Pair<Long, String>, EblanApplicationInfo>()
        val folderEblanApplicationInfosToUpdateById = mutableMapOf<String, FolderEblanApplicationInfo>()
        val folderEblanApplicationInfoIdsToDelete = mutableSetOf<String>()
        val visitedFolderEblanApplicationInfoIds = mutableSetOf<String>()

        folderEblanApplicationInfos.forEach { folderEblanApplicationInfo ->
            cleanupFolderEblanApplicationInfo(
                folderEblanApplicationInfo = folderEblanApplicationInfo,
                eblanApplicationInfosByFolderId = eblanApplicationInfosByFolderId,
                folderEblanApplicationInfosByFolderId = folderEblanApplicationInfosByFolderId,
                eblanApplicationInfosToUpdateByKey = eblanApplicationInfosToUpdateByKey,
                folderEblanApplicationInfosToUpdateById = folderEblanApplicationInfosToUpdateById,
                folderEblanApplicationInfoIdsToDelete = folderEblanApplicationInfoIdsToDelete,
                visitedFolderEblanApplicationInfoIds = visitedFolderEblanApplicationInfoIds,
            )
        }

        if (eblanApplicationInfosToUpdateByKey.isNotEmpty()) {
            eblanApplicationInfoRepository.updateEblanApplicationInfos(
                eblanApplicationInfos = eblanApplicationInfosToUpdateByKey.values.toList(),
            )
        }

        if (folderEblanApplicationInfosToUpdateById.isNotEmpty()) {
            folderEblanApplicationInfoRepository.updateFolderEblanApplicationInfos(
                folderEblanApplicationInfos = folderEblanApplicationInfosToUpdateById.values.toList(),
            )
        }

        folderEblanApplicationInfoIdsToDelete.forEach { folderEblanApplicationInfoId ->
            folderEblanApplicationInfoRepository.deleteFolderEblanApplicationInfoById(
                id = folderEblanApplicationInfoId,
            )
        }
    }

    private fun cleanupFolderEblanApplicationInfo(
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        eblanApplicationInfosByFolderId: MutableMap<String?, MutableList<EblanApplicationInfo>>,
        folderEblanApplicationInfosByFolderId: MutableMap<String?, MutableList<FolderEblanApplicationInfo>>,
        eblanApplicationInfosToUpdateByKey: MutableMap<Pair<Long, String>, EblanApplicationInfo>,
        folderEblanApplicationInfosToUpdateById: MutableMap<String, FolderEblanApplicationInfo>,
        folderEblanApplicationInfoIdsToDelete: MutableSet<String>,
        visitedFolderEblanApplicationInfoIds: MutableSet<String>,
    ) {
        if (!visitedFolderEblanApplicationInfoIds.add(folderEblanApplicationInfo.id)) return

        folderEblanApplicationInfosByFolderId[folderEblanApplicationInfo.id]
            ?.toList()
            .orEmpty()
            .forEach { childFolderEblanApplicationInfo ->
                cleanupFolderEblanApplicationInfo(
                    folderEblanApplicationInfo = childFolderEblanApplicationInfo,
                    eblanApplicationInfosByFolderId = eblanApplicationInfosByFolderId,
                    folderEblanApplicationInfosByFolderId = folderEblanApplicationInfosByFolderId,
                    eblanApplicationInfosToUpdateByKey = eblanApplicationInfosToUpdateByKey,
                    folderEblanApplicationInfosToUpdateById = folderEblanApplicationInfosToUpdateById,
                    folderEblanApplicationInfoIdsToDelete = folderEblanApplicationInfoIdsToDelete,
                    visitedFolderEblanApplicationInfoIds = visitedFolderEblanApplicationInfoIds,
                )
            }

        val childEblanApplicationInfos =
            eblanApplicationInfosByFolderId[folderEblanApplicationInfo.id].orEmpty()
        val childFolderEblanApplicationInfos =
            folderEblanApplicationInfosByFolderId[folderEblanApplicationInfo.id].orEmpty()

        if (childEblanApplicationInfos.size + childFolderEblanApplicationInfos.size > 1) return

        val targetFolderId = folderEblanApplicationInfo.folderId
        val targetFolderIndex =
            if (targetFolderId == null) -1 else folderEblanApplicationInfo.folderIndex

        folderEblanApplicationInfosByFolderId[targetFolderId]?.removeAll { currentFolderEblanApplicationInfo ->
            currentFolderEblanApplicationInfo.id == folderEblanApplicationInfo.id
        }

        when {
            childEblanApplicationInfos.size == 1 && childFolderEblanApplicationInfos.isEmpty() -> {
                val childEblanApplicationInfo = childEblanApplicationInfos.single()
                val updatedEblanApplicationInfo = childEblanApplicationInfo.copy(
                    folderIndex = targetFolderIndex,
                    folderId = targetFolderId,
                )

                eblanApplicationInfosToUpdateByKey[updatedEblanApplicationInfo.serialNumber to updatedEblanApplicationInfo.componentName] =
                    updatedEblanApplicationInfo
                eblanApplicationInfosByFolderId.getOrPut(targetFolderId) { mutableListOf() }
                    .add(updatedEblanApplicationInfo)
            }

            childFolderEblanApplicationInfos.size == 1 && childEblanApplicationInfos.isEmpty() -> {
                val childFolderEblanApplicationInfo = childFolderEblanApplicationInfos.single()

                if (childFolderEblanApplicationInfo.id != folderEblanApplicationInfo.id) {
                    val updatedChildFolderEblanApplicationInfo = childFolderEblanApplicationInfo.copy(
                        index = if (targetFolderId == null) {
                            folderEblanApplicationInfo.index
                        } else {
                            childFolderEblanApplicationInfo.index
                        },
                        folderIndex = targetFolderIndex,
                        folderId = targetFolderId,
                    )

                    folderEblanApplicationInfosToUpdateById[updatedChildFolderEblanApplicationInfo.id] =
                        updatedChildFolderEblanApplicationInfo
                    folderEblanApplicationInfosByFolderId.getOrPut(targetFolderId) { mutableListOf() }
                        .add(updatedChildFolderEblanApplicationInfo)
                }
            }
        }

        eblanApplicationInfosByFolderId.remove(folderEblanApplicationInfo.id)
        folderEblanApplicationInfosByFolderId.remove(folderEblanApplicationInfo.id)
        folderEblanApplicationInfosToUpdateById.remove(folderEblanApplicationInfo.id)
        folderEblanApplicationInfoIdsToDelete.add(folderEblanApplicationInfo.id)
    }
}
