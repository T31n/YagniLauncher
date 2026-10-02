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
import android.os.Build
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.LayoutType
import com.eblan.launcher.feature.home.component.IconOnly
import com.eblan.launcher.feature.home.component.LabelOnly
import com.eblan.launcher.feature.home.component.StartIconEndLabel
import com.eblan.launcher.feature.home.component.StartLabelEndIcon
import com.eblan.launcher.feature.home.component.TopIconBottomLabel
import com.eblan.launcher.feature.home.component.TopLabelBottomIcon
import com.eblan.launcher.feature.home.component.gridItemScaleAnimation
import com.eblan.launcher.feature.home.component.gridItemSharedElement
import com.eblan.launcher.feature.home.component.swipeGestures
import com.eblan.launcher.feature.home.component.whiteBox
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.handleOnPress
import com.eblan.launcher.feature.home.util.onDoubleTap
import com.eblan.launcher.ui.local.LocalLauncherApps
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun InteractiveShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    data: GridItemData.ShortcutInfo,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    isScrollInProgress: Boolean,
    isVisibleFolders: Boolean,
    isVisibleOverlay: Boolean,
    sharedElementKey: SharedElementKey,
    textColor: Color,
    hasInteraction: Boolean,
    isVisibleWhiteBox: Boolean,
    sourceBounds: Rect,
    animations: Boolean,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    maxLines: Int,
    onOpenAppDrawer: () -> Unit,
    onLongPressGridItem: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val androidLauncherAppsWrapper = LocalLauncherApps.current
    val context = LocalContext.current

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }
    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val alpha = if (hasInteraction) {
        0f
    } else if (hasShortcutHostPermission && data.isEnabled) {
        1f
    } else {
        0.3f
    }

    val currentOnOpenAppDrawer by rememberUpdatedState(onOpenAppDrawer)
    val currentOnLongPressGridItem by rememberUpdatedState(onLongPressGridItem)

    val itemModifier = modifier
        .fillMaxSize()
        .padding(gridItemSettings.padding.dp)
        .background(
            color = Color(gridItemSettings.customBackgroundColor),
            shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
        )
        .whiteBox(
            textColor = textColor,
            visible = isVisibleWhiteBox && !isVisibleFolders,
        )
        .pointerInput(key1 = isVisibleOverlay) {
            detectTapGestures(
                onDoubleTap = if (!isVisibleOverlay) {
                    {
                        onDoubleTap(
                            context = context,
                            doubleTap = gridItem.doubleTap,
                            launcherApps = androidLauncherAppsWrapper,
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
            graphicsLayer.apply {
                this.alpha = alpha
            }
            graphicsLayer.record {
                this@drawWithContent.drawContent()
            }
            drawLayer(graphicsLayer = graphicsLayer)
        }

    val iconContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .size(gridItemSettings.iconSize.dp)
                .padding(gridItemSettings.iconPadding.dp),
        ) {
            AsyncImage(
                model = Builder(context = context)
                    .data(data = data.customIcon ?: data.icon)
                    .addLastModifiedToFileCacheKey(true)
                    .size(Size.ORIGINAL)
                    .build(),
                modifier = Modifier
                    .matchParentSize()
                    .then(iconModifier),
                contentDescription = null,
            )

            AsyncImage(
                model = Builder(context = context)
                    .data(data.eblanApplicationInfoIcon)
                    .size(Size.ORIGINAL)
                    .build(),
                modifier = Modifier
                    .size((gridItemSettings.iconSize * 0.25).dp)
                    .alpha(alpha = alpha)
                    .align(Alignment.BottomEnd),
                contentDescription = null,
            )
        }
    }

    val labelContent: @Composable (Modifier) -> Unit = { modifier ->
        if (gridItemSettings.showLabel) {
            Text(
                modifier = modifier
                    .padding(gridItemSettings.textPadding.dp)
                    .alpha(alpha = alpha),
                text = data.customShortLabel ?: data.shortLabel,
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
