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
package com.eblan.launcher.feature.home.screen.application

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.GridItemLayoutType
import com.eblan.launcher.feature.home.component.PreviewFolderGridLayout
import com.eblan.launcher.feature.home.component.gridItemSharedElement
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.getBackgroundColor
import com.eblan.launcher.feature.home.util.getFolderBorderColor
import com.eblan.launcher.feature.home.util.getGridItemBackgroundColor
import com.eblan.launcher.feature.home.util.getGridItemTextColorFromBackgroundColor
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getHorizontalArrangement
import com.eblan.launcher.feature.home.util.getIconPath
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor
import com.eblan.launcher.feature.home.util.getVerticalAlignment
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Composable
internal fun FolderEblanApplicationInfoItem(
    // Core Data
    folderEblanApplicationInfo: FolderEblanApplicationInfo,
    gridItemSettings: GridItemSettings,
    previewFolderEblanApplicationInfoGridItems: List<FolderEblanApplicationInfoGridItem>?,
    iconPackFilePaths: Map<String, String?>,
    // Animation and Transition
    sharedTransitionScope: SharedTransitionScope,
    animations: Boolean,
    // Visibility and State
    isVisibleOverlay: Boolean,
    isVisibleFolder: Boolean,
    isScrollInProgress: Boolean,
    isSwiping: Boolean,
    // Styling and Appearance
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    folderBorderColor: BackgroundColor,
    customFolderBorderColor: Int,
    folderBorderWidth: Int,
    // Interaction
    drag: Drag,
    modifier: Modifier = Modifier,
    // Callbacks
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onDragFolderEblanApplicationInfo: (
        folderId: String,
        movingGridItem: GridItem,
    ) -> Unit,
    onLongPressFolderApplicationInfo: (
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
    onTapFolderApplicationInfo: (folderEntry: FolderEntry) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }
    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val textColor = getGridItemTextColorFromBackgroundColor(
        // Container Background Color
        backgroundColor = backgroundColor,
        customBackgroundColor = customBackgroundColor,
        // Grid Item Colors
        gridItemTextColor = gridItemSettings.textColor,
        gridItemCustomTextColor = gridItemSettings.customTextColor,
        gridItemBackgroundColor = gridItemSettings.backgroundColor,
        gridItemCustomBackgroundColor = gridItemSettings.customBackgroundColor,
        // Text Color
        systemTextColor = systemTextColor,
        systemCustomTextColor = systemCustomTextColor,
    )

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val horizontalAlignment =
        getHorizontalAlignment(gridItemSettings.horizontalAlignment)
    val verticalArrangement =
        getVerticalArrangement(gridItemSettings.verticalArrangement)
    val horizontalArrangement =
        getHorizontalArrangement(gridItemSettings.horizontalArrangement)
    val verticalAlignment =
        getVerticalAlignment(gridItemSettings.verticalAlignment)

    var isLongPress by remember { mutableStateOf(false) }

    val textAlpha = if (isLongPress) 0f else 1f
    val iconAlpha = if (isLongPress || isVisibleFolder) 0f else 1f

    val sharedElementKey = SharedElementKey(
        id = folderEblanApplicationInfo.id,
        parent = SharedElementKey.Parent.SwipeY,
    )

    val currentOnTapFolderApplicationInfo by rememberUpdatedState(onTapFolderApplicationInfo)
    val currentOnLongPressFolderApplicationInfo by rememberUpdatedState(
        onLongPressFolderApplicationInfo,
    )

    val icon = folderEblanApplicationInfo.icon

    LaunchedEffect(
        key1 = drag,
        key2 = isLongPress,
    ) {
        handleDragFolderEblanApplicationInfoItem(
            // Item Data
            folderEblanApplicationInfo = folderEblanApplicationInfo,
            gridItemSettings = gridItemSettings,
            // Drag State
            drag = drag,
            isLongPress = isLongPress,
            isSwiping = isSwiping,
            // Callbacks
            onUpdateIsLongPress = { currentIsLongPress ->
                isLongPress = currentIsLongPress
            },
            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
            onDragFolderEblanApplicationInfo = onDragFolderEblanApplicationInfo,
        )
    }

    val itemModifier = modifier
        .padding(gridItemSettings.padding.dp)
        .background(
            color = getGridItemBackgroundColor(
                backgroundColor = gridItemSettings.backgroundColor,
                customBackgroundColor = gridItemSettings.customBackgroundColor,
            ),
            shape = RoundedCornerShape(gridItemSettings.cornerRadius.dp),
        )
        .run {
            if (!isVisibleOverlay) {
                pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            currentOnTapFolderApplicationInfo(
                                FolderEntry(
                                    id = folderEblanApplicationInfo.id,
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
                                isLongPress = true

                                keyboardController?.hide()

                                currentOnLongPressFolderApplicationInfo(
                                    folderEblanApplicationInfo,
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
        }

    val iconModifier = Modifier
        .onGloballyPositioned { currentLayoutCoordinates ->
            intOffset = currentLayoutCoordinates.positionInRoot().round()
            intSize = currentLayoutCoordinates.size
        }
        .gridItemSharedElement(
            // Transition State
            sharedTransitionScope = sharedTransitionScope,
            sharedElementKey = sharedElementKey,
            // Visibility Control
            enabled = animations,
            visible = !isSwiping &&
                !isScrollInProgress &&
                !isLongPress &&
                !isVisibleOverlay,
        )
        .drawWithContent {
            graphicsLayer.record {
                this@drawWithContent.drawContent()
            }

            drawLayer(graphicsLayer)
        }

    val iconContent: @Composable () -> Unit = {
        val commonModifier = Modifier
            .size(gridItemSettings.iconSize.dp)
            .padding(gridItemSettings.iconPadding.dp)
            .then(iconModifier)
            .alpha(iconAlpha)

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
                border = BorderStroke(
                    width = folderBorderWidth.dp,
                    color = getFolderBorderColor(
                        folderBorderColor = folderBorderColor,
                        customFolderBorderColor = customFolderBorderColor,
                    ),
                ),
                color = getBackgroundColor(
                    backgroundColor = folderBackgroundColor,
                    customBackgroundColor = customFolderBackgroundColor,
                    defaultColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                PreviewFolderGridLayout(
                    // Data
                    gridItems = previewFolderEblanApplicationInfoGridItems,
                    // Layout
                    modifier = Modifier.fillMaxSize(),
                    // Callbacks
                    slotId = { currentFolderEblanApplicationInfoGridItem ->
                        currentFolderEblanApplicationInfoGridItem.id
                    },
                    content = { currentFolderEblanApplicationInfoGridItem ->
                        PreviewFolderEblanApplicationInfoItem(
                            // Item Data
                            folderEblanApplicationInfoGridItem = currentFolderEblanApplicationInfoGridItem,
                            // Item Settings
                            gridItemTextColor = gridItemSettings.textColor,
                            gridItemCustomTextColor = gridItemSettings.customTextColor,
                            iconPackFilePaths = iconPackFilePaths,
                            // Folder Colors
                            folderBackgroundColor = folderBackgroundColor,
                            customFolderBackgroundColor = customFolderBackgroundColor,
                            // Text Colors
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                        )
                    },
                )
            }
        }
    }

    val labelContent: @Composable () -> Unit = {
        Text(
            modifier = Modifier
                .padding(gridItemSettings.textPadding.dp)
                .alpha(textAlpha),
            text = folderEblanApplicationInfo.label,
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
                .alpha(textAlpha),
            text = folderEblanApplicationInfo.label,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            fontSize = gridItemSettings.textSize.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }

    GridItemLayoutType(
        // Layout and Modifier
        layoutType = gridItemSettings.layoutType,
        modifier = itemModifier,
        // Alignment and Arrangement
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        // Content
        iconContent = iconContent,
        labelContent = labelContent,
        labelOnlyContent = labelOnlyContent,
    )
}

@Composable
private fun PreviewFolderEblanApplicationInfoItem(
    // Item Data
    folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
    // Item Settings
    gridItemTextColor: TextColor,
    gridItemCustomTextColor: Int,
    iconPackFilePaths: Map<String, String?>,
    // Folder Colors
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    // Text Colors
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // Layout
    modifier: Modifier = Modifier,
) {
    key(folderEblanApplicationInfoGridItem.id) {
        val context = LocalContext.current

        val folderIconTint = getTextColorFromBackgroundColor(
            // Background Color
            backgroundColor = folderBackgroundColor,
            customBackgroundColor = customFolderBackgroundColor,
            // Text Color
            textColor = gridItemTextColor,
            customTextColor = gridItemCustomTextColor,
            systemCustomTextColor = systemCustomTextColor,
            systemTextColor = systemTextColor,
            // Default Color
            defaultColor = MaterialTheme.colorScheme.onSurface,
        )

        val commonModifier = modifier.padding(1.dp)

        when (val data = folderEblanApplicationInfoGridItem.data) {
            is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> {
                val iconPath = getIconPath(
                    iconPackFilePath = iconPackFilePaths[data.componentName],
                    customIcon = data.customIcon,
                    icon = data.icon,
                )

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

            is FolderEblanApplicationInfoGridItemData.Folder -> {
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
        }
    }
}

private fun handleDragFolderEblanApplicationInfoItem(
    // Item Data
    folderEblanApplicationInfo: FolderEblanApplicationInfo,
    gridItemSettings: GridItemSettings,
    // Drag State
    drag: Drag,
    isLongPress: Boolean,
    isSwiping: Boolean,
    // Callbacks
    onUpdateIsLongPress: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onDragFolderEblanApplicationInfo: (
        folderId: String,
        gridItem: GridItem,
    ) -> Unit,
) {
    if (!isLongPress) return

    when (drag) {
        Drag.Dragging -> {
            val eblanAction = EblanAction(
                eblanActionType = EblanActionType.None,
                serialNumber = 0L,
                componentName = "",
            )

            val gridItem = GridItem(
                id = Uuid.random().toHexString(),
                page = 0,
                startColumn = -1,
                startRow = -1,
                columnSpan = 1,
                rowSpan = 1,
                data = GridItemData.Folder(
                    label = folderEblanApplicationInfo.label,
                    icon = folderEblanApplicationInfo.icon,
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

            onDragFolderEblanApplicationInfo(
                folderEblanApplicationInfo.id,
                gridItem,
            )
        }

        Drag.Cancel, Drag.End -> {
            onUpdateIsLongPress(false)

            if (!isSwiping) {
                onUpdateIsVisibleOverlay(false)
            }
        }

        else -> Unit
    }
}
