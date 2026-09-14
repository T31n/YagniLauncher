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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.eblan.launcher.domain.model.folder.FolderPopupEntry
import com.eblan.launcher.domain.model.grid.FolderGridItemPopup
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.calculateFolderGridDragPosition

internal suspend fun onLongPressFolderGridItem(
    graphicsLayer: GraphicsLayer,
    intOffset: IntOffset,
    intSize: IntSize,
    sharedElementKey: SharedElementKey,
    gridItem: GridItem,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onShowGridItemPopup: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
) {
    onUpdateMoveGridItemResult(
        MoveGridItemResult(
            isSuccess = true,
            movingGridItem = gridItem,
            conflictingGridItem = null,
        ),
    )

    onUpdateImageBitmap(graphicsLayer.toImageBitmap())

    onUpdateOverlayBounds(
        intOffset,
        intSize,
    )

    onUpdateSharedElementKey(sharedElementKey)

    onShowGridItemPopup(
        intOffset,
        intSize,
    )

    onUpdateIsVisibleOverlay(true)
}

internal fun handleDragFolderGridItem(
    density: Density,
    drag: Drag,
    dragIntOffset: IntOffset,
    currentPage: Int,
    folderGridItemPopup: FolderGridItemPopup,
    folderPopupIntOffset: IntOffset,
    isDragging: Boolean,
    isVisibleOverlay: Boolean,
    isScrollInProgress: Boolean,
    lockMovement: Boolean,
    paddingValues: PaddingValues,
    screenHeight: Int,
    screenWidth: Int,
    moveGridItemResult: MoveGridItemResult?,
    layoutDirection: LayoutDirection,
    folderCellWidth: Int,
    folderCellHeight: Int,
    isLastFolderGridItem: Boolean,
    isInProgress: Boolean,
    onMoveFolderGridItem: (
        folderGridItemPopup: FolderGridItemPopup,
        movingFolderGridItem: GridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpsertFolderGridItemPopupEntry: (FolderPopupEntry) -> Unit,
) {
    if (drag != Drag.Dragging ||
        isScrollInProgress ||
        !isVisibleOverlay ||
        !isDragging ||
        lockMovement ||
        moveGridItemResult == null ||
        !isLastFolderGridItem ||
        isInProgress
    ) {
        return
    }

    val folderGridDragPosition = calculateFolderGridDragPosition(
        density = density,
        dragIntOffset = dragIntOffset,
        columns = folderGridItemPopup.columns,
        rows = folderGridItemPopup.rows,
        folderPopupIntOffset = folderPopupIntOffset,
        paddingValues = paddingValues,
        screenHeight = screenHeight,
        screenWidth = screenWidth,
        layoutDirection = layoutDirection,
        folderCellWidth = folderCellWidth,
        folderCellHeight = folderCellHeight,
    )

    if (folderGridDragPosition.x in 0 until folderGridDragPosition.width &&
        folderGridDragPosition.y in 0 until folderGridDragPosition.height
    ) {
        val movingGridItem = moveGridItemResult.movingGridItem

        onUpdateSharedElementKey(
            SharedElementKey(
                id = movingGridItem.id,
                parent = SharedElementKey.Parent.Folder,
            ),
        )

        onMoveFolderGridItem(
            folderGridItemPopup,
            movingGridItem,
            folderGridDragPosition.x,
            folderGridDragPosition.y,
            folderGridDragPosition.width,
            folderGridDragPosition.height,
            currentPage,
        )
    } else if (!folderGridItemPopup.folderPopupEntry.isCloseFolder) {
        onUpsertFolderGridItemPopupEntry(folderGridItemPopup.folderPopupEntry.copy(isCloseFolder = true))
    }
}
