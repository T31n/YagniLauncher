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
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.eblan.launcher.domain.model.folder.FolderPopupEntry
import com.eblan.launcher.domain.model.folder.PreviewFolderEblanApplicationInfo
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.PreviewFolderGridLayout
import com.eblan.launcher.feature.home.component.gridItemScaleAnimation
import com.eblan.launcher.feature.home.component.gridItemSharedElement
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.feature.home.util.handleOnPress
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

@Composable
internal fun FolderEblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    previewFolderEblanApplicationInfo: PreviewFolderEblanApplicationInfo,
    appDrawerSettings: AppDrawerSettings,
    isVisibleOverlay: Boolean,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    animations: Boolean,
    isScrollInProgress: Boolean,
    isSwiping: Boolean,
    drag: Drag,
    onUpdateIsVisibleFolders: (Boolean) -> Unit,
    onUpsertFolderEblanApplicationInfoPopupEntry: (FolderPopupEntry) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateFolderPopupMenu: (Boolean) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateFolderPopupBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val textColor = getTextColorFromBackgroundColor(
        backgroundColor = appDrawerSettings.backgroundColor,
        customBackgroundColor = appDrawerSettings.customBackgroundColor,
        textColor = appDrawerSettings.gridItemSettings.textColor,
        customTextColor = appDrawerSettings.gridItemSettings.customTextColor,
        systemTextColor = systemTextColor,
        systemCustomTextColor = systemCustomTextColor,
    )

    val maxLines = if (appDrawerSettings.gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val icon = previewFolderEblanApplicationInfo.folderEblanApplicationInfo.icon

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = appDrawerSettings.gridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = appDrawerSettings.gridItemSettings.verticalArrangement)

    var isLongPress by remember { mutableStateOf(false) }

    val alpha = if (isLongPress) 0f else 1f

    val sharedElementKey = SharedElementKey(
        id = previewFolderEblanApplicationInfo.folderEblanApplicationInfo.id,
        parent = SharedElementKey.Parent.SwipeY,
    )

    val scale = remember { Animatable(1f) }

    LaunchedEffect(
        key1 = drag,
        key2 = isLongPress,
    ) {
        handleDragFolderEblanApplicationInfoItem(
            drag = drag,
            isLongPress = isLongPress,
            isSwiping = isSwiping,
            onUpdateIsLongPress = {
                isLongPress = it
            },
            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(appDrawerSettings.gridItemSettings.padding.dp)
            .background(
                color = Color(appDrawerSettings.gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(
                    size = appDrawerSettings.gridItemSettings.cornerRadius.dp,
                ),
            )
            .pointerInput(key1 = isVisibleOverlay) {
                detectTapGestures(
                    onTap = if (!isVisibleOverlay) {
                        {
                            onUpdateIsVisibleFolders(true)

                            onUpsertFolderEblanApplicationInfoPopupEntry(
                                FolderPopupEntry(
                                    id = previewFolderEblanApplicationInfo.folderEblanApplicationInfo.id,
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
                    onLongPress = if (!isVisibleOverlay) {
                        {
                            scope.launch {
                                handleOnLongPressEblanApplicationInfoItem(
                                    item = previewFolderEblanApplicationInfo.folderEblanApplicationInfo,
                                    graphicsLayer = graphicsLayer,
                                    intOffset = intOffset,
                                    intSize = intSize,
                                    keyboardController = keyboardController,
                                    sharedElementKey = sharedElementKey,
                                    onUpdate = onUpdateFolderEblanApplicationInfo,
                                    onUpdateImageBitmap = onUpdateImageBitmap,
                                    onUpdateIsLongPress = { isLongPress = it },
                                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                    onUpdateOverlayBounds = onUpdateOverlayBounds,
                                    onUpdatePopupMenu = onUpdateFolderPopupMenu,
                                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                                    onUpdatePopupBounds = onUpdateFolderPopupBounds,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onPress = {
                        handleOnPress(
                            animations = true,
                            scale = scale,
                        )
                    },
                )
            },
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        val commonModifier = Modifier
            .size(appDrawerSettings.gridItemSettings.iconSize.dp)
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
            .alpha(alpha)

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
                    BackgroundColor.Custom -> Color(customFolderBackgroundColor)
                },
            ) {
                PreviewFolderGridLayout(
                    modifier = Modifier.fillMaxSize(),
                    gridItems = previewFolderEblanApplicationInfo.previewFolderGridItems,
                    slotId = { it.id },
                    content = {
                        PreviewFolderEblanApplicationInfoItem(
                            folderEblanApplicationInfoGridItem = it,
                            gridItemSettings = appDrawerSettings.gridItemSettings,
                            folderBackgroundColor = folderBackgroundColor,
                            customFolderBackgroundColor = customFolderBackgroundColor,
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                        )
                    },
                )
            }
        }

        if (appDrawerSettings.gridItemSettings.showLabel) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.alpha(alpha),
                text = previewFolderEblanApplicationInfo.folderEblanApplicationInfo.label,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                fontSize = appDrawerSettings.gridItemSettings.textSize.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PreviewFolderEblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
    gridItemSettings: GridItemSettings,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
) {
    key(folderEblanApplicationInfoGridItem.id) {
        val context = LocalContext.current

        val folderIconTint = getTextColorFromBackgroundColor(
            backgroundColor = folderBackgroundColor,
            customBackgroundColor = customFolderBackgroundColor,
            textColor = gridItemSettings.textColor,
            customTextColor = gridItemSettings.customTextColor,
            systemTextColor = systemTextColor,
            systemCustomTextColor = systemCustomTextColor,
            defaultColor = MaterialTheme.colorScheme.onSurface,
        )

        val commonModifier = modifier.padding(1.dp)

        when (val data = folderEblanApplicationInfoGridItem.data) {
            is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> {
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

            is FolderEblanApplicationInfoGridItemData.Folder -> {
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
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
internal fun handleDragFolderEblanApplicationInfoItem(
    drag: Drag,
    isLongPress: Boolean,
    isSwiping: Boolean,
    onUpdateIsLongPress: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
) {
    if (!isLongPress) return

    when (drag) {
        Drag.Dragging -> {
            // TODO
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
