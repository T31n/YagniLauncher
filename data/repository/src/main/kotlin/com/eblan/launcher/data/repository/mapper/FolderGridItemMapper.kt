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

import com.eblan.launcher.data.room.entity.FolderGridItemEntity
import com.eblan.launcher.data.room.entity.FolderGridItemWrapperEntity
import com.eblan.launcher.domain.model.grid.FolderGridItem
import com.eblan.launcher.domain.model.grid.FolderGridItemWrapper
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData

internal fun FolderGridItemWrapperEntity.asFolderGridItemWrapper(): FolderGridItemWrapper = FolderGridItemWrapper(
    folderGridItem = folderGridItemEntity.asModel(),
    applicationInfoGridItems = applicationInfoGridItemEntities.map {
        it.asModel()
    },
    shortcutInfoGridItems = shortcutInfoGridItemEntities.map {
        it.asModel()
    },
    shortcutConfigGridItems = shortcutConfigGridItemEntities.map {
        it.asModel()
    },
    folderGridItems = folderGridItemEntities.map {
        it.asModel()
    },
)

internal fun FolderGridItemEntity.asModel(): FolderGridItem = FolderGridItem(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    associate = associate,
    label = label,
    override = override,
    icon = icon,
    gridItemSettings = gridItemSettings,
    doubleTap = doubleTap,
    swipeUp = swipeUp,
    swipeDown = swipeDown,
    index = index,
    folderId = folderId,
)

internal fun FolderGridItem.asEntity(): FolderGridItemEntity = FolderGridItemEntity(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    associate = associate,
    label = label,
    override = override,
    icon = icon,
    gridItemSettings = gridItemSettings,
    doubleTap = doubleTap,
    swipeUp = swipeUp,
    swipeDown = swipeDown,
    index = index,
    folderId = folderId,
)

internal fun GridItem.asFolderGridItem(data: GridItemData.Folder): FolderGridItem = FolderGridItem(
    id = id,
    page = page,
    startColumn = startColumn,
    startRow = startRow,
    columnSpan = columnSpan,
    rowSpan = rowSpan,
    associate = associate,
    label = data.label,
    override = override,
    icon = data.icon,
    gridItemSettings = gridItemSettings,
    doubleTap = doubleTap,
    swipeUp = swipeUp,
    swipeDown = swipeDown,
    index = data.index,
    folderId = data.folderId,
)
