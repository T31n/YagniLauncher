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
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest.Builder
import coil3.request.addLastModifiedToFileCacheKey
import coil3.request.crossfade
import coil3.size.Size
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.folder.FolderPopupEntry
import com.eblan.launcher.domain.model.folder.PreviewFolderEblanApplicationInfo
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.PreviewFolderGridLayout
import com.eblan.launcher.feature.home.component.gridItemScaleAnimation
import com.eblan.launcher.feature.home.component.gridItemSharedElement
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.feature.home.util.handleOnPress
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.ui.local.LocalLauncherApps
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun InteractiveFolderEblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    previewFolderEblanApplicationInfos: Map<String, PreviewFolderEblanApplicationInfo>,
    appDrawerSettings: AppDrawerSettings,
    folderCornerRadius: Int,
    isVisibleOverlay: Boolean,
    isScrollInProgress: Boolean,
    isInProgress: Boolean,
    paddingValues: PaddingValues,
    animations: Boolean,
    moveGridItemResult: MoveGridItemResult?,
    onUpdateIsVisibleFolders: (Boolean) -> Unit,
    onUpsertFolderEblanApplicationInfoPopupEntry: (FolderPopupEntry) -> Unit,
) {
    val isSelected =
        moveGridItemResult != null && moveGridItemResult.movingGridItem.id == folderEblanApplicationInfoGridItem.id

    val textColor = getTextColorFromBackgroundColor(
        backgroundColor = appDrawerSettings.backgroundColor,
        customBackgroundColor = appDrawerSettings.customBackgroundColor,
        textColor = appDrawerSettings.gridItemSettings.textColor,
        customTextColor = appDrawerSettings.gridItemSettings.customTextColor,
        systemTextColor = systemTextColor,
        systemCustomTextColor = systemCustomTextColor,
    )

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = appDrawerSettings.gridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = appDrawerSettings.gridItemSettings.verticalArrangement)

    val maxLines = if (appDrawerSettings.gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    when (val data = folderEblanApplicationInfoGridItem.data) {
        is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> {
            InteractiveEblanApplicationInfoItem(
                modifier = modifier,
                sharedTransitionScope = sharedTransitionScope,
                data = data,
                folderEblanApplicationInfoGridItem = folderEblanApplicationInfoGridItem,
                appDrawerSettings = appDrawerSettings,
                isVisibleOverlay = isVisibleOverlay,
                isScrollInProgress = isScrollInProgress,
                isInProgress = isInProgress,
                paddingValues = paddingValues,
                animations = animations,
                textColor = textColor,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
                isSelected = isSelected,
            )
        }

        is FolderEblanApplicationInfoGridItemData.Folder -> {
            InteractiveNestedFolderEblanApplicationInfoItem(
                modifier = modifier,
                sharedTransitionScope = sharedTransitionScope,
                appDrawerSettings = appDrawerSettings,
                customFolderBackgroundColor = customFolderBackgroundColor,
                data = data,
                folderBackgroundColor = folderBackgroundColor,
                folderCornerRadius = folderCornerRadius,
                folderEblanApplicationInfoGridItem = folderEblanApplicationInfoGridItem,
                isVisibleOverlay = isVisibleOverlay,
                isScrollInProgress = isScrollInProgress,
                isInProgress = isInProgress,
                animations = animations,
                previewFolderEblanApplicationInfos = previewFolderEblanApplicationInfos,
                systemCustomTextColor = systemCustomTextColor,
                systemTextColor = systemTextColor,
                textColor = textColor,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                isSelected = isSelected,
                onUpdateIsVisibleFolders = onUpdateIsVisibleFolders,
                onUpsertFolderEblanApplicationInfoPopupEntry = onUpsertFolderEblanApplicationInfoPopupEntry,
            )
        }
    }
}

@Composable
private fun InteractiveEblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    data: FolderEblanApplicationInfoGridItemData.ApplicationInfo,
    folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
    appDrawerSettings: AppDrawerSettings,
    isVisibleOverlay: Boolean,
    isScrollInProgress: Boolean,
    isInProgress: Boolean,
    animations: Boolean,
    paddingValues: PaddingValues,
    textColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
    isSelected: Boolean,
) {
    val context = LocalContext.current

    val density = LocalDensity.current

    val launcherApps = LocalLauncherApps.current

    val layoutDirection = LocalLayoutDirection.current

    val scope = rememberCoroutineScope()

    val icon = data.icon

    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    var isLongPress by remember { mutableStateOf(false) }

    val alpha = if (isLongPress) 0f else 1f

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val scale = remember { Animatable(1f) }

    val graphicsLayer = rememberGraphicsLayer()

    val sharedElementKey = SharedElementKey(
        id = folderEblanApplicationInfoGridItem.id,
        parent = SharedElementKey.Parent.Folder,
    )

    val hasInteraction = isSelected && isVisibleOverlay

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
                            scope.launch {
                                handleOnTapEblanApplicationInfoItem(
                                    componentName = data.componentName,
                                    serialNumber = data.serialNumber,
                                    intOffset = intOffset,
                                    intSize = intSize,
                                    launcherApps = launcherApps,
                                    leftPadding = leftPadding,
                                    topPadding = topPadding,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onLongPress = if (!isVisibleOverlay) {
                        {
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
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        AsyncImage(
            model = Builder(context)
                .data(icon)
                .addLastModifiedToFileCacheKey(true)
                .crossfade(false)
                .build(),
            contentDescription = null,
            modifier = Modifier
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
                    visible = !isScrollInProgress && !hasInteraction && !isInProgress,
                )
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }

                    drawLayer(graphicsLayer)
                },
        )

        if (appDrawerSettings.gridItemSettings.showLabel) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.alpha(alpha),
                text = data.label,
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
private fun InteractiveNestedFolderEblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    appDrawerSettings: AppDrawerSettings,
    customFolderBackgroundColor: Int,
    data: FolderEblanApplicationInfoGridItemData.Folder,
    folderBackgroundColor: BackgroundColor,
    folderCornerRadius: Int,
    folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
    isVisibleOverlay: Boolean,
    isScrollInProgress: Boolean,
    isInProgress: Boolean,
    animations: Boolean,
    previewFolderEblanApplicationInfos: Map<String, PreviewFolderEblanApplicationInfo>,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
    textColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    isSelected: Boolean,
    onUpdateIsVisibleFolders: (Boolean) -> Unit,
    onUpsertFolderEblanApplicationInfoPopupEntry: (FolderPopupEntry) -> Unit,
) {
    val maxLines = if (appDrawerSettings.gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val icon = data.icon

    var isLongPress by remember { mutableStateOf(false) }

    val alpha = if (isLongPress) 0f else 1f

    val scale = remember { Animatable(1f) }

    val graphicsLayer = rememberGraphicsLayer()

    val sharedElementKey = SharedElementKey(
        id = folderEblanApplicationInfoGridItem.id,
        parent = SharedElementKey.Parent.Folder,
    )

    val hasInteraction = isSelected && isVisibleOverlay

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

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
                                    id = folderEblanApplicationInfoGridItem.id,
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
                visible = !isScrollInProgress && !hasInteraction && !isInProgress,
            )
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }

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
                    gridItems = previewFolderEblanApplicationInfos[folderEblanApplicationInfoGridItem.id]?.previewFolderGridItems,
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
                text = data.label,
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

private fun handleOnTapEblanApplicationInfoItem(
    serialNumber: Long,
    componentName: String,
    intOffset: IntOffset,
    intSize: IntSize,
    launcherApps: AndroidLauncherAppsWrapper,
    leftPadding: Int,
    topPadding: Int,
) {
    val left = intOffset.x + leftPadding

    val top = intOffset.y + topPadding

    launcherApps.startMainActivity(
        serialNumber = serialNumber,
        componentName = componentName,
        sourceBounds = Rect(
            left,
            top,
            left + intSize.width,
            top + intSize.height,
        ),
    )
}
