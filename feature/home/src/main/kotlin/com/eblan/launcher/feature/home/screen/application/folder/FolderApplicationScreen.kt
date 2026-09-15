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
import com.eblan.launcher.domain.model.folder.FolderPopupEntry
import com.eblan.launcher.domain.model.folder.PreviewFolderEblanApplicationInfo
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
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    folderEblanApplicationInfoPopup: FolderEblanApplicationInfoPopup,
    paddingValues: PaddingValues,
    safeDrawingHeight: Int,
    safeDrawingWidth: Int,
    isVisibleOverlay: Boolean,
    screenWidth: Int,
    screenHeight: Int,
    folderEblanApplicationInfoPopups: List<FolderEblanApplicationInfoPopup>,
    animations: Boolean,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    previewFolderEblanApplicationInfos: Map<String, PreviewFolderEblanApplicationInfo>,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    appDrawerSettings: AppDrawerSettings,
    folderCornerRadius: Int,
    folderCellWidth: Int,
    folderCellHeight: Int,
    moveFolderEblanApplicationInfoGridItemResult: MoveFolderEblanApplicationInfoGridItemResult?,
    drag: Drag,
    isDragging: Boolean,
    lockMovement: Boolean,
    dragIntOffset: IntOffset,
    showFolderEblanApplicationGridItemPopup: Boolean,
    onDeleteFolderEblanApplicationInfoPopupEntry: (FolderPopupEntry) -> Unit,
    onUpsertFolderEblanApplicationInfoPopupEntry: (FolderPopupEntry) -> Unit,
    onUpdateIsVisibleFolders: (Boolean) -> Unit,
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
    onUpdateIsCloseFolderEblanApplicationInfoGridItemPopup: (Boolean) -> Unit,
    onMoveFolderEblanApplicationInfoGridItemOutsideFolder: (
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        movingGridItem: GridItem,
    ) -> Unit,
    onDismissApplicationScreen: () -> Unit,
) {
    val folderPopupIntOffset = IntOffset(
        x = folderEblanApplicationInfoPopup.folderPopupEntry.x,
        y = folderEblanApplicationInfoPopup.folderPopupEntry.y,
    )

    val folderPopupIntSize = IntSize(
        width = folderEblanApplicationInfoPopup.folderPopupEntry.width,
        height = folderEblanApplicationInfoPopup.folderPopupEntry.height,
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
        columns = folderEblanApplicationInfoPopup.columns,
        rows = folderEblanApplicationInfoPopup.rows,
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
            folderEblanApplicationInfoPopup.folderEblanApplicationInfosByPage.size
        },
    )

    var pageDirection by remember { mutableStateOf<PageDirection?>(null) }

    val isFirstFolderEblanApplicationInfo = folderEblanApplicationInfoPopups.size == 1 &&
        folderEblanApplicationInfoPopups.singleOrNull()?.folderEblanApplicationInfo == folderEblanApplicationInfoPopup.folderEblanApplicationInfo

    val isLastFolderEblanApplicationInfo =
        folderEblanApplicationInfoPopups.lastOrNull()?.folderEblanApplicationInfo == folderEblanApplicationInfoPopup.folderEblanApplicationInfo

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
        folderEblanApplicationInfoPopup,
        isFirstFolderEblanApplicationInfo,
        animations,
        isLastFolderEblanApplicationInfo,
        appDrawerSettings.gridItemSettings,
    ) {
        handleIsCloseFolder(
            animations = animations,
            drag = currentDrag,
            folderEblanApplicationInfoPopup = folderEblanApplicationInfoPopup,
            gridItemSettings = appDrawerSettings.gridItemSettings,
            isDragging = currentIsDragging,
            isFirstFolderGridItem = isFirstFolderEblanApplicationInfo,
            isLastFolderEblanApplicationInfo = isLastFolderEblanApplicationInfo,
            isVisibleOverlay = currentIsVisibleOverlay,
            moveFolderEblanApplicationInfoGridItemResult = currentMoveFolderEblanApplicationInfoGridItemResult,
            progress = progress,
            onAnimateToScrollToPage = folderGridHorizontalPagerState::animateScrollToPage,
            onDeleteFolderPopupEntry = onDeleteFolderEblanApplicationInfoPopupEntry,
            onMoveFolderEblanApplicationInfoGridItemOutsideFolder = onMoveFolderEblanApplicationInfoGridItemOutsideFolder,
            onUpdateIsVisibleFolders = onUpdateIsVisibleFolders,
            onDismissApplicationScreen = onDismissApplicationScreen,
            onUpdateIsDragging = onUpdateIsDragging,
        )
    }

    LaunchedEffect(
        drag,
        dragIntOffset,
        folderEblanApplicationInfoPopup,
        moveFolderEblanApplicationInfoGridItemResult,
        isLastFolderEblanApplicationInfo,
        isInProgress,
    ) {
        handleDragFolderEblanApplicationInfoGridItem(
            density = density,
            drag = currentDrag.value,
            dragIntOffset = dragIntOffset,
            currentPage = folderGridHorizontalPagerState.currentPage,
            folderEblanApplicationInfoPopup = folderEblanApplicationInfoPopup,
            folderPopupIntOffset = folderPopupIntOffset,
            isDragging = currentIsDragging.value,
            isVisibleOverlay = currentIsVisibleOverlay.value,
            isScrollInProgress = folderGridHorizontalPagerState.isScrollInProgress,
            lockMovement = currentLockMovement.value,
            paddingValues = paddingValues,
            screenHeight = screenHeight,
            screenWidth = screenWidth,
            moveFolderEblanApplicationInfoGridItemResult = currentMoveFolderEblanApplicationInfoGridItemResult.value,
            layoutDirection = layoutDirection,
            folderCellWidth = folderCellWidth,
            folderCellHeight = folderCellHeight,
            isLastFolderEblanApplicationInfoGridItem = isLastFolderEblanApplicationInfo,
            isInProgress = isInProgress,
            onMoveFolderEblanApplicationInfoGridItem = onMoveFolderEblanApplicationInfoGridItem,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
            onUpsertFolderEblanApplicationInfoPopupEntry = onUpsertFolderEblanApplicationInfoPopupEntry,
        )
    }

    LaunchedEffect(
        key1 = drag,
        key2 = isLastFolderEblanApplicationInfo,
    ) {
        handleDropFolderGridItem(
            drag = drag,
            isDragging = isDragging,
            lockMovement = lockMovement,
            isVisibleOverlay = isVisibleOverlay,
            isLastFolderGridItem = isLastFolderEblanApplicationInfo,
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
            pageDirection = pageDirection,
            currentPage = folderGridHorizontalPagerState.currentPage,
            isInProgress = isInProgress,
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
        folderEblanApplicationInfoPopup,
        isLastFolderEblanApplicationInfo,
        isInProgress,
    ) {
        handleAnimateScrollToPage(
            density = density,
            drag = drag,
            isVisibleOverlay = currentIsVisibleOverlay.value,
            lockMovement = currentLockMovement.value,
            hasMoveGridItemResult = moveFolderEblanApplicationInfoGridItemResult != null,
            dragIntOffset = dragIntOffset,
            columns = folderEblanApplicationInfoPopup.columns,
            folderPopupIntOffset = folderPopupIntOffset,
            isDragging = currentIsDragging.value,
            paddingValues = paddingValues,
            screenWidth = screenWidth,
            layoutDirection = layoutDirection,
            folderCellWidth = folderCellWidth,
            isLastFolderGridItem = isLastFolderEblanApplicationInfo,
            isInProgress = isInProgress,
            onUpdateFolderPageDirection = {
                pageDirection = it
            },
        )
    }

    BackHandler(
        enabled = !folderEblanApplicationInfoPopup.folderPopupEntry.isCloseFolder &&
            isLastFolderEblanApplicationInfo &&
            !isInProgress,
    ) {
        onUpsertFolderEblanApplicationInfoPopupEntry(
            folderEblanApplicationInfoPopup.folderPopupEntry.copy(
                isCloseFolder = true,
            ),
        )
    }

    HomeHandler(
        enabled = !folderEblanApplicationInfoPopup.folderPopupEntry.isCloseFolder &&
            isLastFolderEblanApplicationInfo &&
            !isInProgress,
    ) {
        onUpsertFolderEblanApplicationInfoPopupEntry(
            folderEblanApplicationInfoPopup.folderPopupEntry.copy(
                isCloseFolder = true,
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(key1 = isLastFolderEblanApplicationInfo) {
                if (isLastFolderEblanApplicationInfo) {
                    detectTapGestures(
                        onPress = {
                            awaitRelease()

                            onUpsertFolderEblanApplicationInfoPopupEntry(
                                folderEblanApplicationInfoPopup.folderPopupEntry.copy(
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
                        modifier = Modifier.fillMaxSize(),
                        columns = folderEblanApplicationInfoPopup.columns,
                        gridItems = folderEblanApplicationInfoPopup.folderEblanApplicationInfosByPage[index],
                        rows = folderEblanApplicationInfoPopup.rows,
                        width = animatedPreviewRect.width().roundToInt(),
                        height = animatedPreviewRect.height().roundToInt(),
                        animate = isVisibleOverlay && !isInProgress && animations,
                        slotId = { it.id },
                        content = {
                            InteractiveFolderEblanApplicationInfoItem(
                                folderEblanApplicationInfoGridItem = it,
                                sharedTransitionScope = sharedTransitionScope,
                                folderBackgroundColor = folderBackgroundColor,
                                customFolderBackgroundColor = customFolderBackgroundColor,
                                systemTextColor = systemTextColor,
                                systemCustomTextColor = systemCustomTextColor,
                                previewFolderEblanApplicationInfos = previewFolderEblanApplicationInfos,
                                appDrawerSettings = appDrawerSettings,
                                folderCornerRadius = folderCornerRadius,
                                isVisibleOverlay = isVisibleOverlay,
                                isScrollInProgress = folderGridHorizontalPagerState.isScrollInProgress,
                                isInProgress = isInProgress,
                                paddingValues = paddingValues,
                                animations = animations,
                                moveFolderEblanApplicationInfoGridItemResult = moveFolderEblanApplicationInfoGridItemResult,
                                progress = progress.value,
                                folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                                drag = drag,
                                showFolderEblanApplicationGridItemPopup = showFolderEblanApplicationGridItemPopup,
                                onUpdateIsVisibleFolders = onUpdateIsVisibleFolders,
                                onUpsertFolderEblanApplicationInfoPopupEntry = onUpsertFolderEblanApplicationInfoPopupEntry,
                                onUpdateImageBitmap = onUpdateImageBitmap,
                                onUpdateIsDragging = onUpdateIsDragging,
                                onUpdateOverlayBounds = onUpdateOverlayBounds,
                                onUpdateSharedElementKey = onUpdateSharedElementKey,
                                onShowGridItemPopup = onShowGridItemPopup,
                                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                onUpdateMoveFolderEblanApplicationInfoGridItemResult = onUpdateMoveFolderEblanApplicationInfoGridItemResult,
                                onUpdateIsCloseFolderEblanApplicationInfoGridItemPopup = onUpdateIsCloseFolderEblanApplicationInfoGridItemPopup,
                            )
                        },
                    )
                }

                FolderTitle(
                    label = folderEblanApplicationInfoPopup.label,
                    gridItemsByPage = folderEblanApplicationInfoPopup.folderEblanApplicationInfosByPage,
                    folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                    progress = progress.value,
                    folderBackgroundColor = folderBackgroundColor,
                    customFolderBackgroundColor = customFolderBackgroundColor,
                    textColor = appDrawerSettings.gridItemSettings.textColor,
                    customTextColor = appDrawerSettings.gridItemSettings.customTextColor,
                    systemCustomTextColor = systemCustomTextColor,
                    systemTextColor = systemTextColor,
                )
            }
        }
    }
}

private suspend fun handleIsCloseFolder(
    animations: Boolean,
    drag: State<Drag>,
    folderEblanApplicationInfoPopup: FolderEblanApplicationInfoPopup,
    gridItemSettings: GridItemSettings,
    isDragging: State<Boolean>,
    isFirstFolderGridItem: Boolean,
    isLastFolderEblanApplicationInfo: Boolean,
    isVisibleOverlay: State<Boolean>,
    moveFolderEblanApplicationInfoGridItemResult: State<MoveFolderEblanApplicationInfoGridItemResult?>,
    progress: Animatable<Float, AnimationVector1D>,
    onAnimateToScrollToPage: suspend (Int) -> Unit,
    onDeleteFolderPopupEntry: (FolderPopupEntry) -> Unit,
    onMoveFolderEblanApplicationInfoGridItemOutsideFolder: (
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        movingGridItem: GridItem,
    ) -> Unit,
    onUpdateIsVisibleFolders: (Boolean) -> Unit,
    onDismissApplicationScreen: () -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
) {
    if (!folderEblanApplicationInfoPopup.folderPopupEntry.isCloseFolder || !isLastFolderEblanApplicationInfo) return

    onAnimateToScrollToPage(0)

    if (animations) {
        progress.animateTo(targetValue = 0f)
    } else {
        progress.snapTo(targetValue = 0f)
    }

    handleMoveFolderGridItemOutsideFolder(
        drag = drag,
        gridItemSettings = gridItemSettings,
        isDragging = isDragging,
        isVisibleOverlay = isVisibleOverlay,
        moveFolderEblanApplicationInfoGridItemResult = moveFolderEblanApplicationInfoGridItemResult,
        onMoveFolderEblanApplicationInfoGridItemOutsideFolder = onMoveFolderEblanApplicationInfoGridItemOutsideFolder,
        onDismissApplicationScreen = onDismissApplicationScreen,
        onUpdateIsDragging = onUpdateIsDragging,
    )

    if (isFirstFolderGridItem) {
        onUpdateIsVisibleFolders(false)
    }

    onDeleteFolderPopupEntry(folderEblanApplicationInfoPopup.folderPopupEntry)
}

@OptIn(ExperimentalUuidApi::class)
private fun handleMoveFolderGridItemOutsideFolder(
    drag: State<Drag>,
    gridItemSettings: GridItemSettings,
    isDragging: State<Boolean>,
    isVisibleOverlay: State<Boolean>,
    moveFolderEblanApplicationInfoGridItemResult: State<MoveFolderEblanApplicationInfoGridItemResult?>,
    onMoveFolderEblanApplicationInfoGridItemOutsideFolder: (
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        movingGridItem: GridItem,
    ) -> Unit,
    onDismissApplicationScreen: () -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
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

    onDismissApplicationScreen()

    onMoveFolderEblanApplicationInfoGridItemOutsideFolder(
        folderEblanApplicationInfoGridItem,
        gridItem,
    )

    onUpdateIsDragging(true)
}
