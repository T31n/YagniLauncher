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
package com.eblan.launcher.feature.home.screen.application.folder

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.grid.MoveFolderEblanApplicationInfoGridItemResult
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.util.calculateFolderGridDragPosition

internal fun handleDragFolderEblanApplicationInfoGridItem(
    // Environment
    density: Density,
    layoutDirection: LayoutDirection,
    // Screen Dimensions
    screenHeight: Int,
    screenWidth: Int,
    paddingValues: PaddingValues,
    // Folder Item State
    currentPage: Int,
    folderEntry: FolderEntry,
    folderEblanApplicationInfoPopup: FolderEblanApplicationInfoPopup,
    folderCellWidth: Int,
    folderCellHeight: Int,
    isLastFolderEblanApplicationInfoGridItem: Boolean,
    moveFolderEblanApplicationInfoGridItemResult: MoveFolderEblanApplicationInfoGridItemResult?,
    // Drag State
    drag: Drag,
    dragIntOffset: IntOffset,
    folderPopupIntOffset: IntOffset,
    isDragging: Boolean,
    isVisibleOverlay: Boolean,
    isScrollInProgress: Boolean,
    lockMovement: Boolean,
    isInProgress: Boolean,
    // Callbacks
    onMoveFolderEblanApplicationInfoGridItem: (
        folderEblanApplicationInfoPopup: FolderEblanApplicationInfoPopup,
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onUpsertFolderEblanApplicationInfoPopupEntry: (FolderEntry) -> Unit,
) {
    if (drag != Drag.Dragging ||
        isScrollInProgress ||
        !isVisibleOverlay ||
        !isDragging ||
        lockMovement ||
        moveFolderEblanApplicationInfoGridItemResult == null ||
        !isLastFolderEblanApplicationInfoGridItem ||
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
        columns = folderEblanApplicationInfoPopup.columns,
        rows = folderEblanApplicationInfoPopup.rows,
        paddingValues = paddingValues,
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
        onMoveFolderEblanApplicationInfoGridItem(
            folderEblanApplicationInfoPopup,
            moveFolderEblanApplicationInfoGridItemResult.folderEblanApplicationInfoGridItem,
            folderGridDragPosition.x,
            folderGridDragPosition.y,
            folderGridDragPosition.width,
            folderGridDragPosition.height,
            currentPage,
        )
    } else if (!folderEntry.isCloseFolder) {
        onUpsertFolderEblanApplicationInfoPopupEntry(
            folderEntry.copy(isCloseFolder = true),
        )
    }
}
