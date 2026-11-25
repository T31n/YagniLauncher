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
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.uuid.Uuid

/** Appends selected applications as new child grid items inside a home-screen folder. */
class AddApplicationInfosToFolderGridItemUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * Creates new application grid items linked to [folderId] starting after its current
     * highest child index and persists them.
     *
     * @param folderId The identifier of the target folder grid item.
     * @param page The page of the target folder grid item.
     * @param associate The grid container associated with the target folder.
     * @param eblanApplicationInfos The applications to append to the folder.
     */
    suspend operator fun invoke(
        folderId: String,
        page: Int,
        associate: Associate,
        eblanApplicationInfos: List<EblanApplicationInfo>,
    ) = withContext(defaultDispatcher) {
        val homeSettings = userDataRepository.userDataFlow.first().homeSettings

        gridRepository.withTransaction {
            val gridItems = gridRepository.getGridItems()

            val gridItemSettings = when (associate) {
                Associate.Grid -> homeSettings.gridItemSettings
                Associate.Dock -> homeSettings.dockGridItemSettings
            }

            val startIndex = sequenceOf(
                gridItems.applicationInfoGridItems
                    .asSequence()
                    .filter { it.folderId == folderId }
                    .map { it.index + 1 },
                gridItems.shortcutInfoGridItems
                    .asSequence()
                    .filter { it.folderId == folderId }
                    .map { it.index + 1 },
                gridItems.shortcutConfigGridItems
                    .asSequence()
                    .filter { it.folderId == folderId }
                    .map { it.index + 1 },
                gridItems.folderGridItems
                    .asSequence()
                    .filter { it.folderId == folderId }
                    .map { it.index + 1 },
            ).flatten().maxOrNull() ?: 0

            val eblanAction = EblanAction(
                eblanActionType = EblanActionType.None,
                serialNumber = 0L,
                componentName = "",
            )

            val newGridItems = eblanApplicationInfos.mapIndexed { index, eblanApplicationInfo ->
                GridItem(
                    id = Uuid.random().toHexString(),
                    page = page,
                    startColumn = -1,
                    startRow = -1,
                    columnSpan = 1,
                    rowSpan = 1,
                    data = GridItemData.ApplicationInfo(
                        serialNumber = eblanApplicationInfo.serialNumber,
                        componentName = eblanApplicationInfo.componentName,
                        packageName = eblanApplicationInfo.packageName,
                        icon = eblanApplicationInfo.icon,
                        label = eblanApplicationInfo.label,
                        customIcon = eblanApplicationInfo.customIcon,
                        customLabel = eblanApplicationInfo.customLabel,
                        index = startIndex + index,
                        folderId = folderId,
                    ),
                    associate = associate,
                    override = false,
                    gridItemSettings = gridItemSettings,
                    doubleTap = eblanAction,
                    swipeUp = eblanAction,
                    swipeDown = eblanAction,
                )
            }

            gridRepository.upsertGridItems(gridItems = newGridItems)
        }
    }
}
