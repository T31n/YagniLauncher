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
package com.eblan.launcher.feature.home.screen.widget

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.application.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.widget.EblanAppWidgetProviderInfo
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.component.gridItemScaleAnimation
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.SCALE
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun AppWidgetScreen(
    modifier: Modifier = Modifier,
    columns: Int,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanApplicationInfoGroup: EblanApplicationInfoGroup?,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    rows: Int,
    screenHeight: Int,
    screenWidth: Int,
    swipeY: Float,
    animations: Boolean,
    isVisibleOverlay: Boolean,
    drag: Drag,
    onDismiss: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onDragAppWidget: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    requireNotNull(eblanApplicationInfoGroup)

    LaunchedEffect(
        key1 = isVisibleOverlay,
        key2 = drag,
    ) {
        if (isVisibleOverlay && (drag == Drag.Cancel || drag == Drag.End)) {
            onUpdateIsVisibleOverlay(false)
        }
    }

    BackHandler(enabled = swipeY < screenHeight.toFloat()) {
        onDismiss()
    }

    HomeHandler(enabled = swipeY < screenHeight.toFloat()) {
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset {
                IntOffset(x = 0, y = swipeY.roundToInt())
            }
            .pointerInput(key1 = Unit) {
                detectTapGestures(
                    onTap = {
                        onDismiss()
                    },
                )
            },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .animateContentSize()
                    .pointerInput(key1 = Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->
                                onVerticalDrag(dragAmount)
                            },
                            onDragEnd = {
                                onDragEnd()
                            },
                            onDragCancel = {
                                onDragEnd()
                            },
                        )
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    model = eblanApplicationInfoGroup.icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(text = eblanApplicationInfoGroup.label.toString())

                Spacer(modifier = Modifier.height(5.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    items(items = eblanAppWidgetProviderInfosGroup[eblanApplicationInfoGroup.packageName].orEmpty()) { eblanAppWidgetProviderInfo ->
                        EblanAppWidgetScreenItem(
                            columns = columns,
                            eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                            gridItemSettings = gridItemSettings,
                            rows = rows,
                            screenHeight = screenHeight,
                            screenWidth = screenWidth,
                            isVisibleOverlay = isVisibleOverlay,
                            animations = animations,
                            onDragAppWidget = onDragAppWidget,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun EblanAppWidgetScreenItem(
    modifier: Modifier = Modifier,
    columns: Int,
    eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo,
    gridItemSettings: GridItemSettings,
    rows: Int,
    screenHeight: Int,
    screenWidth: Int,
    isVisibleOverlay: Boolean,
    animations: Boolean,
    onDragAppWidget: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val scope = rememberCoroutineScope()

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val preview = eblanAppWidgetProviderInfo.preview ?: eblanAppWidgetProviderInfo.applicationIcon

    val graphicsLayer = rememberGraphicsLayer()

    val id = remember { Uuid.random().toHexString() }

    val scale = remember { Animatable(1f) }

    Column(
        modifier = modifier
            .size(200.dp)
            .padding(20.dp)
            .pointerInput(
                isVisibleOverlay,
                gridItemSettings,
                animations,
            ) {
                detectTapGestures(
                    onLongPress = {
                        scope.launch {
                            handleOnLongPressEblanAppWidgetScreenItem(
                                eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                                graphicsLayer = graphicsLayer,
                                gridItemSettings = gridItemSettings,
                                id = id,
                                intOffset = intOffset,
                                intSize = intSize,
                                scale = scale,
                                animations = animations,
                                onDragAppWidget = onDragAppWidget,
                            )
                        }
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val text =
            if (eblanAppWidgetProviderInfo.targetCellWidth > 0 && eblanAppWidgetProviderInfo.targetCellHeight > 0) {
                "${eblanAppWidgetProviderInfo.targetCellWidth}x${eblanAppWidgetProviderInfo.targetCellHeight}"
            } else if (eblanAppWidgetProviderInfo.minWidth > 0 && eblanAppWidgetProviderInfo.minHeight > 0) {
                val cellWidth = screenWidth / columns

                val cellHeight = screenHeight / rows

                val spanX = (eblanAppWidgetProviderInfo.minWidth + cellWidth - 1) / cellWidth

                val spanY = (eblanAppWidgetProviderInfo.minHeight + cellHeight - 1) / cellHeight

                "${spanX}x$spanY"
            } else {
                null
            }

        eblanAppWidgetProviderInfo.label?.let { label ->
            Text(
                text = label,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        eblanAppWidgetProviderInfo.description?.let { description ->
            Text(
                text = description,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (text != null) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        AsyncImage(
            modifier = Modifier
                .onGloballyPositioned {
                    intOffset = it.positionInRoot().round()

                    intSize = it.size
                }
                .gridItemScaleAnimation(
                    enabled = animations,
                    isVisibleOverlay = isVisibleOverlay,
                    scale = scale,
                )
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }

                    drawLayer(graphicsLayer)
                },
            model = preview,
            contentDescription = null,
        )
    }
}

private suspend fun handleOnLongPressEblanAppWidgetScreenItem(
    eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo,
    graphicsLayer: GraphicsLayer,
    gridItemSettings: GridItemSettings,
    id: String,
    intOffset: IntOffset,
    intSize: IntSize,
    scale: Animatable<Float, AnimationVector1D>,
    animations: Boolean,
    onDragAppWidget: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val gridItem = getWidgetGridItem(
        componentName = eblanAppWidgetProviderInfo.componentName,
        configure = eblanAppWidgetProviderInfo.configure,
        gridItemSettings = gridItemSettings,
        icon = eblanAppWidgetProviderInfo.applicationIcon,
        id = id,
        label = eblanAppWidgetProviderInfo.applicationLabel,
        maxResizeHeight = eblanAppWidgetProviderInfo.maxResizeHeight,
        maxResizeWidth = eblanAppWidgetProviderInfo.maxResizeWidth,
        minHeight = eblanAppWidgetProviderInfo.minHeight,
        minResizeHeight = eblanAppWidgetProviderInfo.minResizeHeight,
        minResizeWidth = eblanAppWidgetProviderInfo.minResizeWidth,
        minWidth = eblanAppWidgetProviderInfo.minWidth,
        packageName = eblanAppWidgetProviderInfo.packageName,
        page = 0,
        preview = eblanAppWidgetProviderInfo.preview,
        resizeMode = eblanAppWidgetProviderInfo.resizeMode,
        serialNumber = eblanAppWidgetProviderInfo.serialNumber,
        targetCellHeight = eblanAppWidgetProviderInfo.targetCellHeight,
        targetCellWidth = eblanAppWidgetProviderInfo.targetCellWidth,
    )

    if (animations) {
        scale.animateTo(SCALE)
    }

    onDragAppWidget(
        gridItem,
        graphicsLayer.toImageBitmap(),
        intOffset,
        intSize,
        SharedElementKey(
            id = id,
            parent = SharedElementKey.Parent.Grid,
        ),
    )
}
