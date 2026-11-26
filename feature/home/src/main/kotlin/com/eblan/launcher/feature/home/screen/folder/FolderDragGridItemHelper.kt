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
package com.eblan.launcher.feature.home.screen.folder

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.util.calculateFolderGridDragPosition

internal fun handleDragFolderGridItem(
    // Environment
    density: Density,
    layoutDirection: LayoutDirection,
    // Screen
    screenWidth: Int,
    screenHeight: Int,
    leftPadding: Int,
    topPadding: Int,
    rightPadding: Int,
    bottomPadding: Int,
    // State
    currentPage: Int,
    isDragging: Boolean,
    isInProgress: Boolean,
    isScrollInProgress: Boolean,
    isVisibleOverlay: Boolean,
    lockMovement: Boolean,
    isLastFolderGridItem: Boolean,
    // Drag And Position
    drag: Drag,
    dragIntOffset: IntOffset,
    movingGridItem: GridItem?,
    // Folder
    folderEntry: FolderEntry,
    columns: Int,
    rows: Int,
    folderGridItems: List<GridItem>,
    folderPopupIntOffset: IntOffset,
    folderCellWidth: Int,
    folderCellHeight: Int,
    // Callbacks
    onMoveFolderGridItem: (
        columns: Int,
        rows: Int,
        folderGridItems: List<GridItem>,
        movingGridItem: GridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onUpsertFolderGridItemEntry: (FolderEntry) -> Unit,
) {
    if (drag != Drag.Dragging ||
        isScrollInProgress ||
        !isVisibleOverlay ||
        !isDragging ||
        lockMovement ||
        movingGridItem == null ||
        !isLastFolderGridItem ||
        isInProgress
    ) {
        return
    }

    val folderGridDragPosition = calculateFolderGridDragPosition(
        // Environment and Layout
        density = density,
        layoutDirection = layoutDirection,
        // Screen Dimensions
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        // Grid Dimensions
        columns = columns,
        rows = rows,
        leftPadding = leftPadding,
        topPadding = topPadding,
        rightPadding = rightPadding,
        bottomPadding = bottomPadding,
        // Cell Dimensions
        folderCellWidth = folderCellWidth,
        folderCellHeight = folderCellHeight,
        // Offsets
        folderPopupIntOffset = folderPopupIntOffset,
        dragIntOffset = dragIntOffset,
    )

    if (folderGridDragPosition.x in 0 until folderGridDragPosition.width &&
        folderGridDragPosition.y in 0 until folderGridDragPosition.height
    ) {
        onMoveFolderGridItem(
            columns,
            rows,
            folderGridItems,
            movingGridItem,
            folderGridDragPosition.x,
            folderGridDragPosition.y,
            folderGridDragPosition.width,
            folderGridDragPosition.height,
            currentPage,
        )
    } else if (!folderEntry.isCloseFolder) {
        onUpsertFolderGridItemEntry(folderEntry.copy(isCloseFolder = true))
    }
}
