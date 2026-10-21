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
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.MoveFolderEblanApplicationInfoGridItemResult
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
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
import com.eblan.launcher.feature.home.util.getFolderPopupLayoutInfo
import com.eblan.launcher.feature.home.util.handleAnimateScrollToPage
import com.eblan.launcher.feature.home.util.handleDropFolderGridItem
import com.eblan.launcher.feature.home.util.handlePageDirection
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
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
    appDrawerSettings: AppDrawerSettings,
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
    onUpsertFolderEblanApplicationInfoPopupEntry: (FolderEntry) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onMoveFolderEblanApplicationInfoGridItem: (
        folderEblanApplicationInfoPopup: FolderEblanApplicationInfoPopup,
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
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        movingGridItem: GridItem,
    ) -> Unit,
    onLongPressFolderEblanApplicationInfoGridItem: (
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
    onDragFolderEblanApplicationInfoGridItem: () -> Unit,
    onCloseFolder: (
        folderEntry: FolderEntry,
        isFirstFolderEblanApplicationInfoGridItem: Boolean,
    ) -> Unit,
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

    val folderPopupLayoutInfo = getFolderPopupLayoutInfo(
        // Environment
        density = density,
        layoutDirection = layoutDirection,
        // Layout
        paddingValues = paddingValues,
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

    val animatedFolderRect by remember(key1 = folderPopupLayoutInfo) {
        derivedStateOf {
            getAnimatedRect(
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
        }
    }

    val animatedPreviewRect by remember(key1 = folderPopupLayoutInfo) {
        derivedStateOf {
            getAnimatedRect(
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
        }
    }

    val folderGridHorizontalPagerState = rememberPagerState(
        pageCount = {
            previewFolderEblanApplicationInfo.folderEblanApplicationInfoGridItemsByPage.size
        },
    )

    var pageDirection by remember { mutableStateOf<PageDirection?>(null) }

    val isFirstFolderEblanApplicationInfo = folderEblanApplicationInfoEntries.size == 1 &&
        folderEblanApplicationInfoEntries.singleOrNull()?.id == folderEntry.id

    val isLastFolderEblanApplicationInfo =
        folderEblanApplicationInfoEntries.lastOrNull()?.id == folderEntry.id

    val currentDrag = rememberUpdatedState(drag)
    val currentIsDragging = rememberUpdatedState(isDragging)
    val currentIsVisibleOverlay = rememberUpdatedState(isVisibleOverlay)
    val currentMoveFolderEblanApplicationInfoGridItemResult =
        rememberUpdatedState(moveFolderEblanApplicationInfoGridItemResult)
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
        isFirstFolderEblanApplicationInfo,
        animations,
        isLastFolderEblanApplicationInfo,
        appDrawerSettings.gridItemSettings,
    ) {
        handleIsCloseFolder(
            // Folder Information
            folderEntry = folderEntry,
            isFirstFolderEblanApplicationInfo = isFirstFolderEblanApplicationInfo,
            isLastFolderEblanApplicationInfo = isLastFolderEblanApplicationInfo,
            // Drag and UI State
            drag = currentDrag,
            isDragging = currentIsDragging,
            isVisibleOverlay = currentIsVisibleOverlay,
            moveFolderEblanApplicationInfoGridItemResult = currentMoveFolderEblanApplicationInfoGridItemResult,
            // Animation and Settings
            animations = animations,
            progress = progress,
            gridItemSettings = appDrawerSettings.gridItemSettings,
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
            paddingValues = paddingValues,
            // Folder Item State
            currentPage = folderGridHorizontalPagerState.currentPage,
            folderEntry = folderEntry,
            folderEblanApplicationInfoPopup = previewFolderEblanApplicationInfo,
            folderCellWidth = folderCellWidth,
            folderCellHeight = folderCellHeight,
            isLastFolderEblanApplicationInfoGridItem = isLastFolderEblanApplicationInfo,
            moveFolderEblanApplicationInfoGridItemResult = currentMoveFolderEblanApplicationInfoGridItemResult.value,
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
            onUpsertFolderEblanApplicationInfoPopupEntry = onUpsertFolderEblanApplicationInfoPopupEntry,
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

    LaunchedEffect(key1 = folderGridHorizontalPagerState.isScrollInProgress) {
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
            layoutDirection = layoutDirection,
            screenWidth = screenWidth,
            columns = previewFolderEblanApplicationInfo.columns,
            paddingValues = paddingValues,
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
            onUpdateFolderPageDirection = {
                pageDirection = it
            },
        )
    }

    BackHandler(
        enabled = !folderEntry.isCloseFolder &&
            isLastFolderEblanApplicationInfo &&
            !isInProgress,
    ) {
        onUpsertFolderEblanApplicationInfoPopupEntry(folderEntry.copy(isCloseFolder = true))
    }

    HomeHandler(
        enabled = !folderEntry.isCloseFolder &&
            isLastFolderEblanApplicationInfo &&
            !isInProgress,
    ) {
        onUpsertFolderEblanApplicationInfoPopupEntry(folderEntry.copy(isCloseFolder = true))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(key1 = isLastFolderEblanApplicationInfo) {
                if (isLastFolderEblanApplicationInfo) {
                    detectTapGestures(
                        onPress = {
                            awaitRelease()

                            onUpsertFolderEblanApplicationInfoPopupEntry(folderEntry.copy(isCloseFolder = true))
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
                        slotId = { it.id },
                        content = {
                            InteractiveFolderEblanApplicationInfoItem(
                                // Scope and Animation
                                sharedTransitionScope = sharedTransitionScope,
                                animations = animations,
                                // Folder Item and Content
                                folderEblanApplicationInfoGridItem = it,
                                folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                                folderEblanApplicationInfoEntries = folderEblanApplicationInfoEntries,
                                // Styling and Colors
                                folderBackgroundColor = folderBackgroundColor,
                                customFolderBackgroundColor = customFolderBackgroundColor,
                                systemTextColor = systemTextColor,
                                systemCustomTextColor = systemCustomTextColor,
                                folderCornerRadius = folderCornerRadius,
                                // Layout and Settings
                                appDrawerSettings = appDrawerSettings,
                                paddingValues = paddingValues,
                                iconPackFilePaths = iconPackFilePaths,
                                // State and Progress
                                isVisibleOverlay = isVisibleOverlay,
                                isScrollInProgress = folderGridHorizontalPagerState.isScrollInProgress,
                                isInProgress = isInProgress,
                                progress = progress.value,
                                moveFolderEblanApplicationInfoGridItemResult = moveFolderEblanApplicationInfoGridItemResult,
                                drag = drag,
                                showFolderEblanApplicationInfoGridItemMenu = showFolderEblanApplicationInfoGridItemMenu,
                                // Callbacks
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
                    gridItemsByPage = previewFolderEblanApplicationInfo.folderEblanApplicationInfoGridItemsByPage,
                    progress = progress.value,
                    // State
                    folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                    // Appearance
                    folderBackgroundColor = folderBackgroundColor,
                    customFolderBackgroundColor = customFolderBackgroundColor,
                    textColor = appDrawerSettings.gridItemSettings.textColor,
                    customTextColor = appDrawerSettings.gridItemSettings.customTextColor,
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
    isFirstFolderEblanApplicationInfo: Boolean,
    isLastFolderEblanApplicationInfo: Boolean,
    // Drag and UI State
    drag: State<Drag>,
    isDragging: State<Boolean>,
    isVisibleOverlay: State<Boolean>,
    moveFolderEblanApplicationInfoGridItemResult: State<MoveFolderEblanApplicationInfoGridItemResult?>,
    // Animation and Settings
    animations: Boolean,
    progress: Animatable<Float, AnimationVector1D>,
    gridItemSettings: GridItemSettings,
    // Callbacks
    onAnimateToScrollToPage: suspend (Int) -> Unit,
    onMoveFolderEblanApplicationInfoGridItemOutsideFolder: (
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        movingGridItem: GridItem,
    ) -> Unit,
    onCloseFolder: (
        folderEntry: FolderEntry,
        isFirstFolderEblanApplicationInfo: Boolean,
    ) -> Unit,
) {
    if (!folderEntry.isCloseFolder || !isLastFolderEblanApplicationInfo) return

    onAnimateToScrollToPage(0)

    if (animations) {
        progress.animateTo(targetValue = 0f)
    } else {
        progress.snapTo(targetValue = 0f)
    }

    handleMoveFolderGridItemOutsideFolder(
        // Settings
        gridItemSettings = gridItemSettings,
        // State
        drag = drag,
        isDragging = isDragging,
        isVisibleOverlay = isVisibleOverlay,
        moveFolderEblanApplicationInfoGridItemResult = moveFolderEblanApplicationInfoGridItemResult,
        // Callbacks
        onMoveFolderEblanApplicationInfoGridItemOutsideFolder = onMoveFolderEblanApplicationInfoGridItemOutsideFolder,
    )

    onCloseFolder(
        folderEntry,
        isFirstFolderEblanApplicationInfo,
    )
}

@OptIn(ExperimentalUuidApi::class)
private fun handleMoveFolderGridItemOutsideFolder(
    // Settings
    gridItemSettings: GridItemSettings,
    // State
    drag: State<Drag>,
    isDragging: State<Boolean>,
    isVisibleOverlay: State<Boolean>,
    moveFolderEblanApplicationInfoGridItemResult: State<MoveFolderEblanApplicationInfoGridItemResult?>,
    // Callbacks
    onMoveFolderEblanApplicationInfoGridItemOutsideFolder: (
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        movingGridItem: GridItem,
    ) -> Unit,
) {
    val folderEblanApplicationInfoGridItem =
        moveFolderEblanApplicationInfoGridItemResult.value?.folderEblanApplicationInfoGridItem
            ?: return

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
        folderEblanApplicationInfoGridItem,
        gridItem,
    )
}
