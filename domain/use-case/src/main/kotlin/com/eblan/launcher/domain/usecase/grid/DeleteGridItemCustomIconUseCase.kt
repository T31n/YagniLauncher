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
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteGridItemCustomIconUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(gridItem: GridItem) = withContext(ioDispatcher) {
        val newData = when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                val eblanApplicationInfo =
                    eblanApplicationInfoRepository.getEblanApplicationInfoByComponentName(
                        serialNumber = data.serialNumber,
                        componentName = data.componentName,
                    )

                if (eblanApplicationInfo != null) {
                    eblanApplicationInfoRepository.updateEblanApplicationInfo(
                        eblanApplicationInfo = eblanApplicationInfo.copy(customIcon = null),
                    )
                }

                data.copy(customIcon = null)
            }

            is GridItemData.ShortcutConfig -> data.copy(customIcon = null)

            is GridItemData.ShortcutInfo -> data.copy(customIcon = null)

            is GridItemData.Folder -> data.copy(icon = null)

            else -> error("Unsupported Grid Item")
        }

        deleteGridItemCustomIconFile(gridItem = gridItem)

        gridRepository.updateGridItem(gridItem = gridItem.copy(data = newData))
    }
}
