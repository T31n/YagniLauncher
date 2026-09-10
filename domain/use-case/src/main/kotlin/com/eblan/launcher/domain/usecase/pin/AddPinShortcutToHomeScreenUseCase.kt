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
package com.eblan.launcher.domain.usecase.pin

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.grid.findAvailableRegionByPage
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.EblanActionType
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.util.isTopLevel
import com.eblan.launcher.domain.usecase.util.toGridItems
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AddPinShortcutToHomeScreenUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val fileManager: FileManager,
    private val gridRepository: GridRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val iconKeyGenerator: IconKeyGenerator,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        serialNumber: Long,
        shortcutId: String,
        packageName: String,
        shortLabel: String,
        longLabel: String,
        isEnabled: Boolean,
        icon: String?,
    ): GridItem? = withContext(ioDispatcher) {
        val homeSettings = userDataRepository.userDataFlow.first().homeSettings

        val columns = homeSettings.columns

        val rows = homeSettings.rows

        val pageCount = homeSettings.pageCount

        val initialPage = homeSettings.initialPage

        val eblanApplicationInfoIcon =
            packageManagerWrapper.getComponentName(packageName = packageName)
                ?.let {
                    val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

                    val file = File(
                        directory,
                        iconKeyGenerator.getActivityIconKey(
                            serialNumber = serialNumber,
                            componentName = it,
                        ),
                    )

                    file.absolutePath
                }

        val data = GridItemData.ShortcutInfo(
            shortcutId = shortcutId,
            packageName = packageName,
            serialNumber = serialNumber,
            shortLabel = shortLabel,
            longLabel = longLabel,
            icon = icon,
            isEnabled = isEnabled,
            eblanApplicationInfoIcon = eblanApplicationInfoIcon,
            customIcon = null,
            customShortLabel = null,
            index = -1,
            folderId = null,
        )

        val eblanAction = EblanAction(
            eblanActionType = EblanActionType.None,
            serialNumber = 0L,
            componentName = "",
        )

        val gridItem = GridItem(
            id = Uuid.random().toHexString(),
            page = initialPage,
            startColumn = 0,
            startRow = 0,
            columnSpan = 1,
            rowSpan = 1,
            data = data,
            associate = Associate.Grid,
            override = false,
            gridItemSettings = homeSettings.gridItemSettings,
            doubleTap = eblanAction,
            swipeUp = eblanAction,
            swipeDown = eblanAction,
        )

        val gridItems = gridRepository.getGridItems().toGridItems()
            .filter {
                it.isTopLevel() && it.associate == Associate.Grid
            }

        val newGridItem = findAvailableRegionByPage(
            gridItems = gridItems,
            gridItem = gridItem,
            pageCount = pageCount,
            columns = columns,
            rows = rows,
        )

        if (newGridItem != null) {
            gridRepository.insertGridItem(gridItem = newGridItem)
        }

        newGridItem
    }
}
