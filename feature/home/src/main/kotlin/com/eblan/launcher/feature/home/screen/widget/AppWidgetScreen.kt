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
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.theme.EblanSpacing
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.widget.EblanAppWidgetProviderInfo
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.model.SharedElementKey
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppWidgetScreen(
    // Data
    eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>,
    icon: String?,
    label: String?,
    // Grid
    columns: Int,
    rows: Int,
    gridItemSettings: GridItemSettings,
    // Layout
    paddingValues: PaddingValues,
    screenWidth: Int,
    screenHeight: Int,
    modifier: Modifier = Modifier,
    // Interaction
    swipeY: Float,
    isVisibleOverlay: Boolean,
    // Callbacks
    onDismiss: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragAppWidget: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    val focusManager = LocalFocusManager.current

    val keyboardController = LocalSoftwareKeyboardController.current

    val scrimAlpha = (1f - (swipeY / screenHeight.toFloat())).coerceIn(0f, 1f) * 0.32f

    LaunchedEffect(key1 = label, key2 = icon) {
        focusManager.clearFocus()
        keyboardController?.hide()
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
            .background(Color.Black.copy(alpha = scrimAlpha))
            .run {
                if (!isVisibleOverlay) {
                    pointerInput(key1 = Unit) {
                        detectTapGestures(
                            onTap = {
                                onDismiss()
                            },
                        )
                    }
                } else {
                    this
                }
            },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(x = 0, y = swipeY.roundToInt())
                }
                .padding(
                    top = paddingValues.calculateTopPadding(),
                )
                .clip(MaterialTheme.shapes.large)
                .pointerInput(key1 = Unit) {
                    detectTapGestures {}
                },
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
                    model = icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(text = label.toString())

                Spacer(modifier = Modifier.height(5.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    items(items = eblanAppWidgetProviderInfos) { eblanAppWidgetProviderInfo ->
                        EblanAppWidgetScreenItem(
                            // Widget Info
                            eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                            // Dimensions
                            columns = columns,
                            rows = rows,
                            screenHeight = screenHeight,
                            screenWidth = screenWidth,
                            // Layout and Settings
                            gridItemSettings = gridItemSettings,
                            isVisibleOverlay = isVisibleOverlay,
                            // Actions
                            onDragAppWidget = onDragAppWidget,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EblanAppWidgetScreenItem(
    // Widget Info
    eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo,
    // Dimensions
    columns: Int,
    rows: Int,
    screenHeight: Int,
    screenWidth: Int,
    // Layout and Settings
    gridItemSettings: GridItemSettings,
    isVisibleOverlay: Boolean,
    modifier: Modifier = Modifier,
    // Actions
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

    val graphicsLayer = rememberGraphicsLayer()

    val id = remember { Uuid.random().toHexString() }

    Column(
        modifier = modifier
            .size(200.dp)
            .padding(EblanSpacing.extraLarge)
            .run {
                if (!isVisibleOverlay) {
                    pointerInput(
                        key1 = gridItemSettings,
                    ) {
                        detectTapGestures(
                            onLongPress = {
                                scope.launch {
                                    handleOnLongPressEblanAppWidgetScreenItem(
                                        // Widget Identification
                                        id = id,
                                        eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                                        // Widget Settings and UI
                                        gridItemSettings = gridItemSettings,
                                        graphicsLayer = graphicsLayer,
                                        // Widget Positioning
                                        intOffset = intOffset,
                                        intSize = intSize,
                                        // Drag Callback
                                        onDragAppWidget = onDragAppWidget,
                                    )
                                }
                            },
                        )
                    }
                } else {
                    this
                }
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
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        eblanAppWidgetProviderInfo.description?.let { description ->
            Text(
                text = description,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (text != null) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        Box(
            modifier = Modifier
                .width(160.dp)
                .height(100.dp)
                .onGloballyPositioned(
                    onGloballyPositioned = { currentLayoutCoordinates ->
                        intOffset = currentLayoutCoordinates.positionInRoot().round()

                        intSize = currentLayoutCoordinates.size
                    },
                )
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }

                    drawLayer(graphicsLayer = graphicsLayer)
                },
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = eblanAppWidgetProviderInfo.preview
                    ?: eblanAppWidgetProviderInfo.applicationIcon,
                contentScale = ContentScale.Fit,
                contentDescription = null,
            )
        }
    }
}

private suspend fun handleOnLongPressEblanAppWidgetScreenItem(
    // Widget Identification
    id: String,
    eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo,
    // Widget Settings and UI
    gridItemSettings: GridItemSettings,
    graphicsLayer: GraphicsLayer,
    // Widget Positioning
    intOffset: IntOffset,
    intSize: IntSize,
    // Drag Callback
    onDragAppWidget: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val gridItem = getWidgetGridItem(
        // Identifiers
        id = id,
        serialNumber = eblanAppWidgetProviderInfo.serialNumber,
        packageName = eblanAppWidgetProviderInfo.packageName,
        componentName = eblanAppWidgetProviderInfo.componentName,
        // Display Info
        label = eblanAppWidgetProviderInfo.applicationLabel,
        icon = eblanAppWidgetProviderInfo.applicationIcon,
        preview = eblanAppWidgetProviderInfo.preview,
        configure = eblanAppWidgetProviderInfo.configure,
        // Layout and Dimensions
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
