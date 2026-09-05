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

import android.content.Intent.parseUri
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
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
import com.eblan.launcher.domain.usecase.grid.FOLDER_PREVIEW_COLUMNS
import com.eblan.launcher.domain.usecase.grid.FOLDER_PREVIEW_ROWS
import com.eblan.launcher.feature.home.component.PreviewFolderGridLayout
import com.eblan.launcher.feature.home.component.gridItemScaleAnimation
import com.eblan.launcher.feature.home.component.gridItemSharedElement
import com.eblan.launcher.feature.home.component.swipeGestures
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.feature.home.util.handleOnPress
import com.eblan.launcher.feature.home.util.onDoubleTap
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.settings.rememberIsNotificationAccessGranted
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun InteractiveFolderGridItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    drag: Drag,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    isScrollInProgress: Boolean,
    statusBarNotifications: Map<String, Int>,
    isVisibleOverlay: Boolean,
    moveGridItemResult: MoveGridItemResult?,
    progress: Float,
    showFolderGridItemPopup: Boolean,
    previewFolderGridItems: Map<String, PreviewFolder>,
    minCellWidthPx: Int,
    minCellHeightPx: Int,
    paddingValues: PaddingValues,
    sharedElementKey: SharedElementKey,
    isInProgress: Boolean,
    iconPackInfoFilePaths: Map<String, String?>,
    animations: Boolean,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    folderPopups: List<FolderPopup>,
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
    onUpsertFolderPopupEntry: (FolderPopupEntry) -> Unit,
) {
    val density = LocalDensity.current

    val layoutDirection = LocalLayoutDirection.current

    val isSelected =
        moveGridItemResult != null && moveGridItemResult.movingGridItem.id == gridItem.id

    val currentGridItemSettings = if (gridItem.override) {
        gridItem.gridItemSettings
    } else {
        gridItemSettings
    }

    val currentTextColor = getTextColorFromBackgroundColor(
        backgroundColor = folderBackgroundColor,
        customBackgroundColor = customFolderBackgroundColor,
        textColor = currentGridItemSettings.textColor,
        customTextColor = currentGridItemSettings.customTextColor,
        systemTextColor = systemTextColor,
        systemCustomTextColor = systemCustomTextColor,
    )

    val padding = if (animations) {
        lerp(1.dp, currentGridItemSettings.padding.dp, progress)
    } else {
        currentGridItemSettings.padding.dp
    }

    val iconSize = if (animations) {
        lerp(
            currentGridItemSettings.iconSize.dp / maxOf(
                FOLDER_PREVIEW_COLUMNS,
                FOLDER_PREVIEW_ROWS,
            ),
            currentGridItemSettings.iconSize.dp,
            progress,
        )
    } else {
        currentGridItemSettings.iconSize.dp
    }

    val hasInteraction = isSelected && isVisibleOverlay

    val sourceBounds = getSourceBounds(
        density = density,
        layoutDirection = layoutDirection,
        paddingValues = paddingValues,
        startColumn = gridItem.startColumn,
        startRow = gridItem.startRow,
        cellWidth = minCellWidthPx,
        cellHeight = minCellHeightPx,
    )

    val isVisibleFolder = remember(
        key1 = gridItem,
        key2 = folderPopups,
    ) {
        folderPopups.any { it.folderPopupEntry.id == gridItem.id }
    }

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = currentGridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = currentGridItemSettings.verticalArrangement)

    val maxLines = if (currentGridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    LaunchedEffect(
        key1 = drag,
        key2 = hasInteraction,
        key3 = showFolderGridItemPopup,
    ) {
        if (drag == Drag.Dragging && hasInteraction && showFolderGridItemPopup) {
            onUpdateIsDragging(true)

            onUpdateIsCloseFolderGridItemPopup(true)
        }
    }

    when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo -> {
            InteractiveFolderApplicationInfoGridItem(
                modifier = modifier,
                sharedTransitionScope = sharedTransitionScope,
                data = data,
                gridItem = gridItem,
                gridItemSettings = currentGridItemSettings,
                isScrollInProgress = isScrollInProgress,
                isSelected = isSelected,
                isVisibleOverlay = isVisibleOverlay,
                sharedElementKey = sharedElementKey,
                statusBarNotifications = statusBarNotifications,
                padding = padding,
                iconSize = iconSize,
                sourceBounds = sourceBounds,
                isInProgress = isInProgress,
                iconPackInfoFilePaths = iconPackInfoFilePaths,
                animations = animations,
                textColor = currentTextColor,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
                onOpenAppDrawer = onOpenAppDrawer,
                onShowGridItemPopup = onShowGridItemPopup,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateOverlayBounds = onUpdateOverlayBounds,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
            )
        }

        is GridItemData.ShortcutInfo -> {
            InteractiveFolderShortcutInfoGridItem(
                modifier = modifier,
                sharedTransitionScope = sharedTransitionScope,
                data = data,
                gridItem = gridItem,
                gridItemSettings = currentGridItemSettings,
                hasShortcutHostPermission = hasShortcutHostPermission,
                isScrollInProgress = isScrollInProgress,
                isSelected = isSelected,
                isVisibleOverlay = isVisibleOverlay,
                sharedElementKey = sharedElementKey,
                padding = padding,
                iconSize = iconSize,
                sourceBounds = sourceBounds,
                isInProgress = isInProgress,
                animations = animations,
                textColor = currentTextColor,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
                onOpenAppDrawer = onOpenAppDrawer,
                onShowGridItemPopup = onShowGridItemPopup,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateOverlayBounds = onUpdateOverlayBounds,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
            )
        }

        is GridItemData.ShortcutConfig -> {
            InteractiveFolderShortcutConfigGridItem(
                modifier = modifier,
                sharedTransitionScope = sharedTransitionScope,
                data = data,
                gridItem = gridItem,
                gridItemSettings = currentGridItemSettings,
                isScrollInProgress = isScrollInProgress,
                isSelected = isSelected,
                isVisibleOverlay = isVisibleOverlay,
                sharedElementKey = sharedElementKey,
                padding = padding,
                iconSize = iconSize,
                isInProgress = isInProgress,
                animations = animations,
                textColor = currentTextColor,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
                onOpenAppDrawer = onOpenAppDrawer,
                onShowGridItemPopup = onShowGridItemPopup,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateOverlayBounds = onUpdateOverlayBounds,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
            )
        }

        is GridItemData.Folder -> {
            InteractiveNestedFolderGridItem(
                modifier = modifier,
                sharedTransitionScope = sharedTransitionScope,
                data = data,
                drag = drag,
                gridItem = gridItem,
                gridItemSettings = currentGridItemSettings,
                isScrollInProgress = isScrollInProgress,
                isSelected = isSelected,
                isVisibleOverlay = isVisibleOverlay,
                sharedElementKey = sharedElementKey,
                showFolderGridItemPopup = showFolderGridItemPopup,
                previewFolderGridItems = previewFolderGridItems,
                hasShortcutHostPermission = hasShortcutHostPermission,
                padding = padding,
                iconSize = iconSize,
                isInProgress = isInProgress,
                iconPackInfoFilePaths = iconPackInfoFilePaths,
                animations = animations,
                textColor = currentTextColor,
                folderCornerRadius = folderCornerRadius,
                folderBackgroundColor = folderBackgroundColor,
                customFolderBackgroundColor = customFolderBackgroundColor,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                isVisibleFolder = isVisibleFolder,
                onUpdateIsCloseFolderGridItemPopup = onUpdateIsCloseFolderGridItemPopup,
                onOpenAppDrawer = onOpenAppDrawer,
                onShowGridItemPopup = onShowGridItemPopup,
                onUpsertFolderPopupEntry = onUpsertFolderPopupEntry,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateIsDragging = onUpdateIsDragging,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateOverlayBounds = onUpdateOverlayBounds,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
            )
        }

        else -> error("Unsupported Folder Grid Item")
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun InteractiveFolderApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    data: GridItemData.ApplicationInfo,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    isScrollInProgress: Boolean,
    isSelected: Boolean,
    isVisibleOverlay: Boolean,
    sharedElementKey: SharedElementKey,
    statusBarNotifications: Map<String, Int>,
    padding: Dp,
    iconSize: Dp,
    sourceBounds: Rect,
    isInProgress: Boolean,
    iconPackInfoFilePaths: Map<String, String?>,
    animations: Boolean,
    textColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
    onOpenAppDrawer: () -> Unit,
    onShowGridItemPopup: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
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

    val androidLauncherAppsWrapper = LocalLauncherApps.current

    val context = LocalContext.current

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val icon = iconPackInfoFilePaths[data.componentName] ?: data.icon

    val hasNotifications =
        (statusBarNotifications[data.packageName] ?: 0) > 0

    val hasInteraction = isSelected && isVisibleOverlay

    val alpha = if (hasInteraction) 0f else 1f

    val isNotificationAccessGranted by rememberIsNotificationAccessGranted()

    val scale = remember { Animatable(1f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            )
            .pointerInput(
                key1 = isVisibleOverlay,
                key2 = isInProgress,
            ) {
                detectTapGestures(
                    onDoubleTap = if (!isVisibleOverlay && !isInProgress) {
                        {
                            onDoubleTap(
                                context = context,
                                doubleTap = gridItem.doubleTap,
                                launcherApps = launcherApps,
                                onOpenAppDrawer = onOpenAppDrawer,
                            )
                        }
                    } else {
                        null
                    },
                    onLongPress = if (!isVisibleOverlay && !isInProgress) {
                        {
                            scope.launch {
                                onLongPressFolderGridItem(
                                    graphicsLayer = graphicsLayer,
                                    intOffset = intOffset,
                                    intSize = intSize,
                                    sharedElementKey = sharedElementKey,
                                    gridItem = gridItem,
                                    onUpdateImageBitmap = onUpdateImageBitmap,
                                    onUpdateOverlayBounds = onUpdateOverlayBounds,
                                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                                    onShowGridItemPopup = onShowGridItemPopup,
                                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                    onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onTap = if (!isVisibleOverlay && !isInProgress) {
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
                enabled = !isInProgress,
                onOpenAppDrawer = onOpenAppDrawer,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .alpha(alpha),
        ) {
            AsyncImage(
                model = Builder(context).data(data.customIcon ?: icon)
                    .addLastModifiedToFileCacheKey(true).size(Size.ORIGINAL).build(),
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
                        visible = !isScrollInProgress && !hasInteraction && !isInProgress,
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
                        .size(iconSize * 0.3f)
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
private fun InteractiveFolderShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    data: GridItemData.ShortcutInfo,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    isScrollInProgress: Boolean,
    isSelected: Boolean,
    isVisibleOverlay: Boolean,
    sharedElementKey: SharedElementKey,
    padding: Dp,
    iconSize: Dp,
    sourceBounds: Rect,
    isInProgress: Boolean,
    animations: Boolean,
    textColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
    onOpenAppDrawer: () -> Unit,
    onShowGridItemPopup: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
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

    val hasInteraction = isSelected && isVisibleOverlay

    val defaultAlpha = if (hasShortcutHostPermission && data.isEnabled) 1f else 0.3f

    val alpha = if (hasInteraction) 0f else defaultAlpha

    val scale = remember { Animatable(1f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            )
            .pointerInput(
                key1 = isVisibleOverlay,
                key2 = isInProgress,
            ) {
                detectTapGestures(
                    onDoubleTap = if (!isVisibleOverlay && !isInProgress) {
                        {
                            scope.launch {
                                onDoubleTap(
                                    context = context,
                                    doubleTap = gridItem.doubleTap,
                                    launcherApps = launcherApps,
                                    onOpenAppDrawer = onOpenAppDrawer,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onLongPress = if (!isVisibleOverlay && !isInProgress) {
                        {
                            scope.launch {
                                onLongPressFolderGridItem(
                                    graphicsLayer = graphicsLayer,
                                    intOffset = intOffset,
                                    intSize = intSize,
                                    sharedElementKey = sharedElementKey,
                                    gridItem = gridItem,
                                    onUpdateImageBitmap = onUpdateImageBitmap,
                                    onUpdateOverlayBounds = onUpdateOverlayBounds,
                                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                                    onShowGridItemPopup = onShowGridItemPopup,
                                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                    onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onTap = if (!isVisibleOverlay && !isInProgress) {
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
                enabled = !isInProgress,
                onOpenAppDrawer = onOpenAppDrawer,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(
            modifier = Modifier.size(iconSize),
        ) {
            AsyncImage(
                model = Builder(context).data(customIcon).addLastModifiedToFileCacheKey(true)
                    .size(Size.ORIGINAL).build(),
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
                        visible = !isScrollInProgress && !hasInteraction && !isInProgress,
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
                model = Builder(context).data(data.eblanApplicationInfoIcon).size(Size.ORIGINAL)
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
private fun InteractiveFolderShortcutConfigGridItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    data: GridItemData.ShortcutConfig,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    isScrollInProgress: Boolean,
    isSelected: Boolean,
    isVisibleOverlay: Boolean,
    sharedElementKey: SharedElementKey,
    padding: Dp,
    iconSize: Dp,
    isInProgress: Boolean,
    animations: Boolean,
    textColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
    onOpenAppDrawer: () -> Unit,
    onShowGridItemPopup: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
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

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

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

    val hasInteraction = isSelected && isVisibleOverlay

    val alpha = if (hasInteraction) 0f else 1f

    val scale = remember { Animatable(1f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            )
            .pointerInput(key1 = isVisibleOverlay && !isInProgress) {
                detectTapGestures(
                    onDoubleTap = if (!isVisibleOverlay && !isInProgress) {
                        {
                            onDoubleTap(
                                context = context,
                                doubleTap = gridItem.doubleTap,
                                launcherApps = launcherApps,
                                onOpenAppDrawer = onOpenAppDrawer,
                            )
                        }
                    } else {
                        null
                    },
                    onLongPress = if (!isVisibleOverlay && !isInProgress) {
                        {
                            scope.launch {
                                onLongPressFolderGridItem(
                                    graphicsLayer = graphicsLayer,
                                    intOffset = intOffset,
                                    intSize = intSize,
                                    sharedElementKey = sharedElementKey,
                                    gridItem = gridItem,
                                    onUpdateImageBitmap = onUpdateImageBitmap,
                                    onUpdateOverlayBounds = onUpdateOverlayBounds,
                                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                                    onShowGridItemPopup = onShowGridItemPopup,
                                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                    onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onTap = if (!isVisibleOverlay && !isInProgress) {
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
                enabled = !isInProgress,
                onOpenAppDrawer = onOpenAppDrawer,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        AsyncImage(
            model = Builder(context).data(icon).addLastModifiedToFileCacheKey(true)
                .size(Size.ORIGINAL).build(),
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
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
                    visible = !isScrollInProgress && !hasInteraction && !isInProgress,
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun InteractiveNestedFolderGridItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    data: GridItemData.Folder,
    drag: Drag,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    isScrollInProgress: Boolean,
    isSelected: Boolean,
    isVisibleOverlay: Boolean,
    sharedElementKey: SharedElementKey,
    showFolderGridItemPopup: Boolean,
    previewFolderGridItems: Map<String, PreviewFolder>,
    hasShortcutHostPermission: Boolean,
    padding: Dp,
    iconSize: Dp,
    isInProgress: Boolean,
    iconPackInfoFilePaths: Map<String, String?>,
    animations: Boolean,
    textColor: Color,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    isVisibleFolder: Boolean,
    onUpdateIsCloseFolderGridItemPopup: (Boolean) -> Unit,
    onOpenAppDrawer: () -> Unit,
    onShowGridItemPopup: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpsertFolderPopupEntry: (FolderPopupEntry) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
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

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val hasInteraction = isSelected && isVisibleOverlay

    val textAlpha = if (hasInteraction) 0f else 1f
    val iconAlpha = if (hasInteraction || isVisibleFolder) 0f else 1f

    val scale = remember { Animatable(1f) }

    LaunchedEffect(
        key1 = drag,
        key2 = hasInteraction,
        key3 = showFolderGridItemPopup,
    ) {
        if (drag == Drag.Dragging && hasInteraction && showFolderGridItemPopup) {
            onUpdateIsDragging(true)

            onUpdateIsCloseFolderGridItemPopup(true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            )
            .pointerInput(
                key1 = isVisibleOverlay,
                key2 = isInProgress,
            ) {
                detectTapGestures(
                    onDoubleTap = if (!isVisibleOverlay && !isInProgress) {
                        {
                            onDoubleTap(
                                context = context,
                                doubleTap = gridItem.doubleTap,
                                launcherApps = launcherApps,
                                onOpenAppDrawer = onOpenAppDrawer,
                            )
                        }
                    } else {
                        null
                    },
                    onLongPress = if (!isVisibleOverlay && !isInProgress) {
                        {
                            scope.launch {
                                onLongPressFolderGridItem(
                                    graphicsLayer = graphicsLayer,
                                    intOffset = intOffset,
                                    intSize = intSize,
                                    sharedElementKey = sharedElementKey,
                                    gridItem = gridItem,
                                    onUpdateImageBitmap = onUpdateImageBitmap,
                                    onUpdateOverlayBounds = onUpdateOverlayBounds,
                                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                                    onShowGridItemPopup = onShowGridItemPopup,
                                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                    onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onTap = if (!isVisibleOverlay && !isInProgress) {
                        {
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
                enabled = !isInProgress,
                onOpenAppDrawer = onOpenAppDrawer,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        val commonModifier = Modifier
            .size(iconSize)
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
                visible = !isScrollInProgress && !hasInteraction && !isInProgress,
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
                        PreviewNestedFolderGridItem(
                            alpha = iconAlpha,
                            gridItem = it,
                            hasShortcutHostPermission = hasShortcutHostPermission,
                            iconPackInfoFilePaths = iconPackInfoFilePaths,
                            gridItemSettings = gridItemSettings,
                            folderBackgroundColor = folderBackgroundColor,
                            customFolderBackgroundColor = customFolderBackgroundColor,
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
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

@Composable
private fun PreviewNestedFolderGridItem(
    modifier: Modifier = Modifier,
    alpha: Float,
    gridItem: GridItem,
    hasShortcutHostPermission: Boolean,
    iconPackInfoFilePaths: Map<String, String?>,
    gridItemSettings: GridItemSettings,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
) {
    val context = LocalContext.current

    key(gridItem.id) {
        val currentGridItemSettings = if (gridItem.override) {
            gridItem.gridItemSettings
        } else {
            gridItemSettings
        }

        val alpha = when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo,
            is GridItemData.Folder,
            is GridItemData.ShortcutConfig,
            is GridItemData.Widget,
            -> alpha

            is GridItemData.ShortcutInfo -> {
                if (hasShortcutHostPermission && data.isEnabled) 1f else 0.3f
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
            .alpha(alpha)

        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                val icon = iconPackInfoFilePaths[data.componentName] ?: data.icon

                AsyncImage(
                    model = Builder(context).data(data.customIcon ?: icon)
                        .addLastModifiedToFileCacheKey(true).size(Size.ORIGINAL).build(),
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
                    model = Builder(context).data(icon).addLastModifiedToFileCacheKey(true)
                        .size(Size.ORIGINAL).build(),
                    contentDescription = null,
                    modifier = commonModifier,
                )
            }

            is GridItemData.ShortcutInfo -> {
                AsyncImage(
                    model = Builder(context).data(data.customIcon ?: data.icon)
                        .addLastModifiedToFileCacheKey(true).size(Size.ORIGINAL).build(),
                    contentDescription = null,
                    modifier = commonModifier,
                )
            }

            is GridItemData.Folder -> {
                if (data.icon != null) {
                    AsyncImage(
                        model = Builder(context).data(data.icon).addLastModifiedToFileCacheKey(true)
                            .size(Size.ORIGINAL).build(),
                        contentDescription = null,
                        modifier = commonModifier,
                    )
                } else {
                    Icon(
                        imageVector = EblanLauncherIcons.Folder,
                        contentDescription = null,
                        modifier = commonModifier,
                        tint = folderIconTint,
                    )
                }
            }

            else -> Unit
        }
    }
}

private fun getSourceBounds(
    density: Density,
    layoutDirection: LayoutDirection,
    paddingValues: PaddingValues,
    startColumn: Int,
    startRow: Int,
    cellWidth: Int,
    cellHeight: Int,
): Rect {
    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val x = startColumn * cellWidth

    val y = startRow * cellHeight

    val left = x + leftPadding

    val top = y + topPadding

    return Rect(
        left,
        top,
        left + cellWidth,
        top + cellHeight,
    )
}
