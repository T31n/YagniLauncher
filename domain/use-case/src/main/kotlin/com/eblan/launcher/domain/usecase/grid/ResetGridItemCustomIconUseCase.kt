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
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.usecase.util.toGridItems
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Clears the custom icon from every grid item that references the given icon path. */
class ResetGridItemCustomIconUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Removes [customIcon] from each grid item whose icon reference equals it.
     *
     * Grid items that use a different icon path are left unchanged.
     *
     * @param customIcon The icon path that was reset.
     */
    suspend operator fun invoke(customIcon: String) = withContext(ioDispatcher) {
        gridRepository.withTransaction {
            gridRepository.getGridItems().toGridItems().forEach { gridItem ->
                val data = gridItem.data

                val newData = when {
                    data is GridItemData.ApplicationInfo && data.customIcon == customIcon -> data.copy(customIcon = null)

                    data is GridItemData.ShortcutConfig && data.customIcon == customIcon -> data.copy(customIcon = null)

                    data is GridItemData.ShortcutInfo && data.customIcon == customIcon -> data.copy(customIcon = null)

                    data is GridItemData.Folder && data.icon == customIcon -> data.copy(icon = null)

                    else -> null
                } ?: return@forEach

                gridRepository.updateGridItem(gridItem = gridItem.copy(data = newData))
            }
        }
    }
}
