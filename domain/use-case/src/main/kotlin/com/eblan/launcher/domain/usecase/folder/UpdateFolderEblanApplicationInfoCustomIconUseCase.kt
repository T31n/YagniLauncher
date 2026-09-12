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
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.ContentResolverWrapper
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.usecase.util.getCustomIcon
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class UpdateFolderEblanApplicationInfoCustomIconUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val iconKeyGenerator: IconKeyGenerator,
    private val contentResolverWrapper: ContentResolverWrapper,
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        uri: String,
    ) = withContext(ioDispatcher) {
        folderEblanApplicationInfo.icon?.let {
            val customIconFile = File(it)

            if (customIconFile.exists()) {
                customIconFile.delete()
            }
        }

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
    }
}
