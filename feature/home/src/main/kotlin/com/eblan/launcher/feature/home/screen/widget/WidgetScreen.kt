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

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.component.VerticalSlideReveal
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.application.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.widget.EblanAppWidgetProviderInfo
import com.eblan.launcher.feature.home.R
import com.eblan.launcher.feature.home.component.ScreenEffect
import com.eblan.launcher.feature.home.component.rememberNestedScrollConnectionEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WidgetScreen(
    // Required
    columns: Int,
    rows: Int,
    screenWidth: Int,
    screenHeight: Int,
    alpha: Float,
    isVisibleOverlay: Boolean,
    paddingValues: PaddingValues,
    gridItemSettings: GridItemSettings,
    drag: Drag,
    swipeY: Float,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onDismiss: () -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onDragWidget: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    val scope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()

    val nestedScrollConnection = rememberNestedScrollConnectionEffect(
        scrollableState = lazyListState,
        swipeY = swipeY,
        onVerticalDrag = onVerticalDrag,
        onDragEnd = onDragEnd,
    )

    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()

    ScreenEffect(
        isVisibleOverlay = isVisibleOverlay,
        textFieldState = textFieldState,
        drag = drag,
        swipeY = swipeY,
        screenHeight = screenHeight,
        onDismiss = onDismiss,
        onGetLabel = onGetEblanAppWidgetProviderInfosByLabel,
        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding())
            .graphicsLayer {
                translationY = swipeY
                this.alpha = alpha
                clip = true
                shape = RoundedCornerShape(20.dp)
            }
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
    ) {
        Column(
            modifier = Modifier
                .nestedScroll(nestedScrollConnection)
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                ),
        ) {
            DragHandle(modifier = Modifier.align(Alignment.CenterHorizontally))

            SearchBar(
                state = searchBarState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                inputField = {
                    SearchBarDefaults.InputField(
                        textFieldState = textFieldState,
                        searchBarState = searchBarState,
                        leadingIcon = if (textFieldState.text.isNotEmpty()) {
                            {
                                Icon(
                                    imageVector = EblanLauncherIcons.Search,
                                    contentDescription = null,
                                )
                            }
                        } else {
                            null
                        },
                        onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
                        placeholder = { Text(text = stringResource(R.string.search_widgets)) },
                    )
                },
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
            ) {
                items(eblanAppWidgetProviderInfos.keys.toList()) { eblanApplicationInfoGroup ->
                    key(eblanApplicationInfoGroup.packageName) {
                        EblanApplicationInfoItem(
                            eblanApplicationInfoGroup = eblanApplicationInfoGroup,
                            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                            screenWidth = screenWidth,
                            screenHeight = screenHeight,
                            rows = rows,
                            columns = columns,
                            isVisibleOverlay = isVisibleOverlay,
                            gridItemSettings = gridItemSettings,
                            onDragWidget = onDragWidget,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EblanApplicationInfoItem(
    // Data
    eblanApplicationInfoGroup: EblanApplicationInfoGroup,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    // Dimensions
    screenWidth: Int,
    screenHeight: Int,
    rows: Int,
    columns: Int,
    // State
    isVisibleOverlay: Boolean,
    gridItemSettings: GridItemSettings,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onDragWidget: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        expanded = !expanded
                    },
                    onLongPress = {
                        expanded = !expanded
                    },
                )
            }
            .fillMaxWidth(),
    ) {
        ListItem(
            headlineContent = { Text(text = eblanApplicationInfoGroup.label.toString()) },
            leadingContent = {
                AsyncImage(
                    model = eblanApplicationInfoGroup.icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )
            },
            trailingContent = {
                Icon(
                    imageVector = if (expanded) {
                        EblanLauncherIcons.ArrowDropUp
                    } else {
                        EblanLauncherIcons.ArrowDropDown
                    },
                    contentDescription = null,
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth(),
        )

        VerticalSlideReveal(visible = expanded) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(10.dp))

                eblanAppWidgetProviderInfos[eblanApplicationInfoGroup]?.forEach { eblanAppWidgetProviderInfo ->
                    EblanAppWidgetProviderInfoItem(
                        eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                        columns = columns,
                        rows = rows,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        gridItemSettings = gridItemSettings,
                        isVisibleOverlay = isVisibleOverlay,
                        onDragWidget = onDragWidget,
                    )
                }
            }
        }
    }
}

@Composable
private fun EblanAppWidgetProviderInfoItem(
    // Widget Info
    eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo,
    // Dimensions
    columns: Int,
    rows: Int,
    screenWidth: Int,
    screenHeight: Int,
    // Settings
    gridItemSettings: GridItemSettings,
    isVisibleOverlay: Boolean,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onDragWidget: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }
    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val preview = eblanAppWidgetProviderInfo.preview ?: eblanAppWidgetProviderInfo.applicationIcon

    val graphicsLayer = rememberGraphicsLayer()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
            .pointerInput(
                isVisibleOverlay,
                gridItemSettings,
            ) {
                detectTapGestures(
                    onLongPress = {
                        scope.launch {
                            handleOnLongPress(
                                eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                                gridItemSettings = gridItemSettings,
                                graphicsLayer = graphicsLayer,
                                intOffset = intOffset,
                                intSize = intSize,
                                keyboardController = keyboardController,
                                onDragWidget = onDragWidget,
                            )
                        }
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
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
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = label,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        if (text != null) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        eblanAppWidgetProviderInfo.description?.let { description ->
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
private suspend fun handleOnLongPress(
    // Widget Info
    eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo,
    // Grid Settings
    gridItemSettings: GridItemSettings,
    // Graphics & Layout
    graphicsLayer: GraphicsLayer,
    intOffset: IntOffset,
    intSize: IntSize,
    // Controllers
    keyboardController: SoftwareKeyboardController?,
    // Callbacks
    onDragWidget: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val id = Uuid.random().toHexString()

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

    keyboardController?.hide()

    onDragWidget(
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
