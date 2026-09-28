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
import com.eblan.launcher.domain.model.folder.PreviewFolderEblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi

class DeleteFolderEblanApplicationInfoGridItemsUseCase @Inject constructor(
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    private val userDataRepository: UserDataRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        icon: String?,
        folderId: String,
    ) = withContext(defaultDispatcher) {
        val userData = userDataRepository.userDataFlow.first()

        val previewFolderEblanApplicationInfos =
            folderEblanApplicationInfoRepository.getFolderEblanApplicationInfoWrappers()
                .asPreviewFolderEblanApplicationInfos(
                    maxFolderColumns = userData.folderSettings.maxFolderColumns,
                    maxFolderRows = userData.folderSettings.maxFolderRows,
                )

        getRecursiveFolderGridItems(
            previewFolderEblanApplicationInfos = previewFolderEblanApplicationInfos,
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
                    deleteCustomIcon(icon = data.icon)
                }
            }
        }

        deleteCustomIcon(icon = icon)

        folderEblanApplicationInfoRepository.deleteFolderEblanApplicationInfoById(id = folderId)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun getRecursiveFolderGridItems(
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem? = null,
        previewFolderEblanApplicationInfos: Map<String, PreviewFolderEblanApplicationInfo>,
        folderId: String,
    ): List<FolderEblanApplicationInfoGridItem> = buildList {
        val previewFolderEblanApplicationInfo =
            previewFolderEblanApplicationInfos[folderId] ?: return@buildList

        if (folderEblanApplicationInfoGridItem != null) {
            add(folderEblanApplicationInfoGridItem)
        }

        previewFolderEblanApplicationInfo.folderGridItems.forEach {
            when (it.data) {
                is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> {
                    add(it)
                }

                is FolderEblanApplicationInfoGridItemData.Folder -> {
                    addAll(
                        getRecursiveFolderGridItems(
                            folderEblanApplicationInfoGridItem = it,
                            previewFolderEblanApplicationInfos = previewFolderEblanApplicationInfos,
                            folderId = it.id,
                        ),
                    )
                }
            }
        }
    }

    private fun deleteCustomIcon(icon: String?) {
        if (icon == null) return

        val customIconFile = File(icon)

        if (customIconFile.exists()) {
            customIconFile.delete()
        }
    }
}
