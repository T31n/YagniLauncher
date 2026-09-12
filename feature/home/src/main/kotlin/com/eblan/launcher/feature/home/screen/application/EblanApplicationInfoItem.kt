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

import android.graphics.Rect
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import coil3.request.crossfade
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.AppDrawerType
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.gridItemScaleAnimation
import com.eblan.launcher.feature.home.component.gridItemSharedElement
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.feature.home.util.handleOnPress
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.ui.local.LocalLauncherApps
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(
    ExperimentalUuidApi::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalLayoutApi::class,
)
@Composable
internal fun EblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    appDrawerSettings: AppDrawerSettings,
    drag: Drag,
    eblanApplicationInfo: EblanApplicationInfo,
    paddingValues: PaddingValues,
    isVisibleOverlay: Boolean,
    appDrawerType: AppDrawerType,
    isScrollInProgress: Boolean,
    isSwiping: Boolean,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    iconPackInfoFilePaths: Map<String, String?>,
    animations: Boolean,
    onDismiss: () -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdatePopupMenu: (Boolean) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
    onUpdatePopupBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val density = LocalDensity.current

    val launcherApps = LocalLauncherApps.current

    val layoutDirection = LocalLayoutDirection.current

    val keyboardController = LocalSoftwareKeyboardController.current

    val textColor = getTextColorFromBackgroundColor(
        backgroundColor = appDrawerSettings.backgroundColor,
        customBackgroundColor = appDrawerSettings.customBackgroundColor,
        textColor = appDrawerSettings.gridItemSettings.textColor,
        customTextColor = appDrawerSettings.gridItemSettings.customTextColor,
        systemTextColor = systemTextColor,
        systemCustomTextColor = systemCustomTextColor,
    )

    val maxLines = if (appDrawerSettings.gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val icon = iconPackInfoFilePaths[eblanApplicationInfo.componentName]
        ?: eblanApplicationInfo.icon

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = appDrawerSettings.gridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = appDrawerSettings.gridItemSettings.verticalArrangement)

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

    val iconSizePx = with(density) {
        appDrawerSettings.gridItemSettings.iconSize.dp.roundToPx()
    }

    val sharedElementKey = SharedElementKey(
        id = "${eblanApplicationInfo.serialNumber} ${eblanApplicationInfo.packageName} ${eblanApplicationInfo.componentName}",
        parent = SharedElementKey.Parent.SwipeY,
    )

    val scale = remember { Animatable(1f) }

    LaunchedEffect(
        key1 = drag,
        key2 = isLongPress,
    ) {
        handleDragEblanApplicationInfoItem(
            appDrawerSettings = appDrawerSettings,
            drag = drag,
            eblanApplicationInfo = eblanApplicationInfo,
            isLongPress = isLongPress,
            isSwiping = isSwiping,
            onDismiss = onDismiss,
            onUpdateGridItemSource = onUpdateGridItemSource,
            onUpdateIsDragging = onUpdateIsDragging,
            onUpdateIsLongPress = {
                isLongPress = it
            },
            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
            onUpdatePopupMenu = onUpdatePopupMenu,
            onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
        )
    }

    Column(
        modifier = modifier
            .run {
                if (appDrawerType == AppDrawerType.Vertical) {
                    height(appDrawerSettings.appDrawerRowsHeight.dp)
                } else {
                    fillMaxSize()
                }
            }
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
                                    componentName = eblanApplicationInfo.componentName,
                                    serialNumber = eblanApplicationInfo.serialNumber,
                                    intOffset = intOffset,
                                    intSize = intSize,
                                    keyboardController = keyboardController,
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
                            scope.launch {
                                handleOnLongPressEblanApplicationInfoItem(
                                    item = eblanApplicationInfo,
                                    graphicsLayer = graphicsLayer,
                                    intOffset = intOffset,
                                    intSize = intSize,
                                    keyboardController = keyboardController,
                                    sharedElementKey = sharedElementKey,
                                    onUpdate = onUpdateEblanApplicationInfo,
                                    onUpdateImageBitmap = onUpdateImageBitmap,
                                    onUpdateIsLongPress = { isLongPress = it },
                                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                    onUpdateOverlayBounds = onUpdateOverlayBounds,
                                    onUpdatePopupMenu = onUpdatePopupMenu,
                                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                                    onUpdatePopupBounds = onUpdatePopupBounds,
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
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(eblanApplicationInfo.customIcon ?: icon)
                .addLastModifiedToFileCacheKey(true)
                .size(iconSizePx)
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
                .alpha(alpha),
        )

        if (appDrawerSettings.gridItemSettings.showLabel) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.alpha(alpha),
                text = eblanApplicationInfo.customLabel
                    ?: eblanApplicationInfo.label,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                fontSize = appDrawerSettings.gridItemSettings.textSize.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

internal fun handleOnTapEblanApplicationInfoItem(
    serialNumber: Long,
    componentName: String,
    intOffset: IntOffset,
    intSize: IntSize,
    keyboardController: SoftwareKeyboardController?,
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

    keyboardController?.hide()
}

@OptIn(ExperimentalUuidApi::class)
internal fun handleDragEblanApplicationInfoItem(
    appDrawerSettings: AppDrawerSettings,
    drag: Drag,
    eblanApplicationInfo: EblanApplicationInfo,
    isLongPress: Boolean,
    isSwiping: Boolean,
    onDismiss: () -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsLongPress: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdatePopupMenu: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
) {
    if (!isLongPress) return

    when (drag) {
        Drag.Dragging -> {
            onUpdatePopupMenu(false)

            onDismiss()

            val pagerScreenId = Uuid.random().toHexString()

            val data = GridItemData.ApplicationInfo(
                serialNumber = eblanApplicationInfo.serialNumber,
                componentName = eblanApplicationInfo.componentName,
                packageName = eblanApplicationInfo.packageName,
                icon = eblanApplicationInfo.icon,
                label = eblanApplicationInfo.label,
                customIcon = eblanApplicationInfo.customIcon,
                customLabel = eblanApplicationInfo.customLabel,
                index = -1,
                folderId = null,
            )

            val eblanAction = EblanAction(
                eblanActionType = EblanActionType.None,
                serialNumber = 0L,
                componentName = "",
            )

            val gridItem = GridItem(
                id = pagerScreenId,
                page = 0,
                startColumn = -1,
                startRow = -1,
                columnSpan = 1,
                rowSpan = 1,
                data = data,
                associate = Associate.Grid,
                override = false,
                gridItemSettings = appDrawerSettings.gridItemSettings,
                doubleTap = eblanAction,
                swipeUp = eblanAction,
                swipeDown = eblanAction,
            )

            onUpdateGridItemSource(GridItemSource.New)

            onUpdateMoveGridItemResult(
                MoveGridItemResult(
                    isSuccess = false,
                    movingGridItem = gridItem,
                    conflictingGridItem = null,
                ),
            )

            onUpdateIsDragging(true)
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

@OptIn(ExperimentalUuidApi::class)
internal suspend fun <T> handleOnLongPressEblanApplicationInfoItem(
    item: T,
    graphicsLayer: GraphicsLayer,
    intOffset: IntOffset,
    intSize: IntSize,
    keyboardController: SoftwareKeyboardController?,
    sharedElementKey: SharedElementKey,
    onUpdate: (T) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateIsLongPress: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdatePopupMenu: (Boolean) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdatePopupBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
) {
    onUpdateImageBitmap(graphicsLayer.toImageBitmap())

    onUpdateOverlayBounds(
        intOffset,
        intSize,
    )

    onUpdatePopupBounds(
        intOffset,
        intSize,
    )

    onUpdateSharedElementKey(sharedElementKey)

    onUpdate(item)

    onUpdatePopupMenu(true)

    onUpdateIsLongPress(true)

    keyboardController?.hide()

    onUpdateIsVisibleOverlay(true)
}
