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
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.grid.findAvailableRegionByPage
import com.eblan.launcher.domain.grid.getWidgetGridItemSize
import com.eblan.launcher.domain.grid.getWidgetGridItemSpan
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
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

class AddPinWidgetToHomeScreenUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val fileManager: FileManager,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val gridRepository: GridRepository,
    private val iconKeyGenerator: IconKeyGenerator,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        componentName: String,
        configure: String?,
        packageName: String,
        serialNumber: Long,
        targetCellHeight: Int,
        targetCellWidth: Int,
        minWidth: Int,
        minHeight: Int,
        resizeMode: Int,
        minResizeWidth: Int,
        minResizeHeight: Int,
        maxResizeWidth: Int,
        maxResizeHeight: Int,
        rootWidth: Int,
        rootHeight: Int,
        preview: String?,
    ): GridItem? = withContext(ioDispatcher) {
        val homeSettings = userDataRepository.userDataFlow.first().homeSettings

        val columns = homeSettings.columns

        val rows = homeSettings.rows

        val pageCount = homeSettings.pageCount

        val initialPage = homeSettings.initialPage

        val dockHeight = homeSettings.dockHeight

        val eblanApplicationInfoIcon =
            packageManagerWrapper.getComponentName(packageName = packageName)?.let {
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

        val gridHeight = rootHeight - dockHeight

        val cellWidth = rootWidth / columns

        val cellHeight = gridHeight / rows

        val (checkedColumnSpan, checkedRowSpan) = getWidgetGridItemSpan(
            cellHeight = cellHeight,
            cellWidth = cellWidth,
            minHeight = minHeight,
            minWidth = minWidth,
            targetCellHeight = targetCellHeight,
            targetCellWidth = targetCellWidth,
        )

        val (checkedMinWidth, checkedMinHeight) = getWidgetGridItemSize(
            columns = columns,
            rows = rows,
            gridWidth = rootWidth,
            gridHeight = gridHeight,
            minWidth = minWidth,
            minHeight = minHeight,
            targetCellWidth = targetCellWidth,
            targetCellHeight = targetCellHeight,
        )

        val data = GridItemData.Widget(
            appWidgetId = 0,
            componentName = componentName,
            packageName = packageName,
            serialNumber = serialNumber,
            configure = configure,
            minWidth = checkedMinWidth,
            minHeight = checkedMinHeight,
            resizeMode = resizeMode,
            minResizeWidth = minResizeWidth,
            minResizeHeight = minResizeHeight,
            maxResizeWidth = maxResizeWidth,
            maxResizeHeight = maxResizeHeight,
            targetCellHeight = targetCellHeight,
            targetCellWidth = targetCellWidth,
            preview = preview,
            label = packageManagerWrapper.getApplicationLabel(packageName = packageName),
            icon = eblanApplicationInfoIcon,
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
            columnSpan = checkedColumnSpan,
            rowSpan = checkedRowSpan,
            data = data,
            associate = Associate.Grid,
            override = false,
            gridItemSettings = homeSettings.gridItemSettings,
            doubleTap = eblanAction,
            swipeUp = eblanAction,
            swipeDown = eblanAction,
        )

        val gridItems = gridRepository.getGridItems().toGridItems().filter {
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
