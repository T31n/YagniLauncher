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

        val foldersByFolderId = folderEblanApplicationInfos
            .groupBy { currentFolderEblanApplicationInfo -> currentFolderEblanApplicationInfo.folderId }
            .mapValues { (_, currentFolderEblanApplicationInfos) -> currentFolderEblanApplicationInfos.toMutableList() }
            .toMutableMap()

        val eblanApplicationInfosToUpdateByKey = mutableMapOf<Pair<Long, String>, EblanApplicationInfo>()
        val foldersToUpdateById = mutableMapOf<String, FolderEblanApplicationInfo>()
        val folderIdsToDelete = mutableSetOf<String>()
        val visitedFolderIds = mutableSetOf<String>()

        fun cleanupFolder(folderEblanApplicationInfo: FolderEblanApplicationInfo) {
            if (!visitedFolderIds.add(folderEblanApplicationInfo.id)) return

            foldersByFolderId[folderEblanApplicationInfo.id]
                ?.toList()
                .orEmpty()
                .forEach(::cleanupFolder)

            val childApps = eblanApplicationInfosByFolderId[folderEblanApplicationInfo.id].orEmpty()
            val childFolders = foldersByFolderId[folderEblanApplicationInfo.id].orEmpty()

            if (childApps.size + childFolders.size > 1) return

            val targetFolderId = folderEblanApplicationInfo.folderId
            val targetFolderIndex =
                if (targetFolderId == null) -1 else folderEblanApplicationInfo.folderIndex

            foldersByFolderId[targetFolderId]?.removeAll { currentFolder ->
                currentFolder.id == folderEblanApplicationInfo.id
            }

            when {
                childApps.size == 1 && childFolders.isEmpty() -> {
                    val childApp = childApps.single()
                    val updatedApp = childApp.copy(
                        folderIndex = targetFolderIndex,
                        folderId = targetFolderId,
                    )

                    eblanApplicationInfosToUpdateByKey[updatedApp.serialNumber to updatedApp.componentName] =
                        updatedApp
                    eblanApplicationInfosByFolderId.getOrPut(targetFolderId) { mutableListOf() }
                        .add(updatedApp)
                }

                childFolders.size == 1 && childApps.isEmpty() -> {
                    val childFolder = childFolders.single()

                    if (childFolder.id != folderEblanApplicationInfo.id) {
                        val updatedChildFolder = childFolder.copy(
                            index = if (targetFolderId == null) {
                                folderEblanApplicationInfo.index
                            } else {
                                childFolder.index
                            },
                            folderIndex = targetFolderIndex,
                            folderId = targetFolderId,
                        )

                        foldersToUpdateById[updatedChildFolder.id] = updatedChildFolder
                        foldersByFolderId.getOrPut(targetFolderId) { mutableListOf() }
                            .add(updatedChildFolder)
                    }
                }
            }

            eblanApplicationInfosByFolderId.remove(folderEblanApplicationInfo.id)
            foldersByFolderId.remove(folderEblanApplicationInfo.id)
            foldersToUpdateById.remove(folderEblanApplicationInfo.id)
            folderIdsToDelete.add(folderEblanApplicationInfo.id)
        }

        folderEblanApplicationInfos.forEach(::cleanupFolder)

        if (eblanApplicationInfosToUpdateByKey.isNotEmpty()) {
            eblanApplicationInfoRepository.updateEblanApplicationInfos(
                eblanApplicationInfos = eblanApplicationInfosToUpdateByKey.values.toList(),
            )
        }

        if (foldersToUpdateById.isNotEmpty()) {
            folderEblanApplicationInfoRepository.updateFolderEblanApplicationInfos(
                folderEblanApplicationInfos = foldersToUpdateById.values.toList(),
            )
        }

        folderIdsToDelete.forEach { folderId ->
            folderEblanApplicationInfoRepository.deleteFolderEblanApplicationInfoById(id = folderId)
        }
    }
}
