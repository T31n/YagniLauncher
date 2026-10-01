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
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
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
import com.eblan.launcher.domain.model.folder.PreviewFolder
import com.eblan.launcher.domain.model.grid.FolderGridItemPopup
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
import com.eblan.launcher.feature.home.util.SCALE
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getHorizontalArrangement
import com.eblan.launcher.feature.home.util.getVerticalAlignment
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.feature.home.util.handleOnPress
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
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
    isVisibleFolderGridItems: Boolean,
    moveGridItemResult: MoveGridItemResult?,
    lockMovement: Boolean,
    isDragging: Boolean,
    showGridItemMenu: Boolean,
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
    folderGridItemPopups: List<FolderGridItemPopup>,
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
    onDragGridItem: () -> Unit,
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
        key2 = folderGridItemPopups,
        key3 = isVisibleFolderGridItems,
    ) {
        isVisibleFolderGridItems && folderGridItemPopups.any { it.folderEntry.id == gridItem.id }
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
                modifier = modifier,
                sharedTransitionScope = sharedTransitionScope,
                data = data,
                gridItem = gridItem,
                gridItemSettings = currentGridItemSettings,
                isScrollInProgress = isScrollInProgress,
                isVisibleFolders = isVisibleFolderGridItems,
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
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                maxLines = maxLines,
                onOpenAppDrawer = onOpenAppDrawer,
                onLongPressGridItem = onLongPressGridItem,
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
                onLongPressGridItem = onLongPressGridItem,
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
                isVisibleFolders = isVisibleFolderGridItems,
                isVisibleOverlay = isVisibleOverlay,
                sharedElementKey = sharedElementKey,
                textColor = currentTextColor,
                hasInteraction = hasInteraction,
                isVisibleWhiteBox = isVisibleWhiteBox,
                sourceBounds = sourceBounds,
                animations = animations,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                maxLines = maxLines,
                onOpenAppDrawer = onOpenAppDrawer,
                onLongPressGridItem = onLongPressGridItem,
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
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                maxLines = maxLines,
                onOpenAppDrawer = onOpenAppDrawer,
                onShowFolderWhenDragging = onShowFolderWhenDragging,
                onResetGrid = onResetGrid,
                onLongPressGridItem = onLongPressGridItem,
                onTapFolderGridItem = onTapFolderGridItem,
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
                isVisibleFolders = isVisibleFolderGridItems,
                isVisibleOverlay = isVisibleOverlay,
                sharedElementKey = sharedElementKey,
                textColor = currentTextColor,
                hasInteraction = hasInteraction,
                isVisibleWhiteBox = isVisibleWhiteBox,
                animations = animations,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                maxLines = maxLines,
                onOpenAppDrawer = onOpenAppDrawer,
                onLongPressGridItem = onLongPressGridItem,
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
    onLongPressGridItem: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
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
            .gridItemScaleAnimation(
                enabled = animations,
                isVisibleOverlay = isVisibleOverlay,
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
