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
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.platform.ContentResolverWrapper
import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.usecase.util.deleteCustomIconsIfUnreferenced
import com.eblan.launcher.domain.usecase.util.getCustomIcon
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Replaces a folder's saved icon using an image referenced by a content URI or file path. */
class UpdateFolderEblanApplicationInfoCustomIconUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val iconKeyGenerator: IconKeyGenerator,
    private val contentResolverWrapper: ContentResolverWrapper,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    private val gridRepository: GridRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Saves the image at [uri], persists the folder with the new icon path, then deletes the
     * previous icon file if unreferenced.
     *
     * If the URI cannot be read, no repository update or file deletion is made.
     *
     * @param folderEblanApplicationInfo The folder whose icon should be replaced.
     * @param uri The URI of the replacement image.
     */
    suspend operator fun invoke(
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        uri: String,
    ) = withContext(ioDispatcher) {
        val icon = getCustomIcon(
            contentResolverWrapper = contentResolverWrapper,
            fileManager = fileManager,
            iconKeyGenerator = iconKeyGenerator,
            uri = uri,
        ) ?: return@withContext

        folderEblanApplicationInfoRepository.upsertFolderEblanApplicationInfo(
            folderEblanApplicationInfo = folderEblanApplicationInfo.copy(
                icon = icon,
            ),
        )

        deleteCustomIconsIfUnreferenced(
            iconPaths = listOf(folderEblanApplicationInfo.icon),
            eblanApplicationInfoRepository = eblanApplicationInfoRepository,
            folderEblanApplicationInfoRepository = folderEblanApplicationInfoRepository,
            gridRepository = gridRepository,
        )
    }

    /**
     * Updates a folder's icon path and deletes the previous icon file when changed and no longer
     * referenced.
     */
    suspend fun updateCustomIconPath(
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        icon: String,
    ) = withContext(ioDispatcher) {
        folderEblanApplicationInfoRepository.upsertFolderEblanApplicationInfo(
            folderEblanApplicationInfo = folderEblanApplicationInfo.copy(
                icon = icon,
            ),
        )

        if (folderEblanApplicationInfo.icon != icon) {
            deleteCustomIconsIfUnreferenced(
                iconPaths = listOf(folderEblanApplicationInfo.icon),
                eblanApplicationInfoRepository = eblanApplicationInfoRepository,
                folderEblanApplicationInfoRepository = folderEblanApplicationInfoRepository,
                gridRepository = gridRepository,
            )
        }
    }
}
