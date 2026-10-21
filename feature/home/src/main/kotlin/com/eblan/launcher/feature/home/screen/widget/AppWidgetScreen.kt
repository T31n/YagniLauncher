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
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalLayoutDirection
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
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppWidgetScreen(
    // Content
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanApplicationInfoGroup: EblanApplicationInfoGroup?,
    // Layout
    columns: Int,
    rows: Int,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    screenWidth: Int,
    screenHeight: Int,
    // State
    swipeY: Float,
    isVisibleOverlay: Boolean,
    drag: Drag,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
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

    val layoutDirection = LocalLayoutDirection.current

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
                .padding(
                    top = paddingValues.calculateTopPadding(),
                )
                .clip(RoundedCornerShape(20.dp)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        end = paddingValues.calculateEndPadding(layoutDirection),
                        bottom = paddingValues.calculateBottomPadding(),
                    )
                    .animateContentSize()
                    .run {
                        if (!isVisibleOverlay) {
                            pointerInput(key1 = Unit) {
                                detectVerticalDragGestures(
                                    onVerticalDrag = { _, dragAmount ->
                                        onVerticalDrag(dragAmount)
                                    },
                                    onDragEnd = onDragEnd,
                                    onDragCancel = onDragEnd,
                                )
                            }
                        } else {
                            this
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DragHandle()

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
                            eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                            columns = columns,
                            rows = rows,
                            screenHeight = screenHeight,
                            screenWidth = screenWidth,
                            gridItemSettings = gridItemSettings,
                            isVisibleOverlay = isVisibleOverlay,
                            onDragAppWidget = onDragAppWidget,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun EblanAppWidgetScreenItem(
    // Widget Info
    eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo,
    // Dimensions
    columns: Int,
    rows: Int,
    screenHeight: Int,
    screenWidth: Int,
    // Settings
    gridItemSettings: GridItemSettings,
    isVisibleOverlay: Boolean,
    // Modifier
    modifier: Modifier = Modifier,
    // Callback
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

    Column(
        modifier = modifier
            .size(200.dp)
            .padding(20.dp)
            .pointerInput(
                key1 = isVisibleOverlay,
                key2 = gridItemSettings,
            ) {
                detectTapGestures(
                    onLongPress = {
                        scope.launch {
                            handleOnLongPressEblanAppWidgetScreenItem(
                                id = id,
                                eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                                gridItemSettings = gridItemSettings,
                                graphicsLayer = graphicsLayer,
                                intOffset = intOffset,
                                intSize = intSize,
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
    // Item Information
    id: String,
    eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo,
    gridItemSettings: GridItemSettings,
    // Layout and Rendering
    graphicsLayer: GraphicsLayer,
    intOffset: IntOffset,
    intSize: IntSize,
    // Callbacks
    onDragAppWidget: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val gridItem = getWidgetGridItem(
        id = id,
        serialNumber = eblanAppWidgetProviderInfo.serialNumber,
        packageName = eblanAppWidgetProviderInfo.packageName,
        componentName = eblanAppWidgetProviderInfo.componentName,
        label = eblanAppWidgetProviderInfo.applicationLabel,
        icon = eblanAppWidgetProviderInfo.applicationIcon,
        preview = eblanAppWidgetProviderInfo.preview,
        configure = eblanAppWidgetProviderInfo.configure,
        gridItemSettings = gridItemSettings,
        page = 0,
        minWidth = eblanAppWidgetProviderInfo.minWidth,
        minHeight = eblanAppWidgetProviderInfo.minHeight,
        targetCellWidth = eblanAppWidgetProviderInfo.targetCellWidth,
        targetCellHeight = eblanAppWidgetProviderInfo.targetCellHeight,
        resizeMode = eblanAppWidgetProviderInfo.resizeMode,
        maxResizeWidth = eblanAppWidgetProviderInfo.maxResizeWidth,
        maxResizeHeight = eblanAppWidgetProviderInfo.maxResizeHeight,
        minResizeWidth = eblanAppWidgetProviderInfo.minResizeWidth,
        minResizeHeight = eblanAppWidgetProviderInfo.minResizeHeight,
    )

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
