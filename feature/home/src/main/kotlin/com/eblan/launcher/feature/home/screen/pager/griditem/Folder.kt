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

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
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
import com.eblan.launcher.domain.model.folder.PreviewFolder
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.LayoutType
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.IconOnly
import com.eblan.launcher.feature.home.component.LabelOnly
import com.eblan.launcher.feature.home.component.PreviewFolderGridLayout
import com.eblan.launcher.feature.home.component.StartIconEndLabel
import com.eblan.launcher.feature.home.component.StartLabelEndIcon
import com.eblan.launcher.feature.home.component.TopIconBottomLabel
import com.eblan.launcher.feature.home.component.TopLabelBottomIcon
import com.eblan.launcher.feature.home.component.gridItemScaleAnimation
import com.eblan.launcher.feature.home.component.gridItemSharedElement
import com.eblan.launcher.feature.home.component.swipeGestures
import com.eblan.launcher.feature.home.component.whiteBox
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.pager.handleConflictingGridItem
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor
import com.eblan.launcher.feature.home.util.handleOnPress
import com.eblan.launcher.feature.home.util.onDoubleTap
import com.eblan.launcher.ui.local.LocalLauncherApps
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun InteractiveFolderGridItem(
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
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    maxLines: Int,
    systemCustomTextColor: Int,
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
) {
    val launcherApps = LocalLauncherApps.current
    val context = LocalContext.current

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }
    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }

    val currentDrag = rememberUpdatedState(drag)
    val currentIsDragging = rememberUpdatedState(isDragging)
    val currentIsVisibleOverlay = rememberUpdatedState(isVisibleOverlay)
    val currentGridItem = rememberUpdatedState(gridItem)
    val currentLockMovement = rememberUpdatedState(lockMovement)
    val currentFolderGridItems =
        rememberUpdatedState(previewFolderGridItems[gridItem.id]?.folderGridItems)
    val currentOnOpenAppDrawer by rememberUpdatedState(onOpenAppDrawer)
    val currentOnLongPressGridItem by rememberUpdatedState(onLongPressGridItem)
    val currentOnTapFolderGridItem by rememberUpdatedState(onTapFolderGridItem)
    val currentOnShowFolderWhenDragging by rememberUpdatedState(onShowFolderWhenDragging)

    val textAlpha = if (hasInteraction) 0f else 1f
    val iconAlpha = if (hasInteraction || isVisibleFolder) 0f else 1f

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
            onShowFolderWhenDragging = currentOnShowFolderWhenDragging,
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
                onTap = if (!isVisibleOverlay) {
                    {
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
        )

    val iconModifier = Modifier
        .onGloballyPositioned(
            onGloballyPositioned = {
                intOffset = it.positionInRoot().round()
                intSize = it.size
            },
        )
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
            drawLayer(graphicsLayer = graphicsLayer)
        }

    val iconContent: @Composable () -> Unit = {
        val commonModifier = Modifier
            .size(gridItemSettings.iconSize.dp)
            .padding(gridItemSettings.iconPadding.dp)
            .then(iconModifier)
            .alpha(alpha = iconAlpha)
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
                    BackgroundColor.Custom -> Color(color = customFolderBackgroundColor)
                },
            ) {
                PreviewFolderGridLayout(
                    modifier = Modifier.fillMaxSize(),
                    gridItems = previewFolderGridItems[gridItem.id]?.previewFolderGridItems,
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
    }

    val labelContent: @Composable (Modifier) -> Unit = { modifier ->
        if (gridItemSettings.showLabel) {
            Text(
                modifier = modifier
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
    }

    when (gridItemSettings.layoutType) {
        LayoutType.TopIconBottomLabel -> {
            TopIconBottomLabel(
                modifier = itemModifier,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                icon = iconContent,
                label = labelContent,
            )
        }

        LayoutType.TopLabelBottomIcon -> {
            TopLabelBottomIcon(
                modifier = itemModifier,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                icon = iconContent,
                label = labelContent,
            )
        }

        LayoutType.StartIconEndLabel -> {
            StartIconEndLabel(
                modifier = itemModifier,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                icon = iconContent,
                label = labelContent,
            )
        }

        LayoutType.StartLabelEndIcon -> {
            StartLabelEndIcon(
                modifier = itemModifier,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                icon = iconContent,
                label = labelContent,
            )
        }

        LayoutType.IconOnly -> {
            IconOnly(
                modifier = itemModifier,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                icon = iconContent,
            )
        }

        LayoutType.LabelOnly -> {
            LabelOnly(
                modifier = itemModifier,
                iconSize = gridItemSettings.iconSize.dp,
                iconPadding = gridItemSettings.iconPadding.dp,
                iconModifier = iconModifier,
                label = labelContent,
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
            .fillMaxSize()
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
