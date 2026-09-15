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
package com.eblan.launcher.data.repository.mapper

import com.eblan.launcher.data.room.entity.WidgetGridItemEntity
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.WidgetGridItem

internal fun WidgetGridItemEntity.asModel(): WidgetGridItem = WidgetGridItem(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    associate = associate,
    appWidgetId = appWidgetId,
    packageName = packageName,
    componentName = componentName,
    configure = configure,
    minWidth = minWidth,
    minHeight = minHeight,
    resizeMode = resizeMode,
    minResizeWidth = minResizeWidth,
    minResizeHeight = minResizeHeight,
    maxResizeWidth = maxResizeWidth,
    maxResizeHeight = maxResizeHeight,
    targetCellHeight = targetCellHeight,
    targetCellWidth = targetCellWidth,
    preview = preview,
    label = label,
    icon = icon,
    override = override,
    serialNumber = serialNumber,
    gridItemSettings = gridItemSettings,
)

internal fun WidgetGridItem.asEntity(): WidgetGridItemEntity = WidgetGridItemEntity(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    associate = associate,
    appWidgetId = appWidgetId,
    packageName = packageName,
    componentName = componentName,
    configure = configure,
    minWidth = minWidth,
    minHeight = minHeight,
    resizeMode = resizeMode,
    minResizeWidth = minResizeWidth,
    minResizeHeight = minResizeHeight,
    maxResizeWidth = maxResizeWidth,
    maxResizeHeight = maxResizeHeight,
    targetCellHeight = targetCellHeight,
    targetCellWidth = targetCellWidth,
    preview = preview,
    label = label,
    icon = icon,
    override = override,
    serialNumber = serialNumber,
    gridItemSettings = gridItemSettings,
)

internal fun GridItem.asWidgetGridItem(data: GridItemData.Widget): WidgetGridItem = WidgetGridItem(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    associate = associate,
    appWidgetId = data.appWidgetId,
    packageName = data.packageName,
    componentName = data.componentName,
    configure = data.configure,
    minWidth = data.minWidth,
    minHeight = data.minHeight,
    resizeMode = data.resizeMode,
    minResizeWidth = data.minResizeWidth,
    minResizeHeight = data.minResizeHeight,
    maxResizeWidth = data.maxResizeWidth,
    maxResizeHeight = data.maxResizeHeight,
    targetCellHeight = data.targetCellHeight,
    targetCellWidth = data.targetCellWidth,
    preview = data.preview,
    label = data.label,
    icon = data.icon,
    override = override,
    serialNumber = data.serialNumber,
    gridItemSettings = gridItemSettings,
)
