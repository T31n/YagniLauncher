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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.folder.FolderPopup
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.FolderGridLayout
import com.eblan.launcher.feature.home.component.FolderTitle
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.getAnimatedRect
import com.eblan.launcher.feature.home.util.getFolderPopupLayoutInfo
import com.eblan.launcher.feature.home.util.handleAnimateScrollToPage
import com.eblan.launcher.feature.home.util.handleDropFolderGridItem
import com.eblan.launcher.feature.home.util.handlePageDirection
import kotlin.math.roundToInt

@Composable
internal fun FolderScreen(
    // Required Parameters
    sharedTransitionScope: SharedTransitionScope,
    drag: Drag,
    folderEntry: FolderEntry,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    safeDrawingHeight: Int,
    safeDrawingWidth: Int,
    statusBarNotifications: Map<String, Int>,
    isVisibleOverlay: Boolean,
    hasShortcutHostPermission: Boolean,
    moveGridItemResult: MoveGridItemResult?,
    isDragging: Boolean,
    dragIntOffset: IntOffset,
    lockMovement: Boolean,
    folderCellWidth: Int,
    folderCellHeight: Int,
    screenHeight: Int,
    screenWidth: Int,
    folderGridItemEntries: List<FolderEntry>,
    showFolderGridItemMenu: Boolean,
    folderPopups: Map<String, FolderPopup>,
    iconPackFilePaths: Map<String, String?>,
    animations: Boolean,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderBackgroundColor: BackgroundColor,
    folderCornerRadius: Int,
    customFolderBackgroundColor: Int,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onMoveFolderGridItemOutsideFolder: (
        gridItem: GridItem,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
    onOpenAppDrawer: () -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onMoveFolderGridItem: (
        folderPopup: FolderPopup,
        movingFolderGridItem: GridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onDismissFolderGridItemMenu: () -> Unit,
    onResetGrid: () -> Unit,
    onResetGridAfterMoveFolder: () -> Unit,
    onUpsertFolderGridItemPopupEntry: (FolderEntry) -> Unit,
    onLongPressFolderGridItem: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
    onDragFolderGridItem: () -> Unit,
    onCloseFolder: (
        folderEntry: FolderEntry,
        isFirstFolderGridItem: Boolean,
    ) -> Unit,
) {
    val folderPopup = folderPopups[folderEntry.id] ?: return

    val folderPopupIntOffset = IntOffset(
        x = folderEntry.x,
        y = folderEntry.y,
    )
    val folderPopupIntSize = IntSize(
        width = folderEntry.width,
        height = folderEntry.height,
    )

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val folderPopupLayoutInfo = getFolderPopupLayoutInfo(
        density = density,
        layoutDirection = layoutDirection,
        paddingValues = paddingValues,
        safeDrawingWidth = safeDrawingWidth,
        safeDrawingHeight = safeDrawingHeight,
        folderPopupIntOffset = folderPopupIntOffset,
        folderPopupIntSize = folderPopupIntSize,
        folderCellWidth = folderCellWidth,
        folderCellHeight = folderCellHeight,
        columns = folderPopup.columns,
        rows = folderPopup.rows,
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
            folderPopup.folderGridItemsByPage.size
        },
    )

    var pageDirection by remember { mutableStateOf<PageDirection?>(null) }

    val isFirstFolderGridItem = folderGridItemEntries.size == 1 &&
        folderGridItemEntries.singleOrNull()?.id == folderEntry.id

    val isLastFolderGridItem =
        folderGridItemEntries.lastOrNull()?.id == folderEntry.id

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
        folderEntry,
        isFirstFolderGridItem,
        animations,
        isLastFolderGridItem,
    ) {
        handleIsCloseFolder(
            drag = currentDrag,
            isDragging = currentIsDragging,
            isVisibleOverlay = currentIsVisibleOverlay,
            moveGridItemResult = currentMoveGridItemResult,
            folderEntry = folderEntry,
            progress = progress,
            isFirstFolderGridItem = isFirstFolderGridItem,
            animations = animations,
            isLastFolderGridItem = isLastFolderGridItem,
            folderPopup = folderPopup,
            onAnimateToScrollToPage = folderGridHorizontalPagerState::animateScrollToPage,
            onMoveFolderGridItemOutsideFolder = onMoveFolderGridItemOutsideFolder,
            onCloseFolder = onCloseFolder,
        )
    }

    LaunchedEffect(
        drag,
        dragIntOffset,
        folderEntry,
        moveGridItemResult,
        isLastFolderGridItem,
        isInProgress,
    ) {
        handleDragFolderGridItem(
            density = density,
            layoutDirection = layoutDirection,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            paddingValues = paddingValues,
            currentPage = folderGridHorizontalPagerState.currentPage,
            isDragging = currentIsDragging.value,
            isInProgress = isInProgress,
            isScrollInProgress = folderGridHorizontalPagerState.isScrollInProgress,
            isVisibleOverlay = currentIsVisibleOverlay.value,
            lockMovement = currentLockMovement.value,
            isLastFolderGridItem = isLastFolderGridItem,
            drag = drag,
            dragIntOffset = dragIntOffset,
            moveGridItemResult = moveGridItemResult,
            folderEntry = folderEntry,
            folderPopup = folderPopup,
            folderPopupIntOffset = folderPopupIntOffset,
            folderCellWidth = folderCellWidth,
            folderCellHeight = folderCellHeight,
            onMoveFolderGridItem = onMoveFolderGridItem,
            onUpsertFolderGridItemPopupEntry = onUpsertFolderGridItemPopupEntry,
        )
    }

    LaunchedEffect(
        key1 = drag,
        key2 = isLastFolderGridItem,
    ) {
        handleDropFolderGridItem(
            drag = drag,
            isDragging = currentIsDragging.value,
            lockMovement = currentLockMovement.value,
            isVisibleOverlay = currentIsVisibleOverlay.value,
            isLastFolderGridItem = isLastFolderGridItem,
            onResetGrid = onResetGrid,
            onResetGridAfterMoveFolder = onResetGridAfterMoveFolder,
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
            onDismissFolderGridItemMenu()
        }
    }

    LaunchedEffect(
        drag,
        dragIntOffset,
        moveGridItemResult,
        folderEntry,
        isLastFolderGridItem,
        isInProgress,
    ) {
        handleAnimateScrollToPage(
            density = density,
            layoutDirection = layoutDirection,
            screenWidth = screenWidth,
            columns = folderPopup.columns,
            paddingValues = paddingValues,
            drag = drag,
            isDragging = currentIsDragging.value,
            dragIntOffset = dragIntOffset,
            hasMoveGridItemResult = moveGridItemResult != null,
            folderPopupIntOffset = folderPopupIntOffset,
            folderCellWidth = folderCellWidth,
            isLastFolderGridItem = isLastFolderGridItem,
            isVisibleOverlay = currentIsVisibleOverlay.value,
            lockMovement = currentLockMovement.value,
            isInProgress = isInProgress,
            onUpdateFolderPageDirection = {
                pageDirection = it
            },
        )
    }

    BackHandler(
        enabled = !folderEntry.isCloseFolder &&
            isLastFolderGridItem &&
            !isInProgress,
    ) {
        onUpsertFolderGridItemPopupEntry(folderEntry.copy(isCloseFolder = true))
    }

    HomeHandler(
        enabled = !folderEntry.isCloseFolder &&
            isLastFolderGridItem &&
            !isInProgress,
    ) {
        onUpsertFolderGridItemPopupEntry(folderEntry.copy(isCloseFolder = true))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(key1 = isLastFolderGridItem) {
                if (isLastFolderGridItem) {
                    detectTapGestures(
                        onPress = {
                            awaitRelease()

                            onUpsertFolderGridItemPopupEntry(
                                folderEntry.copy(
                                    isCloseFolder = true,
                                ),
                            )
                        },
                    )
                }
            },
    ) {
        Surface(
            modifier = Modifier
                .size(
                    width = with(density) { animatedFolderRect.width().toDp() },
                    height = with(density) { animatedFolderRect.height().toDp() },
                )
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
                .clipToBounds(),
            shape = RoundedCornerShape(folderCornerRadius.dp),
            color = when (folderBackgroundColor) {
                BackgroundColor.System -> MaterialTheme.colorScheme.surface
                BackgroundColor.Light -> Color.White
                BackgroundColor.Dark -> Color.Black
                BackgroundColor.Custom -> Color(customFolderBackgroundColor)
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
                        gridItems = folderPopup.folderGridItemsByPage[index],
                        columns = folderPopup.columns,
                        rows = folderPopup.rows,
                        width = animatedPreviewRect.width().roundToInt(),
                        height = animatedPreviewRect.height().roundToInt(),
                        animate = isVisibleOverlay && !isInProgress && animations,
                        modifier = Modifier.fillMaxSize(),
                        slotId = { it.id },
                        content = {
                            InteractiveFolderGridItem(
                                gridItem = it,
                                gridItemSettings = gridItemSettings,
                                folderGridItemEntries = folderGridItemEntries,
                                sharedTransitionScope = sharedTransitionScope,
                                drag = drag,
                                hasShortcutHostPermission = hasShortcutHostPermission,
                                isScrollInProgress = folderGridHorizontalPagerState.isScrollInProgress,
                                statusBarNotifications = statusBarNotifications,
                                isVisibleOverlay = isVisibleOverlay,
                                moveGridItemResult = moveGridItemResult,
                                progress = progress.value,
                                showFolderGridItemMenu = showFolderGridItemMenu,
                                folderPopups = folderPopups,
                                minCellWidthPx = folderPopupLayoutInfo.minCellWidthPx,
                                minCellHeightPx = folderPopupLayoutInfo.minCellHeightPx,
                                paddingValues = paddingValues,
                                sharedElementKey = SharedElementKey(
                                    id = it.id,
                                    parent = SharedElementKey.Parent.Folder,
                                ),
                                isInProgress = isInProgress,
                                iconPackFilePaths = iconPackFilePaths,
                                animations = animations,
                                systemTextColor = systemTextColor,
                                systemCustomTextColor = systemCustomTextColor,
                                folderCornerRadius = folderCornerRadius,
                                folderBackgroundColor = folderBackgroundColor,
                                customFolderBackgroundColor = customFolderBackgroundColor,
                                onOpenAppDrawer = onOpenAppDrawer,
                                onUpsertFolderGridItemPopupEntry = onUpsertFolderGridItemPopupEntry,
                                onLongPressFolderGridItem = onLongPressFolderGridItem,
                                onDragFolderGridItem = onDragFolderGridItem,
                            )
                        },
                    )
                }

                FolderTitle(
                    label = folderPopup.folderGridItem.label,
                    folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                    gridItemsByPage = folderPopup.folderGridItemsByPage,
                    progress = progress.value,
                    folderBackgroundColor = folderBackgroundColor,
                    customFolderBackgroundColor = customFolderBackgroundColor,
                    textColor = gridItemSettings.textColor,
                    customTextColor = gridItemSettings.customTextColor,
                    systemTextColor = systemTextColor,
                    systemCustomTextColor = systemCustomTextColor,
                )
            }
        }
    }
}

private suspend fun handleIsCloseFolder(
    // State
    drag: State<Drag>,
    isDragging: State<Boolean>,
    isVisibleOverlay: State<Boolean>,
    moveGridItemResult: State<MoveGridItemResult?>,
    folderEntry: FolderEntry,
    progress: Animatable<Float, AnimationVector1D>,
    isFirstFolderGridItem: Boolean,
    animations: Boolean,
    isLastFolderGridItem: Boolean,
    folderPopup: FolderPopup,
    // Callbacks
    onAnimateToScrollToPage: suspend (Int) -> Unit,
    onMoveFolderGridItemOutsideFolder: (
        gridItem: GridItem,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
    onCloseFolder: (
        folderEntry: FolderEntry,
        isFirstFolderGridItem: Boolean,
    ) -> Unit,
) {
    if (!folderEntry.isCloseFolder || !isLastFolderGridItem) return

    onAnimateToScrollToPage(0)

    if (animations) {
        progress.animateTo(targetValue = 0f)
    } else {
        progress.snapTo(targetValue = 0f)
    }

    handleMoveFolderGridItemOutsideFolder(
        drag = drag,
        isDragging = isDragging,
        moveGridItemResult = moveGridItemResult,
        folderPopup = folderPopup,
        isVisibleOverlay = isVisibleOverlay,
        onMoveFolderGridItemOutsideFolder = onMoveFolderGridItemOutsideFolder,
    )

    onCloseFolder(
        folderEntry,
        isFirstFolderGridItem,
    )
}

private fun handleMoveFolderGridItemOutsideFolder(
    // Drag State
    drag: State<Drag>,
    isDragging: State<Boolean>,
    moveGridItemResult: State<MoveGridItemResult?>,
    // UI State
    folderPopup: FolderPopup,
    isVisibleOverlay: State<Boolean>,
    // Callbacks
    onMoveFolderGridItemOutsideFolder: (
        gridItem: GridItem,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
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
                page = folderPopup.folderGridItem.page,
                startColumn = -1,
                startRow = -1,
                data = data.copy(
                    index = -1,
                    folderId = null,
                ),
            )
        }

        is GridItemData.Folder -> {
            gridItem.copy(
                page = folderPopup.folderGridItem.page,
                startColumn = -1,
                startRow = -1,
                data = data.copy(
                    index = -1,
                    folderId = null,
                ),
            )
        }

        is GridItemData.ShortcutConfig -> {
            gridItem.copy(
                page = folderPopup.folderGridItem.page,
                startColumn = -1,
                startRow = -1,
                data = data.copy(
                    index = -1,
                    folderId = null,
                ),
            )
        }

        is GridItemData.ShortcutInfo -> {
            gridItem.copy(
                page = folderPopup.folderGridItem.page,
                startColumn = -1,
                startRow = -1,
                data = data.copy(
                    index = -1,
                    folderId = null,
                ),
            )
        }

        is GridItemData.Widget -> error("Unsupported Folder Grid Item")
    }

    onMoveFolderGridItemOutsideFolder(
        newGridItem,
        SharedElementKey(
            id = gridItem.id,
            parent = when (folderPopup.folderGridItem.associate) {
                Associate.Grid -> SharedElementKey.Parent.Grid
                Associate.Dock -> SharedElementKey.Parent.Dock
            },
        ),
    )
}
