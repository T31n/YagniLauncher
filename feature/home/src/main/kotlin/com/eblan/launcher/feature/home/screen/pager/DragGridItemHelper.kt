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
package com.eblan.launcher.feature.home.screen.pager

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.State
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.grid.getWidgetGridItemSize
import com.eblan.launcher.domain.grid.getWidgetGridItemSpan
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.PAGE_INDICATOR_HEIGHT
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

internal suspend fun handlePageDirection(
    // State
    folderGridItemEntries: State<List<FolderEntry>>,
    pageDirection: PageDirection?,
    currentPage: Int,
    // Actions
    onAnimateScrollToPage: suspend (Int) -> Unit
) {
    delay(500L.milliseconds)

    if (pageDirection == null || folderGridItemEntries.value.isNotEmpty()) return

    when (pageDirection) {
        PageDirection.Left -> onAnimateScrollToPage(currentPage - 1)
        PageDirection.Right -> onAnimateScrollToPage(currentPage + 1)
    }
}

internal fun handleAnimateScrollToPage(
    // Scroll State
    isDragging: Boolean,
    dragIntOffset: IntOffset,
    associate: Associate?,
    // Layout and Display
    screenWidth: Int,
    density: Density,
    layoutDirection: LayoutDirection,
    paddingValues: PaddingValues,
    gridItemSource: GridItemSource?,
    // Callbacks
    onUpdateDockPageDirection: (PageDirection?) -> Unit,
    onUpdateGridPageDirection: (PageDirection?) -> Unit
) {
    if (gridItemSource == null || !isDragging) return

    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val rightPadding = with(density) {
        paddingValues.calculateRightPadding(layoutDirection).roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding

    val safeDrawingWidth = screenWidth - horizontalPadding

    val edgeDistance = with(density) {
        20.dp.roundToPx()
    }

    val dragX = dragIntOffset.x - leftPadding

    animateScrollToPage(
        associate = associate,
        dragX = dragX,
        edgeDistance = edgeDistance,
        safeDrawingWidth = safeDrawingWidth,
        onUpdateDockPageDirection = onUpdateDockPageDirection,
        onUpdateGridPageDirection = onUpdateGridPageDirection
    )
}

internal fun handleDragGridItem(
    // Grid Dimensions
    columns: Int,
    rows: Int,
    gridCurrentPage: Int,
    // Dock Dimensions
    dockColumns: Int,
    dockRows: Int,
    dockGridCurrentPage: Int,
    dockHeight: Int,
    // Screen and Layout
    screenWidth: Int,
    screenHeight: Int,
    density: Density,
    layoutDirection: LayoutDirection,
    paddingValues: PaddingValues,
    // Drag State
    drag: Drag,
    dragIntOffset: IntOffset,
    isDragging: Boolean,
    isVisibleOverlay: Boolean,
    lockMovement: Boolean,
    gridItemSource: GridItemSource?,
    moveGridItemResult: MoveGridItemResult?,
    // Scroll State
    isGridScrollInProgress: Boolean,
    isDockScrollInProgress: Boolean,
    // Callbacks
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onUpdateAssociate: (Associate) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit
) {
    if (drag != Drag.Dragging ||
        isGridScrollInProgress ||
        isDockScrollInProgress ||
        !isVisibleOverlay ||
        !isDragging ||
        lockMovement
    ) {
        return
    }

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

    val layoutDirectionX = when (layoutDirection) {
        LayoutDirection.Rtl -> safeDrawingWidth - localDragX
        LayoutDirection.Ltr -> localDragX
    }

    val dockHeightPx = with(density) {
        dockHeight.dp.roundToPx()
    }

    val isOnDock = dockHeightPx > 0 && localDragY > safeDrawingHeight - dockHeightPx

    when (val currentGridItemSource = gridItemSource ?: return) {
        is GridItemSource.Existing,
        is GridItemSource.ExistingFolder,
        is GridItemSource.New,
        is GridItemSource.NewFolder,
        is GridItemSource.Pin,
        -> {
            if (isOnDock) {
                dragDockGridItem(
                    gridItemSource = currentGridItemSource,
                    currentPage = dockGridCurrentPage,
                    moveGridItemResult = moveGridItemResult,
                    dockColumns = dockColumns,
                    dockRows = dockRows,
                    dockHeightPx = dockHeightPx,
                    dragX = layoutDirectionX,
                    dragY = localDragY,
                    safeDrawingWidth = safeDrawingWidth,
                    safeDrawingHeight = safeDrawingHeight,
                    onMoveGridItem = onMoveGridItem,
                    onUpdateAssociate = onUpdateAssociate,
                    onUpdateSharedElementKey = onUpdateSharedElementKey
                )
            } else {
                dragGridItem(
                    density = density,
                    columns = columns,
                    rows = rows,
                    currentPage = gridCurrentPage,
                    gridItemSource = currentGridItemSource,
                    moveGridItemResult = moveGridItemResult,
                    dockHeightPx = dockHeightPx,
                    safeDrawingWidth = safeDrawingWidth,
                    safeDrawingHeight = safeDrawingHeight,
                    dragX = layoutDirectionX,
                    dragY = localDragY,
                    onMoveGridItem = onMoveGridItem,
                    onUpdateAssociate = onUpdateAssociate,
                    onUpdateSharedElementKey = onUpdateSharedElementKey
                )
            }
        }
    }
}

internal suspend fun handleConflictingGridItem(
    // Item State
    gridItem: State<GridItem>,
    folderGridItems: State<List<GridItem>?>,
    // Drag State
    drag: State<Drag>,
    isDragging: State<Boolean>,
    isVisibleOverlay: State<Boolean>,
    moveGridItemResult: MoveGridItemResult?,
    lockMovement: State<Boolean>,
    intOffset: IntOffset,
    intSize: IntSize,
    // Callbacks
    onShowFolderWhenDragging: (
        folderEntry: FolderEntry,
        gridItem: GridItem,
    ) -> Unit
) {
    delay(1000L.milliseconds)

    val conflictingGridItem = moveGridItemResult?.conflictingGridItem ?: return

    if (drag.value != Drag.Dragging ||
        !moveGridItemResult.isSuccess ||
        !isVisibleOverlay.value ||
        !isDragging.value ||
        lockMovement.value ||
        conflictingGridItem.id != gridItem.value.id
    ) {
        return
    }

    val movingFolderGridItem = calculateMovingFolderGridItem(
        movingGridItem = moveGridItemResult.movingGridItem,
        conflictingGridItem = conflictingGridItem,
        folderGridItems = folderGridItems.value
    )

    onShowFolderWhenDragging(
        FolderEntry(
            id = conflictingGridItem.id,
            x = intOffset.x,
            y = intOffset.y,
            width = intSize.width,
            height = intSize.height,
            isCloseFolder = false,
        ),
        movingFolderGridItem,
    )
}

private fun getMoveGridItem(
    // Grid Dimensions
    columns: Int,
    rows: Int,
    gridWidth: Int,
    gridHeight: Int,
    // Cell Dimensions
    cellWidth: Int,
    cellHeight: Int,
    // Grid Position
    gridX: Int,
    gridY: Int,
    // Item Details
    gridItem: GridItem,
    gridItemSource: GridItemSource,
    associate: Associate,
    currentPage: Int
): GridItem = when (gridItemSource) {
    is GridItemSource.Existing,
    is GridItemSource.ExistingFolder,
    -> {
        val (startColumn, startRow) = getStartPosition(
            x = gridX,
            y = gridY,
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            columns = columns,
            rows = rows,
            columnSpan = gridItem.columnSpan,
            rowSpan = gridItem.rowSpan
        )

        gridItem.copy(
            page = currentPage,
            startColumn = startColumn,
            startRow = startRow,
            associate = associate,
        )
    }

    is GridItemSource.New,
    is GridItemSource.NewFolder,
    is GridItemSource.Pin,
    -> {
        getMoveNewGridItem(
            gridItem = gridItem,
            associate = associate,
            columns = columns,
            rows = rows,
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            gridX = gridX,
            gridY = gridY,
            currentPage = currentPage
        )
    }
}

private fun getMoveNewGridItem(
    // Grid Item
    gridItem: GridItem,
    associate: Associate,
    // Grid Dimensions
    columns: Int,
    rows: Int,
    gridWidth: Int,
    gridHeight: Int,
    cellWidth: Int,
    cellHeight: Int,
    // Position
    gridX: Int,
    gridY: Int,
    currentPage: Int
): GridItem = when (val data = gridItem.data) {
    is GridItemData.Widget -> {
        val (checkedColumnSpan, checkedRowSpan) = getWidgetGridItemSpan(
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            minWidth = data.minWidth,
            minHeight = data.minHeight,
            targetCellWidth = data.targetCellWidth,
            targetCellHeight = data.targetCellHeight,
        )

        val (checkedMinWidth, checkedMinHeight) = getWidgetGridItemSize(
            columns = columns,
            rows = rows,
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            minWidth = data.minWidth,
            minHeight = data.minHeight,
            targetCellWidth = data.targetCellWidth,
            targetCellHeight = data.targetCellHeight,
        )

        val (startColumn, startRow) = getStartPosition(
            x = gridX,
            y = gridY,
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            columns = columns,
            rows = rows,
            columnSpan = checkedColumnSpan,
            rowSpan = checkedRowSpan
        )

        val newData = data.copy(
            minWidth = checkedMinWidth,
            minHeight = checkedMinHeight,
        )

        gridItem.copy(
            page = currentPage,
            startColumn = startColumn,
            startRow = startRow,
            columnSpan = checkedColumnSpan.coerceIn(1, columns),
            rowSpan = checkedRowSpan.coerceIn(1, rows),
            data = newData,
            associate = associate,
        )
    }

    else -> {
        val (startColumn, startRow) = getStartPosition(
            x = gridX,
            y = gridY,
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            columns = columns,
            rows = rows,
            columnSpan = gridItem.columnSpan,
            rowSpan = gridItem.rowSpan
        )

        gridItem.copy(
            page = currentPage,
            startColumn = startColumn,
            startRow = startRow,
            associate = associate,
        )
    }
}

private fun animateScrollToPage(
    // Scroll Target
    associate: Associate?,
    dragX: Int,
    edgeDistance: Int,
    safeDrawingWidth: Int,
    // Callbacks
    onUpdateDockPageDirection: (PageDirection?) -> Unit,
    onUpdateGridPageDirection: (PageDirection?) -> Unit
) {
    val isOnLeftGrid = dragX < edgeDistance

    val isOnRightGrid = dragX > safeDrawingWidth - edgeDistance

    fun animateScrollToPage(onUpdatePageDirection: (PageDirection?) -> Unit) {
        if (isOnLeftGrid) {
            onUpdatePageDirection(PageDirection.Left)
        } else if (isOnRightGrid) {
            onUpdatePageDirection(PageDirection.Right)
        } else {
            onUpdatePageDirection(null)
        }
    }

    when (associate) {
        Associate.Grid -> animateScrollToPage(onUpdatePageDirection = onUpdateGridPageDirection)
        Associate.Dock -> animateScrollToPage(onUpdatePageDirection = onUpdateDockPageDirection)
        null -> Unit
    }
}

private fun getStartPosition(
    // Coordinates
    x: Int,
    y: Int,
    // Cell Dimensions
    cellWidth: Int,
    cellHeight: Int,
    // Grid Size
    columns: Int,
    rows: Int,
    // Span
    columnSpan: Int,
    rowSpan: Int
): Pair<Int, Int> {
    val safeColumnSpan = columnSpan.coerceIn(1, columns)
    val safeRowSpan = rowSpan.coerceIn(1, rows)

    val targetColumn = x / cellWidth
    val targetRow = y / cellHeight

    val maxStartColumn = (columns - safeColumnSpan).coerceAtLeast(0)
    val maxStartRow = (rows - safeRowSpan).coerceAtLeast(0)

    val startColumn = targetColumn.coerceIn(0, maxStartColumn)
    val startRow = targetRow.coerceIn(0, maxStartRow)

    return startColumn to startRow
}

private fun calculateMovingFolderGridItem(
    // Grid Items
    movingGridItem: GridItem,
    conflictingGridItem: GridItem,
    folderGridItems: List<GridItem>?
): GridItem {
    val maxIndex = folderGridItems?.maxOfOrNull {
        when (val data = it.data) {
            is GridItemData.ApplicationInfo -> data.index + 1
            is GridItemData.ShortcutInfo -> data.index + 1
            is GridItemData.ShortcutConfig -> data.index + 1
            is GridItemData.Folder -> data.index + 1
            else -> error("Unsupported folder grid item")
        }
    } ?: 0

    val movingData = when (val data = movingGridItem.data) {
        is GridItemData.ApplicationInfo -> data.copy(
            index = maxIndex,
            folderId = conflictingGridItem.id,
        )

        is GridItemData.ShortcutConfig -> data.copy(
            index = maxIndex,
            folderId = conflictingGridItem.id,
        )

        is GridItemData.ShortcutInfo -> data.copy(
            index = maxIndex,
            folderId = conflictingGridItem.id,
        )

        is GridItemData.Folder -> {
            data.copy(
                index = maxIndex,
                folderId = conflictingGridItem.id,
            )
        }

        else -> error("Unsupported Folder GridItem ")
    }

    return movingGridItem.copy(data = movingData)
}

private fun dragGridItem(
    // Grid State
    density: Density,
    columns: Int,
    rows: Int,
    currentPage: Int,
    gridItemSource: GridItemSource,
    moveGridItemResult: MoveGridItemResult?,
    dockHeightPx: Int,
    safeDrawingWidth: Int,
    safeDrawingHeight: Int,
    // Drag State
    dragX: Int,
    dragY: Int,
    // Callbacks
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onUpdateAssociate: (Associate) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit
) {
    val movingGridItem = moveGridItemResult?.movingGridItem ?: return

    val pageIndicatorHeightPx = with(density) {
        PAGE_INDICATOR_HEIGHT.roundToPx()
    }

    val gridHeightWithPadding = safeDrawingHeight - dockHeightPx - pageIndicatorHeightPx

    val cellWidth = safeDrawingWidth / columns
    val cellHeight = gridHeightWithPadding / rows

    val newMovingGridItem = getMoveGridItem(
        columns = columns,
        rows = rows,
        gridWidth = safeDrawingWidth,
        gridHeight = gridHeightWithPadding,
        cellWidth = cellWidth,
        cellHeight = cellHeight,
        gridX = dragX,
        gridY = dragY,
        gridItem = movingGridItem,
        gridItemSource = gridItemSource,
        associate = Associate.Grid,
        currentPage = currentPage
    )

    if (isGridItemSpanWithinBounds(
            gridItem = newMovingGridItem,
            columns = columns,
            rows = rows,
        ) && newMovingGridItem != movingGridItem
    ) {
        onUpdateAssociate(Associate.Grid)

        onUpdateSharedElementKey(
            SharedElementKey(
                id = newMovingGridItem.id,
                parent = SharedElementKey.Parent.Grid,
            ),
        )

        onMoveGridItem(
            newMovingGridItem,
            dragX,
            dragY,
            columns,
            rows,
            safeDrawingWidth,
            gridHeightWithPadding,
        )
    }
}

private fun dragDockGridItem(
    // Grid State
    gridItemSource: GridItemSource,
    currentPage: Int,
    moveGridItemResult: MoveGridItemResult?,
    // Dock Dimensions
    dockColumns: Int,
    dockRows: Int,
    dockHeightPx: Int,
    // Drag Position
    dragX: Int,
    dragY: Int,
    // Drawing Bounds
    safeDrawingWidth: Int,
    safeDrawingHeight: Int,
    // Callbacks
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onUpdateAssociate: (Associate) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit
) {
    val movingGridItem = moveGridItemResult?.movingGridItem ?: return

    val cellWidth = safeDrawingWidth / dockColumns
    val cellHeight = dockHeightPx / dockRows

    val dockY = dragY - (safeDrawingHeight - dockHeightPx)

    val newMovingGridItem = getMoveGridItem(
        columns = dockColumns,
        rows = dockRows,
        gridWidth = safeDrawingWidth,
        gridHeight = dockHeightPx,
        cellWidth = cellWidth,
        cellHeight = cellHeight,
        gridX = dragX,
        gridY = dockY,
        gridItem = movingGridItem,
        gridItemSource = gridItemSource,
        associate = Associate.Dock,
        currentPage = currentPage
    )

    if (isGridItemSpanWithinBounds(
            gridItem = newMovingGridItem,
            columns = dockColumns,
            rows = dockRows,
        ) && newMovingGridItem != movingGridItem
    ) {
        onUpdateAssociate(Associate.Dock)

        onUpdateSharedElementKey(
            SharedElementKey(
                id = newMovingGridItem.id,
                parent = SharedElementKey.Parent.Dock,
            ),
        )

        onMoveGridItem(
            newMovingGridItem,
            dragX,
            dockY,
            dockColumns,
            dockRows,
            safeDrawingWidth,
            dockHeightPx,
        )
    }
}
