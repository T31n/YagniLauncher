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
import com.eblan.launcher.domain.grid.findAvailableRegionByPage
import com.eblan.launcher.domain.grid.getDockHeight
import com.eblan.launcher.domain.grid.getPageIndicatorHeight
import com.eblan.launcher.domain.grid.getWidgetGridItemSize
import com.eblan.launcher.domain.grid.getWidgetGridItemSpan
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.domain.platform.LauncherAppsWrapper
import com.eblan.launcher.domain.platform.PackageManagerWrapper
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.util.isTopLevel
import com.eblan.launcher.domain.usecase.util.toGridItems
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.uuid.Uuid

/** Creates a home-screen grid item for a pinned widget when a free region is available. */
class AddPinWidgetToHomeScreenUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val fileManager: FileManager,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val gridRepository: GridRepository,
    private val iconKeyGenerator: IconKeyGenerator,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Builds a widget item using its provider metadata and inserts it at the first available region.
     *
     * @param componentName The widget provider component name.
     * @param configure The optional widget configuration activity.
     * @param packageName The provider package name.
     * @param serialNumber The user profile that owns the provider.
     * @param targetCellHeight The provider's preferred height in cells.
     * @param targetCellWidth The provider's preferred width in cells.
     * @param minWidth The widget's minimum width in pixels.
     * @param minHeight The widget's minimum height in pixels.
     * @param resizeMode The widget's resize mode.
     * @param minResizeWidth The minimum resizable width in pixels.
     * @param minResizeHeight The minimum resizable height in pixels.
     * @param maxResizeWidth The maximum resizable width in pixels.
     * @param maxResizeHeight The maximum resizable height in pixels.
     * @param rootWidth The home-grid root width in pixels.
     * @param rootHeight The vertical safe-drawing height in pixels.
     * @param density The display density factor used to convert dp to pixels.
     * @param preview The optional widget preview image bytes.
     * @return The inserted grid item, or `null` if no region is available.
     */
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
        density: Float,
        preview: ByteArray?,
    ): GridItem? = withContext(ioDispatcher) {
        val homeSettings = userDataRepository.userDataFlow.first().homeSettings

        val columns = homeSettings.columns

        val rows = homeSettings.rows

        val pageCount = homeSettings.pageCount

        val initialPage = homeSettings.initialPage

        val pageIndicatorHeight =
            (getPageIndicatorHeight(homeSettings.pageIndicatorType) * density.coerceAtLeast(0f))
                .roundToInt()

        val availableHeight = (rootHeight - pageIndicatorHeight).coerceAtLeast(0)

        val dockHeight = getDockHeight(
            enableDock = homeSettings.enableDock,
            rows = rows,
            dockRows = homeSettings.dockRows,
            availableHeight = availableHeight,
        )

        val eblanApplicationInfoIcon =
            launcherAppsWrapper.getFastActivityList(
                serialNumber = serialNumber,
                packageName = packageName,
            ).firstOrNull()?.componentName?.let { currentComponentName ->
                val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

                val file = File(
                    directory,
                    iconKeyGenerator.getActivityIconKey(
                        serialNumber = serialNumber,
                        componentName = currentComponentName,
                    ),
                )

                file.absolutePath
            }

        val gridHeight = availableHeight - dockHeight

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
            preview = null,
            label = packageManagerWrapper.getApplicationLabel(
                packageName = packageName,
                serialNumber = serialNumber,
            ),
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

        val previewPath = preview?.let { byteArray ->
            val directory = fileManager.getFilesDirectory(FileManager.WIDGETS_DIR)

            fileManager.updateAndGetFilePath(
                directory = directory,
                name = iconKeyGenerator.getWidgetPreviewKey(
                    serialNumber = serialNumber,
                    componentName = componentName,
                ),
                byteArray = byteArray,
            )
        }

        gridRepository.withTransaction {
            val gridItems = gridRepository.getGridItems().toGridItems().filter { currentGridItem ->
                currentGridItem.isTopLevel() && currentGridItem.associate == Associate.Grid
            }

            val newGridItem = findAvailableRegionByPage(
                gridItems = gridItems,
                gridItem = gridItem,
                pageCount = pageCount,
                columns = columns,
                rows = rows,
            )

            if (newGridItem != null) {
                val updatedGridItem = newGridItem.copy(
                    data = data.copy(preview = previewPath),
                )

                gridRepository.insertGridItem(updatedGridItem)

                updatedGridItem
            } else {
                null
            }
        }
    }
}
