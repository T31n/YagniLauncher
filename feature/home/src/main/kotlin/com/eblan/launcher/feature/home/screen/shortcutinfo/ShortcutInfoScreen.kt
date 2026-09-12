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
package com.eblan.launcher.feature.home.screen.shortcutinfo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfo
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.feature.home.component.gridItemScaleAnimation
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.SCALE
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
internal fun ShortcutInfoScreen(
    modifier: Modifier = Modifier,
    eblanShortcutInfosGroup: List<EblanShortcutInfo>,
    gridItemSettings: GridItemSettings,
    icon: String?,
    isVisibleOverlay: Boolean,
    animations: Boolean,
    onUpdateIsDragging: (Boolean) -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
    onUpdateTransitionState: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier
            .sizeIn(
                maxWidth = 300.dp,
                maxHeight = 150.dp,
            )
            .verticalScroll(rememberScrollState()),
    ) {
        eblanShortcutInfosGroup.forEach { eblanShortcutInfo ->
            ShortcutInfoMenuItem(
                eblanShortcutInfo = eblanShortcutInfo,
                gridItemSettings = gridItemSettings,
                icon = icon,
                isVisibleOverlay = isVisibleOverlay,
                animations = animations,
                onUpdateIsDragging = onUpdateIsDragging,
                onTapShortcutInfo = onTapShortcutInfo,
                onUpdateGridItemSource = onUpdateGridItemSource,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateOverlayBounds = onUpdateOverlayBounds,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                onUpdateTransitionState = onUpdateTransitionState,
            )
        }
    }
}

@Composable
internal fun PrivateShortcutInfoMenu(
    modifier: Modifier = Modifier,
    eblanShortcutInfosGroup: List<EblanShortcutInfo>,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
) {
    Column(
        modifier = modifier
            .sizeIn(
                maxWidth = 300.dp,
                maxHeight = 150.dp,
            )
            .verticalScroll(rememberScrollState()),
    ) {
        eblanShortcutInfosGroup.forEach { eblanShortcutInfo ->
            PrivateShortcutInfoMenuItem(
                eblanShortcutInfo = eblanShortcutInfo,
                onTapShortcutInfo = onTapShortcutInfo,
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun ShortcutInfoMenuItem(
    modifier: Modifier = Modifier,
    eblanShortcutInfo: EblanShortcutInfo,
    gridItemSettings: GridItemSettings,
    icon: String?,
    isVisibleOverlay: Boolean,
    animations: Boolean,
    onUpdateIsDragging: (Boolean) -> Unit,
    onTapShortcutInfo: (Long, String, String) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
    onUpdateTransitionState: (Boolean) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val scale = remember { Animatable(1f) }

    ListItem(
        modifier = modifier
            .clickable {
                onTapShortcutInfo(
                    eblanShortcutInfo.serialNumber,
                    eblanShortcutInfo.packageName,
                    eblanShortcutInfo.shortcutId,
                )
            },
        headlineContent = {
            Text(text = eblanShortcutInfo.shortLabel)
        },
        leadingContent = {
            AsyncImage(
                model = eblanShortcutInfo.icon,
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .onGloballyPositioned {
                        intOffset = it.positionInRoot().round()

                        intSize = it.size
                    }
                    .gridItemScaleAnimation(
                        isVisibleOverlay = isVisibleOverlay,
                        animations = animations,
                        scale = scale,
                    )
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }

                        drawLayer(graphicsLayer)
                    }
                    .pointerInput(
                        isVisibleOverlay,
                        gridItemSettings,
                        animations,
                    ) {
                        detectTapGestures(
                            onLongPress = {
                                scope.launch {
                                    handleOnLongPress(
                                        eblanShortcutInfo = eblanShortcutInfo,
                                        graphicsLayer = graphicsLayer,
                                        gridItemSettings = gridItemSettings,
                                        icon = icon,
                                        intOffset = intOffset,
                                        intSize = intSize,
                                        scale = scale,
                                        animations = animations,
                                        onUpdateGridItemSource = onUpdateGridItemSource,
                                        onUpdateImageBitmap = onUpdateImageBitmap,
                                        onUpdateIsDragging = onUpdateIsDragging,
                                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                        onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                                        onUpdateOverlayBounds = onUpdateOverlayBounds,
                                        onUpdateSharedElementKey = onUpdateSharedElementKey,
                                        onUpdateTransitionState = onUpdateTransitionState,
                                    )
                                }
                            },
                        )
                    },
            )
        },
    )
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun PrivateShortcutInfoMenuItem(
    modifier: Modifier = Modifier,
    eblanShortcutInfo: EblanShortcutInfo,
    onTapShortcutInfo: (Long, String, String) -> Unit,
) {
    ListItem(
        modifier = modifier
            .clickable {
                onTapShortcutInfo(
                    eblanShortcutInfo.serialNumber,
                    eblanShortcutInfo.packageName,
                    eblanShortcutInfo.shortcutId,
                )
            },
        headlineContent = {
            Text(text = eblanShortcutInfo.shortLabel)
        },
        leadingContent = {
            AsyncImage(
                model = eblanShortcutInfo.icon,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
            )
        },
    )
}

@OptIn(ExperimentalUuidApi::class)
private suspend fun handleOnLongPress(
    eblanShortcutInfo: EblanShortcutInfo,
    graphicsLayer: GraphicsLayer,
    gridItemSettings: GridItemSettings,
    icon: String?,
    intOffset: IntOffset,
    intSize: IntSize,
    scale: Animatable<Float, AnimationVector1D>,
    animations: Boolean,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateTransitionState: (Boolean) -> Unit,
) {
    val id = Uuid.random().toHexString()

    val gridItem = getShortcutInfoGridItem(
        eblanShortcutInfo = eblanShortcutInfo,
        gridItemSettings = gridItemSettings,
        icon = icon,
        id = id,
    )

    if (animations) {
        scale.animateTo(SCALE)
    }

    onUpdateGridItemSource(GridItemSource.New)

    onUpdateMoveGridItemResult(
        MoveGridItemResult(
            isSuccess = false,
            movingGridItem = gridItem,
            conflictingGridItem = null,
        ),
    )

    onUpdateImageBitmap(graphicsLayer.toImageBitmap())

    onUpdateOverlayBounds(
        intOffset,
        intSize,
    )

    onUpdateSharedElementKey(
        SharedElementKey(
            id = id,
            parent = SharedElementKey.Parent.Grid,
        ),
    )

    onUpdateIsVisibleOverlay(true)

    onUpdateIsDragging(true)

    onUpdateTransitionState(false)
}

private fun getShortcutInfoGridItem(
    eblanShortcutInfo: EblanShortcutInfo,
    gridItemSettings: GridItemSettings,
    icon: String?,
    id: String,
): GridItem {
    val data = GridItemData.ShortcutInfo(
        shortcutId = eblanShortcutInfo.shortcutId,
        packageName = eblanShortcutInfo.packageName,
        serialNumber = eblanShortcutInfo.serialNumber,
        shortLabel = eblanShortcutInfo.shortLabel,
        longLabel = eblanShortcutInfo.longLabel,
        icon = eblanShortcutInfo.icon,
        isEnabled = eblanShortcutInfo.isEnabled,
        eblanApplicationInfoIcon = icon,
        customIcon = null,
        customShortLabel = null,
        index = -1,
        folderId = null,
    )

    val eblanAction = EblanAction(
        eblanActionType = EblanActionType.None,
        serialNumber = 0L,
        componentName = "",
    )

    return GridItem(
        id = id,
        page = 0,
        startColumn = -1,
        startRow = -1,
        columnSpan = 1,
        rowSpan = 1,
        data = data,
        associate = Associate.Grid,
        override = false,
        gridItemSettings = gridItemSettings,
        doubleTap = eblanAction,
        swipeUp = eblanAction,
        swipeDown = eblanAction,
    )
}
