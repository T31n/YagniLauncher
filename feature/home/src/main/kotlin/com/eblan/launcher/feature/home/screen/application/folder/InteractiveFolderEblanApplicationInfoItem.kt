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

import android.graphics.Rect
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest.Builder
import coil3.request.addLastModifiedToFileCacheKey
import coil3.size.Size
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.MoveFolderEblanApplicationInfoGridItemResult
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.domain.usecase.util.FOLDER_PREVIEW_COLUMNS
import com.eblan.launcher.domain.usecase.util.FOLDER_PREVIEW_ROWS
import com.eblan.launcher.feature.home.component.PreviewFolderGridLayout
import com.eblan.launcher.feature.home.component.gridItemScaleAnimation
import com.eblan.launcher.feature.home.component.gridItemSharedElement
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.ui.local.LocalLauncherApps
import kotlinx.coroutines.launch

@Composable
internal fun InteractiveFolderEblanApplicationInfoItem(
    // Scope and Animation
    sharedTransitionScope: SharedTransitionScope,
    animations: Boolean,
    // Folder Item and Content
    folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    folderEblanApplicationInfoEntries: List<FolderEntry>,
    // Styling and Colors
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderCornerRadius: Int,
    // Layout and Settings
    appDrawerSettings: AppDrawerSettings,
    paddingValues: PaddingValues,
    iconPackFilePaths: Map<String, String?>,
    // State and Progress
    isVisibleOverlay: Boolean,
    isScrollInProgress: Boolean,
    isInProgress: Boolean,
    progress: Float,
    moveFolderEblanApplicationInfoGridItemResult: MoveFolderEblanApplicationInfoGridItemResult?,
    drag: Drag,
    showFolderEblanApplicationInfoGridItemMenu: Boolean,
    modifier: Modifier = Modifier,
    // Callbacks
    onLongPressFolderEblanApplicationInfoGridItem: (
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
    onDragFolderEblanApplicationInfoGridItem: () -> Unit,
    onTapFolderEblanApplicationInfoItem: (FolderEntry) -> Unit
) {
    val isSelected =
        moveFolderEblanApplicationInfoGridItemResult != null && moveFolderEblanApplicationInfoGridItemResult.folderEblanApplicationInfoGridItem.id == folderEblanApplicationInfoGridItem.id

    val textColor = getTextColorFromBackgroundColor(
        // Background Color
        backgroundColor = folderBackgroundColor,
        customBackgroundColor = customFolderBackgroundColor,
        // Text Color
        textColor = appDrawerSettings.gridItemSettings.textColor,
        customTextColor = appDrawerSettings.gridItemSettings.customTextColor,
        systemCustomTextColor = systemCustomTextColor,
        systemTextColor = systemTextColor
    )

    val padding = if (animations) {
        lerp(1.dp, appDrawerSettings.gridItemSettings.padding.dp, progress)
    } else {
        appDrawerSettings.gridItemSettings.padding.dp
    }

    val iconPadding = if (animations) {
        lerp(0.dp, appDrawerSettings.gridItemSettings.iconPadding.dp, progress)
    } else {
        appDrawerSettings.gridItemSettings.iconPadding.dp
    }

    val textPadding = if (animations) {
        lerp(0.dp, appDrawerSettings.gridItemSettings.textPadding.dp, progress)
    } else {
        appDrawerSettings.gridItemSettings.textPadding.dp
    }

    val iconSize = if (animations) {
        lerp(
            appDrawerSettings.gridItemSettings.iconSize.dp / maxOf(
                FOLDER_PREVIEW_COLUMNS,
                FOLDER_PREVIEW_ROWS,
            ),
            appDrawerSettings.gridItemSettings.iconSize.dp,
            progress,
        )
    } else {
        appDrawerSettings.gridItemSettings.iconSize.dp
    }

    val hasInteraction = isSelected && isVisibleOverlay

    val isVisibleFolder = remember(
        key1 = folderEblanApplicationInfoGridItem,
        key2 = folderEblanApplicationInfoEntries,
    ) {
        folderEblanApplicationInfoEntries.any { it.id == folderEblanApplicationInfoGridItem.id }
    }

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = appDrawerSettings.gridItemSettings.horizontalAlignment)
    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = appDrawerSettings.gridItemSettings.verticalArrangement)

    val maxLines = if (appDrawerSettings.gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val sharedElementKey = SharedElementKey(
        id = folderEblanApplicationInfoGridItem.id,
        parent = SharedElementKey.Parent.Folder,
    )

    LaunchedEffect(
        key1 = drag,
        key2 = hasInteraction,
        key3 = showFolderEblanApplicationInfoGridItemMenu,
    ) {
        if (drag == Drag.Dragging && hasInteraction && showFolderEblanApplicationInfoGridItemMenu) {
            onDragFolderEblanApplicationInfoGridItem()
        }
    }

    when (val data = folderEblanApplicationInfoGridItem.data) {
        is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> {
            InteractiveEblanApplicationInfoItem(
                // Scope and Animation
                sharedTransitionScope = sharedTransitionScope,
                sharedElementKey = sharedElementKey,
                animations = animations,
                // Item and Data
                folderEblanApplicationInfoGridItem = folderEblanApplicationInfoGridItem,
                data = data,
                appDrawerSettings = appDrawerSettings,
                iconPackFilePaths = iconPackFilePaths,
                // Layout and Alignment
                paddingValues = paddingValues,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                padding = padding,
                iconSize = iconSize,
                iconPadding = iconPadding,
                textPadding = textPadding,
                // Appearance and Style
                textColor = textColor,
                maxLines = maxLines,
                // State
                isVisibleOverlay = isVisibleOverlay,
                isScrollInProgress = isScrollInProgress,
                isInProgress = isInProgress,
                hasInteraction = hasInteraction,
                // Modifier
                modifier = modifier,
                // Callbacks
                onLongPressFolderEblanApplicationInfoGridItem = onLongPressFolderEblanApplicationInfoGridItem
            )
        }

        is FolderEblanApplicationInfoGridItemData.Folder -> {
            InteractiveNestedFolderEblanApplicationInfoItem(
                // Data
                data = data,
                folderEblanApplicationInfoGridItem = folderEblanApplicationInfoGridItem,
                folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                appDrawerSettings = appDrawerSettings,
                iconPackFilePaths = iconPackFilePaths,
                // Animation
                sharedTransitionScope = sharedTransitionScope,
                sharedElementKey = sharedElementKey,
                animations = animations,
                // State
                isVisibleOverlay = isVisibleOverlay,
                isScrollInProgress = isScrollInProgress,
                isInProgress = isInProgress,
                hasInteraction = hasInteraction,
                isVisibleFolder = isVisibleFolder,
                // Appearance
                customFolderBackgroundColor = customFolderBackgroundColor,
                folderBackgroundColor = folderBackgroundColor,
                folderCornerRadius = folderCornerRadius,
                systemCustomTextColor = systemCustomTextColor,
                systemTextColor = systemTextColor,
                textColor = textColor,
                // Layout
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                padding = padding,
                iconSize = iconSize,
                iconPadding = iconPadding,
                textPadding = textPadding,
                modifier = modifier,
                // Callbacks
                onLongPressFolderEblanApplicationInfoGridItem = onLongPressFolderEblanApplicationInfoGridItem,
                onTapFolderEblanApplicationInfoItem = onTapFolderEblanApplicationInfoItem
            )
        }
    }
}

@Composable
private fun InteractiveEblanApplicationInfoItem(
    // Scope and Animation
    sharedTransitionScope: SharedTransitionScope,
    sharedElementKey: SharedElementKey,
    animations: Boolean,
    // Item and Data
    folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
    data: FolderEblanApplicationInfoGridItemData.ApplicationInfo,
    appDrawerSettings: AppDrawerSettings,
    iconPackFilePaths: Map<String, String?>,
    // Layout and Alignment
    paddingValues: PaddingValues,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    padding: Dp,
    iconSize: Dp,
    iconPadding: Dp,
    textPadding: Dp,
    // Appearance and Style
    textColor: Color,
    maxLines: Int,
    // State
    isVisibleOverlay: Boolean,
    isScrollInProgress: Boolean,
    isInProgress: Boolean,
    hasInteraction: Boolean,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onLongPressFolderEblanApplicationInfoGridItem: (
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val launcherApps = LocalLauncherApps.current
    val layoutDirection = LocalLayoutDirection.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()

    val iconPackFilePath = iconPackFilePaths[data.componentName]
    val icon = data.icon
    val customIcon = data.customIcon

    val iconPath = when {
        iconPackFilePath != null -> iconPackFilePath
        customIcon != null -> customIcon
        icon != null -> icon
        else -> null
    }

    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val alpha = if (hasInteraction) 0f else 1f

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }
    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val left = intOffset.x + leftPadding
    val top = intOffset.y + topPadding

    val sourceBounds = Rect(
        left,
        top,
        left + intSize.width,
        top + intSize.height,
    )

    val graphicsLayer = rememberGraphicsLayer()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                color = Color(appDrawerSettings.gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(
                    size = appDrawerSettings.gridItemSettings.cornerRadius.dp,
                ),
            )
            .run {
                if (!isVisibleOverlay && !isInProgress) {
                    pointerInput(key1 = Unit) {
                        detectTapGestures(
                            onTap = {
                                launcherApps.startMainActivity(
                                    serialNumber = data.serialNumber,
                                    componentName = data.componentName,
                                    sourceBounds = sourceBounds,
                                )
                            },
                            onLongPress = {
                                scope.launch {
                                    keyboardController?.hide()

                                    onLongPressFolderEblanApplicationInfoGridItem(
                                        folderEblanApplicationInfoGridItem,
                                        graphicsLayer.toImageBitmap(),
                                        intOffset,
                                        intSize,
                                        sharedElementKey,
                                    )
                                }
                            },
                        )
                    }
                } else {
                    this
                }
            },
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        AsyncImage(
            model = Builder(context)
                .data(iconPath)
                .addLastModifiedToFileCacheKey(true)
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
                .alpha(alpha),
        )

        Text(
            modifier = Modifier
                .padding(textPadding)
                .alpha(alpha),
            text = data.label,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            fontSize = appDrawerSettings.gridItemSettings.textSize.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun InteractiveNestedFolderEblanApplicationInfoItem(
    // Data
    data: FolderEblanApplicationInfoGridItemData.Folder,
    folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    appDrawerSettings: AppDrawerSettings,
    iconPackFilePaths: Map<String, String?>,
    // Animation
    sharedTransitionScope: SharedTransitionScope,
    sharedElementKey: SharedElementKey,
    animations: Boolean,
    // State
    isVisibleOverlay: Boolean,
    isScrollInProgress: Boolean,
    isInProgress: Boolean,
    hasInteraction: Boolean,
    isVisibleFolder: Boolean,
    // Appearance
    customFolderBackgroundColor: Int,
    folderBackgroundColor: BackgroundColor,
    folderCornerRadius: Int,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
    textColor: Color,
    // Layout
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    padding: Dp,
    iconSize: Dp,
    iconPadding: Dp,
    textPadding: Dp,
    modifier: Modifier = Modifier,
    // Callbacks
    onLongPressFolderEblanApplicationInfoGridItem: (
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
    onTapFolderEblanApplicationInfoItem: (FolderEntry) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()

    val maxLines = if (appDrawerSettings.gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val textAlpha = if (hasInteraction) 0f else 1f
    val iconAlpha = if (hasInteraction || isVisibleFolder) 0f else 1f

    val graphicsLayer = rememberGraphicsLayer()

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }
    var intSize by remember { mutableStateOf(IntSize.Zero) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                color = Color(appDrawerSettings.gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(
                    size = appDrawerSettings.gridItemSettings.cornerRadius.dp,
                ),
            )
            .run {
                if (!isVisibleOverlay && !isInProgress) {
                    pointerInput(key1 = Unit) {
                        detectTapGestures(
                            onTap = {
                                onTapFolderEblanApplicationInfoItem(
                                    FolderEntry(
                                        id = folderEblanApplicationInfoGridItem.id,
                                        x = intOffset.x,
                                        y = intOffset.y,
                                        width = intSize.width,
                                        height = intSize.height,
                                        isCloseFolder = false,
                                    ),
                                )
                            },
                            onLongPress = {
                                scope.launch {
                                    keyboardController?.hide()

                                    onLongPressFolderEblanApplicationInfoGridItem(
                                        folderEblanApplicationInfoGridItem,
                                        graphicsLayer.toImageBitmap(),
                                        intOffset,
                                        intSize,
                                        sharedElementKey,
                                    )
                                }
                            },
                        )
                    }
                } else {
                    this
                }
            },
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        val commonModifier =
            Modifier
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
                    gridItems = folderEblanApplicationInfoPopups[folderEblanApplicationInfoGridItem.id]?.previewFolderEblanApplicationInfoGridItems,
                    // Layout
                    modifier = Modifier.fillMaxSize(),
                    // Callbacks
                    slotId = { it.id },
                    content = {
                        PreviewFolderEblanApplicationInfoItem(
                            // Item Data
                            folderEblanApplicationInfoGridItem = it,
                            gridItemSettings = appDrawerSettings.gridItemSettings,
                            // Appearance
                            alpha = iconAlpha,
                            folderBackgroundColor = folderBackgroundColor,
                            customFolderBackgroundColor = customFolderBackgroundColor,
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                            // Icons
                            iconPackFilePaths = iconPackFilePaths
                        )
                    }
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
            fontSize = appDrawerSettings.gridItemSettings.textSize.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PreviewFolderEblanApplicationInfoItem(
    // Item Data
    folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
    gridItemSettings: GridItemSettings,
    // Appearance
    alpha: Float,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // Icons
    iconPackFilePaths: Map<String, String?>,
    // Layout
    modifier: Modifier = Modifier
) {
    key(folderEblanApplicationInfoGridItem.id) {
        val context = LocalContext.current

        val folderIconTint = getTextColorFromBackgroundColor(
            // Background Color
            backgroundColor = folderBackgroundColor,
            customBackgroundColor = customFolderBackgroundColor,
            // Text Color
            textColor = gridItemSettings.textColor,
            customTextColor = gridItemSettings.customTextColor,
            systemCustomTextColor = systemCustomTextColor,
            systemTextColor = systemTextColor,
            // Default Color
            defaultColor = MaterialTheme.colorScheme.onSurface
        )

        val commonModifier = modifier
            .fillMaxSize()
            .padding(1.dp)
            .alpha(alpha)

        when (val data = folderEblanApplicationInfoGridItem.data) {
            is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> {
                val iconPackFilePath = iconPackFilePaths[data.componentName]
                val icon = data.icon
                val customIcon = data.customIcon

                val iconPath = when {
                    iconPackFilePath != null -> iconPackFilePath
                    customIcon != null -> customIcon
                    icon != null -> icon
                    else -> null
                }

                AsyncImage(
                    model = Builder(context).data(iconPath)
                        .addLastModifiedToFileCacheKey(true).size(Size.ORIGINAL).build(),
                    contentDescription = null,
                    modifier = commonModifier,
                )
            }

            is FolderEblanApplicationInfoGridItemData.Folder -> {
                val icon = data.icon

                if (icon != null) {
                    AsyncImage(
                        model = Builder(context).data(icon).addLastModifiedToFileCacheKey(true)
                            .size(Size.ORIGINAL).build(),
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
        }
    }
}
