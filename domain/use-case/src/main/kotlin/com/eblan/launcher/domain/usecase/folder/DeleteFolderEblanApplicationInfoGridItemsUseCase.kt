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
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.util.deleteCustomIconsIfUnreferenced
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Deletes a folder tree, clears child application folder links, and removes its unreferenced icon files. */
class DeleteFolderEblanApplicationInfoGridItemsUseCase @Inject constructor(
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    private val userDataRepository: UserDataRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val gridRepository: GridRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Removes all descendants of [folderId], restores contained applications to the top level, and
     * deletes the folder and its unreferenced icon files.
     *
     * @param icon The icon path of the folder being deleted, if any.
     * @param folderId The ID of the folder to delete.
     */
    suspend operator fun invoke(
        icon: String?,
        folderId: String,
    ) = withContext(ioDispatcher) {
        val userData = userDataRepository.userDataFlow.first()

        val folderEblanApplicationInfoPopups =
            folderEblanApplicationInfoRepository.getFolderEblanApplicationInfoWrappers()
                .asPreviewFolderEblanApplicationInfos(
                    maxFolderColumns = userData.folderSettings.maxFolderColumns,
                    maxFolderRows = userData.folderSettings.maxFolderRows,
                )

        val customIconPaths = mutableListOf<String>()

        getRecursiveFolderGridItems(
            folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
            folderId = folderId,
        ).forEach { folderGridItem ->
            when (val data = folderGridItem.data) {
                is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> {
                    val eblanApplicationInfos =
                        eblanApplicationInfoRepository.getEblanApplicationInfosByPackageName(
                            serialNumber = data.serialNumber,
                            packageName = data.packageName,
                        ).filter { it.componentName == data.componentName && it.folderId != null }
                            .map {
                                it.copy(
                                    folderIndex = -1,
                                    folderId = null,
                                )
                            }

                    eblanApplicationInfoRepository.updateEblanApplicationInfos(eblanApplicationInfos = eblanApplicationInfos)
                }

                is FolderEblanApplicationInfoGridItemData.Folder -> {
                    data.icon?.let(customIconPaths::add)
                }
            }
        }

        icon?.let(customIconPaths::add)

        folderEblanApplicationInfoRepository.deleteFolderEblanApplicationInfoById(id = folderId)

        deleteCustomIconsIfUnreferenced(
            iconPaths = customIconPaths,
            eblanApplicationInfoRepository = eblanApplicationInfoRepository,
            folderEblanApplicationInfoRepository = folderEblanApplicationInfoRepository,
            gridRepository = gridRepository,
        )
    }

    /** Collects a folder and all nested descendants from the preview-folder map. */
    private fun getRecursiveFolderGridItems(
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem? = null,
        folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
        folderId: String,
    ): List<FolderEblanApplicationInfoGridItem> = buildList {
        val previewFolderEblanApplicationInfo =
            folderEblanApplicationInfoPopups[folderId] ?: return@buildList

        if (folderEblanApplicationInfoGridItem != null) {
            add(folderEblanApplicationInfoGridItem)
        }

        previewFolderEblanApplicationInfo.folderEblanApplicationInfoGridItems.forEach {
            when (it.data) {
                is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> {
                    add(it)
                }

                is FolderEblanApplicationInfoGridItemData.Folder -> {
                    addAll(
                        getRecursiveFolderGridItems(
                            folderEblanApplicationInfoGridItem = it,
                            folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                            folderId = it.id,
                        ),
                    )
                }
            }
        }
    }
}
