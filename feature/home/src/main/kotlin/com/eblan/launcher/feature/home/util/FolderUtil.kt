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
package com.eblan.launcher.feature.home.util

import android.graphics.RectF
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.eblan.launcher.domain.usecase.util.FOLDER_PREVIEW_COLUMNS
import com.eblan.launcher.domain.usecase.util.FOLDER_PREVIEW_ROWS
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.FolderGridDragPosition
import com.eblan.launcher.feature.home.model.FolderPopupLayoutInfo
import com.eblan.launcher.feature.home.model.PageDirection
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

internal fun getAnimatedRect(
    progress: Float,
    startWidth: Float,
    startHeight: Float,
    endWidth: Float,
    endHeight: Float,
    startCenterX: Float,
    startCenterY: Float,
    endCenterX: Float,
    endCenterY: Float,
): RectF {
    val width = lerp(
        startWidth,
        endWidth,
        progress,
    )

    val height = lerp(
        startHeight,
        endHeight,
        progress,
    )

    val left = lerp(
        startCenterX,
        endCenterX,
        progress,
    ) - width / 2f

    val top = lerp(
        startCenterY,
        endCenterY,
        progress,
    ) - height / 2f

    return RectF(
        left,
        top,
        left + width,
        top + height,
    )
}

internal fun getFolderPopupLayoutInfo(
    density: Density,
    layoutDirection: LayoutDirection,
    paddingValues: PaddingValues,
    safeDrawingWidth: Int,
    safeDrawingHeight: Int,
    folderPopupIntOffset: IntOffset,
    folderPopupIntSize: IntSize,
    folderCellWidth: Int,
    folderCellHeight: Int,
    columns: Int,
    rows: Int,
): FolderPopupLayoutInfo {
    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val minCellWidthPx = with(density) { folderCellWidth.dp.roundToPx() }
    val minCellHeightPx = with(density) { folderCellHeight.dp.roundToPx() }

    val availableWidth = (safeDrawingWidth - leftPadding * 2).coerceAtLeast(0)
    val availableHeight = (safeDrawingHeight - topPadding * 2).coerceAtLeast(0)

    val folderTitleHeightPx = with(density) {
        PAGE_INDICATOR_HEIGHT.roundToPx()
    }

    val folderGridWidthPx =
        (minCellWidthPx * columns).coerceAtMost(availableWidth)

    val folderGridHeightPx = (minCellHeightPx * rows).coerceAtMost(
        (availableHeight - folderTitleHeightPx).coerceAtLeast(0),
    )

    val endHeight = folderGridHeightPx + folderTitleHeightPx

    val maximumX = (safeDrawingWidth - folderGridWidthPx + leftPadding).coerceAtLeast(leftPadding)
    val maximumY = (safeDrawingHeight - endHeight + topPadding).coerceAtLeast(topPadding)

    val endIntOffset = IntOffset(
        x = folderPopupIntOffset.x.coerceIn(leftPadding, maximumX),
        y = folderPopupIntOffset.y.coerceIn(topPadding, maximumY),
    )

    val startWidth = folderPopupIntSize.width.toFloat()
    val startHeight = folderPopupIntSize.height.toFloat()

    val startCenterX = folderPopupIntOffset.x + startWidth / 2f
    val startCenterY = folderPopupIntOffset.y + startHeight / 2f

    val endCenterX = endIntOffset.x + folderGridWidthPx.toFloat() / 2f
    val endCenterY = endIntOffset.y + endHeight.toFloat() / 2f

    val previewCellSize = minOf(
        folderPopupIntSize.width,
        folderPopupIntSize.height,
    ) / maxOf(FOLDER_PREVIEW_COLUMNS, FOLDER_PREVIEW_ROWS)

    val startPreviewWidth = minOf(
        (previewCellSize * columns).toFloat(),
        availableWidth.toFloat(),
    )

    val startPreviewHeight = minOf(
        (previewCellSize * rows).toFloat(),
        (availableHeight - folderTitleHeightPx).coerceAtLeast(0).toFloat(),
    )

    return FolderPopupLayoutInfo(
        minCellWidthPx = minCellWidthPx,
        minCellHeightPx = minCellHeightPx,
        folderGridWidthPx = folderGridWidthPx,
        folderGridHeightPx = folderGridHeightPx,
        endHeight = endHeight,
        startWidth = startWidth,
        startHeight = startHeight,
        startCenterX = startCenterX,
        startCenterY = startCenterY,
        endCenterX = endCenterX,
        endCenterY = endCenterY,
        startPreviewWidth = startPreviewWidth,
        startPreviewHeight = startPreviewHeight,
    )
}

internal suspend fun handlePageDirection(
    pageDirection: PageDirection?,
    currentPage: Int,
    isInProgress: Boolean,
    onAnimateScrollToPage: suspend (Int) -> Unit,
) {
    if (pageDirection == null || isInProgress) return

    delay(500L.milliseconds)

    when (pageDirection) {
        PageDirection.Left -> onAnimateScrollToPage(currentPage - 1)
        PageDirection.Right -> onAnimateScrollToPage(currentPage + 1)
    }
}

internal fun handleAnimateScrollToPage(
    density: Density,
    drag: Drag,
    isVisibleOverlay: Boolean,
    lockMovement: Boolean,
    hasMoveGridItemResult: Boolean,
    dragIntOffset: IntOffset,
    columns: Int,
    folderPopupIntOffset: IntOffset,
    isDragging: Boolean,
    paddingValues: PaddingValues,
    screenWidth: Int,
    layoutDirection: LayoutDirection,
    folderCellWidth: Int,
    isLastFolderGridItem: Boolean,
    isInProgress: Boolean,
    onUpdateFolderPageDirection: (PageDirection?) -> Unit,
) {
    if (drag != Drag.Dragging ||
        !isVisibleOverlay ||
        !isDragging ||
        lockMovement ||
        !hasMoveGridItemResult ||
        !isLastFolderGridItem ||
        isInProgress
    ) {
        return
    }

    onUpdateFolderPageDirection(
        calculateFolderPageDirection(
            density = density,
            dragIntOffset = dragIntOffset,
            columns = columns,
            folderPopupIntOffset = folderPopupIntOffset,
            paddingValues = paddingValues,
            screenWidth = screenWidth,
            layoutDirection = layoutDirection,
            folderCellWidth = folderCellWidth,
        ),
    )
}

internal fun calculateFolderGridDragPosition(
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

internal fun handleDropFolderGridItem(
    drag: Drag,
    isDragging: Boolean,
    lockMovement: Boolean,
    isVisibleOverlay: Boolean,
    isLastFolderGridItem: Boolean,
    onResetGrid: () -> Unit,
    onResetGridAfterMoveFolder: () -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
) {
    if (drag == Drag.None ||
        drag == Drag.Start ||
        drag == Drag.Dragging ||
        !isLastFolderGridItem
    ) {
        return
    }

    if (isVisibleOverlay && !isDragging) {
        onUpdateIsVisibleOverlay(false)

        return
    }

    if (lockMovement) {
        onUpdateIsVisibleOverlay(false)

        onUpdateIsDragging(false)

        onResetGrid()

        return
    }

    if (isVisibleOverlay) {
        onResetGridAfterMoveFolder()

        onUpdateIsDragging(false)
    }
}

private fun calculateFolderPageDirection(
    density: Density,
    dragIntOffset: IntOffset,
    columns: Int,
    folderPopupIntOffset: IntOffset,
    paddingValues: PaddingValues,
    screenWidth: Int,
    layoutDirection: LayoutDirection,
    folderCellWidth: Int,
): PageDirection? {
    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val rightPadding = with(density) {
        paddingValues.calculateRightPadding(layoutDirection).roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding

    val safeDrawingWidth = screenWidth - horizontalPadding

    val availableWidth = safeDrawingWidth.coerceAtLeast(0)

    val cellWidthDp = folderCellWidth.dp
    val cellWidthPx = with(receiver = density) { cellWidthDp.roundToPx() }

    val folderGridWidthPx = (cellWidthPx * columns)
        .coerceAtMost(availableWidth)

    val edgeDistance = with(density) {
        20.dp.roundToPx()
    }

    val x = folderPopupIntOffset.x - leftPadding

    val dragX = dragIntOffset.x - leftPadding

    val popupX = x.coerceIn(0, availableWidth - folderGridWidthPx) + leftPadding

    val folderDragX = dragX - popupX

    val isOnLeftGrid = folderDragX < edgeDistance
    val isOnRightGrid = folderDragX > folderGridWidthPx - edgeDistance

    return when {
        isOnLeftGrid -> PageDirection.Left
        isOnRightGrid -> PageDirection.Right
        else -> null
    }
}
