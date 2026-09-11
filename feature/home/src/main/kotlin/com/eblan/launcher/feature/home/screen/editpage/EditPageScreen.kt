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
package com.eblan.launcher.feature.home.screen.editpage

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.util.Consumer
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.model.folder.PreviewFolder
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.HomeSettings
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.GridLayout
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.model.Screen
import kotlinx.coroutines.launch
import com.eblan.launcher.common.R as commonR

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun EditGridPageScreen(
    modifier: Modifier = Modifier,
    pageItems: List<PageItem>?,
    hasShortcutHostPermission: Boolean,
    homeSettings: HomeSettings,
    paddingValues: PaddingValues,
    screenWidth: Int,
    screenHeight: Int,
    textColor: TextColor,
    previewFolderGridItems: Map<String, PreviewFolder>,
    iconPackInfoFilePaths: Map<String, String?>,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    onSaveEditPage: (
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
        associate: Associate,
    ) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
) {
    requireNotNull(pageItems)

    val density = LocalDensity.current

    val layoutDirection = LocalLayoutDirection.current

    val (gridWidthDp, gridHeightDp) = getGridSize(
        density = density,
        paddingValues = paddingValues,
        layoutDirection = layoutDirection,
        screenWidth = screenWidth,
        screenHeight = screenHeight,
    )

    var currentPageItems by remember { mutableStateOf(pageItems) }

    val pageItemsToDelete = remember { mutableStateListOf<PageItem>() }

    var selectedId by remember { mutableIntStateOf(homeSettings.initialPage) }

    val lazyListState = rememberLazyListState()

    val lazyRowDragDropState =
        rememberLazyRowDragDropState(lazyListState = lazyListState) { from, to ->
            currentPageItems = currentPageItems.toMutableList().apply {
                add(
                    index = to,
                    element = removeAt(from),
                )
            }
        }

    val activity = LocalActivity.current as ComponentActivity

    val scope = rememberCoroutineScope()

    val columns = homeSettings.columns

    val rows = homeSettings.rows

    var expanded by remember { mutableStateOf(false) }

    DisposableEffect(key1 = activity) {
        val listener = Consumer<Intent> { intent ->
            scope.launch {
                handleActionMainIntent(
                    intent = intent,
                    onUpdateScreen = onUpdateScreen,
                )
            }
        }

        activity.addOnNewIntentListener(listener)

        onDispose {
            activity.removeOnNewIntentListener(listener)
        }
    }

    BackHandler {
        onUpdateScreen(Screen.Pager)
    }

    HomeHandler {
        onUpdateScreen(Screen.Pager)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier
                .dragRowContainer(lazyRowDragDropState = lazyRowDragDropState)
                .matchParentSize(),
            contentPadding = paddingValues,
            state = lazyListState,
        ) {
            itemsIndexed(
                items = currentPageItems,
                key = { _, pageItem -> pageItem.id },
            ) { index, pageItem ->
                DraggableRowItem(
                    modifier = Modifier
                        .size(
                            width = gridWidthDp,
                            height = gridHeightDp,
                        )
                        .padding(10.dp),
                    lazyRowDragDropState = lazyRowDragDropState,
                    index = index,
                ) {
                    GridLayout(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp),
                            ),
                        columns = columns,
                        gridItems = pageItem.gridItems,
                        rows = rows,
                        animate = false,
                        content = {
                            GridItemContent(
                                gridItem = it,
                                gridItemSettings = homeSettings.gridItemSettings,
                                hasShortcutHostPermission = hasShortcutHostPermission,
                                textColor = textColor,
                                previewFolderGridItems = previewFolderGridItems,
                                iconPackInfoFilePaths = iconPackInfoFilePaths,
                                folderBackgroundColor = folderBackgroundColor,
                                customFolderBackgroundColor = customFolderBackgroundColor,
                                systemTextColor = systemTextColor,
                                systemCustomTextColor = systemCustomTextColor,
                                folderCornerRadius = homeSettings.folderCornerRadius,
                            )
                        },
                    )

                    PageButtons(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(5.dp),
                        pageItem = pageItem,
                        selectedId = selectedId,
                        onDeleteClick = {
                            currentPageItems = currentPageItems.toMutableList().apply {
                                removeIf { currentPageItem ->
                                    currentPageItem.id == pageItem.id
                                }
                            }

                            pageItemsToDelete.add(pageItem)
                        },
                        onHomeClick = {
                            selectedId = pageItem.id
                        },
                    )
                }
            }
        }

        ExpandableFloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = paddingValues.calculateEndPadding(layoutDirection),
                    bottom = paddingValues.calculateBottomPadding(),
                ),
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onAdd = {
                currentPageItems = currentPageItems.toMutableList().apply {
                    add(PageItem(id = maxOf { it.id } + 1, gridItems = emptyList()))
                }
            },
            onSave = {
                onSaveEditPage(
                    selectedId,
                    currentPageItems,
                    pageItemsToDelete,
                    Associate.Grid,
                )

                expanded = false
            },
            onCancel = {
                onUpdateScreen(Screen.Pager)
            },
        )
    }
}

private fun getGridSize(
    density: Density,
    paddingValues: PaddingValues,
    layoutDirection: LayoutDirection,
    screenWidth: Int,
    screenHeight: Int,
): Pair<Dp, Dp> {
    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val rightPadding = with(density) {
        paddingValues.calculateRightPadding(layoutDirection).roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding

    val verticalPadding = topPadding + bottomPadding

    val gridWidthDp = with(density) {
        (screenWidth - horizontalPadding).toDp()
    }

    val gridHeightDp = with(density) {
        (screenHeight - verticalPadding).toDp()
    }

    return gridWidthDp to gridHeightDp
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun EditDockGridPageScreen(
    modifier: Modifier = Modifier,
    pageItems: List<PageItem>?,
    hasShortcutHostPermission: Boolean,
    homeSettings: HomeSettings,
    paddingValues: PaddingValues,
    textColor: TextColor,
    previewFolderGridItems: Map<String, PreviewFolder>,
    iconPackInfoFilePaths: Map<String, String?>,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    onSaveEditPage: (
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
        associate: Associate,
    ) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
) {
    requireNotNull(pageItems)

    val layoutDirection = LocalLayoutDirection.current

    var currentPageItems by remember { mutableStateOf(pageItems) }

    val pageItemsToDelete = remember { mutableStateListOf<PageItem>() }

    var selectedId by remember { mutableIntStateOf(homeSettings.dockInitialPage) }

    val lazyListState = rememberLazyListState()

    val lazyColumnDragDropState =
        rememberLazyColumnDragDropState(lazyListState = lazyListState) { from, to ->
            currentPageItems = currentPageItems.toMutableList().apply {
                add(
                    index = to,
                    element = removeAt(from),
                )
            }
        }

    val activity = LocalActivity.current as ComponentActivity

    val scope = rememberCoroutineScope()

    val columns = homeSettings.dockColumns

    val rows = homeSettings.dockRows

    val cardHeight = homeSettings.dockHeight.dp

    var expanded by remember { mutableStateOf(false) }

    DisposableEffect(key1 = activity) {
        val listener = Consumer<Intent> { intent ->
            scope.launch {
                handleActionMainIntent(
                    intent = intent,
                    onUpdateScreen = onUpdateScreen,
                )
            }
        }

        activity.addOnNewIntentListener(listener)

        onDispose {
            activity.removeOnNewIntentListener(listener)
        }
    }

    BackHandler {
        onUpdateScreen(Screen.Pager)
    }

    HomeHandler {
        onUpdateScreen(Screen.Pager)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .dragColumnContainer(lazyColumnDragDropState = lazyColumnDragDropState)
                .matchParentSize(),
            state = lazyListState,
            contentPadding = paddingValues,
        ) {
            itemsIndexed(
                items = currentPageItems,
                key = { _, pageItem -> pageItem.id },
            ) { index, pageItem ->
                DraggableColumnItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    lazyColumnDragDropState = lazyColumnDragDropState,
                    index = index,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp),
                            ),
                    ) {
                        GridLayout(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(cardHeight),
                            columns = columns,
                            gridItems = pageItem.gridItems,
                            rows = rows,
                            animate = false,
                            content = {
                                GridItemContent(
                                    gridItem = it,
                                    gridItemSettings = homeSettings.gridItemSettings,
                                    hasShortcutHostPermission = hasShortcutHostPermission,
                                    textColor = textColor,
                                    previewFolderGridItems = previewFolderGridItems,
                                    iconPackInfoFilePaths = iconPackInfoFilePaths,
                                    folderBackgroundColor = folderBackgroundColor,
                                    customFolderBackgroundColor = customFolderBackgroundColor,
                                    systemTextColor = systemTextColor,
                                    systemCustomTextColor = systemCustomTextColor,
                                    folderCornerRadius = homeSettings.folderCornerRadius,
                                )
                            },
                        )

                        PageButtons(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(5.dp),
                            pageItem = pageItem,
                            selectedId = selectedId,
                            onDeleteClick = {
                                currentPageItems = currentPageItems.toMutableList().apply {
                                    removeIf { currentPageItem ->
                                        currentPageItem.id == pageItem.id
                                    }
                                }

                                pageItemsToDelete.add(pageItem)
                            },
                            onHomeClick = {
                                selectedId = pageItem.id
                            },
                        )
                    }
                }
            }
        }

        ExpandableFloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = paddingValues.calculateEndPadding(layoutDirection),
                    bottom = paddingValues.calculateBottomPadding(),
                ),
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onAdd = {
                currentPageItems = currentPageItems.toMutableList().apply {
                    add(PageItem(id = maxOf { it.id } + 1, gridItems = emptyList()))
                }
            },
            onSave = {
                onSaveEditPage(
                    selectedId,
                    currentPageItems,
                    pageItemsToDelete,
                    Associate.Dock,
                )

                expanded = false
            },
            onCancel = {
                onUpdateScreen(Screen.Pager)
            },
        )
    }
}

@Composable
internal fun PageButtons(
    modifier: Modifier = Modifier,
    pageItem: PageItem,
    selectedId: Int,
    onDeleteClick: () -> Unit,
    onHomeClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        tonalElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier.padding(5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            IconButton(
                onClick = onDeleteClick,
                enabled = pageItem.id != selectedId,
            ) {
                Icon(
                    imageVector = EblanLauncherIcons.Delete,
                    contentDescription = null,
                )
            }

            IconButton(
                onClick = onHomeClick,
                enabled = pageItem.id != selectedId,
            ) {
                Icon(
                    imageVector = EblanLauncherIcons.Home,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
internal fun ExpandableFloatingActionButton(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAdd: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(modifier = modifier.padding(15.dp)) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInHorizontally { it },
            exit = fadeOut() + slideOutHorizontally { it },
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                ElevatedButton(onClick = onAdd) {
                    Text(
                        text = stringResource(commonR.string.add),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                ElevatedButton(
                    onClick = onSave,
                ) {
                    Text(
                        text = stringResource(commonR.string.save),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                ElevatedButton(
                    onClick = onCancel,
                ) {
                    Text(
                        text = stringResource(commonR.string.cancel),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        FloatingActionButton(
            modifier = Modifier.align(Alignment.End),
            onClick = {
                onExpandedChange(!expanded)
            },
        ) {
            Icon(
                imageVector = if (expanded) {
                    EblanLauncherIcons.Close
                } else {
                    EblanLauncherIcons.Add
                },
                contentDescription = null,
            )
        }
    }
}

internal fun handleActionMainIntent(
    intent: Intent,
    onUpdateScreen: (Screen) -> Unit,
) {
    if (intent.action != Intent.ACTION_MAIN && !intent.hasCategory(Intent.CATEGORY_HOME)) {
        return
    }

    if ((intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
        return
    }

    onUpdateScreen(Screen.Pager)
}
