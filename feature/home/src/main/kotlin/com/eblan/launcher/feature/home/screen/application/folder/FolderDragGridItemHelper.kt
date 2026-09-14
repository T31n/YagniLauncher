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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.folder.FolderPopupEntry
import com.eblan.launcher.domain.model.grid.MoveFolderEblanApplicationInfoGridItemResult
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.PAGE_INDICATOR_HEIGHT

internal suspend fun onLongPressFolderEblanApplicationInfoGridItem(
    graphicsLayer: GraphicsLayer,
    intOffset: IntOffset,
    intSize: IntSize,
    sharedElementKey: SharedElementKey,
    folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
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
    onUpdateMoveFolderEblanApplicationInfoGridItemResult: (MoveFolderEblanApplicationInfoGridItemResult) -> Unit,
) {
    onUpdateMoveFolderEblanApplicationInfoGridItemResult(
        MoveFolderEblanApplicationInfoGridItemResult(
            isSuccess = true,
            folderEblanApplicationInfoGridItem = folderEblanApplicationInfoGridItem,
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

internal fun handleDragFolderEblanApplicationInfoGridItem(
    density: Density,
    drag: Drag,
    dragIntOffset: IntOffset,
    currentPage: Int,
    folderEblanApplicationInfoPopup: FolderEblanApplicationInfoPopup,
    folderPopupIntOffset: IntOffset,
    isDragging: Boolean,
    isVisibleOverlay: Boolean,
    isScrollInProgress: Boolean,
    lockMovement: Boolean,
    paddingValues: PaddingValues,
    screenHeight: Int,
    screenWidth: Int,
    moveFolderEblanApplicationInfoGridItemResult: MoveFolderEblanApplicationInfoGridItemResult?,
    layoutDirection: LayoutDirection,
    folderCellWidth: Int,
    folderCellHeight: Int,
    isLastFolderEblanApplicationInfoGridItem: Boolean,
    isInProgress: Boolean,
    onMoveFolderEblanApplicationInfoGridItem: (
        folderEblanApplicationInfoPopup: FolderEblanApplicationInfoPopup,
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpsertFolderEblanApplicationInfoPopupEntry: (FolderPopupEntry) -> Unit,
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
        density = density,
        dragIntOffset = dragIntOffset,
        folderPopupIntOffset = folderPopupIntOffset,
        columns = folderEblanApplicationInfoPopup.columns,
        rows = folderEblanApplicationInfoPopup.rows,
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
        val movingFolderGridItem =
            moveFolderEblanApplicationInfoGridItemResult.folderEblanApplicationInfoGridItem

        onUpdateSharedElementKey(
            SharedElementKey(
                id = movingFolderGridItem.id,
                parent = SharedElementKey.Parent.Folder,
            ),
        )

        onMoveFolderEblanApplicationInfoGridItem(
            folderEblanApplicationInfoPopup,
            movingFolderGridItem,
            folderGridDragPosition.x,
            folderGridDragPosition.y,
            folderGridDragPosition.width,
            folderGridDragPosition.height,
            currentPage,
        )
    } else if (!folderEblanApplicationInfoPopup.folderPopupEntry.isCloseFolder) {
        onUpsertFolderEblanApplicationInfoPopupEntry(
            folderEblanApplicationInfoPopup.folderPopupEntry.copy(
                isCloseFolder = true,
            ),
        )
    }
}

private data class FolderGridDragPosition(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

private fun calculateFolderGridDragPosition(
    density: Density,
    dragIntOffset: IntOffset,
    columns: Int,
    rows: Int,
    folderPopupIntOffset: IntOffset,
    paddingValues: PaddingValues,
    screenHeight: Int,
    screenWidth: Int,
    layoutDirection: LayoutDirection,
    folderCellWidth: Int,
    folderCellHeight: Int,
): FolderGridDragPosition {
    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val rightPadding = with(density) {
        paddingValues.calculateRightPadding(layoutDirection).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding
    val verticalPadding = topPadding + bottomPadding

    val safeDrawingWidth = screenWidth - horizontalPadding
    val safeDrawingHeight = screenHeight - verticalPadding

    val localDragX = dragIntOffset.x - leftPadding
    val localDragY = dragIntOffset.y - topPadding

    val minCellWidthPx = with(receiver = density) {
        folderCellWidth.dp.roundToPx()
    }
    val minCellHeightPx = with(receiver = density) {
        folderCellHeight.dp.roundToPx()
    }

    val availableWidth = safeDrawingWidth.coerceAtLeast(0)
    val availableHeight = safeDrawingHeight.coerceAtLeast(0)

    val folderTitleHeightPx = with(receiver = density) {
        PAGE_INDICATOR_HEIGHT.roundToPx()
    }

    val folderGridWidthPx =
        (minCellWidthPx * columns).coerceAtMost(availableWidth)
    val folderGridHeightPx = (minCellHeightPx * rows).coerceAtMost(
        (availableHeight - folderTitleHeightPx).coerceAtLeast(0),
    )

    val endHeight = folderGridHeightPx + folderTitleHeightPx

    val maximumX = (
        safeDrawingWidth -
            folderGridWidthPx +
            leftPadding
        ).coerceAtLeast(minimumValue = leftPadding)

    val maximumY = (
        safeDrawingHeight -
            endHeight +
            topPadding
        ).coerceAtLeast(minimumValue = topPadding)

    val endIntOffset = IntOffset(
        x = folderPopupIntOffset.x.coerceIn(
            minimumValue = leftPadding,
            maximumValue = maximumX,
        ),
        y = folderPopupIntOffset.y.coerceIn(
            minimumValue = topPadding,
            maximumValue = maximumY,
        ),
    )

    val localEndIntOffset = IntOffset(
        x = endIntOffset.x - leftPadding,
        y = endIntOffset.y - topPadding,
    )

    val dragX = localDragX - localEndIntOffset.x
    val dragY = localDragY - localEndIntOffset.y

    val layoutDirectionX = when (layoutDirection) {
        LayoutDirection.Rtl -> folderGridWidthPx - dragX
        LayoutDirection.Ltr -> dragX
    }

    return FolderGridDragPosition(
        x = layoutDirectionX,
        y = dragY,
        width = folderGridWidthPx,
        height = endHeight,
    )
}
