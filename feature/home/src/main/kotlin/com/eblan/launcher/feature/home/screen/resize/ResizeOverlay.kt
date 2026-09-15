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
    cellHeight: Int,
    cellWidth: Int,
    columns: Int,
    gridHeight: Int,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    gridWidth: Int,
    height: Int,
    lockMovement: Boolean,
    rows: Int,
    textColor: TextColor,
    width: Int,
    x: Int,
    y: Int,
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
            customTextColor = gridItemSettings.customTextColor,
            textColor = textColor,
        )
    }

    when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo,
        is GridItemData.ShortcutInfo,
        is GridItemData.Folder,
        is GridItemData.ShortcutConfig,
        -> {
            GridItemResizeOverlay(
                cellHeight = cellHeight,
                cellWidth = cellWidth,
                color = currentTextColor,
                columns = columns,
                gridHeight = gridHeight,
                gridItem = gridItem,
                gridWidth = gridWidth,
                height = height,
                lockMovement = lockMovement,
                rows = rows,
                width = width,
                x = x,
                y = y,
                onResizeGridItem = onResizeGridItem,
            )
        }

        is GridItemData.Widget -> {
            WidgetGridItemResizeOverlay(
                color = currentTextColor,
                columns = columns,
                data = data,
                gridHeight = gridHeight,
                gridItem = gridItem,
                gridWidth = gridWidth,
                height = height,
                lockMovement = lockMovement,
                rows = rows,
                width = width,
                x = x,
                y = y,
                onResizeWidgetGridItem = onResizeGridItem,
            )
        }
    }
}
