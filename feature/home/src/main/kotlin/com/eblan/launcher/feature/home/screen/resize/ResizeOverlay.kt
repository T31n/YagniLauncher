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

import androidx.compose.runtime.Composable
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getTextColor

@Composable
internal fun ResizeOverlay(
    // Position
    x: Int,
    y: Int,
    // Dimensions
    width: Int,
    height: Int,
    gridWidth: Int,
    gridHeight: Int,
    cellWidth: Int,
    cellHeight: Int,
    // Grid
    columns: Int,
    rows: Int,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    // Behavior
    lockMovement: Boolean,
    textColor: TextColor,
    // Callbacks
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
    ) -> Unit,
) {
    val currentTextColor = if (gridItem.override) {
        getGridItemTextColor(
            gridItemCustomTextColor = gridItem.gridItemSettings.customTextColor,
            gridItemTextColor = gridItem.gridItemSettings.textColor,
            systemCustomTextColor = gridItemSettings.customTextColor,
            systemTextColor = textColor,
        )
    } else {
        getTextColor(
            textColor = textColor,
            customTextColor = gridItemSettings.customTextColor,
        )
    }

    when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo,
        is GridItemData.ShortcutInfo,
        is GridItemData.Folder,
        is GridItemData.ShortcutConfig,
        -> {
            GridItemResizeOverlay(
                gridItem = gridItem,
                columns = columns,
                rows = rows,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                x = x,
                y = y,
                width = width,
                height = height,
                color = currentTextColor,
                lockMovement = lockMovement,
                onResizeGridItem = onResizeGridItem,
            )
        }

        is GridItemData.Widget -> {
            WidgetGridItemResizeOverlay(
                data = data,
                gridItem = gridItem,
                columns = columns,
                rows = rows,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                width = width,
                height = height,
                x = x,
                y = y,
                color = currentTextColor,
                lockMovement = lockMovement,
                onResizeWidgetGridItem = onResizeGridItem,
            )
        }
    }
}
