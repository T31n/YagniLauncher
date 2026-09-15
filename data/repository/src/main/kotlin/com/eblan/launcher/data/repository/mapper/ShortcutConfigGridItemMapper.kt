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

import com.eblan.launcher.data.room.entity.ShortcutConfigGridItemEntity
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.ShortcutConfigGridItem

internal fun ShortcutConfigGridItemEntity.asModel(): ShortcutConfigGridItem = ShortcutConfigGridItem(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    associate = associate,
    componentName = componentName,
    packageName = packageName,
    activityIcon = activityIcon,
    activityLabel = activityLabel,
    applicationIcon = applicationIcon,
    applicationLabel = applicationLabel,
    override = override,
    serialNumber = serialNumber,
    shortcutIntentName = shortcutIntentName,
    shortcutIntentIcon = shortcutIntentIcon,
    shortcutIntentUri = shortcutIntentUri,
    customIcon = customIcon,
    customLabel = customLabel,
    gridItemSettings = gridItemSettings,
    doubleTap = doubleTap,
    swipeUp = swipeUp,
    swipeDown = swipeDown,
    index = index,
    folderId = folderId,
)

internal fun ShortcutConfigGridItem.asEntity(): ShortcutConfigGridItemEntity = ShortcutConfigGridItemEntity(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    associate = associate,
    componentName = componentName,
    packageName = packageName,
    activityIcon = activityIcon,
    activityLabel = activityLabel,
    applicationIcon = applicationIcon,
    applicationLabel = applicationLabel,
    override = override,
    serialNumber = serialNumber,
    shortcutIntentName = shortcutIntentName,
    shortcutIntentIcon = shortcutIntentIcon,
    shortcutIntentUri = shortcutIntentUri,
    customIcon = customIcon,
    customLabel = customLabel,
    gridItemSettings = gridItemSettings,
    doubleTap = doubleTap,
    swipeUp = swipeUp,
    swipeDown = swipeDown,
    index = index,
    folderId = folderId,
)

internal fun GridItem.asShortcutConfigGridItem(data: GridItemData.ShortcutConfig): ShortcutConfigGridItem = ShortcutConfigGridItem(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    associate = associate,
    componentName = data.componentName,
    packageName = data.packageName,
    activityIcon = data.activityIcon,
    activityLabel = data.activityLabel,
    applicationIcon = data.applicationIcon,
    applicationLabel = data.applicationLabel,
    override = override,
    serialNumber = data.serialNumber,
    shortcutIntentName = data.shortcutIntentName,
    shortcutIntentIcon = data.shortcutIntentIcon,
    shortcutIntentUri = data.shortcutIntentUri,
    customIcon = data.customIcon,
    customLabel = data.customLabel,
    gridItemSettings = gridItemSettings,
    doubleTap = doubleTap,
    swipeUp = swipeUp,
    swipeDown = swipeDown,
    index = data.index,
    folderId = data.folderId,
)
