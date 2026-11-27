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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.MoveFolderEblanApplicationInfoGridItemResult
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.FolderGridLayout
import com.eblan.launcher.feature.home.component.FolderTitle
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.getAnimatedRect
import com.eblan.launcher.feature.home.util.getBackgroundColor
import com.eblan.launcher.feature.home.util.getFolderBorderColor
import com.eblan.launcher.feature.home.util.getFolderPopupLayoutInfo
import com.eblan.launcher.feature.home.util.handleAnimateScrollToPage
import com.eblan.launcher.feature.home.util.handleDropFolderGridItem
import com.eblan.launcher.feature.home.util.handlePageDirection
import kotlin.math.roundToInt
import kotlin.uuid.Uuid

@Composable
internal fun FolderApplicationScreen(
    // Core State and Layout
    sharedTransitionScope: SharedTransitionScope,
    folderEntry: FolderEntry,
    paddingValues: PaddingValues,
    safeDrawingHeight: Int,
    safeDrawingWidth: Int,
    isVisibleOverlay: Boolean,
    screenWidth: Int,
    screenHeight: Int,
    modifier: Modifier = Modifier,
    // Folder Content and Appearance
    folderEblanApplicationInfoEntries: List<FolderEntry>,
    animations: Boolean,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    folderBorderColor: BackgroundColor,
    customFolderBorderColor: Int,
    folderBorderWidth: Int,
    gridItemSettings: GridItemSettings,
    folderCornerRadius: Int,
    folderCellWidth: Int,
    folderCellHeight: Int,
    iconPackFilePaths: Map<String, String?>,
    // Drag and Drop State
    moveFolderEblanApplicationInfoGridItemResult: MoveFolderEblanApplicationInfoGridItemResult?,
    drag: Drag,
    isDragging: Boolean,
    lockMovement: Boolean,
    dragIntOffset: IntOffset,
    showFolderEblanApplicationInfoGridItemMenu: Boolean,
    // Actions and Callbacks
    onUpsertFolderEblanApplicationInfoEntry: (FolderEntry) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onMoveFolderEblanApplicationInfoGridItem: (
        columns: Int,
        rows: Int,
        folderEblanApplicationInfoGridItems: List<FolderEblanApplicationInfoGridItem>,
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onDismissFolderEblanApplicationGridItemPopup: () -> Unit,
    onResetGrid: () -> Unit,
    onResetGridAfterMoveFolderEblanApplicationInfo: () -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onMoveFolderEblanApplicationInfoGridItemOutsideFolder: (
        folderId: String,
        isFolder: Boolean,
        movingGridItem: GridItem,
    ) -> Unit,
    onLongPressFolderEblanApplicationInfoGridItem: (
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        imageBitmap: suspend () -> ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
    onDragFolderEblanApplicationInfoGridItem: () -> Unit,
    onCloseFolder: (FolderEntry) -> Unit,
    onDismiss: () -> Unit,
    onTapFolderEblanApplicationInfoItem: (FolderEntry) -> Unit,
) {
    val previewFolderEblanApplicationInfo = folderEblanApplicationInfoPopups[folderEntry.id] ?: return

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

    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val rightPadding = with(density) {
        paddingValues.calculateRightPadding(layoutDirection).roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val folderPopupLayoutInfo = getFolderPopupLayoutInfo(
        // Environment
        density = density,
        // Layout
        leftPadding = leftPadding,
        topPadding = topPadding,
        safeDrawingWidth = safeDrawingWidth,
        safeDrawingHeight = safeDrawingHeight,
        // Popup
        folderPopupIntOffset = folderPopupIntOffset,
        folderPopupIntSize = folderPopupIntSize,
        // Grid
        folderCellWidth = folderCellWidth,
        folderCellHeight = folderCellHeight,
        columns = previewFolderEblanApplicationInfo.columns,
        rows = previewFolderEblanApplicationInfo.rows,
    )

    val progress = remember { Animatable(0f) }

    val animatedFolderRect = getAnimatedRect(
        // Animation Progress
        progress = progress.value,
        // Dimensions
        startWidth = folderPopupLayoutInfo.startWidth,
        startHeight = folderPopupLayoutInfo.startHeight,
        endWidth = folderPopupLayoutInfo.folderGridWidthPx.toFloat(),
        endHeight = folderPopupLayoutInfo.endHeight.toFloat(),
        // Center Coordinates
        startCenterX = folderPopupLayoutInfo.startCenterX,
        startCenterY = folderPopupLayoutInfo.startCenterY,
        endCenterX = folderPopupLayoutInfo.endCenterX,
        endCenterY = folderPopupLayoutInfo.endCenterY,
    )

    val animatedPreviewRect = getAnimatedRect(
        // Animation Progress
        progress = progress.value,
        // Dimensions
        startWidth = folderPopupLayoutInfo.startPreviewWidth,
        startHeight = folderPopupLayoutInfo.startPreviewHeight,
        endWidth = folderPopupLayoutInfo.folderGridWidthPx.toFloat(),
        endHeight = folderPopupLayoutInfo.folderGridHeightPx.toFloat(),
        // Center Coordinates
        startCenterX = folderPopupLayoutInfo.startCenterX,
        startCenterY = folderPopupLayoutInfo.startCenterY,
        endCenterX = folderPopupLayoutInfo.endCenterX,
        endCenterY = folderPopupLayoutInfo.endCenterY,
    )

    val folderGridHorizontalPagerState = rememberPagerState(
        pageCount = {
            previewFolderEblanApplicationInfo.folderEblanApplicationInfoGridItemsByPage.size
        },
    )

    var pageDirection by remember { mutableStateOf<PageDirection?>(null) }

    val isLastFolderEblanApplicationInfo =
        folderEblanApplicationInfoEntries.lastOrNull()?.id == folderEntry.id

    val currentDrag = rememberUpdatedState(drag)
    val currentIsDragging = rememberUpdatedState(isDragging)
    val currentIsVisibleOverlay = rememberUpdatedState(isVisibleOverlay)
    val currentMovingFolderEblanApplicationInfoGridItem =
        rememberUpdatedState(moveFolderEblanApplicationInfoGridItemResult?.folderEblanApplicationInfoGridItem)
    val currentLockMovement = rememberUpdatedState(lockMovement)

    val isInProgress by remember {
        derivedStateOf { progress.isRunning || progress.value != 1f }
    }

    LaunchedEffect(animations) {
        if (animations) {
            progress.animateTo(targetValue = 1f)
        } else {
            progress.snapTo(1f)
        }
    }

    LaunchedEffect(
        folderEntry,
        animations,
        isLastFolderEblanApplicationInfo,
        gridItemSettings,
    ) {
        handleIsCloseFolder(
            // Folder Information
            folderEntry = folderEntry,
            isLastFolderEblanApplicationInfo = isLastFolderEblanApplicationInfo,
            // Drag and UI State
            drag = currentDrag,
            isDragging = currentIsDragging,
            isVisibleOverlay = currentIsVisibleOverlay,
            movingFolderEblanApplicationInfoGridItem = currentMovingFolderEblanApplicationInfoGridItem,
            // Animation and Settings
            animations = animations,
            progress = progress,
            gridItemSettings = gridItemSettings,
            // Callbacks
            onAnimateToScrollToPage = folderGridHorizontalPagerState::animateScrollToPage,
            onMoveFolderEblanApplicationInfoGridItemOutsideFolder = onMoveFolderEblanApplicationInfoGridItemOutsideFolder,
            onCloseFolder = onCloseFolder,
        )
    }

    LaunchedEffect(
        drag,
        dragIntOffset,
        folderEntry,
        moveFolderEblanApplicationInfoGridItemResult,
        isLastFolderEblanApplicationInfo,
        isInProgress,
    ) {
        handleDragFolderEblanApplicationInfoGridItem(
            // Environment
            density = density,
            layoutDirection = layoutDirection,
            // Screen Dimensions
            screenHeight = screenHeight,
            screenWidth = screenWidth,
            leftPadding = leftPadding,
            topPadding = topPadding,
            rightPadding = rightPadding,
            bottomPadding = bottomPadding,
            // Folder Item State
            currentPage = folderGridHorizontalPagerState.currentPage,
            folderEntry = folderEntry,
            columns = previewFolderEblanApplicationInfo.columns,
            rows = previewFolderEblanApplicationInfo.rows,
            folderEblanApplicationInfoGridItems = previewFolderEblanApplicationInfo.folderEblanApplicationInfoGridItems,
            folderCellWidth = folderCellWidth,
            folderCellHeight = folderCellHeight,
            isLastFolderEblanApplicationInfoGridItem = isLastFolderEblanApplicationInfo,
            movingFolderEblanApplicationInfoGridItem = currentMovingFolderEblanApplicationInfoGridItem.value,
            // Drag State
            drag = currentDrag.value,
            dragIntOffset = dragIntOffset,
            folderPopupIntOffset = folderPopupIntOffset,
            isDragging = currentIsDragging.value,
            isVisibleOverlay = currentIsVisibleOverlay.value,
            isScrollInProgress = folderGridHorizontalPagerState.isScrollInProgress,
            lockMovement = currentLockMovement.value,
            isInProgress = isInProgress,
            // Callbacks
            onMoveFolderEblanApplicationInfoGridItem = onMoveFolderEblanApplicationInfoGridItem,
            onUpsertFolderEblanApplicationInfoEntry = onUpsertFolderEblanApplicationInfoEntry,
        )
    }

    LaunchedEffect(
        key1 = drag,
        key2 = isLastFolderEblanApplicationInfo,
    ) {
        handleDropFolderGridItem(
            // Drag State
            drag = drag,
            isDragging = isDragging,
            lockMovement = lockMovement,
            isVisibleOverlay = isVisibleOverlay,
            isLastFolderGridItem = isLastFolderEblanApplicationInfo,
            // Callbacks
            onResetGrid = onResetGrid,
            onResetGridAfterMoveFolder = onResetGridAfterMoveFolderEblanApplicationInfo,
            onUpdateIsDragging = onUpdateIsDragging,
            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
        )
    }

    LaunchedEffect(
        key1 = pageDirection,
        key2 = isInProgress,
    ) {
        handlePageDirection(
            // Page State
            pageDirection = pageDirection,
            currentPage = folderGridHorizontalPagerState.currentPage,
            isInProgress = isInProgress,
            // Actions
            onAnimateScrollToPage = folderGridHorizontalPagerState::animateScrollToPage,
        )
    }

    LaunchedEffect(folderGridHorizontalPagerState.isScrollInProgress) {
        if (folderGridHorizontalPagerState.isScrollInProgress) {
            onDismissFolderEblanApplicationGridItemPopup()
        }
    }

    LaunchedEffect(
        drag,
        dragIntOffset,
        moveFolderEblanApplicationInfoGridItemResult,
        previewFolderEblanApplicationInfo,
        isLastFolderEblanApplicationInfo,
        isInProgress,
    ) {
        handleAnimateScrollToPage(
            // Environment and Layout
            density = density,
            screenWidth = screenWidth,
            columns = previewFolderEblanApplicationInfo.columns,
            leftPadding = leftPadding,
            rightPadding = rightPadding,
            // Drag and Gesture
            drag = drag,
            isDragging = currentIsDragging.value,
            dragIntOffset = dragIntOffset,
            // Grid and Folder State
            hasMoveGridItemResult = moveFolderEblanApplicationInfoGridItemResult != null,
            folderPopupIntOffset = folderPopupIntOffset,
            folderCellWidth = folderCellWidth,
            isLastFolderGridItem = isLastFolderEblanApplicationInfo,
            isVisibleOverlay = currentIsVisibleOverlay.value,
            lockMovement = currentLockMovement.value,
            isInProgress = isInProgress,
            // Callbacks
            onUpdateFolderPageDirection = { currentPageDirection ->
                pageDirection = currentPageDirection
            },
        )
    }

    BackHandler(
        enabled = !folderEntry.isCloseFolder &&
            isLastFolderEblanApplicationInfo &&
            !isInProgress,
    ) {
        onUpsertFolderEblanApplicationInfoEntry(folderEntry.copy(isCloseFolder = true))
    }

    HomeHandler(
        enabled = !folderEntry.isCloseFolder &&
            isLastFolderEblanApplicationInfo &&
            !isInProgress,
    ) {
        onUpsertFolderEblanApplicationInfoEntry(folderEntry.copy(isCloseFolder = true))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isLastFolderEblanApplicationInfo) {
                if (isLastFolderEblanApplicationInfo) {
                    detectTapGestures(
                        onPress = {
                            awaitRelease()

                            onUpsertFolderEblanApplicationInfoEntry(folderEntry.copy(isCloseFolder = true))
                        },
                    )
                }
            },
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
            shape = RoundedCornerShape(folderCornerRadius.dp),
            color = getBackgroundColor(
                backgroundColor = folderBackgroundColor,
                customBackgroundColor = customFolderBackgroundColor,
                defaultColor = MaterialTheme.colorScheme.surface,
            ),
            shadowElevation = 2.dp,
            border = BorderStroke(
                width = folderBorderWidth.dp,
                color = getFolderBorderColor(
                    folderBorderColor = folderBorderColor,
                    customFolderBorderColor = customFolderBorderColor,
                ),
            ),
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
                        // Grid Configuration
                        gridItems = previewFolderEblanApplicationInfo.folderEblanApplicationInfoGridItemsByPage[index],
                        columns = previewFolderEblanApplicationInfo.columns,
                        rows = previewFolderEblanApplicationInfo.rows,
                        width = animatedPreviewRect.width().roundToInt(),
                        height = animatedPreviewRect.height().roundToInt(),
                        // Animation and Modifier
                        animate = isVisibleOverlay && !isInProgress && animations,
                        modifier = Modifier.fillMaxSize(),
                        // Callbacks
                        slotId = { currentFolderEblanApplicationInfoGridItem ->
                            currentFolderEblanApplicationInfoGridItem.id
                        },
                        content = { currentFolderEblanApplicationInfoGridItem ->
                            InteractiveFolderEblanApplicationInfoItem(
                                // Scope and Animation
                                sharedTransitionScope = sharedTransitionScope,
                                animations = animations,
                                // Folder Item and Content
                                folderEblanApplicationInfoGridItem = currentFolderEblanApplicationInfoGridItem,
                                previewNestedFolderEblanApplicationInfoGridItems = folderEblanApplicationInfoPopups[currentFolderEblanApplicationInfoGridItem.id]?.previewFolderEblanApplicationInfoGridItems,
                                isVisibleFolder = folderEblanApplicationInfoEntries.any { currentFolderEntry ->
                                    currentFolderEntry.id == currentFolderEblanApplicationInfoGridItem.id
                                },
                                // Styling and Colors
                                folderBackgroundColor = folderBackgroundColor,
                                customFolderBackgroundColor = customFolderBackgroundColor,
                                folderBorderColor = folderBorderColor,
                                customFolderBorderColor = customFolderBorderColor,
                                folderBorderWidth = folderBorderWidth,
                                systemTextColor = systemTextColor,
                                systemCustomTextColor = systemCustomTextColor,
                                folderCornerRadius = folderCornerRadius,
                                // Layout and Settings
                                gridItemSettings = gridItemSettings,
                                leftPadding = leftPadding,
                                topPadding = topPadding,
                                iconPackFilePaths = iconPackFilePaths,
                                // State and Progress
                                isVisibleOverlay = isVisibleOverlay,
                                isScrollInProgress = folderGridHorizontalPagerState.isScrollInProgress,
                                isInProgress = isInProgress,
                                progress = progress.value,
                                movingFolderEblanApplicationInfoGridItemId = moveFolderEblanApplicationInfoGridItemResult?.folderEblanApplicationInfoGridItem?.id,
                                drag = drag,
                                showFolderEblanApplicationInfoGridItemMenu = showFolderEblanApplicationInfoGridItemMenu,
                                // Callbacks
                                onDismiss = onDismiss,
                                onLongPressFolderEblanApplicationInfoGridItem = onLongPressFolderEblanApplicationInfoGridItem,
                                onDragFolderEblanApplicationInfoGridItem = onDragFolderEblanApplicationInfoGridItem,
                                onTapFolderEblanApplicationInfoItem = onTapFolderEblanApplicationInfoItem,
                            )
                        },
                    )
                }

                FolderTitle(
                    // Content
                    label = previewFolderEblanApplicationInfo.folderEblanApplicationInfo.label,
                    pageCount = previewFolderEblanApplicationInfo.folderEblanApplicationInfoGridItemsByPage.size,
                    progress = progress.value,
                    // State
                    folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                    // Appearance
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
    // Folder Information
    folderEntry: FolderEntry,
    isLastFolderEblanApplicationInfo: Boolean,
    // Drag and UI State
    drag: State<Drag>,
    isDragging: State<Boolean>,
    isVisibleOverlay: State<Boolean>,
    movingFolderEblanApplicationInfoGridItem: State<FolderEblanApplicationInfoGridItem?>,
    // Animation and Settings
    animations: Boolean,
    progress: Animatable<Float, AnimationVector1D>,
    gridItemSettings: GridItemSettings,
    // Callbacks
    onAnimateToScrollToPage: suspend (Int) -> Unit,
    onMoveFolderEblanApplicationInfoGridItemOutsideFolder: (
        folderId: String,
        isFolder: Boolean,
        movingGridItem: GridItem,
    ) -> Unit,
    onCloseFolder: (FolderEntry) -> Unit,
) {
    if (!folderEntry.isCloseFolder || !isLastFolderEblanApplicationInfo) return

    onAnimateToScrollToPage(0)

    if (animations) {
        progress.animateTo(targetValue = 0f)
    } else {
        progress.snapTo(0f)
    }

    handleMoveFolderGridItemOutsideFolder(
        // Settings
        gridItemSettings = gridItemSettings,
        // State
        drag = drag,
        isDragging = isDragging,
        isVisibleOverlay = isVisibleOverlay,
        movingFolderEblanApplicationInfoGridItem = movingFolderEblanApplicationInfoGridItem,
        // Callbacks
        onMoveFolderEblanApplicationInfoGridItemOutsideFolder = onMoveFolderEblanApplicationInfoGridItemOutsideFolder,
    )

    onCloseFolder(folderEntry)
}

private fun handleMoveFolderGridItemOutsideFolder(
    // Settings
    gridItemSettings: GridItemSettings,
    // State
    drag: State<Drag>,
    isDragging: State<Boolean>,
    isVisibleOverlay: State<Boolean>,
    movingFolderEblanApplicationInfoGridItem: State<FolderEblanApplicationInfoGridItem?>,
    // Callbacks
    onMoveFolderEblanApplicationInfoGridItemOutsideFolder: (
        folderId: String,
        isFolder: Boolean,
        movingGridItem: GridItem,
    ) -> Unit,
) {
    val folderEblanApplicationInfoGridItem =
        movingFolderEblanApplicationInfoGridItem.value ?: return

    if (drag.value != Drag.Dragging ||
        !isDragging.value ||
        !isVisibleOverlay.value
    ) {
        return
    }

    val eblanAction = EblanAction(
        eblanActionType = EblanActionType.None,
        serialNumber = 0L,
        componentName = "",
    )

    val homeGridItemId = Uuid.random().toHexString()

    val gridItem = when (val data = folderEblanApplicationInfoGridItem.data) {
        is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> {
            GridItem(
                id = homeGridItemId,
                page = 0,
                startColumn = -1,
                startRow = -1,
                columnSpan = 1,
                rowSpan = 1,
                data = GridItemData.ApplicationInfo(
                    serialNumber = data.serialNumber,
                    componentName = data.componentName,
                    packageName = data.packageName,
                    icon = data.icon,
                    label = data.label,
                    customIcon = data.customIcon,
                    customLabel = data.customLabel,
                    index = -1,
                    folderId = null,
                ),
                associate = Associate.Grid,
                override = false,
                gridItemSettings = gridItemSettings,
                doubleTap = eblanAction,
                swipeUp = eblanAction,
                swipeDown = eblanAction,
            )
        }

        is FolderEblanApplicationInfoGridItemData.Folder -> {
            GridItem(
                id = homeGridItemId,
                page = 0,
                startColumn = -1,
                startRow = -1,
                columnSpan = 1,
                rowSpan = 1,
                data = GridItemData.Folder(
                    label = data.label,
                    icon = data.icon,
                    index = -1,
                    folderId = null,
                ),
                associate = Associate.Grid,
                override = false,
                gridItemSettings = gridItemSettings,
                doubleTap = eblanAction,
                swipeUp = eblanAction,
                swipeDown = eblanAction,
            )
        }
    }

    onMoveFolderEblanApplicationInfoGridItemOutsideFolder(
        folderEblanApplicationInfoGridItem.id,
        folderEblanApplicationInfoGridItem.data is FolderEblanApplicationInfoGridItemData.Folder,
        gridItem,
    )
}
