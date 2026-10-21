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
import androidx.compose.animation.SharedTransitionScope
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
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.folder.FolderPopup
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.domain.usecase.util.FOLDER_PREVIEW_COLUMNS
import com.eblan.launcher.domain.usecase.util.FOLDER_PREVIEW_ROWS
import com.eblan.launcher.feature.home.component.PreviewFolderGridLayout
import com.eblan.launcher.feature.home.component.gridItemScaleAnimation
import com.eblan.launcher.feature.home.component.gridItemSharedElement
import com.eblan.launcher.feature.home.component.swipeGestures
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.feature.home.util.handleEblanAction
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalStatusBarManager
import com.eblan.launcher.ui.settings.rememberIsNotificationAccessGranted
import kotlinx.coroutines.launch

@Composable
internal fun InteractiveFolderGridItem(
    // Grid Item
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    folderGridItemEntries: List<FolderEntry>,
    sharedTransitionScope: SharedTransitionScope,
    drag: Drag,
    hasShortcutHostPermission: Boolean,
    isScrollInProgress: Boolean,
    statusBarNotifications: Map<String, Int>,
    isVisibleOverlay: Boolean,
    moveGridItemResult: MoveGridItemResult?,
    progress: Float,
    showFolderGridItemMenu: Boolean,
    folderPopups: Map<String, FolderPopup>,
    minCellWidthPx: Int,
    minCellHeightPx: Int,
    paddingValues: PaddingValues,
    sharedElementKey: SharedElementKey,
    isInProgress: Boolean,
    iconPackFilePaths: Map<String, String?>,
    animations: Boolean,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    modifier: Modifier = Modifier,
    // Callbacks
    onOpenAppDrawer: () -> Unit,
    onUpsertFolderGridItemPopupEntry: (FolderEntry) -> Unit,
    onLongPressFolderGridItem: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
    onDragFolderGridItem: () -> Unit,
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
        // Background Color
        backgroundColor = folderBackgroundColor,
        customBackgroundColor = customFolderBackgroundColor,
        // Text Color
        textColor = currentGridItemSettings.textColor,
        customTextColor = currentGridItemSettings.customTextColor,
        systemCustomTextColor = systemCustomTextColor,
        systemTextColor = systemTextColor,
    )

    val padding = if (animations) {
        lerp(1.dp, currentGridItemSettings.padding.dp, progress)
    } else {
        currentGridItemSettings.padding.dp
    }

    val iconPadding = if (animations) {
        lerp(0.dp, currentGridItemSettings.iconPadding.dp, progress)
    } else {
        currentGridItemSettings.iconPadding.dp
    }

    val textPadding = if (animations) {
        lerp(0.dp, currentGridItemSettings.textPadding.dp, progress)
    } else {
        currentGridItemSettings.textPadding.dp
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
        // Layout
        density = density,
        layoutDirection = layoutDirection,
        paddingValues = paddingValues,
        // Position
        startColumn = gridItem.startColumn,
        startRow = gridItem.startRow,
        // Dimensions
        cellWidth = minCellWidthPx,
        cellHeight = minCellHeightPx,
    )

    val isVisibleFolder = remember(
        key1 = gridItem,
        key2 = folderGridItemEntries,
    ) {
        folderGridItemEntries.any { it.id == gridItem.id }
    }

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = currentGridItemSettings.horizontalAlignment)
    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = currentGridItemSettings.verticalArrangement)

    val maxLines = if (currentGridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    LaunchedEffect(
        key1 = drag,
        key2 = hasInteraction,
        key3 = showFolderGridItemMenu,
    ) {
        if (drag == Drag.Dragging && hasInteraction && showFolderGridItemMenu) {
            onDragFolderGridItem()
        }
    }

    when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo -> {
            InteractiveApplicationInfoGridItem(
                // Item Data and Configuration
                data = data,
                gridItem = gridItem,
                gridItemSettings = currentGridItemSettings,
                // State and Appearance
                statusBarNotifications = statusBarNotifications,
                iconPackFilePaths = iconPackFilePaths,
                isScrollInProgress = isScrollInProgress,
                isSelected = isSelected,
                isVisibleOverlay = isVisibleOverlay,
                isInProgress = isInProgress,
                animations = animations,
                // Dimensions and Layout
                padding = padding,
                iconSize = iconSize,
                iconPadding = iconPadding,
                textPadding = textPadding,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
                textColor = currentTextColor,
                // Transitions and Modifier
                sharedTransitionScope = sharedTransitionScope,
                sharedElementKey = sharedElementKey,
                sourceBounds = sourceBounds,
                modifier = modifier,
                // Callbacks
                onOpenAppDrawer = onOpenAppDrawer,
                onLongPressFolderGridItem = onLongPressFolderGridItem,
            )
        }

        is GridItemData.ShortcutInfo -> {
            InteractiveShortcutInfoGridItem(
                // Item Data
                gridItem = gridItem,
                data = data,
                gridItemSettings = currentGridItemSettings,
                hasShortcutHostPermission = hasShortcutHostPermission,
                // State
                isScrollInProgress = isScrollInProgress,
                isSelected = isSelected,
                isVisibleOverlay = isVisibleOverlay,
                isInProgress = isInProgress,
                animations = animations,
                // Animation and Layout
                sharedTransitionScope = sharedTransitionScope,
                sharedElementKey = sharedElementKey,
                sourceBounds = sourceBounds,
                // Styling and Dimensions
                padding = padding,
                iconSize = iconSize,
                textColor = currentTextColor,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
                iconPadding = iconPadding,
                textPadding = textPadding,
                // Modifier
                modifier = modifier,
                // Callbacks
                onOpenAppDrawer = onOpenAppDrawer,
                onLongPressFolderGridItem = onLongPressFolderGridItem,
            )
        }

        is GridItemData.ShortcutConfig -> {
            InteractiveShortcutConfigGridItem(
                // Item Data
                gridItem = gridItem,
                data = data,
                gridItemSettings = currentGridItemSettings,
                // State
                isSelected = isSelected,
                isVisibleOverlay = isVisibleOverlay,
                isScrollInProgress = isScrollInProgress,
                isInProgress = isInProgress,
                animations = animations,
                // Layout and Sizing
                padding = padding,
                iconSize = iconSize,
                iconPadding = iconPadding,
                textPadding = textPadding,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
                textColor = currentTextColor,
                // Shared Transition
                sharedTransitionScope = sharedTransitionScope,
                sharedElementKey = sharedElementKey,
                // Modifier
                modifier = modifier,
                // Callbacks
                onOpenAppDrawer = onOpenAppDrawer,
                onLongPressFolderGridItem = onLongPressFolderGridItem,
            )
        }

        is GridItemData.Folder -> {
            InteractiveNestedFolderGridItem(
                // Grid Item Data
                data = data,
                gridItem = gridItem,
                gridItemSettings = currentGridItemSettings,
                // State
                isScrollInProgress = isScrollInProgress,
                isSelected = isSelected,
                isVisibleOverlay = isVisibleOverlay,
                sharedElementKey = sharedElementKey,
                folderPopups = folderPopups,
                hasShortcutHostPermission = hasShortcutHostPermission,
                isInProgress = isInProgress,
                animations = animations,
                isVisibleFolder = isVisibleFolder,
                sharedTransitionScope = sharedTransitionScope,
                // Appearance and Styling
                padding = padding,
                iconSize = iconSize,
                iconPackFilePaths = iconPackFilePaths,
                textColor = currentTextColor,
                folderCornerRadius = folderCornerRadius,
                folderBackgroundColor = folderBackgroundColor,
                customFolderBackgroundColor = customFolderBackgroundColor,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                iconPadding = iconPadding,
                textPadding = textPadding,
                // Modifier
                modifier = modifier,
                // Callbacks
                onOpenAppDrawer = onOpenAppDrawer,
                onUpsertFolderGridItemPopupEntry = onUpsertFolderGridItemPopupEntry,
                onLongPressFolderGridItem = onLongPressFolderGridItem,
            )
        }

        else -> error("Unsupported Folder Grid Item")
    }
}

@Composable
private fun InteractiveApplicationInfoGridItem(
    // Item Data and Configuration
    data: GridItemData.ApplicationInfo,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    // State and Appearance
    statusBarNotifications: Map<String, Int>,
    iconPackFilePaths: Map<String, String?>,
    isScrollInProgress: Boolean,
    isSelected: Boolean,
    isVisibleOverlay: Boolean,
    isInProgress: Boolean,
    animations: Boolean,
    // Dimensions and Layout
    padding: Dp,
    iconSize: Dp,
    iconPadding: Dp,
    textPadding: Dp,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
    textColor: Color,
    // Transitions and Modifier
    sharedTransitionScope: SharedTransitionScope,
    sharedElementKey: SharedElementKey,
    sourceBounds: Rect,
    modifier: Modifier = Modifier,
    // Callbacks
    onOpenAppDrawer: () -> Unit,
    onLongPressFolderGridItem: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val launcherApps = LocalLauncherApps.current
    val androidLauncherAppsWrapper = LocalLauncherApps.current
    val androidStatusBarManagerWrapper = LocalStatusBarManager.current
    val context = LocalContext.current

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }
    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val icon = iconPackFilePaths[data.componentName] ?: data.icon

    val hasNotifications =
        (statusBarNotifications[data.packageName] ?: 0) > 0

    val hasInteraction = isSelected && isVisibleOverlay

    val alpha = if (hasInteraction) 0f else 1f

    val isNotificationAccessGranted by rememberIsNotificationAccessGranted()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            )
            .run {
                if (!isVisibleOverlay && !isInProgress) {
                    pointerInput(key1 = Unit) {
                        detectTapGestures(
                            onDoubleTap = if (gridItem.doubleTap.eblanActionType != EblanActionType.None) {
                                {
                                    handleEblanAction(
                                        // Context and Action
                                        context = context,
                                        eblanAction = gridItem.doubleTap,
                                        // System Wrappers
                                        launcherApps = launcherApps,
                                        statusBarManagerWrapper = androidStatusBarManagerWrapper,
                                        // Callbacks
                                        onOpenAppDrawer = onOpenAppDrawer,
                                    )
                                }
                            } else {
                                null
                            },
                            onLongPress = {
                                scope.launch {
                                    onLongPressFolderGridItem(
                                        gridItem,
                                        graphicsLayer.toImageBitmap(),
                                        intOffset,
                                        intSize,
                                        sharedElementKey,
                                    )
                                }
                            },
                            onTap = {
                                androidLauncherAppsWrapper.startMainActivity(
                                    serialNumber = data.serialNumber,
                                    componentName = data.componentName,
                                    sourceBounds = sourceBounds,
                                )
                            },
                        )
                    }
                } else {
                    this
                }
            }
            .swipeGestures(
                // State
                enabled = !isInProgress,
                // Gestures
                swipeDown = gridItem.swipeDown,
                swipeUp = gridItem.swipeUp,
                // Actions
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
                    .padding(iconPadding)
                    .onGloballyPositioned {
                        intOffset = it.positionInRoot().round()

                        intSize = it.size
                    }
                    .gridItemSharedElement(
                        // Transition State
                        sharedTransitionScope = sharedTransitionScope,
                        sharedElementKey = sharedElementKey,
                        // Visibility Control
                        enabled = animations && !isInProgress,
                        visible = !isScrollInProgress && !hasInteraction,
                    )
                    .gridItemScaleAnimation(
                        animations = animations,
                        hasInteraction = hasInteraction,
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

        Text(
            modifier = Modifier
                .padding(textPadding)
                .alpha(alpha),
            text = data.customLabel ?: data.label,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            fontSize = gridItemSettings.textSize.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun InteractiveShortcutInfoGridItem(
    // Item Data
    gridItem: GridItem,
    data: GridItemData.ShortcutInfo,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    // State
    isScrollInProgress: Boolean,
    isSelected: Boolean,
    isVisibleOverlay: Boolean,
    isInProgress: Boolean,
    animations: Boolean,
    // Animation and Layout
    sharedTransitionScope: SharedTransitionScope,
    sharedElementKey: SharedElementKey,
    sourceBounds: Rect,
    // Styling and Dimensions
    padding: Dp,
    iconSize: Dp,
    textColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
    iconPadding: Dp,
    textPadding: Dp,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onOpenAppDrawer: () -> Unit,
    onLongPressFolderGridItem: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val androidLauncherAppsWrapper = LocalLauncherApps.current
    val launcherApps = LocalLauncherApps.current
    val androidStatusBarManagerWrapper = LocalStatusBarManager.current
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            )
            .run {
                if (!isVisibleOverlay && !isInProgress) {
                    pointerInput(key1 = Unit) {
                        detectTapGestures(
                            onDoubleTap = if (gridItem.doubleTap.eblanActionType != EblanActionType.None) {
                                {
                                    handleEblanAction(
                                        // Context and Action
                                        context = context,
                                        eblanAction = gridItem.doubleTap,
                                        // System Wrappers
                                        launcherApps = launcherApps,
                                        statusBarManagerWrapper = androidStatusBarManagerWrapper,
                                        // Callbacks
                                        onOpenAppDrawer = onOpenAppDrawer,
                                    )
                                }
                            } else {
                                null
                            },
                            onLongPress = {
                                scope.launch {
                                    onLongPressFolderGridItem(
                                        gridItem,
                                        graphicsLayer.toImageBitmap(),
                                        intOffset,
                                        intSize,
                                        sharedElementKey,
                                    )
                                }
                            },
                            onTap = {
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
                            },
                        )
                    }
                } else {
                    this
                }
            }
            .swipeGestures(
                // State
                enabled = !isInProgress,
                // Gestures
                swipeDown = gridItem.swipeDown,
                swipeUp = gridItem.swipeUp,
                // Actions
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
                    .padding(iconPadding)
                    .onGloballyPositioned {
                        intOffset = it.positionInRoot().round()

                        intSize = it.size
                    }
                    .gridItemSharedElement(
                        // Transition State
                        sharedTransitionScope = sharedTransitionScope,
                        sharedElementKey = sharedElementKey,
                        // Visibility Control
                        enabled = animations && !isInProgress,
                        visible = !isScrollInProgress && !hasInteraction,
                    )
                    .gridItemScaleAnimation(
                        animations = animations,
                        hasInteraction = hasInteraction,
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
                    .size(iconSize * 0.25f)
                    .alpha(alpha)
                    .align(Alignment.BottomEnd),
                contentDescription = null,
            )
        }

        Text(
            modifier = Modifier
                .padding(textPadding)
                .alpha(alpha),
            text = customShortLabel,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            fontSize = gridItemSettings.textSize.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun InteractiveShortcutConfigGridItem(
    // Item Data
    gridItem: GridItem,
    data: GridItemData.ShortcutConfig,
    gridItemSettings: GridItemSettings,
    // State
    isSelected: Boolean,
    isVisibleOverlay: Boolean,
    isScrollInProgress: Boolean,
    isInProgress: Boolean,
    animations: Boolean,
    // Layout and Sizing
    padding: Dp,
    iconSize: Dp,
    iconPadding: Dp,
    textPadding: Dp,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
    textColor: Color,
    // Shared Transition
    sharedTransitionScope: SharedTransitionScope,
    sharedElementKey: SharedElementKey,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onOpenAppDrawer: () -> Unit,
    onLongPressFolderGridItem: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val launcherApps = LocalLauncherApps.current
    val androidStatusBarManagerWrapper = LocalStatusBarManager.current
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            )
            .run {
                if (!isVisibleOverlay && !isInProgress) {
                    pointerInput(key1 = Unit) {
                        detectTapGestures(
                            onDoubleTap = if (gridItem.doubleTap.eblanActionType != EblanActionType.None) {
                                {
                                    handleEblanAction(
                                        // Context and Action
                                        context = context,
                                        eblanAction = gridItem.doubleTap,
                                        // System Wrappers
                                        launcherApps = launcherApps,
                                        statusBarManagerWrapper = androidStatusBarManagerWrapper,
                                        // Callbacks
                                        onOpenAppDrawer = onOpenAppDrawer,
                                    )
                                }
                            } else {
                                null
                            },
                            onLongPress = {
                                scope.launch {
                                    onLongPressFolderGridItem(
                                        gridItem,
                                        graphicsLayer.toImageBitmap(),
                                        intOffset,
                                        intSize,
                                        sharedElementKey,
                                    )
                                }
                            },
                            onTap = {
                                data.shortcutIntentUri?.let {
                                    context.startActivity(parseUri(it, 0))
                                }
                            },
                        )
                    }
                } else {
                    this
                }
            }
            .swipeGestures(
                // State
                enabled = !isInProgress,
                // Gestures
                swipeDown = gridItem.swipeDown,
                swipeUp = gridItem.swipeUp,
                // Actions
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
                .padding(iconPadding)
                .onGloballyPositioned {
                    intOffset = it.positionInRoot().round()

                    intSize = it.size
                }
                .gridItemSharedElement(
                    // Transition State
                    sharedTransitionScope = sharedTransitionScope,
                    sharedElementKey = sharedElementKey,
                    // Visibility Control
                    enabled = animations && !isInProgress,
                    visible = !isScrollInProgress && !hasInteraction,
                )
                .gridItemScaleAnimation(
                    animations = animations,
                    hasInteraction = hasInteraction,
                )
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }

                    drawLayer(graphicsLayer)
                }
                .alpha(alpha),
        )

        Text(
            modifier = Modifier
                .padding(textPadding)
                .alpha(alpha),
            text = label.toString(),
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            fontSize = gridItemSettings.textSize.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun InteractiveNestedFolderGridItem(
    // Grid Item Data
    data: GridItemData.Folder,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    // State
    isScrollInProgress: Boolean,
    isSelected: Boolean,
    isVisibleOverlay: Boolean,
    sharedElementKey: SharedElementKey,
    folderPopups: Map<String, FolderPopup>,
    hasShortcutHostPermission: Boolean,
    isInProgress: Boolean,
    animations: Boolean,
    isVisibleFolder: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    // Appearance and Styling
    padding: Dp,
    iconSize: Dp,
    iconPackFilePaths: Map<String, String?>,
    textColor: Color,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    iconPadding: Dp,
    textPadding: Dp,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onOpenAppDrawer: () -> Unit,
    onUpsertFolderGridItemPopupEntry: (FolderEntry) -> Unit,
    onLongPressFolderGridItem: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val launcherApps = LocalLauncherApps.current
    val androidStatusBarManagerWrapper = LocalStatusBarManager.current
    val context = LocalContext.current

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }
    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val hasInteraction = isSelected && isVisibleOverlay

    val textAlpha = if (hasInteraction) 0f else 1f
    val iconAlpha = if (hasInteraction || isVisibleFolder) 0f else 1f

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            )
            .run {
                if (!isVisibleOverlay && !isInProgress) {
                    pointerInput(key1 = Unit) {
                        detectTapGestures(
                            onDoubleTap = if (gridItem.doubleTap.eblanActionType != EblanActionType.None) {
                                {
                                    handleEblanAction(
                                        // Context and Action
                                        context = context,
                                        eblanAction = gridItem.doubleTap,
                                        // System Wrappers
                                        launcherApps = launcherApps,
                                        statusBarManagerWrapper = androidStatusBarManagerWrapper,
                                        // Callbacks
                                        onOpenAppDrawer = onOpenAppDrawer,
                                    )
                                }
                            } else {
                                null
                            },
                            onLongPress = {
                                scope.launch {
                                    onLongPressFolderGridItem(
                                        gridItem,
                                        graphicsLayer.toImageBitmap(),
                                        intOffset,
                                        intSize,
                                        sharedElementKey,
                                    )
                                }
                            },
                            onTap = {
                                onUpsertFolderGridItemPopupEntry(
                                    FolderEntry(
                                        id = gridItem.id,
                                        x = intOffset.x,
                                        y = intOffset.y,
                                        width = intSize.width,
                                        height = intSize.height,
                                        isCloseFolder = false,
                                    ),
                                )
                            },
                        )
                    }
                } else {
                    this
                }
            }
            .swipeGestures(
                // State
                enabled = !isInProgress,
                // Gestures
                swipeDown = gridItem.swipeDown,
                swipeUp = gridItem.swipeUp,
                // Actions
                onOpenAppDrawer = onOpenAppDrawer,
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        val commonModifier = Modifier
            .size(iconSize)
            .padding(iconPadding)
            .onGloballyPositioned {
                intOffset = it.positionInRoot().round()

                intSize = it.size
            }
            .gridItemSharedElement(
                // Transition State
                sharedTransitionScope = sharedTransitionScope,
                sharedElementKey = sharedElementKey,
                // Visibility Control
                enabled = animations && !isInProgress,
                visible = !isScrollInProgress && !hasInteraction,
            )
            .gridItemScaleAnimation(
                animations = animations,
                hasInteraction = hasInteraction,
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
                    // Data
                    gridItems = folderPopups[gridItem.id]?.previewFolderGridItems,
                    // Layout
                    modifier = Modifier.fillMaxSize(),
                    // Callbacks
                    slotId = { it.id },
                    content = {
                        PreviewNestedFolderGridItem(
                            // Item Data
                            gridItem = it,
                            gridItemSettings = gridItemSettings,
                            // Permissions
                            hasShortcutHostPermission = hasShortcutHostPermission,
                            // Assets and Styling
                            iconPackFilePaths = iconPackFilePaths,
                            alpha = iconAlpha,
                            folderBackgroundColor = folderBackgroundColor,
                            customFolderBackgroundColor = customFolderBackgroundColor,
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                        )
                    },
                )
            }
        }

        Text(
            modifier = Modifier
                .padding(textPadding)
                .alpha(textAlpha),
            text = data.label,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            fontSize = gridItemSettings.textSize.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PreviewNestedFolderGridItem(
    // Item Data
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    // Permissions
    hasShortcutHostPermission: Boolean,
    // Assets and Styling
    iconPackFilePaths: Map<String, String?>,
    alpha: Float,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // Layout
    modifier: Modifier = Modifier,
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
            // Background Color
            backgroundColor = folderBackgroundColor,
            customBackgroundColor = customFolderBackgroundColor,
            // Text Color
            textColor = currentGridItemSettings.textColor,
            customTextColor = currentGridItemSettings.customTextColor,
            systemCustomTextColor = systemCustomTextColor,
            systemTextColor = systemTextColor,
            // Default Color
            defaultColor = MaterialTheme.colorScheme.onSurface,
        )

        val commonModifier = modifier
            .fillMaxSize()
            .padding(1.dp)
            .alpha(alpha)

        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                val icon = iconPackFilePaths[data.componentName] ?: data.icon

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
    // Layout
    density: Density,
    layoutDirection: LayoutDirection,
    paddingValues: PaddingValues,
    // Position
    startColumn: Int,
    startRow: Int,
    // Dimensions
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
