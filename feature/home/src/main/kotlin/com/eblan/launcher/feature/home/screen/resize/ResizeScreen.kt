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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.HomeHandler

@Composable
internal fun ResizeScreen(
    // Layout
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    // Content
    dockHeightPx: Int,
    pageIndicatorHeightPx: Int,
    columns: Int,
    rows: Int,
    gridItemTextColor: TextColor,
    gridItemCustomTextColor: Int,
    gridItemBackgroundColor: BackgroundColor,
    gridItemCustomBackgroundColor: Int,
    dockColumns: Int,
    dockRows: Int,
    dockGridItemTextColor: TextColor,
    dockGridItemCustomTextColor: Int,
    dockGridItemBackgroundColor: BackgroundColor,
    dockGridItemCustomBackgroundColor: Int,
    resizeGridItem: GridItem,
    // Appearance
    textColor: TextColor,
    lockMovement: Boolean,
    // Callbacks
    onResizeCancel: () -> Unit,
    onResizeEnd: () -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
    ) -> Unit,
    onUpdateIsResizing: (Boolean) -> Unit,
) {
    BackHandler {
        onResizeCancel()
    }

    HomeHandler {
        onResizeCancel()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onUpdateIsResizing(false)

                        onResizeEnd()
                    },
                )
            },
    ) {
        when (resizeGridItem.associate) {
            Associate.Grid -> {
                val gridHeight = constraints.maxHeight - pageIndicatorHeightPx - dockHeightPx

                val cellWidth = constraints.maxWidth / columns
                val cellHeight = gridHeight / rows

                val x = resizeGridItem.startColumn * cellWidth
                val y = resizeGridItem.startRow * cellHeight

                val width = resizeGridItem.columnSpan * cellWidth
                val height = resizeGridItem.rowSpan * cellHeight

                ResizeOverlay(
                    // Position and Size
                    x = x,
                    y = y,
                    width = width,
                    height = height,
                    // Grid Dimensions
                    gridWidth = constraints.maxWidth,
                    gridHeight = gridHeight,
                    cellWidth = cellWidth,
                    cellHeight = cellHeight,
                    // Grid Structure
                    columns = columns,
                    rows = rows,
                    // Grid Item
                    gridItem = resizeGridItem,
                    gridItemTextColor = gridItemTextColor,
                    gridItemCustomTextColor = gridItemCustomTextColor,
                    gridItemBackgroundColor = gridItemBackgroundColor,
                    gridItemCustomBackgroundColor = gridItemCustomBackgroundColor,
                    // Behavior and Appearance
                    lockMovement = lockMovement,
                    textColor = textColor,
                    // Callbacks
                    onResizeGridItem = onResizeGridItem,
                )
            }

            Associate.Dock -> {
                val cellWidth = constraints.maxWidth / dockColumns
                val cellHeight = dockHeightPx / dockRows

                val x = resizeGridItem.startColumn * cellWidth
                val y = resizeGridItem.startRow * cellHeight

                val dockY = y + constraints.maxHeight - dockHeightPx

                val width = resizeGridItem.columnSpan * cellWidth
                val height = resizeGridItem.rowSpan * cellHeight

                ResizeOverlay(
                    // Position and Size
                    x = x,
                    y = dockY,
                    width = width,
                    height = height,
                    // Grid Dimensions
                    gridWidth = constraints.maxWidth,
                    gridHeight = dockHeightPx,
                    cellWidth = cellWidth,
                    cellHeight = cellHeight,
                    // Grid Structure
                    columns = dockColumns,
                    rows = dockRows,
                    // Grid Item
                    gridItem = resizeGridItem,
                    gridItemTextColor = dockGridItemTextColor,
                    gridItemCustomTextColor = dockGridItemCustomTextColor,
                    gridItemBackgroundColor = dockGridItemBackgroundColor,
                    gridItemCustomBackgroundColor = dockGridItemCustomBackgroundColor,
                    // Behavior and Appearance
                    lockMovement = lockMovement,
                    textColor = textColor,
                    // Callbacks
                    onResizeGridItem = onResizeGridItem,
                )
            }
        }
    }
}
