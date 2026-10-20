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

import android.content.Intent.parseUri
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.feature.home.component.GridItemLayoutType
import com.eblan.launcher.feature.home.component.gridItemScaleAnimation
import com.eblan.launcher.feature.home.component.gridItemSharedElement
import com.eblan.launcher.feature.home.component.swipeGestures
import com.eblan.launcher.feature.home.component.whiteBox
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.handleEblanAction
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalStatusBarManager
import kotlinx.coroutines.launch

@Composable
internal fun InteractiveShortcutConfigGridItem(
    // Data
    data: GridItemData.ShortcutConfig,
    gridItem: GridItem,
    // Settings and State
    gridItemSettings: GridItemSettings,
    isScrollInProgress: Boolean,
    isVisibleFolders: Boolean,
    isVisibleOverlay: Boolean,
    textColor: Color,
    hasInteraction: Boolean,
    isVisibleWhiteBox: Boolean,
    animations: Boolean,
    maxLines: Int,
    // Layout and Animation
    sharedTransitionScope: SharedTransitionScope,
    sharedElementKey: SharedElementKey,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    modifier: Modifier = Modifier,
    // Callbacks
    onOpenAppDrawer: () -> Unit,
    onLongPressGridItem: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit
) {
    val launcherApps = LocalLauncherApps.current
    val androidStatusBarManagerWrapper = LocalStatusBarManager.current
    val context = LocalContext.current

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }
    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val alpha = if (hasInteraction) 0f else 1f

    val currentOnOpenAppDrawer by rememberUpdatedState(onOpenAppDrawer)
    val currentOnLongPressGridItem by rememberUpdatedState(onLongPressGridItem)

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

    val label = if (data.customLabel != null) {
        data.customLabel
    } else if (data.shortcutIntentName != null) {
        data.shortcutIntentName
    } else if (data.activityLabel != null) {
        data.activityLabel
    } else {
        data.applicationLabel
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
            visible = isVisibleWhiteBox && !isVisibleFolders,
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
                            data.shortcutIntentUri?.let {
                                context.startActivity(parseUri(it, 0))
                            }
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
        AsyncImage(
            model = Builder(context = context)
                .data(iconPath)
                .addLastModifiedToFileCacheKey(true)
                .size(Size.ORIGINAL)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(gridItemSettings.iconSize.dp)
                .padding(gridItemSettings.iconPadding.dp)
                .then(iconModifier)
                .alpha(alpha = alpha),
        )
    }

    val labelContent: @Composable () -> Unit = {
        Text(
            modifier = Modifier
                .padding(gridItemSettings.textPadding.dp)
                .alpha(alpha = alpha),
            text = label.toString(),
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
                .alpha(alpha = alpha),
            text = label.toString(),
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
