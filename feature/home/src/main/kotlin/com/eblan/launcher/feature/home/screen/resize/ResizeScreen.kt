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
package com.eblan.launcher.feature.home.screen.resize

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.userdata.HomeSettings
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.util.PAGE_INDICATOR_HEIGHT

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun ResizeScreen(
    modifier: Modifier = Modifier,
    homeSettings: HomeSettings,
    lockMovement: Boolean,
    paddingValues: PaddingValues,
    textColor: TextColor,
    resizeGridItem: GridItem,
    onResizeCancel: () -> Unit,
    onResizeEnd: () -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
    ) -> Unit,
    onUpdateIsResizing: (Boolean) -> Unit,
) {
    val density = LocalDensity.current

    val dockHeight = homeSettings.dockHeight.dp

    val dockHeightPx = with(density) {
        dockHeight.roundToPx()
    }

    val pageIndicatorHeightPx = with(density) {
        PAGE_INDICATOR_HEIGHT.roundToPx()
    }

    BackHandler {
        onResizeCancel()
    }

    HomeHandler {
        onResizeCancel()
    }

    BoxWithConstraints(
        modifier = modifier
            .pointerInput(key1 = Unit) {
                detectTapGestures(
                    onTap = {
                        onUpdateIsResizing(false)

                        onResizeEnd()
                    },
                )
            }
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        when (resizeGridItem.associate) {
            Associate.Grid -> {
                val gridHeight = constraints.maxHeight - pageIndicatorHeightPx - dockHeightPx

                val cellWidth = constraints.maxWidth / homeSettings.columns

                val cellHeight = gridHeight / homeSettings.rows

                val x = resizeGridItem.startColumn * cellWidth

                val y = resizeGridItem.startRow * cellHeight

                val width = resizeGridItem.columnSpan * cellWidth

                val height = resizeGridItem.rowSpan * cellHeight

                ResizeOverlay(
                    cellHeight = cellHeight,
                    cellWidth = cellWidth,
                    columns = homeSettings.columns,
                    gridHeight = gridHeight,
                    gridItem = resizeGridItem,
                    gridItemSettings = homeSettings.gridItemSettings,
                    gridWidth = constraints.maxWidth,
                    height = height,
                    lockMovement = lockMovement,
                    rows = homeSettings.rows,
                    textColor = textColor,
                    width = width,
                    x = x,
                    y = y,
                    onResizeGridItem = onResizeGridItem,
                )
            }

            Associate.Dock -> {
                val cellWidth = constraints.maxWidth / homeSettings.dockColumns

                val cellHeight = dockHeightPx / homeSettings.dockRows

                val x = resizeGridItem.startColumn * cellWidth

                val y = resizeGridItem.startRow * cellHeight

                val dockY = y + constraints.maxHeight - dockHeightPx

                val width = resizeGridItem.columnSpan * cellWidth

                val height = resizeGridItem.rowSpan * cellHeight

                ResizeOverlay(
                    cellHeight = cellHeight,
                    cellWidth = cellWidth,
                    columns = homeSettings.dockColumns,
                    gridHeight = dockHeightPx,
                    gridItem = resizeGridItem,
                    gridItemSettings = homeSettings.gridItemSettings,
                    gridWidth = constraints.maxWidth,
                    height = height,
                    lockMovement = lockMovement,
                    rows = homeSettings.dockRows,
                    textColor = textColor,
                    width = width,
                    x = x,
                    y = dockY,
                    onResizeGridItem = onResizeGridItem,
                )
            }
        }
    }
}
