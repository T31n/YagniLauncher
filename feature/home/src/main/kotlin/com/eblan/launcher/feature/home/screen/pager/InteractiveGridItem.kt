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

import android.content.Intent.parseUri
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import coil3.request.ImageRequest.Builder
import coil3.request.addLastModifiedToFileCacheKey
import coil3.size.Size
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.BackgroundColor
import com.eblan.launcher.domain.model.FolderPopup
import com.eblan.launcher.domain.model.FolderPopupEntry
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.PreviewFolder
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.PreviewFolderGridLayout
import com.eblan.launcher.feature.home.component.gridItemScaleAnimation
import com.eblan.launcher.feature.home.component.gridItemSharedElement
import com.eblan.launcher.feature.home.component.swipeGestures
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.SCALE
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.feature.home.util.handleOnPress
import com.eblan.launcher.feature.home.util.onDoubleTap
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.settings.rememberIsNotificationAccessGranted
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun InteractiveGridItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    drag: Drag,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    isScrollInProgress: Boolean,
    statusBarNotifications: Map<String, Int>,
    textColor: TextColor,
    isVisibleOverlay: Boolean,
    isVisibleFolders: Boolean,
    moveGridItemResult: MoveGridItemResult?,
    lockMovement: Boolean,
    isDragging: Boolean,
    showGridItemPopup: Boolean,
    previewFolderGridItems: Map<String, PreviewFolder>,
    cellWidth: Int,
    cellHeight: Int,
    leftPadding: Int,
    topOffset: Int,
    sharedElementKey: SharedElementKey,
    iconPackInfoFilePaths: Map<String, String?>,
    animations: Boolean,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemCustomTextColor: Int,
    folderPopups: List<FolderPopup>,
    onOpenAppDrawer: () -> Unit,
    onUpsertFolderPopupEntry: (FolderPopupEntry) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
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
    onUpdateIsCloseGridItemPopup: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
    onShowFolderWhenDragging: (
        folderPopupEntry: FolderPopupEntry,
        movingGridItem: GridItem,
    ) -> Unit,
    onResetGrid: () -> Unit,
    onUpdateIsVisibleFolders: (Boolean) -> Unit,
) {
    val isSelected =
        moveGridItemResult != null && moveGridItemResult.movingGridItem.id == gridItem.id

    val currentGridItemSettings = if (gridItem.override) {
        gridItem.gridItemSettings
    } else {
        gridItemSettings
    }

    val currentTextColor = getGridItemTextColor(
        gridItemCustomTextColor = currentGridItemSettings.customTextColor,
        gridItemTextColor = currentGridItemSettings.textColor,
        systemCustomTextColor = gridItemSettings.customTextColor,
        systemTextColor = textColor,
    )

    val hasInteraction = isSelected && isVisibleOverlay

    val isVisibleWhiteBox = hasInteraction && drag == Drag.Dragging

    val sourceBounds = getSourceBounds(
        gridItem = gridItem,
        cellWidth = cellWidth,
        cellHeight = cellHeight,
        leftPadding = leftPadding,
        topOffset = topOffset,
    )

    val isVisibleFolder = remember(
        key1 = gridItem,
        key2 = folderPopups,
        key3 = isVisibleFolders,
    ) {
        isVisibleFolders && folderPopups.any { it.folderPopupEntry.id == gridItem.id }
    }

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = currentGridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = currentGridItemSettings.verticalArrangement)

    val maxLines = if (currentGridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    LaunchedEffect(
        key1 = drag,
        key2 = hasInteraction,
        key3 = showGridItemPopup,
    ) {
        if (drag == Drag.Dragging &&
            hasInteraction &&
            showGridItemPopup
        ) {
            onUpdateIsDragging(true)

            onUpdateIsCloseGridItemPopup(true)
        }
    }

    when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo -> {
            InteractiveApplicationInfoGridItem(
                modifier = modifier,
                sharedTransitionScope = sharedTransitionScope,
                data = data,
                gridItem = gridItem,
                gridItemSettings = currentGridItemSettings,
                isScrollInProgress = isScrollInProgress,
                isVisibleFolders = isVisibleFolders,
                isVisibleOverlay = isVisibleOverlay,
                sharedElementKey = sharedElementKey,
                statusBarNotifications = statusBarNotifications,
                textColor = currentTextColor,
                hasInteraction = hasInteraction,
                isVisibleWhiteBox = isVisibleWhiteBox,
                sourceBounds = sourceBounds,
                iconPackInfoFilePaths = iconPackInfoFilePaths,
                animations = animations,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
                onOpenAppDrawer = onOpenAppDrawer,
                onShowGridItemPopup = onShowGridItemPopup,
                onUpdateGridItemSource = onUpdateGridItemSource,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateOverlayBounds = onUpdateOverlayBounds,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
            )
        }

        is GridItemData.Widget -> {
            InteractiveWidgetGridItem(
                modifier = modifier,
                sharedTransitionScope = sharedTransitionScope,
                data = data,
                isScrollInProgress = isScrollInProgress,
                isVisibleOverlay = isVisibleOverlay,
                sharedElementKey = sharedElementKey,
                textColor = currentTextColor,
                gridItem = gridItem,
                hasInteraction = hasInteraction,
                isVisibleWhiteBox = isVisibleWhiteBox,
                animations = animations,
                onShowGridItemPopup = onShowGridItemPopup,
                onUpdateGridItemSource = onUpdateGridItemSource,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateOverlayBounds = onUpdateOverlayBounds,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
            )
        }

        is GridItemData.ShortcutInfo -> {
            InteractiveShortcutInfoGridItem(
                modifier = modifier,
                sharedTransitionScope = sharedTransitionScope,
                data = data,
                gridItem = gridItem,
                gridItemSettings = currentGridItemSettings,
                hasShortcutHostPermission = hasShortcutHostPermission,
                isScrollInProgress = isScrollInProgress,
                isVisibleFolders = isVisibleFolders,
                isVisibleOverlay = isVisibleOverlay,
                sharedElementKey = sharedElementKey,
                textColor = currentTextColor,
                hasInteraction = hasInteraction,
                isVisibleWhiteBox = isVisibleWhiteBox,
                sourceBounds = sourceBounds,
                animations = animations,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
                onOpenAppDrawer = onOpenAppDrawer,
                onShowGridItemPopup = onShowGridItemPopup,
                onUpdateGridItemSource = onUpdateGridItemSource,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateOverlayBounds = onUpdateOverlayBounds,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
            )
        }

        is GridItemData.Folder -> {
            InteractiveFolderGridItem(
                modifier = modifier,
                sharedTransitionScope = sharedTransitionScope,
                data = data,
                drag = drag,
                gridItem = gridItem,
                gridItemSettings = currentGridItemSettings,
                isScrollInProgress = isScrollInProgress,
                isVisibleFolder = isVisibleFolder,
                isVisibleOverlay = isVisibleOverlay,
                sharedElementKey = sharedElementKey,
                textColor = currentTextColor,
                moveGridItemResult = moveGridItemResult,
                lockMovement = lockMovement,
                isDragging = isDragging,
                hasInteraction = hasInteraction,
                isVisibleWhiteBox = isVisibleWhiteBox,
                previewFolderGridItems = previewFolderGridItems,
                hasShortcutHostPermission = hasShortcutHostPermission,
                iconPackInfoFilePaths = iconPackInfoFilePaths,
                animations = animations,
                folderCornerRadius = folderCornerRadius,
                folderBackgroundColor = folderBackgroundColor,
                customFolderBackgroundColor = customFolderBackgroundColor,
                systemTextColor = textColor,
                systemCustomTextColor = systemCustomTextColor,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
                onOpenAppDrawer = onOpenAppDrawer,
                onShowGridItemPopup = onShowGridItemPopup,
                onUpsertFolderPopupEntry = onUpsertFolderPopupEntry,
                onUpdateGridItemSource = onUpdateGridItemSource,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateOverlayBounds = onUpdateOverlayBounds,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                onShowFolderWhenDragging = onShowFolderWhenDragging,
                onResetGrid = onResetGrid,
                onUpdateIsVisibleFolders = onUpdateIsVisibleFolders,
            )
        }

        is GridItemData.ShortcutConfig -> {
            InteractiveShortcutConfigGridItem(
                modifier = modifier,
                sharedTransitionScope = sharedTransitionScope,
                data = data,
                gridItem = gridItem,
                gridItemSettings = currentGridItemSettings,
                isScrollInProgress = isScrollInProgress,
                isVisibleFolders = isVisibleFolders,
                isVisibleOverlay = isVisibleOverlay,
                sharedElementKey = sharedElementKey,
                textColor = currentTextColor,
                hasInteraction = hasInteraction,
                isVisibleWhiteBox = isVisibleWhiteBox,
                animations = animations,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
                onOpenAppDrawer = onOpenAppDrawer,
                onShowGridItemPopup = onShowGridItemPopup,
                onUpdateGridItemSource = onUpdateGridItemSource,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateOverlayBounds = onUpdateOverlayBounds,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun InteractiveApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    data: GridItemData.ApplicationInfo,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    isScrollInProgress: Boolean,
    isVisibleFolders: Boolean,
    isVisibleOverlay: Boolean,
    sharedElementKey: SharedElementKey,
    statusBarNotifications: Map<String, Int>,
    textColor: Color,
    hasInteraction: Boolean,
    isVisibleWhiteBox: Boolean,
    sourceBounds: Rect,
    iconPackInfoFilePaths: Map<String, String?>,
    animations: Boolean,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
    onOpenAppDrawer: () -> Unit,
    onShowGridItemPopup: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
) {
    val androidLauncherAppsWrapper = LocalLauncherApps.current

    val launcherApps = LocalLauncherApps.current

    val context = LocalContext.current

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val icon = iconPackInfoFilePaths[data.componentName] ?: data.icon

    val hasNotifications =
        (statusBarNotifications[data.packageName] ?: 0) > 0

    val alpha = if (hasInteraction) 0f else 1f

    val isNotificationAccessGranted by rememberIsNotificationAccessGranted()

    val scale = remember { Animatable(1f) }

    val currentOnOpenAppDrawer by rememberUpdatedState(onOpenAppDrawer)
    val currentOnShowGridItemPopup by rememberUpdatedState(onShowGridItemPopup)
    val currentOnUpdateImageBitmap by rememberUpdatedState(onUpdateImageBitmap)
    val currentOnUpdateOverlayBounds by rememberUpdatedState(onUpdateOverlayBounds)
    val currentOnUpdateSharedElementKey by rememberUpdatedState(onUpdateSharedElementKey)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(gridItemSettings.padding.dp)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            )
            .whiteBox(
                textColor = textColor,
                visible = isVisibleWhiteBox && !isVisibleFolders,
            )
            .pointerInput(key1 = isVisibleOverlay) {
                detectTapGestures(
                    onDoubleTap = if (!isVisibleOverlay) {
                        {
                            onDoubleTap(
                                context = context,
                                doubleTap = gridItem.doubleTap,
                                launcherApps = launcherApps,
                                onOpenAppDrawer = currentOnOpenAppDrawer,
                            )
                        }
                    } else {
                        null
                    },
                    onLongPress = if (!isVisibleOverlay) {
                        {
                            scope.launch {
                                onLongPress(
                                    graphicsLayer = graphicsLayer,
                                    intOffset = intOffset,
                                    intSize = intSize,
                                    sharedElementKey = sharedElementKey,
                                    gridItem = gridItem,
                                    onUpdateGridItemSource = onUpdateGridItemSource,
                                    onUpdateImageBitmap = currentOnUpdateImageBitmap,
                                    onUpdateOverlayBounds = currentOnUpdateOverlayBounds,
                                    onUpdateSharedElementKey = currentOnUpdateSharedElementKey,
                                    onShowGridItemPopup = currentOnShowGridItemPopup,
                                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                    onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onTap = if (!isVisibleOverlay) {
                        {
                            androidLauncherAppsWrapper.startMainActivity(
                                serialNumber = data.serialNumber,
                                componentName = data.componentName,
                                sourceBounds = sourceBounds,
                            )
                        }
                    } else {
                        null
                    },
                    onPress = {
                        handleOnPress(
                            animations = animations,
                            scale = scale,
                        )
                    },
                )
            }
            .swipeGestures(
                swipeDown = gridItem.swipeDown,
                swipeUp = gridItem.swipeUp,
                onOpenAppDrawer = onOpenAppDrawer,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(
            modifier = Modifier
                .size(gridItemSettings.iconSize.dp)
                .alpha(alpha),
        ) {
            AsyncImage(
                model = Builder(context).data(data.customIcon ?: icon)
                    .addLastModifiedToFileCacheKey(true)
                    .size(Size.ORIGINAL)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .onGloballyPositioned {
                        intOffset = it.positionInRoot().round()

                        intSize = it.size
                    }
                    .gridItemScaleAnimation(
                        isVisibleOverlay = isVisibleOverlay,
                        animations = animations,
                        scale = scale,
                    )
                    .gridItemSharedElement(
                        enabled = animations,
                        sharedElementKey = sharedElementKey,
                        sharedTransitionScope = sharedTransitionScope,
                        visible = !isScrollInProgress && !hasInteraction,
                    )
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }

                        drawLayer(graphicsLayer)
                    },
            )

            if (isNotificationAccessGranted && hasNotifications) {
                Box(
                    modifier = Modifier
                        .size(gridItemSettings.iconSize.dp * 0.3f)
                        .align(Alignment.TopEnd)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        ),
                )
            }
        }

        if (gridItemSettings.showLabel) {
            Text(
                modifier = Modifier.alpha(alpha),
                text = data.customLabel ?: data.label,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                fontSize = gridItemSettings.textSize.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun InteractiveWidgetGridItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    data: GridItemData.Widget,
    isScrollInProgress: Boolean,
    isVisibleOverlay: Boolean,
    sharedElementKey: SharedElementKey,
    textColor: Color,
    gridItem: GridItem,
    hasInteraction: Boolean,
    isVisibleWhiteBox: Boolean,
    animations: Boolean,
    onShowGridItemPopup: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = data.appWidgetId)

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val alpha = if (hasInteraction) 0f else 1f

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val scale = remember { Animatable(1f) }

    val currentOnShowGridItemPopup by rememberUpdatedState(onShowGridItemPopup)
    val currentOnUpdateImageBitmap by rememberUpdatedState(onUpdateImageBitmap)
    val currentOnUpdateOverlayBounds by rememberUpdatedState(onUpdateOverlayBounds)
    val currentOnUpdateSharedElementKey by rememberUpdatedState(onUpdateSharedElementKey)

    Box(
        modifier = modifier
            .fillMaxSize()
            .whiteBox(textColor = textColor, visible = isVisibleWhiteBox),
    ) {
        val commonModifier = Modifier
            .matchParentSize()
            .onGloballyPositioned {
                intOffset = it.positionInRoot().round()

                intSize = it.size
            }
            .gridItemScaleAnimation(
                isVisibleOverlay = isVisibleOverlay,
                animations = animations,
                scale = scale,
            )
            .gridItemSharedElement(
                enabled = animations,
                sharedElementKey = sharedElementKey,
                sharedTransitionScope = sharedTransitionScope,
                visible = !isScrollInProgress && !hasInteraction,
            )
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .alpha(alpha)

        if (appWidgetInfo != null) {
            AndroidView(
                factory = {
                    appWidgetHost.createView(
                        appWidgetId = data.appWidgetId,
                        appWidgetProviderInfo = appWidgetInfo,
                    )
                },
                modifier = commonModifier,
                update = {
                    if (!isVisibleOverlay) {
                        it.setOnLongClickListener {
                            scope.launch {
                                if (animations) {
                                    scale.animateTo(targetValue = SCALE)
                                }

                                onLongPress(
                                    graphicsLayer = graphicsLayer,
                                    intOffset = intOffset,
                                    intSize = intSize,
                                    sharedElementKey = sharedElementKey,
                                    gridItem = gridItem,
                                    onUpdateGridItemSource = onUpdateGridItemSource,
                                    onUpdateImageBitmap = currentOnUpdateImageBitmap,
                                    onUpdateOverlayBounds = currentOnUpdateOverlayBounds,
                                    onUpdateSharedElementKey = currentOnUpdateSharedElementKey,
                                    onShowGridItemPopup = currentOnShowGridItemPopup,
                                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                    onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                                )
                            }

                            true
                        }
                    }
                },
            )
        } else {
            AsyncImage(
                model = data.preview ?: data.icon,
                contentDescription = null,
                modifier = commonModifier.pointerInput(key1 = isVisibleOverlay) {
                    detectTapGestures(
                        onLongPress = if (!isVisibleOverlay) {
                            {
                                scope.launch {
                                    onLongPress(
                                        graphicsLayer = graphicsLayer,
                                        intOffset = intOffset,
                                        intSize = intSize,
                                        sharedElementKey = sharedElementKey,
                                        gridItem = gridItem,
                                        onUpdateGridItemSource = onUpdateGridItemSource,
                                        onUpdateImageBitmap = currentOnUpdateImageBitmap,
                                        onUpdateOverlayBounds = currentOnUpdateOverlayBounds,
                                        onUpdateSharedElementKey = currentOnUpdateSharedElementKey,
                                        onShowGridItemPopup = currentOnShowGridItemPopup,
                                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                        onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                                    )
                                }
                            }
                        } else {
                            null
                        },
                        onPress = {
                            handleOnPress(
                                animations = animations,
                                scale = scale,
                            )
                        },
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun InteractiveShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    data: GridItemData.ShortcutInfo,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    isScrollInProgress: Boolean,
    isVisibleFolders: Boolean,
    isVisibleOverlay: Boolean,
    sharedElementKey: SharedElementKey,
    textColor: Color,
    hasInteraction: Boolean,
    isVisibleWhiteBox: Boolean,
    sourceBounds: Rect,
    animations: Boolean,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
    onOpenAppDrawer: () -> Unit,
    onShowGridItemPopup: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
) {
    val androidLauncherAppsWrapper = LocalLauncherApps.current

    val launcherApps = LocalLauncherApps.current

    val context = LocalContext.current

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val customIcon = data.customIcon ?: data.icon

    val customShortLabel = data.customShortLabel ?: data.shortLabel

    val defaultAlpha = if (hasShortcutHostPermission && data.isEnabled) 1f else 0.3f

    val alpha = if (hasInteraction) 0f else defaultAlpha

    val scale = remember { Animatable(1f) }

    val currentOnOpenAppDrawer by rememberUpdatedState(onOpenAppDrawer)
    val currentOnShowGridItemPopup by rememberUpdatedState(onShowGridItemPopup)
    val currentOnUpdateImageBitmap by rememberUpdatedState(onUpdateImageBitmap)
    val currentOnUpdateOverlayBounds by rememberUpdatedState(onUpdateOverlayBounds)
    val currentOnUpdateSharedElementKey by rememberUpdatedState(onUpdateSharedElementKey)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(gridItemSettings.padding.dp)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            )
            .whiteBox(
                textColor = textColor,
                visible = isVisibleWhiteBox && !isVisibleFolders,
            )
            .pointerInput(key1 = isVisibleOverlay) {
                detectTapGestures(
                    onDoubleTap = if (!isVisibleOverlay) {
                        {
                            onDoubleTap(
                                context = context,
                                doubleTap = gridItem.doubleTap,
                                launcherApps = launcherApps,
                                onOpenAppDrawer = currentOnOpenAppDrawer,
                            )
                        }
                    } else {
                        null
                    },
                    onLongPress = if (!isVisibleOverlay) {
                        {
                            scope.launch {
                                onLongPress(
                                    graphicsLayer = graphicsLayer,
                                    intOffset = intOffset,
                                    intSize = intSize,
                                    sharedElementKey = sharedElementKey,
                                    gridItem = gridItem,
                                    onUpdateGridItemSource = onUpdateGridItemSource,
                                    onUpdateImageBitmap = currentOnUpdateImageBitmap,
                                    onUpdateOverlayBounds = currentOnUpdateOverlayBounds,
                                    onUpdateSharedElementKey = currentOnUpdateSharedElementKey,
                                    onShowGridItemPopup = currentOnShowGridItemPopup,
                                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                    onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onTap = if (!isVisibleOverlay) {
                        {
                            if (
                                hasShortcutHostPermission &&
                                data.isEnabled &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
                            ) {
                                androidLauncherAppsWrapper.startShortcut(
                                    serialNumber = data.serialNumber,
                                    packageName = data.packageName,
                                    id = data.shortcutId,
                                    sourceBounds = sourceBounds,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onPress = {
                        handleOnPress(
                            animations = animations,
                            scale = scale,
                        )
                    },
                )
            }
            .swipeGestures(
                swipeDown = gridItem.swipeDown,
                swipeUp = gridItem.swipeUp,
                onOpenAppDrawer = onOpenAppDrawer,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(
            modifier = Modifier.size(gridItemSettings.iconSize.dp),
        ) {
            AsyncImage(
                model = Builder(context).data(customIcon)
                    .addLastModifiedToFileCacheKey(true)
                    .size(Size.ORIGINAL)
                    .build(),
                modifier = Modifier
                    .matchParentSize()
                    .onGloballyPositioned {
                        intOffset = it.positionInRoot().round()

                        intSize = it.size
                    }
                    .gridItemScaleAnimation(
                        isVisibleOverlay = isVisibleOverlay,
                        animations = animations,
                        scale = scale,
                    )
                    .gridItemSharedElement(
                        enabled = animations,
                        sharedElementKey = sharedElementKey,
                        sharedTransitionScope = sharedTransitionScope,
                        visible = !isScrollInProgress && !hasInteraction,
                    )
                    .drawWithContent {
                        graphicsLayer.apply {
                            this.alpha = alpha
                        }

                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }

                        drawLayer(graphicsLayer)
                    },
                contentDescription = null,
            )

            AsyncImage(
                model = Builder(context).data(data.eblanApplicationInfoIcon)
                    .size(Size.ORIGINAL)
                    .build(),
                modifier = Modifier
                    .size((gridItemSettings.iconSize * 0.25).dp)
                    .alpha(alpha)
                    .align(Alignment.BottomEnd),
                contentDescription = null,
            )
        }

        if (gridItemSettings.showLabel) {
            Text(
                modifier = Modifier.alpha(alpha),
                text = customShortLabel,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                fontSize = gridItemSettings.textSize.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun InteractiveFolderGridItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    data: GridItemData.Folder,
    drag: Drag,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    isScrollInProgress: Boolean,
    isVisibleFolder: Boolean,
    isVisibleOverlay: Boolean,
    sharedElementKey: SharedElementKey,
    textColor: Color,
    moveGridItemResult: MoveGridItemResult?,
    lockMovement: Boolean,
    isDragging: Boolean,
    hasInteraction: Boolean,
    isVisibleWhiteBox: Boolean,
    previewFolderGridItems: Map<String, PreviewFolder>,
    hasShortcutHostPermission: Boolean,
    iconPackInfoFilePaths: Map<String, String?>,
    animations: Boolean,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
    systemCustomTextColor: Int,
    onOpenAppDrawer: () -> Unit,
    onShowGridItemPopup: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpsertFolderPopupEntry: (FolderPopupEntry) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
    onShowFolderWhenDragging: (
        folderPopupEntry: FolderPopupEntry,
        movingGridItem: GridItem,
    ) -> Unit,
    onResetGrid: () -> Unit,
    onUpdateIsVisibleFolders: (Boolean) -> Unit,
) {
    val launcherApps = LocalLauncherApps.current

    val context = LocalContext.current

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val textAlpha = if (hasInteraction) 0f else 1f
    val iconAlpha = if (hasInteraction || isVisibleFolder) 0f else 1f

    val currentDrag = rememberUpdatedState(drag)
    val currentIsDragging = rememberUpdatedState(isDragging)
    val currentIsVisibleOverlay = rememberUpdatedState(isVisibleOverlay)
    val currentGridItem = rememberUpdatedState(gridItem)
    val currentLockMovement = rememberUpdatedState(lockMovement)
    val currentFolderGridItems =
        rememberUpdatedState(previewFolderGridItems[gridItem.id]?.folderGridItems)
    val currentOnOpenAppDrawer by rememberUpdatedState(onOpenAppDrawer)
    val currentOnShowGridItemPopup by rememberUpdatedState(onShowGridItemPopup)
    val currentOnUpdateImageBitmap by rememberUpdatedState(onUpdateImageBitmap)
    val currentOnUpdateOverlayBounds by rememberUpdatedState(onUpdateOverlayBounds)
    val currentOnUpdateSharedElementKey by rememberUpdatedState(onUpdateSharedElementKey)
    val currentOnUpdateIsVisibleFolders by rememberUpdatedState(onUpdateIsVisibleFolders)

    val scale = remember { Animatable(1f) }

    LaunchedEffect(key1 = moveGridItemResult) {
        handleConflictingGridItem(
            drag = currentDrag,
            isDragging = currentIsDragging,
            isVisibleOverlay = currentIsVisibleOverlay,
            moveGridItemResult = moveGridItemResult,
            lockMovement = currentLockMovement,
            intOffset = intOffset,
            intSize = intSize,
            gridItem = currentGridItem,
            folderGridItems = currentFolderGridItems,
            onShowFolderWhenDragging = onShowFolderWhenDragging,
            onUpdateSharedElementKey = currentOnUpdateSharedElementKey,
            onUpdateIsVisibleFolders = currentOnUpdateIsVisibleFolders,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(gridItemSettings.padding.dp)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            )
            .whiteBox(
                textColor = textColor,
                visible = isVisibleWhiteBox && !isVisibleFolder,
            )
            .pointerInput(key1 = isVisibleOverlay) {
                detectTapGestures(
                    onDoubleTap = if (!isVisibleOverlay) {
                        {
                            onDoubleTap(
                                context = context,
                                doubleTap = gridItem.doubleTap,
                                launcherApps = launcherApps,
                                onOpenAppDrawer = currentOnOpenAppDrawer,
                            )
                        }
                    } else {
                        null
                    },
                    onLongPress = if (!isVisibleOverlay) {
                        {
                            scope.launch {
                                onLongPress(
                                    graphicsLayer = graphicsLayer,
                                    intOffset = intOffset,
                                    intSize = intSize,
                                    sharedElementKey = sharedElementKey,
                                    gridItem = gridItem,
                                    onUpdateGridItemSource = onUpdateGridItemSource,
                                    onUpdateImageBitmap = currentOnUpdateImageBitmap,
                                    onUpdateOverlayBounds = currentOnUpdateOverlayBounds,
                                    onUpdateSharedElementKey = currentOnUpdateSharedElementKey,
                                    onShowGridItemPopup = currentOnShowGridItemPopup,
                                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                    onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onTap = if (!isVisibleOverlay) {
                        {
                            currentOnUpdateIsVisibleFolders(true)

                            onUpsertFolderPopupEntry(
                                FolderPopupEntry(
                                    id = gridItem.id,
                                    x = intOffset.x,
                                    y = intOffset.y,
                                    width = intSize.width,
                                    height = intSize.height,
                                    isCloseFolder = false,
                                ),
                            )
                        }
                    } else {
                        null
                    },
                    onPress = {
                        handleOnPress(
                            animations = animations,
                            scale = scale,
                        )
                    },
                )
            }
            .swipeGestures(
                swipeDown = gridItem.swipeDown,
                swipeUp = gridItem.swipeUp,
                onOpenAppDrawer = onOpenAppDrawer,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        val commonModifier = Modifier
            .size(gridItemSettings.iconSize.dp)
            .onGloballyPositioned {
                intOffset = it.positionInRoot().round()

                intSize = it.size
            }
            .gridItemScaleAnimation(
                isVisibleOverlay = isVisibleOverlay,
                animations = animations,
                scale = scale,
            )
            .gridItemSharedElement(
                enabled = animations,
                sharedElementKey = sharedElementKey,
                sharedTransitionScope = sharedTransitionScope,
                visible = !isScrollInProgress && !hasInteraction,
            )
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .alpha(iconAlpha)

        if (data.icon != null) {
            AsyncImage(
                model = data.icon,
                contentDescription = null,
                modifier = commonModifier,
            )
        } else {
            Surface(
                modifier = commonModifier,
                shape = RoundedCornerShape(folderCornerRadius.dp),
                color = when (folderBackgroundColor) {
                    BackgroundColor.System -> MaterialTheme.colorScheme.surface
                    BackgroundColor.Light -> Color.White
                    BackgroundColor.Dark -> Color.Black
                    BackgroundColor.Custom -> Color(customFolderBackgroundColor)
                },
            ) {
                PreviewFolderGridLayout(
                    modifier = Modifier.fillMaxSize(),
                    gridItems = previewFolderGridItems[gridItem.id]?.previewFolderGridItems,
                    content = {
                        PreviewFolderGridItem(
                            sharedTransitionScope = sharedTransitionScope,
                            gridItem = it,
                            isScrollInProgress = isScrollInProgress,
                            isVisibleOverlay = isVisibleOverlay,
                            parent = sharedElementKey.parent,
                            moveGridItemResult = moveGridItemResult,
                            drag = drag,
                            folderGridItems = previewFolderGridItems[gridItem.id]?.folderGridItems,
                            isVisibleFolders = isVisibleFolder,
                            hasShortcutHostPermission = hasShortcutHostPermission,
                            iconPackInfoFilePaths = iconPackInfoFilePaths,
                            gridItemSettings = gridItemSettings,
                            folderBackgroundColor = folderBackgroundColor,
                            customFolderBackgroundColor = customFolderBackgroundColor,
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                            onResetGrid = onResetGrid,
                        )
                    },
                )
            }
        }

        if (gridItemSettings.showLabel) {
            Text(
                modifier = Modifier.alpha(textAlpha),
                text = data.label,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                fontSize = gridItemSettings.textSize.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun InteractiveShortcutConfigGridItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    data: GridItemData.ShortcutConfig,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    isScrollInProgress: Boolean,
    isVisibleFolders: Boolean,
    isVisibleOverlay: Boolean,
    sharedElementKey: SharedElementKey,
    textColor: Color,
    hasInteraction: Boolean,
    isVisibleWhiteBox: Boolean,
    animations: Boolean,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
    onOpenAppDrawer: () -> Unit,
    onShowGridItemPopup: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
) {
    val launcherApps = LocalLauncherApps.current

    val context = LocalContext.current

    var intOffset = remember { IntOffset.Zero }

    var intSize = remember { IntSize.Zero }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val icon = when {
        data.customIcon != null -> data.customIcon
        data.shortcutIntentIcon != null -> data.shortcutIntentIcon
        data.activityIcon != null -> data.activityIcon
        else -> data.applicationIcon
    }

    val label = when {
        data.customLabel != null -> data.customLabel
        data.shortcutIntentName != null -> data.shortcutIntentName
        data.activityLabel != null -> data.activityLabel
        else -> data.applicationLabel
    }

    val alpha = if (hasInteraction) 0f else 1f

    val scale = remember { Animatable(1f) }

    val currentOnOpenAppDrawer by rememberUpdatedState(onOpenAppDrawer)
    val currentOnShowGridItemPopup by rememberUpdatedState(onShowGridItemPopup)
    val currentOnUpdateImageBitmap by rememberUpdatedState(onUpdateImageBitmap)
    val currentOnUpdateOverlayBounds by rememberUpdatedState(onUpdateOverlayBounds)
    val currentOnUpdateSharedElementKey by rememberUpdatedState(onUpdateSharedElementKey)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(gridItemSettings.padding.dp)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            )
            .whiteBox(
                textColor = textColor,
                visible = isVisibleWhiteBox && !isVisibleFolders,
            )
            .pointerInput(key1 = isVisibleOverlay) {
                detectTapGestures(
                    onDoubleTap = if (!isVisibleOverlay) {
                        {
                            onDoubleTap(
                                context = context,
                                doubleTap = gridItem.doubleTap,
                                launcherApps = launcherApps,
                                onOpenAppDrawer = currentOnOpenAppDrawer,
                            )
                        }
                    } else {
                        null
                    },
                    onLongPress = if (!isVisibleOverlay) {
                        {
                            scope.launch {
                                onLongPress(
                                    graphicsLayer = graphicsLayer,
                                    intOffset = intOffset,
                                    intSize = intSize,
                                    sharedElementKey = sharedElementKey,
                                    gridItem = gridItem,
                                    onUpdateGridItemSource = onUpdateGridItemSource,
                                    onUpdateImageBitmap = currentOnUpdateImageBitmap,
                                    onUpdateOverlayBounds = currentOnUpdateOverlayBounds,
                                    onUpdateSharedElementKey = currentOnUpdateSharedElementKey,
                                    onShowGridItemPopup = currentOnShowGridItemPopup,
                                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                    onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onTap = if (!isVisibleOverlay) {
                        {
                            data.shortcutIntentUri?.let {
                                context.startActivity(parseUri(it, 0))
                            }
                        }
                    } else {
                        null
                    },
                    onPress = {
                        handleOnPress(
                            animations = animations,
                            scale = scale,
                        )
                    },
                )
            }
            .swipeGestures(
                swipeDown = gridItem.swipeDown,
                swipeUp = gridItem.swipeUp,
                onOpenAppDrawer = onOpenAppDrawer,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        AsyncImage(
            model = Builder(context)
                .data(icon)
                .addLastModifiedToFileCacheKey(true)
                .size(Size.ORIGINAL)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(gridItemSettings.iconSize.dp)
                .onGloballyPositioned {
                    intOffset = it.positionInRoot().round()

                    intSize = it.size
                }
                .gridItemScaleAnimation(
                    isVisibleOverlay = isVisibleOverlay,
                    animations = animations,
                    scale = scale,
                )
                .gridItemSharedElement(
                    enabled = animations,
                    sharedElementKey = sharedElementKey,
                    sharedTransitionScope = sharedTransitionScope,
                    visible = !isScrollInProgress && !hasInteraction,
                )
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }

                    drawLayer(graphicsLayer)
                }
                .alpha(alpha),
        )

        if (gridItemSettings.showLabel) {
            Text(
                modifier = Modifier.alpha(alpha),
                text = label.toString(),
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                fontSize = gridItemSettings.textSize.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PreviewFolderGridItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    gridItem: GridItem,
    isScrollInProgress: Boolean,
    isVisibleOverlay: Boolean,
    parent: SharedElementKey.Parent,
    moveGridItemResult: MoveGridItemResult?,
    drag: Drag,
    folderGridItems: List<GridItem>?,
    isVisibleFolders: Boolean,
    hasShortcutHostPermission: Boolean,
    gridItemSettings: GridItemSettings,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    iconPackInfoFilePaths: Map<String, String?>,
    onResetGrid: () -> Unit,
) {
    key(gridItem.id) {
        val context = LocalContext.current

        val currentGridItemSettings = if (gridItem.override) {
            gridItem.gridItemSettings
        } else {
            gridItemSettings
        }

        val isSelected =
            moveGridItemResult != null && moveGridItemResult.movingGridItem.id == gridItem.id

        val hasInteraction = isSelected && isVisibleOverlay

        val alpha = when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo,
            is GridItemData.Folder,
            is GridItemData.ShortcutConfig,
            is GridItemData.Widget,
            -> if (hasInteraction) 0f else 1f

            is GridItemData.ShortcutInfo -> {
                if (hasInteraction) {
                    0f
                } else if (hasShortcutHostPermission && data.isEnabled) {
                    1f
                } else {
                    0.3f
                }
            }
        }

        val folderIconTint = getTextColorFromBackgroundColor(
            backgroundColor = folderBackgroundColor,
            customBackgroundColor = customFolderBackgroundColor,
            textColor = currentGridItemSettings.textColor,
            customTextColor = currentGridItemSettings.customTextColor,
            systemTextColor = systemTextColor,
            systemCustomTextColor = systemCustomTextColor,
            defaultColor = MaterialTheme.colorScheme.onSurface,
        )

        val commonModifier = modifier
            .padding(1.dp)
            .run {
                if (!isScrollInProgress && !hasInteraction) {
                    with(sharedTransitionScope) {
                        sharedElementWithCallerManagedVisibility(
                            rememberSharedContentState(
                                key = SharedElementKey(
                                    id = gridItem.id,
                                    parent = parent,
                                ),
                            ),
                            visible = true,
                        )
                    }
                } else {
                    this
                }
            }
            .alpha(alpha)

        LaunchedEffect(
            drag,
            folderGridItems,
            moveGridItemResult?.movingGridItem?.id,
            isVisibleFolders,
        ) {
            val id = moveGridItemResult?.movingGridItem?.id

            if ((drag == Drag.Cancel || drag == Drag.End) &&
                id != null &&
                folderGridItems != null &&
                folderGridItems.any { it.id == id } &&
                !isVisibleFolders
            ) {
                onResetGrid()
            }
        }

        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                val icon = iconPackInfoFilePaths[data.componentName] ?: data.icon

                AsyncImage(
                    model = Builder(context)
                        .data(data.customIcon ?: icon)
                        .addLastModifiedToFileCacheKey(true)
                        .size(Size.ORIGINAL)
                        .build(),
                    contentDescription = null,
                    modifier = commonModifier,
                )
            }

            is GridItemData.ShortcutConfig -> {
                val icon = when {
                    data.customIcon != null -> data.customIcon
                    data.shortcutIntentIcon != null -> data.shortcutIntentIcon
                    data.activityIcon != null -> data.activityIcon
                    else -> data.applicationIcon
                }

                AsyncImage(
                    model = Builder(context)
                        .data(icon)
                        .addLastModifiedToFileCacheKey(true)
                        .size(Size.ORIGINAL)
                        .build(),
                    contentDescription = null,
                    modifier = commonModifier,
                )
            }

            is GridItemData.ShortcutInfo -> {
                AsyncImage(
                    model = Builder(context)
                        .data(data.customIcon ?: data.icon)
                        .addLastModifiedToFileCacheKey(true)
                        .size(Size.ORIGINAL)
                        .build(),
                    contentDescription = null,
                    modifier = commonModifier,
                )
            }

            is GridItemData.Folder -> {
                if (data.icon != null) {
                    AsyncImage(
                        model = Builder(context)
                            .data(data.icon)
                            .addLastModifiedToFileCacheKey(true)
                            .size(Size.ORIGINAL)
                            .build(),
                        contentDescription = null,
                        modifier = commonModifier,
                    )
                } else {
                    Icon(
                        modifier = commonModifier,
                        imageVector = EblanLauncherIcons.Folder,
                        contentDescription = null,
                        tint = folderIconTint,
                    )
                }
            }

            else -> Unit
        }
    }
}

private fun getSourceBounds(
    gridItem: GridItem,
    cellWidth: Int,
    cellHeight: Int,
    leftPadding: Int,
    topOffset: Int,
): Rect {
    val x = gridItem.startColumn * cellWidth
    val y = gridItem.startRow * cellHeight

    val width = gridItem.columnSpan * cellWidth
    val height = gridItem.rowSpan * cellHeight

    val left = x + leftPadding
    val top = y + topOffset

    return Rect(
        left,
        top,
        left + width,
        top + height,
    )
}

private fun Modifier.whiteBox(
    textColor: Color,
    visible: Boolean,
): Modifier = if (visible) {
    drawWithCache {
        val strokeWidth = 3.dp.toPx()

        val cornerRadius = 5.dp.toPx()

        val inset = strokeWidth / 2f

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            color = textColor.copy(alpha = 0.3f).toArgb()
            setShadowLayer(12.dp.toPx(), 0f, 0f, textColor.toArgb())
        }

        onDrawBehind {
            drawContext.canvas.nativeCanvas.drawRoundRect(
                inset,
                inset,
                size.width - inset,
                size.height - inset,
                cornerRadius,
                cornerRadius,
                paint,
            )
        }
    }
} else {
    this
}
