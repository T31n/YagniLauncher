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

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
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
import com.eblan.launcher.feature.home.component.GridItemLayoutType
import com.eblan.launcher.feature.home.component.PreviewFolderGridLayout
import com.eblan.launcher.feature.home.component.gridItemScaleAnimation
import com.eblan.launcher.feature.home.component.gridItemSharedElement
import com.eblan.launcher.feature.home.component.swipeGestures
import com.eblan.launcher.feature.home.component.whiteBox
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.pager.handleConflictingGridItem
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor
import com.eblan.launcher.feature.home.util.handleEblanAction
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalStatusBarManager
import kotlinx.coroutines.launch

@Composable
internal fun InteractiveFolderGridItem(
    // Data
    data: GridItemData.Folder,
    gridItem: GridItem,
    sharedTransitionScope: SharedTransitionScope,
    sharedElementKey: SharedElementKey,
    iconPackFilePaths: Map<String, String?>,
    folderPopups: Map<String, FolderPopup>,
    // State
    isScrollInProgress: Boolean,
    isVisibleFolder: Boolean,
    isVisibleOverlay: Boolean,
    isDragging: Boolean,
    hasInteraction: Boolean,
    isVisibleWhiteBox: Boolean,
    hasShortcutHostPermission: Boolean,
    animations: Boolean,
    moveGridItemResult: MoveGridItemResult?,
    // Interaction
    drag: Drag,
    lockMovement: Boolean,
    // Appearance
    gridItemSettings: GridItemSettings,
    textColor: Color,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    maxLines: Int,
    // Layout
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    // Modifier
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
    onTapFolderGridItem: (FolderEntry) -> Unit
) {
    val launcherApps = LocalLauncherApps.current
    val androidStatusBarManagerWrapper = LocalStatusBarManager.current
    val context = LocalContext.current

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }
    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val currentDrag = rememberUpdatedState(drag)
    val currentIsDragging = rememberUpdatedState(isDragging)
    val currentIsVisibleOverlay = rememberUpdatedState(isVisibleOverlay)
    val currentGridItem = rememberUpdatedState(gridItem)
    val currentLockMovement = rememberUpdatedState(lockMovement)
    val currentFolderGridItems =
        rememberUpdatedState(folderPopups[gridItem.id]?.folderGridItems)
    val currentOnOpenAppDrawer by rememberUpdatedState(onOpenAppDrawer)
    val currentOnLongPressGridItem by rememberUpdatedState(onLongPressGridItem)
    val currentOnTapFolderGridItem by rememberUpdatedState(onTapFolderGridItem)
    val currentOnShowFolderWhenDragging by rememberUpdatedState(onShowFolderWhenDragging)

    val textAlpha = if (hasInteraction) 0f else 1f
    val iconAlpha = if (hasInteraction || isVisibleFolder) 0f else 1f

    val icon = data.icon

    LaunchedEffect(key1 = moveGridItemResult) {
        handleConflictingGridItem(
            gridItem = currentGridItem,
            folderGridItems = currentFolderGridItems,
            drag = currentDrag,
            isDragging = currentIsDragging,
            isVisibleOverlay = currentIsVisibleOverlay,
            moveGridItemResult = moveGridItemResult,
            lockMovement = currentLockMovement,
            intOffset = intOffset,
            intSize = intSize,
            onShowFolderWhenDragging = currentOnShowFolderWhenDragging
        )
    }

    val itemModifier = modifier
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
        .run {
            if (!isVisibleOverlay) {
                pointerInput(key1 = Unit) {
                    detectTapGestures(
                        onDoubleTap = if (gridItem.doubleTap.eblanActionType != EblanActionType.None) {
                            {
                                handleEblanAction(
                                    context = context,
                                    eblanAction = gridItem.doubleTap,
                                    launcherApps = launcherApps,
                                    statusBarManagerWrapper = androidStatusBarManagerWrapper,
                                    onOpenAppDrawer = currentOnOpenAppDrawer
                                )
                            }
                        } else {
                            null
                        },
                        onLongPress = {
                            scope.launch {
                                currentOnLongPressGridItem(
                                    gridItem,
                                    graphicsLayer.toImageBitmap(),
                                    intOffset,
                                    intSize,
                                    sharedElementKey,
                                )
                            }
                        },
                        onTap = {
                            currentOnTapFolderGridItem(
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
            swipeDown = gridItem.swipeDown,
            swipeUp = gridItem.swipeUp,
            onOpenAppDrawer = onOpenAppDrawer,
        )

    val iconModifier = Modifier
        .onGloballyPositioned(
            onGloballyPositioned = {
                intOffset = it.positionInRoot().round()
                intSize = it.size
            },
        )
        .gridItemSharedElement(
            enabled = animations,
            sharedElementKey = sharedElementKey,
            sharedTransitionScope = sharedTransitionScope,
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
            drawLayer(graphicsLayer = graphicsLayer)
        }

    val iconContent: @Composable () -> Unit = {
        val commonModifier = Modifier
            .size(gridItemSettings.iconSize.dp)
            .padding(gridItemSettings.iconPadding.dp)
            .then(iconModifier)
            .alpha(alpha = iconAlpha)

        if (icon != null) {
            AsyncImage(
                model = icon,
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
                    BackgroundColor.Custom -> Color(color = customFolderBackgroundColor)
                },
            ) {
                PreviewFolderGridLayout(
                    gridItems = folderPopups[gridItem.id]?.previewFolderGridItems,
                    modifier = Modifier.fillMaxSize(),
                    slotId = { it.id },
                    content = {
                        PreviewFolderGridItem(
                            sharedTransitionScope = sharedTransitionScope,
                            gridItem = it,
                            isScrollInProgress = isScrollInProgress,
                            isVisibleOverlay = isVisibleOverlay,
                            parent = sharedElementKey.parent,
                            moveGridItemResult = moveGridItemResult,
                            drag = drag,
                            folderGridItems = folderPopups[gridItem.id]?.folderGridItems,
                            isVisibleFolders = isVisibleFolder,
                            hasShortcutHostPermission = hasShortcutHostPermission,
                            iconPackFilePaths = iconPackFilePaths,
                            gridItemSettings = gridItemSettings,
                            folderBackgroundColor = folderBackgroundColor,
                            customFolderBackgroundColor = customFolderBackgroundColor,
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                            animations = animations,
                            onResetGrid = onResetGrid,
                        )
                    }
                )
            }
        }
    }

    val labelContent: @Composable () -> Unit = {
        Text(
            modifier = Modifier
                .padding(gridItemSettings.textPadding.dp)
                .alpha(alpha = textAlpha),
            text = data.label,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            fontSize = gridItemSettings.textSize.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }

    val labelOnlyContent: @Composable () -> Unit = {
        Text(
            modifier = Modifier
                .padding(gridItemSettings.textPadding.dp)
                .then(iconModifier)
                .alpha(alpha = textAlpha),
            text = data.label,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            fontSize = gridItemSettings.textSize.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }

    GridItemLayoutType(
        gridItemSettings = gridItemSettings,
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        modifier = itemModifier,
        iconContent = iconContent,
        labelContent = labelContent,
        labelOnlyContent = labelOnlyContent
    )
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
    iconPackFilePaths: Map<String, String?>,
    animations: Boolean,
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
            systemCustomTextColor = systemCustomTextColor,
            systemTextColor = systemTextColor,
            defaultColor = MaterialTheme.colorScheme.onSurface
        )

        val commonModifier = modifier
            .fillMaxSize()
            .padding(1.dp)
            .gridItemSharedElement(
                enabled = animations,
                sharedElementKey = SharedElementKey(
                    id = gridItem.id,
                    parent = parent,
                ),
                sharedTransitionScope = sharedTransitionScope,
                visible = !isScrollInProgress && !hasInteraction,
            )
            .gridItemScaleAnimation(
                animations = animations,
                hasInteraction = hasInteraction,
            )
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
                val iconPackFilePath = iconPackFilePaths[data.componentName] ?: data.icon
                val icon = data.icon
                val customIcon = data.customIcon

                val iconPath = when {
                    iconPackFilePath != null -> iconPackFilePath
                    customIcon != null -> customIcon
                    icon != null -> icon
                    else -> null
                }

                AsyncImage(
                    model = Builder(context)
                        .data(iconPath)
                        .addLastModifiedToFileCacheKey(true)
                        .size(Size.ORIGINAL)
                        .build(),
                    contentDescription = null,
                    modifier = commonModifier,
                )
            }

            is GridItemData.ShortcutConfig -> {
                val customIcon = data.customIcon
                val shortcutIntentIcon = data.shortcutIntentIcon
                val activityIcon = data.activityIcon
                val applicationIcon = data.applicationIcon

                val iconPath = when {
                    customIcon != null -> customIcon
                    shortcutIntentIcon != null -> shortcutIntentIcon
                    activityIcon != null -> activityIcon
                    applicationIcon != null -> applicationIcon
                    else -> null
                }

                AsyncImage(
                    model = Builder(context)
                        .data(iconPath)
                        .addLastModifiedToFileCacheKey(true)
                        .size(Size.ORIGINAL)
                        .build(),
                    contentDescription = null,
                    modifier = commonModifier,
                )
            }

            is GridItemData.ShortcutInfo -> {
                val icon = data.icon
                val customIcon = data.customIcon

                val iconPath = when {
                    customIcon != null -> customIcon
                    icon != null -> icon
                    else -> null
                }

                AsyncImage(
                    model = Builder(context)
                        .data(iconPath)
                        .addLastModifiedToFileCacheKey(true)
                        .size(Size.ORIGINAL)
                        .build(),
                    contentDescription = null,
                    modifier = commonModifier,
                )
            }

            is GridItemData.Folder -> {
                val icon = data.icon

                if (icon != null) {
                    AsyncImage(
                        model = Builder(context)
                            .data(icon)
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

            else -> {
                Unit
            }
        }
    }
}
