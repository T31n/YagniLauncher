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

import android.graphics.RectF
import androidx.activity.compose.BackHandler
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.BackgroundColor
import com.eblan.launcher.domain.model.FolderPopup
import com.eblan.launcher.domain.model.FolderPopupEntry
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.PreviewFolder
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.domain.usecase.grid.FOLDER_PREVIEW_COLUMNS
import com.eblan.launcher.domain.usecase.grid.FOLDER_PREVIEW_ROWS
import com.eblan.launcher.feature.home.component.FolderGridLayout
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.component.PageIndicator
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.PAGE_INDICATOR_HEIGHT
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor
import kotlin.math.roundToInt

@Composable
internal fun FolderScreen(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    drag: Drag,
    folderPopup: FolderPopup,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    safeDrawingHeight: Int,
    safeDrawingWidth: Int,
    statusBarNotifications: Map<String, Int>,
    isVisibleOverlay: Boolean,
    hasShortcutHostPermission: Boolean,
    moveGridItemResult: MoveGridItemResult?,
    homeSettings: HomeSettings,
    isDragging: Boolean,
    dragIntOffset: IntOffset,
    lockMovement: Boolean,
    folderCellWidth: Int,
    folderCellHeight: Int,
    screenHeight: Int,
    screenWidth: Int,
    folderPopups: List<FolderPopup>,
    showFolderGridItemPopup: Boolean,
    previewFolderGridItems: Map<String, PreviewFolder>,
    iconPackInfoFilePaths: Map<String, String?>,
    animations: Boolean,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    onDeleteFolderPopupEntry: (FolderPopupEntry) -> Unit,
    onMoveFolderGridItemOutsideFolder: (GridItem) -> Unit,
    onOpenAppDrawer: () -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onShowGridItemPopup: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateIsCloseFolderGridItemPopup: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
    onMoveFolderGridItem: (
        folderPopup: FolderPopup,
        movingFolderGridItem: GridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onDismissFolderGridItemPopup: () -> Unit,
    onResetGrid: () -> Unit,
    onDragEndAfterMoveFolder: () -> Unit,
    onUpsertFolderPopupEntry: (FolderPopupEntry) -> Unit,
    onUpdateIsVisibleFolders: (Boolean) -> Unit,
) {
    val folderPopupIntOffset = IntOffset(
        x = folderPopup.folderPopupEntry.x,
        y = folderPopup.folderPopupEntry.y,
    )

    val folderPopupIntSize = IntSize(
        width = folderPopup.folderPopupEntry.width,
        height = folderPopup.folderPopupEntry.height,
    )

    val density = LocalDensity.current

    val layoutDirection = LocalLayoutDirection.current

    val folderPopupLayoutInfo = getFolderPopupLayoutInfo(
        density = density,
        layoutDirection = layoutDirection,
        homeSettings = homeSettings,
        paddingValues = paddingValues,
        safeDrawingWidth = safeDrawingWidth,
        safeDrawingHeight = safeDrawingHeight,
        folderPopup = folderPopup,
        folderPopupIntOffset = folderPopupIntOffset,
        folderPopupIntSize = folderPopupIntSize,
    )

    val progress = remember { Animatable(0f) }

    val animatedFolderRect by remember(key1 = folderPopupLayoutInfo) {
        derivedStateOf {
            getAnimatedRect(
                progress = progress.value,
                startWidth = folderPopupLayoutInfo.startWidth,
                startHeight = folderPopupLayoutInfo.startHeight,
                endWidth = folderPopupLayoutInfo.folderGridWidthPx.toFloat(),
                endHeight = folderPopupLayoutInfo.endHeight.toFloat(),
                startCenterX = folderPopupLayoutInfo.startCenterX,
                startCenterY = folderPopupLayoutInfo.startCenterY,
                endCenterX = folderPopupLayoutInfo.endCenterX,
                endCenterY = folderPopupLayoutInfo.endCenterY,
            )
        }
    }

    val animatedPreviewRect by remember(key1 = folderPopupLayoutInfo) {
        derivedStateOf {
            getAnimatedRect(
                progress = progress.value,
                startWidth = folderPopupLayoutInfo.startPreviewWidth,
                startHeight = folderPopupLayoutInfo.startPreviewHeight,
                endWidth = folderPopupLayoutInfo.folderGridWidthPx.toFloat(),
                endHeight = folderPopupLayoutInfo.folderGridHeightPx.toFloat(),
                startCenterX = folderPopupLayoutInfo.startCenterX,
                startCenterY = folderPopupLayoutInfo.startCenterY,
                endCenterX = folderPopupLayoutInfo.endCenterX,
                endCenterY = folderPopupLayoutInfo.endCenterY,
            )
        }
    }

    val folderGridHorizontalPagerState = rememberPagerState(
        pageCount = {
            folderPopup.gridItemsByPage.size
        },
    )

    var pageDirection by remember { mutableStateOf<PageDirection?>(null) }

    val isFirstFolderGridItem = folderPopups.size == 1 &&
        folderPopups.singleOrNull()?.gridItem == folderPopup.gridItem

    val isLastFolderGridItem = folderPopups.lastOrNull()?.gridItem == folderPopup.gridItem

    val currentDrag = rememberUpdatedState(drag)
    val currentIsDragging = rememberUpdatedState(isDragging)
    val currentIsVisibleOverlay = rememberUpdatedState(isVisibleOverlay)
    val currentMoveGridItemResult = rememberUpdatedState(moveGridItemResult)
    val currentLockMovement = rememberUpdatedState(lockMovement)

    val isInProgress by remember {
        derivedStateOf { progress.value < 1f }
    }

    LaunchedEffect(key1 = animations) {
        if (animations) {
            progress.animateTo(targetValue = 1f)
        } else {
            progress.snapTo(targetValue = 1f)
        }
    }

    LaunchedEffect(
        key1 = folderPopup,
        key2 = isFirstFolderGridItem,
        key3 = animations,
    ) {
        handleIsCloseFolder(
            drag = currentDrag,
            isDragging = currentIsDragging,
            isVisibleOverlay = currentIsVisibleOverlay,
            moveGridItemResult = currentMoveGridItemResult,
            folderPopup = folderPopup,
            progress = progress,
            isFirstFolderGridItem = isFirstFolderGridItem,
            animations = animations,
            onAnimateToScrollToPage = folderGridHorizontalPagerState::animateScrollToPage,
            onDeleteFolderPopupEntry = onDeleteFolderPopupEntry,
            onMoveFolderGridItemOutsideFolder = onMoveFolderGridItemOutsideFolder,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
            onUpdateIsVisibleFolders = onUpdateIsVisibleFolders,
        )
    }

    LaunchedEffect(
        drag,
        dragIntOffset,
        folderPopup,
        moveGridItemResult,
        isLastFolderGridItem,
        isInProgress,
    ) {
        handleDragFolderGridItem(
            density = density,
            drag = drag,
            dragIntOffset = dragIntOffset,
            currentPage = folderGridHorizontalPagerState.currentPage,
            folderPopup = folderPopup,
            folderPopupIntOffset = folderPopupIntOffset,
            isDragging = currentIsDragging,
            isVisibleOverlay = currentIsVisibleOverlay,
            isScrollInProgress = folderGridHorizontalPagerState.isScrollInProgress,
            lockMovement = currentLockMovement,
            paddingValues = paddingValues,
            screenHeight = screenHeight,
            screenWidth = screenWidth,
            moveGridItemResult = moveGridItemResult,
            layoutDirection = layoutDirection,
            folderCellWidth = folderCellWidth,
            folderCellHeight = folderCellHeight,
            isLastFolderGridItem = isLastFolderGridItem,
            isInProgress = isInProgress,
            onMoveFolderGridItem = onMoveFolderGridItem,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
            onUpsertFolderPopupEntry = onUpsertFolderPopupEntry,
        )
    }

    LaunchedEffect(
        key1 = drag,
        key2 = isLastFolderGridItem,
    ) {
        handleDropFolderGridItem(
            drag = drag,
            isDragging = currentIsDragging,
            lockMovement = currentLockMovement,
            isVisibleOverlay = currentIsVisibleOverlay,
            isLastFolderGridItem = isLastFolderGridItem,
            onResetGrid = onResetGrid,
            onDragEndAfterMoveFolder = onDragEndAfterMoveFolder,
            onUpdateIsDragging = onUpdateIsDragging,
            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
        )
    }

    LaunchedEffect(
        key1 = pageDirection,
        key2 = isInProgress,
    ) {
        handlePageDirection(
            pageDirection = pageDirection,
            currentPage = folderGridHorizontalPagerState.currentPage,
            isInProgress = isInProgress,
            onAnimateScrollToPage = folderGridHorizontalPagerState::animateScrollToPage,
        )
    }

    LaunchedEffect(key1 = folderGridHorizontalPagerState.isScrollInProgress) {
        if (folderGridHorizontalPagerState.isScrollInProgress) {
            onDismissFolderGridItemPopup()
        }
    }

    LaunchedEffect(
        drag,
        dragIntOffset,
        moveGridItemResult,
        folderPopup,
        isLastFolderGridItem,
        isInProgress,
    ) {
        handleAnimateScrollToPage(
            density = density,
            drag = drag,
            isVisibleOverlay = currentIsVisibleOverlay,
            lockMovement = currentLockMovement,
            moveGridItemResult = moveGridItemResult,
            dragIntOffset = dragIntOffset,
            folderPopup = folderPopup,
            folderPopupIntOffset = folderPopupIntOffset,
            isDragging = currentIsDragging,
            paddingValues = paddingValues,
            screenWidth = screenWidth,
            layoutDirection = layoutDirection,
            folderCellWidth = folderCellWidth,
            isLastFolderGridItem = isLastFolderGridItem,
            isInProgress = isInProgress,
            onUpdateFolderPageDirection = {
                pageDirection = it
            },
        )
    }

    BackHandler(
        enabled = !folderPopup.folderPopupEntry.isCloseFolder &&
            isLastFolderGridItem &&
            !isInProgress,
    ) {
        onUpsertFolderPopupEntry(folderPopup.folderPopupEntry.copy(isCloseFolder = true))
    }

    HomeHandler(
        enabled = !folderPopup.folderPopupEntry.isCloseFolder &&
            isLastFolderGridItem &&
            !isInProgress,
    ) {
        onUpsertFolderPopupEntry(folderPopup.folderPopupEntry.copy(isCloseFolder = true))
    }

    Box(
        modifier = modifier
            .pointerInput(key1 = isLastFolderGridItem) {
                if (isLastFolderGridItem) {
                    detectTapGestures(
                        onPress = {
                            awaitRelease()

                            onUpsertFolderPopupEntry(
                                folderPopup.folderPopupEntry.copy(
                                    isCloseFolder = true,
                                ),
                            )
                        },
                    )
                }
            }
            .fillMaxSize(),
    ) {
        Surface(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = when (layoutDirection) {
                            LayoutDirection.Ltr -> animatedFolderRect.left.roundToInt()

                            LayoutDirection.Rtl -> screenWidth - animatedFolderRect.width()
                                .roundToInt() - animatedFolderRect.left.roundToInt()
                        },
                        y = animatedFolderRect.top.roundToInt(),
                    )
                }
                .size(
                    width = with(density) { animatedFolderRect.width().toDp() },
                    height = with(density) { animatedFolderRect.height().toDp() },
                )
                .clipToBounds(),
            shape = RoundedCornerShape(homeSettings.folderCornerRadius.dp),
            color = when (homeSettings.folderBackgroundColor) {
                BackgroundColor.System -> MaterialTheme.colorScheme.surface
                BackgroundColor.Light -> Color.White
                BackgroundColor.Dark -> Color.Black
                BackgroundColor.Custom -> Color(homeSettings.customFolderBackgroundColor)
            },
            shadowElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.size(
                    width = with(density) { folderPopupLayoutInfo.folderGridWidthPx.toDp() },
                    height = with(density) { folderPopupLayoutInfo.endHeight.toDp() },
                ),
            ) {
                HorizontalPager(
                    modifier = Modifier.weight(1f),
                    state = folderGridHorizontalPagerState,
                    userScrollEnabled = !isVisibleOverlay && !isInProgress,
                ) { index ->
                    FolderGridLayout(
                        modifier = Modifier.fillMaxSize(),
                        columns = folderPopup.columns,
                        gridItems = folderPopup.gridItemsByPage[index],
                        rows = folderPopup.rows,
                        width = animatedPreviewRect.width().roundToInt(),
                        height = animatedPreviewRect.height().roundToInt(),
                        animate = isVisibleOverlay && animations,
                        content = {
                            InteractiveFolderGridItem(
                                sharedTransitionScope = sharedTransitionScope,
                                drag = drag,
                                gridItem = it,
                                gridItemSettings = gridItemSettings,
                                hasShortcutHostPermission = hasShortcutHostPermission,
                                isScrollInProgress = folderGridHorizontalPagerState.isScrollInProgress,
                                statusBarNotifications = statusBarNotifications,
                                isVisibleOverlay = isVisibleOverlay,
                                moveGridItemResult = moveGridItemResult,
                                progress = progress.value,
                                showFolderGridItemPopup = showFolderGridItemPopup,
                                previewFolderGridItems = previewFolderGridItems,
                                minCellWidthPx = folderPopupLayoutInfo.minCellWidthPx,
                                minCellHeightPx = folderPopupLayoutInfo.minCellHeightPx,
                                paddingValues = paddingValues,
                                sharedElementKey = SharedElementKey(
                                    id = it.id,
                                    parent = SharedElementKey.Parent.Folder,
                                ),
                                isInProgress = isInProgress,
                                iconPackInfoFilePaths = iconPackInfoFilePaths,
                                animations = animations,
                                systemTextColor = systemTextColor,
                                systemCustomTextColor = systemCustomTextColor,
                                folderCornerRadius = homeSettings.folderCornerRadius,
                                folderBackgroundColor = homeSettings.folderBackgroundColor,
                                customFolderBackgroundColor = homeSettings.customFolderBackgroundColor,
                                folderPopups = folderPopups,
                                onOpenAppDrawer = onOpenAppDrawer,
                                onUpdateImageBitmap = onUpdateImageBitmap,
                                onUpdateIsDragging = onUpdateIsDragging,
                                onUpdateOverlayBounds = onUpdateOverlayBounds,
                                onUpdateSharedElementKey = onUpdateSharedElementKey,
                                onShowGridItemPopup = onShowGridItemPopup,
                                onUpdateIsCloseFolderGridItemPopup = onUpdateIsCloseFolderGridItemPopup,
                                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                                onUpsertFolderPopupEntry = onUpsertFolderPopupEntry,
                            )
                        },
                    )
                }

                FolderTitle(
                    label = folderPopup.label,
                    gridItemsByPage = folderPopup.gridItemsByPage,
                    folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                    progress = progress.value,
                    folderBackgroundColor = homeSettings.folderBackgroundColor,
                    customFolderBackgroundColor = homeSettings.customFolderBackgroundColor,
                    textColor = gridItemSettings.textColor,
                    customTextColor = gridItemSettings.customTextColor,
                    systemCustomTextColor = systemCustomTextColor,
                    systemTextColor = systemTextColor,
                )
            }
        }
    }
}

@Composable
internal fun FolderTitle(
    modifier: Modifier = Modifier,
    label: String,
    gridItemsByPage: Map<Int, List<GridItem>>,
    folderGridHorizontalPagerState: PagerState,
    progress: Float,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    textColor: TextColor,
    customTextColor: Int,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
) {
    val color = getTextColorFromBackgroundColor(
        backgroundColor = folderBackgroundColor,
        customBackgroundColor = customFolderBackgroundColor,
        textColor = textColor,
        customTextColor = customTextColor,
        systemTextColor = systemTextColor,
        systemCustomTextColor = systemCustomTextColor,
        defaultColor = MaterialTheme.colorScheme.onSurface,
    )

    Box(
        modifier = modifier
            .alpha(if (progress > 0.5) 1f else 0f)
            .fillMaxWidth()
            .height(PAGE_INDICATOR_HEIGHT)
            .padding(horizontal = 10.dp),
    ) {
        if (gridItemsByPage.size > 1) {
            Row(
                modifier = Modifier.matchParentSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = label,
                    color = color,
                    style = MaterialTheme.typography.bodySmall,
                )

                PageIndicator(
                    color = color,
                    gridHorizontalPagerState = folderGridHorizontalPagerState,
                    infiniteScroll = false,
                    pageCount = gridItemsByPage.size,
                )
            }
        } else {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = label,
                color = color,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private data class FolderPopupLayoutInfo(
    val minCellWidthPx: Int,
    val minCellHeightPx: Int,
    val folderGridWidthPx: Int,
    val folderGridHeightPx: Int,
    val endHeight: Int,
    val startWidth: Float,
    val startHeight: Float,
    val startCenterX: Float,
    val startCenterY: Float,
    val endCenterX: Float,
    val endCenterY: Float,
    val startPreviewWidth: Float,
    val startPreviewHeight: Float,
)

private fun getAnimatedRect(
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

private suspend fun handleIsCloseFolder(
    drag: State<Drag>,
    isDragging: State<Boolean>,
    isVisibleOverlay: State<Boolean>,
    moveGridItemResult: State<MoveGridItemResult?>,
    folderPopup: FolderPopup,
    progress: Animatable<Float, AnimationVector1D>,
    isFirstFolderGridItem: Boolean,
    animations: Boolean,
    onAnimateToScrollToPage: suspend (Int) -> Unit,
    onDeleteFolderPopupEntry: (FolderPopupEntry) -> Unit,
    onMoveFolderGridItemOutsideFolder: (GridItem) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateIsVisibleFolders: (Boolean) -> Unit,
) {
    if (!folderPopup.folderPopupEntry.isCloseFolder) return

    onAnimateToScrollToPage(0)

    if (animations) {
        progress.animateTo(targetValue = 0f)
    } else {
        progress.snapTo(targetValue = 0f)
    }

    handleMoveFolderGridItemOutsideFolder(
        drag = drag,
        folderPopup = folderPopup,
        isDragging = isDragging,
        isVisibleOverlay = isVisibleOverlay,
        moveGridItemResult = moveGridItemResult,
        onMoveFolderGridItemOutsideFolder = onMoveFolderGridItemOutsideFolder,
        onUpdateSharedElementKey = onUpdateSharedElementKey,
    )

    if (isFirstFolderGridItem) {
        onUpdateIsVisibleFolders(false)
    }

    onDeleteFolderPopupEntry(folderPopup.folderPopupEntry)
}

private fun handleMoveFolderGridItemOutsideFolder(
    drag: State<Drag>,
    folderPopup: FolderPopup,
    isDragging: State<Boolean>,
    isVisibleOverlay: State<Boolean>,
    moveGridItemResult: State<MoveGridItemResult?>,
    onMoveFolderGridItemOutsideFolder: (GridItem) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    val gridItem = moveGridItemResult.value?.movingGridItem ?: return

    if (drag.value != Drag.Dragging ||
        !isDragging.value ||
        !isVisibleOverlay.value
    ) {
        return
    }

    val newGridItem = when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo -> {
            gridItem.copy(
                page = folderPopup.gridItem.page,
                startColumn = folderPopup.gridItem.startColumn,
                startRow = folderPopup.gridItem.startRow,
                data = data.copy(
                    index = -1,
                    folderId = null,
                ),
            )
        }

        is GridItemData.Folder -> {
            gridItem.copy(
                page = folderPopup.gridItem.page,
                startColumn = folderPopup.gridItem.startColumn,
                startRow = folderPopup.gridItem.startRow,
                data = data.copy(
                    index = -1,
                    folderId = null,
                ),
            )
        }

        is GridItemData.ShortcutConfig -> {
            gridItem.copy(
                page = folderPopup.gridItem.page,
                startColumn = folderPopup.gridItem.startColumn,
                startRow = folderPopup.gridItem.startRow,
                data = data.copy(
                    index = -1,
                    folderId = null,
                ),
            )
        }

        is GridItemData.ShortcutInfo -> {
            gridItem.copy(
                page = folderPopup.gridItem.page,
                startColumn = folderPopup.gridItem.startColumn,
                startRow = folderPopup.gridItem.startRow,
                data = data.copy(
                    index = -1,
                    folderId = null,
                ),
            )
        }

        is GridItemData.Widget -> error("Unsupported Folder Grid Item")
    }

    onUpdateSharedElementKey(
        SharedElementKey(
            id = gridItem.id,
            parent = when (folderPopup.gridItem.associate) {
                Associate.Grid -> SharedElementKey.Parent.Grid
                Associate.Dock -> SharedElementKey.Parent.Dock
            },
        ),
    )

    onMoveFolderGridItemOutsideFolder(newGridItem)
}

private fun getFolderPopupLayoutInfo(
    density: Density,
    layoutDirection: LayoutDirection,
    homeSettings: HomeSettings,
    paddingValues: PaddingValues,
    safeDrawingWidth: Int,
    safeDrawingHeight: Int,
    folderPopup: FolderPopup,
    folderPopupIntOffset: IntOffset,
    folderPopupIntSize: IntSize,
): FolderPopupLayoutInfo {
    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val minCellWidthPx = with(density) { homeSettings.folderCellWidth.dp.roundToPx() }
    val minCellHeightPx = with(density) { homeSettings.folderCellHeight.dp.roundToPx() }

    val availableWidth = (safeDrawingWidth - leftPadding * 2).coerceAtLeast(0)
    val availableHeight = (safeDrawingHeight - topPadding * 2).coerceAtLeast(0)

    val folderTitleHeightPx = with(density) {
        PAGE_INDICATOR_HEIGHT.roundToPx()
    }

    val folderGridWidthPx = (minCellWidthPx * folderPopup.columns).coerceAtMost(availableWidth)

    val folderGridHeightPx = (minCellHeightPx * folderPopup.rows).coerceAtMost(
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
        (previewCellSize * folderPopup.columns).toFloat(),
        availableWidth.toFloat(),
    )

    val startPreviewHeight = minOf(
        (previewCellSize * folderPopup.rows).toFloat(),
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
