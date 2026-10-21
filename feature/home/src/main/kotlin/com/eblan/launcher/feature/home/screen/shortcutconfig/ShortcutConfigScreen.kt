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
package com.eblan.launcher.feature.home.screen.shortcutconfig

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.launcherapps.EblanUser
import com.eblan.launcher.domain.model.shortcutconfig.EblanShortcutConfig
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.feature.home.component.ScreenEffect
import com.eblan.launcher.feature.home.component.rememberNestedScrollConnectionEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.eblan.launcher.common.R as commonR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShortcutConfigScreen(
    // Configuration
    eblanShortcutConfigs: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    gridItemSettings: GridItemSettings,
    // Layout
    paddingValues: PaddingValues,
    screenHeight: Int,
    modifier: Modifier = Modifier,
    // State
    swipeY: Float,
    alpha: Float,
    isVisibleOverlay: Boolean,
    drag: Drag,
    // Callbacks
    onDismiss: () -> Unit,
    onGetEblanShortcutConfigsByLabel: (String) -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onDragShortcutConfig: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current

    val horizontalPagerState = rememberPagerState(
        pageCount = {
            eblanShortcutConfigs.keys.size
        },
    )

    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()

    val scope = rememberCoroutineScope()

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
        onGetLabel = onGetEblanShortcutConfigsByLabel,
        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay
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
            modifier = modifier
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateLeftPadding(layoutDirection),
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
                        placeholder = { Text(text = stringResource(commonR.string.search_applications)) },
                    )
                },
            )

            if (eblanShortcutConfigs.keys.size > 1) {
                EblanShortcutConfigTabRow(
                    currentPage = horizontalPagerState.currentPage,
                    eblanShortcutConfigs = eblanShortcutConfigs,
                    onAnimateScrollToPage = horizontalPagerState::animateScrollToPage,
                )

                HorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    state = horizontalPagerState,
                ) { index ->
                    EblanShortcutConfigsPage(
                        // Configuration
                        eblanShortcutConfigs = eblanShortcutConfigs,
                        gridItemSettings = gridItemSettings,
                        // State
                        index = index,
                        paddingValues = paddingValues,
                        swipeY = swipeY,
                        isVisibleOverlay = isVisibleOverlay,
                        // Callbacks
                        onDragEnd = onDragEnd,
                        onVerticalDrag = onVerticalDrag,
                        onDragShortcutConfig = onDragShortcutConfig
                    )
                }
            } else {
                EblanShortcutConfigsPage(
                    // Configuration
                    eblanShortcutConfigs = eblanShortcutConfigs,
                    gridItemSettings = gridItemSettings,
                    // State
                    index = 0,
                    paddingValues = paddingValues,
                    swipeY = swipeY,
                    isVisibleOverlay = isVisibleOverlay,
                    // Callbacks
                    onDragEnd = onDragEnd,
                    onVerticalDrag = onVerticalDrag,
                    onDragShortcutConfig = onDragShortcutConfig
                )
            }
        }
    }
}

@Composable
private fun EblanShortcutConfigTabRow(
    currentPage: Int,
    eblanShortcutConfigs: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    onAnimateScrollToPage: suspend (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()

    SecondaryTabRow(selectedTabIndex = currentPage) {
        eblanShortcutConfigs.keys.forEachIndexed { index, eblanUser ->
            Tab(
                selected = currentPage == index,
                onClick = {
                    scope.launch {
                        onAnimateScrollToPage(index)
                    }
                },
                text = {
                    Text(
                        text = eblanUser.eblanUserType.name,
                        maxLines = 1,
                    )
                },
            )
        }
    }
}

@Composable
private fun EblanShortcutConfigsPage(
    // Configuration
    eblanShortcutConfigs: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    gridItemSettings: GridItemSettings,
    // State
    index: Int,
    paddingValues: PaddingValues,
    swipeY: Float,
    isVisibleOverlay: Boolean,
    modifier: Modifier = Modifier,
    // Callbacks
    onDragEnd: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onDragShortcutConfig: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit
) {
    val lazyListState = rememberLazyListState()

    val serialNumber = eblanShortcutConfigs.keys.toList().getOrElse(
        index = index,
        defaultValue = {
            0
        },
    )

    val nestedScrollConnection = rememberNestedScrollConnectionEffect(
        // Scroll State
        scrollableState = lazyListState,
        swipeY = swipeY,
        // Drag Callbacks
        onVerticalDrag = onVerticalDrag,
        onDragEnd = onDragEnd
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
            items(eblanShortcutConfigs[serialNumber].orEmpty().keys.toList()) { eblanApplicationInfoGroup ->
                key(eblanApplicationInfoGroup.serialNumber, eblanApplicationInfoGroup.packageName) {
                    EblanApplicationInfoItem(
                        // Application Info
                        eblanApplicationInfoGroup = eblanApplicationInfoGroup,
                        eblanShortcutConfigs = eblanShortcutConfigs[serialNumber].orEmpty(),
                        // Grid and Visibility
                        gridItemSettings = gridItemSettings,
                        isVisibleOverlay = isVisibleOverlay,
                        // Layout
                        modifier = modifier,
                        // Actions
                        onDragShortcutConfig = onDragShortcutConfig
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
    eblanShortcutConfigs: Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>,
    // Grid and Visibility
    gridItemSettings: GridItemSettings,
    isVisibleOverlay: Boolean,
    // Layout
    modifier: Modifier = Modifier,
    // Actions
    onDragShortcutConfig: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        expanded = !expanded
                    },
                    onLongPress = {
                        expanded = !expanded
                    },
                )
            },
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

                eblanShortcutConfigs[eblanApplicationInfoGroup]?.forEach { eblanShortcutConfig ->
                    EblanShortcutConfigItem(
                        // Shortcut and Grid Configuration
                        eblanShortcutConfig = eblanShortcutConfig,
                        gridItemSettings = gridItemSettings,
                        // Display Settings
                        isVisibleOverlay = isVisibleOverlay,
                        // Actions
                        onDragShortcutConfig = onDragShortcutConfig
                    )
                }
            }
        }
    }
}

@Composable
private fun EblanShortcutConfigItem(
    // Shortcut and Grid Configuration
    eblanShortcutConfig: EblanShortcutConfig,
    gridItemSettings: GridItemSettings,
    // Display Settings
    isVisibleOverlay: Boolean,
    modifier: Modifier = Modifier,
    // Actions
    onDragShortcutConfig: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }
    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val graphicsLayer = rememberGraphicsLayer()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
            .pointerInput(
                key1 = isVisibleOverlay,
                key2 = gridItemSettings,
            ) {
                detectTapGestures(
                    onLongPress = {
                        scope.launch {
                            handleOnLongPress(
                                // Configuration
                                eblanShortcutConfig = eblanShortcutConfig,
                                gridItemSettings = gridItemSettings,
                                // Layout
                                intOffset = intOffset,
                                intSize = intSize,
                                graphicsLayer = graphicsLayer,
                                // Controller
                                keyboardController = keyboardController,
                                // Callbacks
                                onDragShortcutConfig = onDragShortcutConfig
                            )
                        }
                    },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AsyncImage(
            model = eblanShortcutConfig.activityIcon,
            contentDescription = null,
            modifier = Modifier
                .size(gridItemSettings.iconSize.dp)
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
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = eblanShortcutConfig.activityLabel.toString(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
private suspend fun handleOnLongPress(
    // Configuration
    eblanShortcutConfig: EblanShortcutConfig,
    gridItemSettings: GridItemSettings,
    // Layout
    intOffset: IntOffset,
    intSize: IntSize,
    graphicsLayer: GraphicsLayer,
    // Controller
    keyboardController: SoftwareKeyboardController?,
    // Callbacks
    onDragShortcutConfig: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit
) {
    val id = Uuid.random().toHexString()

    val gridItem = getShortcutConfigGridItem(
        eblanShortcutConfig = eblanShortcutConfig,
        gridItemSettings = gridItemSettings,
        id = id,
    )

    keyboardController?.hide()

    onDragShortcutConfig(
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

private fun getShortcutConfigGridItem(
    eblanShortcutConfig: EblanShortcutConfig,
    gridItemSettings: GridItemSettings,
    id: String,
): GridItem {
    val data = GridItemData.ShortcutConfig(
        serialNumber = eblanShortcutConfig.serialNumber,
        componentName = eblanShortcutConfig.componentName,
        packageName = eblanShortcutConfig.packageName,
        activityLabel = eblanShortcutConfig.activityLabel,
        activityIcon = eblanShortcutConfig.activityIcon,
        applicationIcon = eblanShortcutConfig.activityIcon,
        applicationLabel = eblanShortcutConfig.activityLabel,
        shortcutIntentName = null,
        shortcutIntentIcon = null,
        shortcutIntentUri = null,
        customIcon = null,
        customLabel = null,
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
