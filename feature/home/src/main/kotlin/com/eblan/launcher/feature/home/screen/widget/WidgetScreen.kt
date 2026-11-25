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
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.layout.ContentScale
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.component.VerticalSlideReveal
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.designsystem.theme.EblanSpacing
import com.eblan.launcher.domain.model.application.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.launcherapps.EblanUser
import com.eblan.launcher.domain.model.widget.EblanAppWidgetProviderInfo
import com.eblan.launcher.feature.home.R
import com.eblan.launcher.feature.home.component.ScreenEffect
import com.eblan.launcher.feature.home.component.getEblanUserTypeTitle
import com.eblan.launcher.feature.home.component.rememberNestedScrollConnectionEffect
import com.eblan.launcher.feature.home.contract.WidgetScreenContract
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WidgetScreen(
    // Contract
    contract: WidgetScreenContract,
    // Grid Configuration
    columns: Int,
    rows: Int,
    gridItemSettings: GridItemSettings,
    // Dimensions
    screenWidth: Int,
    screenHeight: Int,
    paddingValues: PaddingValues,
    // State
    alpha: Float,
    swipeY: Float,
    // Interaction
    drag: Drag,
    modifier: Modifier = Modifier,
    // Actions
    onDismiss: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragWidget: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val isVisibleOverlay by contract.isVisibleOverlay.collectAsStateWithLifecycle()
    val eblanAppWidgetProviderInfos by contract.eblanAppWidgetProviderInfos.collectAsStateWithLifecycle()

    val layoutDirection = LocalLayoutDirection.current

    val scope = rememberCoroutineScope()

    val horizontalPagerState = rememberPagerState(
        pageCount = {
            eblanAppWidgetProviderInfos.keys.size
        },
    )
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()

    ScreenEffect(
        // State and Visibility
        isVisibleOverlay = isVisibleOverlay,
        textFieldState = textFieldState,
        screenHeight = screenHeight,
        // Gestures
        drag = drag,
        swipeY = swipeY,
        // Callbacks
        onDismiss = onDismiss,
        onGetLabel = contract::getEblanAppWidgetProviderInfosByLabel,
        onUpdateIsVisibleOverlay = contract::updateIsVisibleOverlay,
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding())
            .graphicsLayer {
                translationY = swipeY
                this.alpha = alpha
                clip = true
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                )
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
                    .padding(
                        start = EblanSpacing.medium,
                        end = EblanSpacing.medium,
                        bottom = EblanSpacing.small,
                    ),
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

            if (eblanAppWidgetProviderInfos.keys.size > 1) {
                EblanAppWidgetProviderInfoTabRow(
                    currentPage = horizontalPagerState.currentPage,
                    eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                    onAnimateScrollToPage = horizontalPagerState::animateScrollToPage,
                )

                HorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    state = horizontalPagerState,
                ) { index ->
                    EblanAppWidgetProvidersPage(
                        // Configuration
                        eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                        // Layout
                        index = index,
                        paddingValues = paddingValues,
                        swipeY = swipeY,
                        rows = rows,
                        columns = columns,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        // Grid and Visibility
                        gridItemSettings = gridItemSettings,
                        isVisibleOverlay = isVisibleOverlay,
                        // Gestures
                        onVerticalDrag = onVerticalDrag,
                        onDragEnd = onDragEnd,
                        // Actions
                        onDragWidget = onDragWidget,
                    )
                }
            } else {
                EblanAppWidgetProvidersPage(
                    // Configuration
                    eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                    // Layout
                    index = 0,
                    paddingValues = paddingValues,
                    swipeY = swipeY,
                    rows = rows,
                    columns = columns,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    // Grid and Visibility
                    gridItemSettings = gridItemSettings,
                    isVisibleOverlay = isVisibleOverlay,
                    // Gestures
                    onVerticalDrag = onVerticalDrag,
                    onDragEnd = onDragEnd,
                    // Actions
                    onDragWidget = onDragWidget,
                )
            }
        }
    }
}

@Composable
private fun EblanAppWidgetProviderInfoTabRow(
    currentPage: Int,
    eblanAppWidgetProviderInfos: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>>,
    onAnimateScrollToPage: suspend (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()

    SecondaryTabRow(selectedTabIndex = currentPage) {
        eblanAppWidgetProviderInfos.keys.forEachIndexed { index, eblanUser ->
            Tab(
                selected = currentPage == index,
                onClick = {
                    scope.launch {
                        onAnimateScrollToPage(index)
                    }
                },
                text = {
                    Text(
                        text = eblanUser.eblanUserType.getEblanUserTypeTitle(),
                        maxLines = 1,
                    )
                },
            )
        }
    }
}

@Composable
private fun EblanAppWidgetProvidersPage(
    // Configuration
    eblanAppWidgetProviderInfos: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>>,
    // Layout
    index: Int,
    paddingValues: PaddingValues,
    swipeY: Float,
    rows: Int,
    columns: Int,
    screenWidth: Int,
    screenHeight: Int,
    // Grid and Visibility
    gridItemSettings: GridItemSettings,
    isVisibleOverlay: Boolean,
    modifier: Modifier = Modifier,
    // Gestures
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    // Actions
    onDragWidget: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val eblanUser = eblanAppWidgetProviderInfos.keys.toList().getOrNull(index)
    val eblanApplicationInfoGroups = eblanUser?.let(eblanAppWidgetProviderInfos::get).orEmpty()
    val nestedScrollConnection = rememberNestedScrollConnectionEffect(
        // Scroll State
        canScrollBackward = lazyListState.canScrollBackward,
        swipeY = swipeY,
        // Drag Callbacks
        onVerticalDrag = onVerticalDrag,
        onDragEnd = onDragEnd,
    )

    Box(
        modifier = modifier
            .nestedScroll(nestedScrollConnection)
            .fillMaxSize(),
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.matchParentSize(),
            contentPadding = PaddingValues(
                bottom = paddingValues.calculateBottomPadding(),
            ),
        ) {
            items(eblanApplicationInfoGroups.keys.toList()) { eblanApplicationInfoGroup ->
                key(eblanApplicationInfoGroup.serialNumber, eblanApplicationInfoGroup.packageName) {
                    EblanApplicationInfoItem(
                        // Application Info
                        eblanApplicationInfoGroup = eblanApplicationInfoGroup,
                        eblanAppWidgetProviderInfos = eblanApplicationInfoGroups,
                        // Layout Dimensions
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        rows = rows,
                        columns = columns,
                        // Display Settings
                        isVisibleOverlay = isVisibleOverlay,
                        gridItemSettings = gridItemSettings,
                        // Actions
                        onDragWidget = onDragWidget,
                    )
                }
            }
        }
    }
}

@Composable
private fun EblanApplicationInfoItem(
    // Application Info
    eblanApplicationInfoGroup: EblanApplicationInfoGroup,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    // Layout Dimensions
    screenWidth: Int,
    screenHeight: Int,
    rows: Int,
    columns: Int,
    // Display Settings
    isVisibleOverlay: Boolean,
    gridItemSettings: GridItemSettings,
    modifier: Modifier = Modifier,
    // Actions
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
                        // Widget Info
                        eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                        // Dimensions
                        columns = columns,
                        rows = rows,
                        screenWidth = screenWidth,
                        screenHeight = screenHeight,
                        // Grid and Visibility
                        gridItemSettings = gridItemSettings,
                        isVisibleOverlay = isVisibleOverlay,
                        // Actions
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
    // Grid and Visibility
    gridItemSettings: GridItemSettings,
    isVisibleOverlay: Boolean,
    modifier: Modifier = Modifier,
    // Actions
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

    val graphicsLayer = rememberGraphicsLayer()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(EblanSpacing.extraLarge)
            .pointerInput(
                isVisibleOverlay,
                gridItemSettings,
            ) {
                detectTapGestures(
                    onLongPress = {
                        scope.launch {
                            handleOnLongPress(
                                // Widget Info
                                eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
                                gridItemSettings = gridItemSettings,
                                // Graphics and Positioning
                                graphicsLayer = graphicsLayer,
                                intOffset = intOffset,
                                intSize = intSize,
                                keyboardController = keyboardController,
                                // Drag Callback
                                onDragWidget = onDragWidget,
                            )
                        }
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
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
                style = MaterialTheme.typography.titleMedium,
            )
        }

        if (text != null) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        eblanAppWidgetProviderInfo.description?.let { description ->
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private suspend fun handleOnLongPress(
    // Widget Info
    eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo,
    gridItemSettings: GridItemSettings,
    // Graphics and Positioning
    graphicsLayer: GraphicsLayer,
    intOffset: IntOffset,
    intSize: IntSize,
    keyboardController: SoftwareKeyboardController?,
    // Drag Callback
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
