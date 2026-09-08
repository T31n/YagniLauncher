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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.component.VerticalSlideReveal
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.feature.home.R
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.component.OffsetNestedScrollConnection
import com.eblan.launcher.feature.home.component.gridItemScaleAnimation
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.SCALE
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
internal fun WidgetScreen(
    modifier: Modifier = Modifier,
    columns: Int,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    rows: Int,
    screenHeight: Int,
    screenWidth: Int,
    swipeY: Float,
    alpha: Float,
    cornerSize: Dp,
    isVisibleOverlay: Boolean,
    animations: Boolean,
    drag: Drag,
    onDismiss: () -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()

    val currentOnVerticalDrag by rememberUpdatedState(onVerticalDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    val nestedScrollConnection = remember {
        OffsetNestedScrollConnection(
            onVerticalDrag = currentOnVerticalDrag,
            onDragEnd = currentOnDragEnd,
        )
    }

    val searchBarState = rememberSearchBarState()

    val textFieldState = rememberTextFieldState()

    LaunchedEffect(key1 = textFieldState) {
        snapshotFlow { textFieldState.text }.debounce(500L.milliseconds)
            .onEach {
                onGetEblanAppWidgetProviderInfosByLabel(it.toString())
            }.collect()
    }

    LaunchedEffect(key1 = swipeY) {
        if (swipeY == screenHeight.toFloat()) {
            keyboardController?.hide()
        }
    }

    LaunchedEffect(
        key1 = isVisibleOverlay,
        key2 = drag,
    ) {
        if (isVisibleOverlay && (drag == Drag.Cancel || drag == Drag.End)) {
            onUpdateIsVisibleOverlay(false)
        }
    }

    LaunchedEffect(
        key1 = swipeY,
        key2 = lazyListState.canScrollBackward,
    ) {
        nestedScrollConnection.updateSwipeY(swipeY)
        nestedScrollConnection.updateCanScrollBackward(lazyListState.canScrollBackward)
    }

    BackHandler(enabled = swipeY < screenHeight.toFloat()) {
        onDismiss()
    }

    HomeHandler(enabled = swipeY < screenHeight.toFloat()) {
        onDismiss()
    }

    Surface(
        modifier = modifier
            .graphicsLayer {
                translationY = swipeY
                this.alpha = alpha
                clip = true
                shape = RoundedCornerShape(cornerSize)
            }
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .nestedScroll(nestedScrollConnection)
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                ),
        ) {
            SearchBar(
                state = searchBarState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                inputField = {
                    SearchBarDefaults.InputField(
                        textFieldState = textFieldState,
                        searchBarState = searchBarState,
                        leadingIcon = {
                            Icon(
                                imageVector = EblanLauncherIcons.Search,
                                contentDescription = null,
                            )
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
                            columns = columns,
                            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                            eblanApplicationInfoGroup = eblanApplicationInfoGroup,
                            gridItemSettings = gridItemSettings,
                            rows = rows,
                            screenHeight = screenHeight,
                            screenWidth = screenWidth,
                            isVisibleOverlay = isVisibleOverlay,
                            animations = animations,
                            onUpdateOverlayBounds = onUpdateOverlayBounds,
                            onUpdateImageBitmap = onUpdateImageBitmap,
                            onUpdateGridItemSource = onUpdateGridItemSource,
                            onUpdateSharedElementKey = onUpdateSharedElementKey,
                            onDismiss = onDismiss,
                            onUpdateIsDragging = onUpdateIsDragging,
                            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                            onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun EblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    columns: Int,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    eblanApplicationInfoGroup: EblanApplicationInfoGroup,
    gridItemSettings: GridItemSettings,
    rows: Int,
    screenHeight: Int,
    screenWidth: Int,
    isVisibleOverlay: Boolean,
    animations: Boolean,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onDismiss: () -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
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
                        columns = columns,
                        eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                        gridItemSettings = gridItemSettings,
                        rows = rows,
                        screenHeight = screenHeight,
                        screenWidth = screenWidth,
                        isVisibleOverlay = isVisibleOverlay,
                        animations = animations,
                        onUpdateOverlayBounds = onUpdateOverlayBounds,
                        onUpdateImageBitmap = onUpdateImageBitmap,
                        onUpdateGridItemSource = onUpdateGridItemSource,
                        onUpdateSharedElementKey = onUpdateSharedElementKey,
                        onDismiss = onDismiss,
                        onUpdateIsDragging = onUpdateIsDragging,
                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun EblanAppWidgetProviderInfoItem(
    modifier: Modifier = Modifier,
    columns: Int,
    eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo,
    gridItemSettings: GridItemSettings,
    rows: Int,
    screenHeight: Int,
    screenWidth: Int,
    isVisibleOverlay: Boolean,
    animations: Boolean,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onDismiss: () -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val preview = eblanAppWidgetProviderInfo.preview ?: eblanAppWidgetProviderInfo.applicationIcon

    val graphicsLayer = rememberGraphicsLayer()

    val scale = remember { Animatable(1f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
            .pointerInput(
                isVisibleOverlay,
                gridItemSettings,
                animations,
            ) {
                detectTapGestures(
                    onLongPress = {
                        scope.launch {
                            handleOnLongPress(
                                eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                                graphicsLayer = graphicsLayer,
                                gridItemSettings = gridItemSettings,
                                intOffset = intOffset,
                                intSize = intSize,
                                keyboardController = keyboardController,
                                scale = scale,
                                animations = animations,
                                onDismiss = onDismiss,
                                onUpdateGridItemSource = onUpdateGridItemSource,
                                onUpdateImageBitmap = onUpdateImageBitmap,
                                onUpdateIsDragging = onUpdateIsDragging,
                                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                                onUpdateOverlayBounds = onUpdateOverlayBounds,
                                onUpdateSharedElementKey = onUpdateSharedElementKey,
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
    eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo,
    graphicsLayer: GraphicsLayer,
    gridItemSettings: GridItemSettings,
    intOffset: IntOffset,
    intSize: IntSize,
    keyboardController: SoftwareKeyboardController?,
    scale: Animatable<Float, AnimationVector1D>,
    animations: Boolean,
    onDismiss: () -> Unit,
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
) {
    val id = Uuid.random().toHexString()

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

    keyboardController?.hide()

    onUpdateIsVisibleOverlay(true)

    onUpdateIsDragging(true)

    onDismiss()
}
