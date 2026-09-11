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
package com.eblan.launcher.domain.usecase.grid

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.ContentResolverWrapper
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.usecase.util.getCustomIcon
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateGridItemCustomIconUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val iconKeyGenerator: IconKeyGenerator,
    private val contentResolverWrapper: ContentResolverWrapper,
    private val gridRepository: GridRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        gridItem: GridItem,
        uri: String,
    ) = withContext(ioDispatcher) {
        val data = when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo,
            is GridItemData.Folder,
            is GridItemData.ShortcutConfig,
            is GridItemData.ShortcutInfo,
            -> data

            else -> error("Unsupported Grid Item")
        }

        val customIcon = getCustomIcon(
            contentResolverWrapper = contentResolverWrapper,
            fileManager = fileManager,
            iconKeyGenerator = iconKeyGenerator,
            uri = uri,
        ) ?: return@withContext

        val newData = when (data) {
            is GridItemData.ApplicationInfo -> data.copy(customIcon = customIcon)
            is GridItemData.Folder -> data.copy(icon = customIcon)
            is GridItemData.ShortcutConfig -> data.copy(customIcon = customIcon)
            is GridItemData.ShortcutInfo -> data.copy(customIcon = customIcon)
            else -> data
        }

        gridRepository.updateGridItem(gridItem = gridItem.copy(data = newData))

        // Delete the old custom icon after successfully updated the new grid item
        deleteGridItemCustomIconFile(gridItem = gridItem)
    }
}
