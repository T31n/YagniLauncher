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
package com.eblan.launcher.feature.home.screen.pager.griditem

import android.graphics.Rect
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.folder.FolderPopup
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.gridItemScaleAnimation
import com.eblan.launcher.feature.home.component.gridItemSharedElement
import com.eblan.launcher.feature.home.component.whiteBox
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getHorizontalArrangement
import com.eblan.launcher.feature.home.util.getVerticalAlignment
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import kotlinx.coroutines.launch

@Composable
internal fun InteractiveGridItem(
    // Item Data
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    sharedElementKey: SharedElementKey,
    statusBarNotifications: Map<String, Int>,
    moveGridItemResult: MoveGridItemResult?,
    // Layout and Dimensions
    cellWidth: Int,
    cellHeight: Int,
    leftPadding: Int,
    topOffset: Int,
    // Styling and Appearance
    textColor: TextColor,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemCustomTextColor: Int,
    iconPackFilePaths: Map<String, String?>,
    // Animation and Transition
    sharedTransitionScope: SharedTransitionScope,
    animations: Boolean,
    // State and Flags
    drag: Drag,
    hasShortcutHostPermission: Boolean,
    isScrollInProgress: Boolean,
    isVisibleOverlay: Boolean,
    isVisibleFolderGridItems: Boolean,
    lockMovement: Boolean,
    isDragging: Boolean,
    showGridItemMenu: Boolean,
    folderPopups: Map<String, FolderPopup>,
    folderGridItemEntries: List<FolderEntry>,
    modifier: Modifier = Modifier,
    // Callbacks
    onOpenAppDrawer: () -> Unit,
    onShowFolderWhenDragging: (
        folderEntry: FolderEntry,
        gridItem: GridItem,
    ) -> Unit,
    onResetGrid: () -> Unit,
    onLongPressGridItem: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
    onTapFolderGridItem: (FolderEntry) -> Unit,
    onDragGridItem: () -> Unit
) {
    val isSelected =
        moveGridItemResult != null && moveGridItemResult.movingGridItem.id == gridItem.id

    val currentGridItemSettings = if (gridItem.override) {
        gridItem.gridItemSettings
    } else {
        gridItemSettings
    }

    val currentTextColor = getGridItemTextColor(
        // Grid Item Text Colors
        gridItemTextColor = currentGridItemSettings.textColor,
        gridItemCustomTextColor = currentGridItemSettings.customTextColor,
        // System Text Colors
        systemTextColor = textColor,
        systemCustomTextColor = gridItemSettings.customTextColor
    )

    val hasInteraction = isSelected && isVisibleOverlay

    val isVisibleWhiteBox = hasInteraction && drag == Drag.Dragging

    val sourceBounds = getSourceBounds(
        // Grid Item
        gridItem = gridItem,
        // Dimensions
        cellWidth = cellWidth,
        cellHeight = cellHeight,
        // Offsets
        leftPadding = leftPadding,
        topOffset = topOffset
    )

    val isVisibleFolder = remember(
        key1 = gridItem,
        key2 = folderGridItemEntries,
        key3 = isVisibleFolderGridItems,
    ) {
        isVisibleFolderGridItems && folderGridItemEntries.any { it.id == gridItem.id }
    }

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = currentGridItemSettings.horizontalAlignment)
    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = currentGridItemSettings.verticalArrangement)
    val horizontalArrangement =
        getHorizontalArrangement(horizontalArrangement = currentGridItemSettings.horizontalArrangement)
    val verticalAlignment =
        getVerticalAlignment(verticalAlignment = currentGridItemSettings.verticalAlignment)

    val maxLines = if (currentGridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    LaunchedEffect(
        key1 = drag,
        key2 = hasInteraction,
        key3 = showGridItemMenu,
    ) {
        if (drag == Drag.Dragging &&
            hasInteraction &&
            showGridItemMenu
        ) {
            onDragGridItem()
        }
    }

    when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo -> {
            InteractiveApplicationInfoGridItem(
                // Item Data
                gridItem = gridItem,
                data = data,
                gridItemSettings = currentGridItemSettings,
                // State
                isScrollInProgress = isScrollInProgress,
                hasInteraction = hasInteraction,
                animations = animations,
                statusBarNotifications = statusBarNotifications,
                iconPackFilePaths = iconPackFilePaths,
                // Appearance
                textColor = currentTextColor,
                maxLines = maxLines,
                isVisibleFolders = isVisibleFolderGridItems,
                isVisibleOverlay = isVisibleOverlay,
                isVisibleWhiteBox = isVisibleWhiteBox,
                // Shared Transition
                sharedTransitionScope = sharedTransitionScope,
                sharedElementKey = sharedElementKey,
                sourceBounds = sourceBounds,
                // Layout
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                modifier = modifier,
                // Callbacks
                onOpenAppDrawer = onOpenAppDrawer,
                onLongPressGridItem = onLongPressGridItem
            )
        }

        is GridItemData.Widget -> {
            InteractiveWidgetGridItem(
                // Data
                data = data,
                gridItem = gridItem,
                // Transition
                sharedElementKey = sharedElementKey,
                sharedTransitionScope = sharedTransitionScope,
                // State
                hasInteraction = hasInteraction,
                isScrollInProgress = isScrollInProgress,
                // Appearance
                textColor = currentTextColor,
                isVisibleOverlay = isVisibleOverlay,
                isVisibleWhiteBox = isVisibleWhiteBox,
                animations = animations,
                modifier = modifier,
                // Callback
                onLongPressGridItem = onLongPressGridItem
            )
        }

        is GridItemData.ShortcutInfo -> {
            InteractiveShortcutInfoGridItem(
                // Data
                data = data,
                gridItem = gridItem,
                gridItemSettings = currentGridItemSettings,
                // State
                hasShortcutHostPermission = hasShortcutHostPermission,
                isScrollInProgress = isScrollInProgress,
                isVisibleFolders = isVisibleFolderGridItems,
                isVisibleOverlay = isVisibleOverlay,
                hasInteraction = hasInteraction,
                isVisibleWhiteBox = isVisibleWhiteBox,
                animations = animations,
                // Appearance
                textColor = currentTextColor,
                sourceBounds = sourceBounds,
                // Layout
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                maxLines = maxLines,
                // Animation
                sharedTransitionScope = sharedTransitionScope,
                sharedElementKey = sharedElementKey,
                // Modifier
                modifier = modifier,
                // Callbacks
                onOpenAppDrawer = onOpenAppDrawer,
                onLongPressGridItem = onLongPressGridItem
            )
        }

        is GridItemData.Folder -> {
            InteractiveFolderGridItem(
                // Item Data
                data = data,
                gridItem = gridItem,
                // Transition
                sharedTransitionScope = sharedTransitionScope,
                sharedElementKey = sharedElementKey,
                // Configuration
                iconPackFilePaths = iconPackFilePaths,
                folderPopups = folderPopups,
                gridItemSettings = currentGridItemSettings,
                // State
                isScrollInProgress = isScrollInProgress,
                isVisibleFolder = isVisibleFolder,
                isVisibleOverlay = isVisibleOverlay,
                isDragging = isDragging,
                hasInteraction = hasInteraction,
                isVisibleWhiteBox = isVisibleWhiteBox,
                hasShortcutHostPermission = hasShortcutHostPermission,
                animations = animations,
                moveGridItemResult = moveGridItemResult,
                drag = drag,
                lockMovement = lockMovement,
                // Appearance
                textColor = currentTextColor,
                folderCornerRadius = folderCornerRadius,
                folderBackgroundColor = folderBackgroundColor,
                customFolderBackgroundColor = customFolderBackgroundColor,
                systemTextColor = textColor,
                systemCustomTextColor = systemCustomTextColor,
                maxLines = maxLines,
                // Layout
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                modifier = modifier,
                // Callbacks
                onOpenAppDrawer = onOpenAppDrawer,
                onShowFolderWhenDragging = onShowFolderWhenDragging,
                onResetGrid = onResetGrid,
                onLongPressGridItem = onLongPressGridItem,
                onTapFolderGridItem = onTapFolderGridItem
            )
        }

        is GridItemData.ShortcutConfig -> {
            InteractiveShortcutConfigGridItem(
                // Grid Item Data
                gridItem = gridItem,
                data = data,
                gridItemSettings = currentGridItemSettings,
                // State Flags
                isScrollInProgress = isScrollInProgress,
                isVisibleFolders = isVisibleFolderGridItems,
                isVisibleOverlay = isVisibleOverlay,
                hasInteraction = hasInteraction,
                isVisibleWhiteBox = isVisibleWhiteBox,
                animations = animations,
                // Appearance and Styling
                textColor = currentTextColor,
                maxLines = maxLines,
                // Shared Transition
                sharedTransitionScope = sharedTransitionScope,
                sharedElementKey = sharedElementKey,
                // Layout and Alignment
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                // Modifier
                modifier = modifier,
                // Callbacks
                onOpenAppDrawer = onOpenAppDrawer,
                onLongPressGridItem = onLongPressGridItem
            )
        }
    }
}

@Composable
private fun InteractiveWidgetGridItem(
    // Data
    data: GridItemData.Widget,
    gridItem: GridItem,
    // Transition
    sharedElementKey: SharedElementKey,
    sharedTransitionScope: SharedTransitionScope,
    // State
    hasInteraction: Boolean,
    isScrollInProgress: Boolean,
    // Appearance
    textColor: Color,
    isVisibleOverlay: Boolean,
    isVisibleWhiteBox: Boolean,
    animations: Boolean,
    modifier: Modifier = Modifier,
    // Callback
    onLongPressGridItem: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit
) {
    val appWidgetHost = LocalAppWidgetHost.current
    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = data.appWidgetId)

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val alpha = if (hasInteraction) 0f else 1f

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }
    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val currentOnLongPressGridItem by rememberUpdatedState(onLongPressGridItem)

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
            .gridItemSharedElement(
                // Transition State
                sharedTransitionScope = sharedTransitionScope,
                sharedElementKey = sharedElementKey,
                // Visibility Control
                enabled = animations,
                visible = !isScrollInProgress && !hasInteraction
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
                                currentOnLongPressGridItem(
                                    gridItem,
                                    graphicsLayer.toImageBitmap(),
                                    intOffset,
                                    intSize,
                                    sharedElementKey,
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
                                    currentOnLongPressGridItem(
                                        gridItem,
                                        graphicsLayer.toImageBitmap(),
                                        intOffset,
                                        intSize,
                                        sharedElementKey,
                                    )
                                }
                            }
                        } else {
                            null
                        },
                    )
                },
            )
        }
    }
}

private fun getSourceBounds(
    // Grid Item
    gridItem: GridItem,
    // Dimensions
    cellWidth: Int,
    cellHeight: Int,
    // Offsets
    leftPadding: Int,
    topOffset: Int
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
