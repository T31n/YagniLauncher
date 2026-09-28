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

import com.eblan.launcher.data.room.entity.ShortcutInfoGridItemEntity
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.ShortcutInfoGridItem

internal fun ShortcutInfoGridItemEntity.asModel(): ShortcutInfoGridItem = ShortcutInfoGridItem(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    associate = associate,
    shortcutId = shortcutId,
    packageName = packageName,
    shortLabel = shortLabel,
    longLabel = longLabel,
    icon = icon,
    override = override,
    serialNumber = serialNumber,
    isEnabled = isEnabled,
    gridItemSettings = gridItemSettings,
    customIcon = customIcon,
    customShortLabel = customShortLabel,
    eblanApplicationInfoIcon = eblanApplicationInfoIcon,
    doubleTap = doubleTap,
    swipeUp = swipeUp,
    swipeDown = swipeDown,
    index = index,
    folderId = folderId,
)

internal fun ShortcutInfoGridItem.asEntity(): ShortcutInfoGridItemEntity = ShortcutInfoGridItemEntity(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    associate = associate,
    shortcutId = shortcutId,
    packageName = packageName,
    shortLabel = shortLabel,
    longLabel = longLabel,
    icon = icon,
    override = override,
    serialNumber = serialNumber,
    gridItemSettings = gridItemSettings,
    isEnabled = isEnabled,
    eblanApplicationInfoIcon = eblanApplicationInfoIcon,
    customIcon = customIcon,
    customShortLabel = customShortLabel,
    doubleTap = doubleTap,
    swipeUp = swipeUp,
    swipeDown = swipeDown,
    index = index,
    folderId = folderId,
)

internal fun GridItem.asShortcutInfoGridItem(data: GridItemData.ShortcutInfo): ShortcutInfoGridItem = ShortcutInfoGridItem(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    associate = associate,
    shortcutId = data.shortcutId,
    packageName = data.packageName,
    shortLabel = data.shortLabel,
    longLabel = data.longLabel,
    icon = data.icon,
    override = override,
    serialNumber = data.serialNumber,
    isEnabled = data.isEnabled,
    eblanApplicationInfoIcon = data.eblanApplicationInfoIcon,
    customIcon = data.customIcon,
    customShortLabel = data.customShortLabel,
    gridItemSettings = gridItemSettings,
    doubleTap = doubleTap,
    swipeUp = swipeUp,
    swipeDown = swipeDown,
    index = data.index,
    folderId = data.folderId,
)
