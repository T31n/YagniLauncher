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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.util.Consumer
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.designsystem.theme.EblanSpacing
import com.eblan.launcher.domain.model.folder.FolderPopup
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.home.PageItem
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.FolderSettings
import com.eblan.launcher.domain.model.userdata.HomeSettings
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.GridLayout
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.screen.editpage.griditem.PageGridItem
import com.eblan.launcher.ui.lazylist.DraggableColumnItem
import com.eblan.launcher.ui.lazylist.dragColumnContainer
import com.eblan.launcher.ui.lazylist.rememberLazyColumnDragDropState
import kotlinx.coroutines.launch
import com.eblan.launcher.common.R as commonR

@Composable
internal fun EditGridPageScreen(
    // Content
    pageItems: List<PageItem>?,
    folderPopupGridItems: Map<String, FolderPopup>,
    iconPackFilePaths: Map<String, String?>,
    // Layout
    paddingValues: PaddingValues,
    screenWidth: Int,
    screenHeight: Int,
    modifier: Modifier = Modifier,
    // Configuration
    hasShortcutHostPermission: Boolean,
    homeSettings: HomeSettings,
    folderSettings: FolderSettings,
    // Appearance
    textColor: TextColor,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    folderBorderColor: BackgroundColor,
    customFolderBorderColor: Int,
    folderBorderWidth: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // Actions
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
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        density = density,
        paddingValues = paddingValues,
        layoutDirection = layoutDirection,
    )

    var currentPageItems by remember(pageItems) { mutableStateOf(pageItems) }
    val pageItemsToDelete = remember { mutableStateListOf<PageItem>() }
    var selectedId by remember(homeSettings.initialPage) {
        mutableIntStateOf(homeSettings.initialPage)
    }
    var scrollToLastTrigger by remember { mutableIntStateOf(0) }

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

    LaunchedEffect(key1 = scrollToLastTrigger) {
        if (scrollToLastTrigger > 0 && currentPageItems.isNotEmpty()) {
            lazyListState.animateScrollToItem(currentPageItems.lastIndex)
        }
    }

    EditPageScaffold(
        modifier = modifier,
        paddingValues = paddingValues,
        onAdd = {
            val nextId = (currentPageItems.maxOfOrNull { it.id } ?: -1) + 1
            currentPageItems = currentPageItems + PageItem(
                id = nextId,
                gridItems = emptyList(),
            )
            scrollToLastTrigger++
        },
        onSave = {
            onSaveEditPage(
                selectedId,
                currentPageItems,
                pageItemsToDelete,
                Associate.Grid,
            )
        },
        onCancel = {
            onUpdateScreen(Screen.Pager)
        },
        onUpdateScreen = onUpdateScreen,
    ) { contentPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            val gridAspectRatio = if (gridHeightDp > 0.dp) {
                gridWidthDp / gridHeightDp
            } else {
                0.5625f
            }
            val maxPreviewWidth = maxWidth * 0.68f
            val maxPreviewHeight = (maxHeight - 88.dp).coerceAtLeast(120.dp)
            val cardWidth = minOf(maxPreviewWidth, maxPreviewHeight * gridAspectRatio)
            val cardHeight = cardWidth / gridAspectRatio
            val scale = if (gridWidthDp > 0.dp) cardWidth / gridWidthDp else 1f
            val horizontalEdgePadding = ((maxWidth - cardWidth) / 2f).coerceAtLeast(EblanSpacing.large)

            LazyRow(
                modifier = Modifier
                    .dragRowContainer(lazyRowDragDropState = lazyRowDragDropState)
                    .fillMaxSize(),
                state = lazyListState,
                contentPadding = PaddingValues(horizontal = horizontalEdgePadding),
                horizontalArrangement = Arrangement.spacedBy(EblanSpacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                itemsIndexed(
                    items = currentPageItems,
                    key = { _, pageItem -> pageItem.id },
                ) { index, pageItem ->
                    val isHome = pageItem.id == selectedId

                    DraggableRowItem(
                        lazyRowDragDropState = lazyRowDragDropState,
                        index = index,
                    ) { isDragging ->
                        val dragScale by animateFloatAsState(
                            targetValue = if (isDragging) 1.04f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "DragGridPageScale",
                        )

                        Surface(
                            modifier = Modifier
                                .size(
                                    width = cardWidth,
                                    height = cardHeight,
                                )
                                .graphicsLayer {
                                    scaleX = dragScale
                                    scaleY = dragScale
                                },
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(
                                alpha = if (isDragging) 0.72f else 0.52f,
                            ),
                            border = BorderStroke(
                                width = if (isHome || isDragging) 2.dp else 1.dp,
                                color = when {
                                    isHome -> MaterialTheme.colorScheme.primary
                                    isDragging -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                },
                            ),
                            tonalElevation = if (isDragging) 8.dp else 2.dp,
                        ) {
                            CompositionLocalProvider(
                                LocalDensity provides Density(
                                    density = density.density * scale,
                                    fontScale = density.fontScale,
                                ),
                            ) {
                                GridLayout(
                                    gridItems = pageItem.gridItems,
                                    columns = homeSettings.columns,
                                    rows = homeSettings.rows,
                                    animate = false,
                                    modifier = Modifier.fillMaxSize(),
                                    content = { currentGridItem ->
                                        PageGridItem(
                                            gridItem = currentGridItem,
                                            gridItemSettings = homeSettings.gridItemSettings,
                                            folderPopupGridItems = folderPopupGridItems,
                                            hasShortcutHostPermission = hasShortcutHostPermission,
                                            textColor = textColor,
                                            systemTextColor = systemTextColor,
                                            systemCustomTextColor = systemCustomTextColor,
                                            folderBackgroundColor = folderBackgroundColor,
                                            customFolderBackgroundColor = customFolderBackgroundColor,
                                            folderBorderColor = folderBorderColor,
                                            customFolderBorderColor = customFolderBorderColor,
                                            folderBorderWidth = folderBorderWidth,
                                            folderCornerRadius = folderSettings.folderCornerRadius,
                                            iconPackFilePaths = iconPackFilePaths,
                                        )
                                    },
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(EblanSpacing.small))

                        PageButtons(
                            pageNumber = index + 1,
                            isHome = isHome,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
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
    }
}

private fun getGridSize(
    // Screen Dimensions
    screenWidth: Int,
    screenHeight: Int,
    // Layout Context
    density: Density,
    paddingValues: PaddingValues,
    layoutDirection: LayoutDirection,
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

@Composable
internal fun EditDockGridPageScreen(
    // Content
    pageItems: List<PageItem>?,
    folderPopupGridItems: Map<String, FolderPopup>,
    hasShortcutHostPermission: Boolean,
    // Settings
    homeSettings: HomeSettings,
    folderSettings: FolderSettings,
    // Appearance
    textColor: TextColor,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    folderBorderColor: BackgroundColor,
    customFolderBorderColor: Int,
    folderBorderWidth: Int,
    iconPackFilePaths: Map<String, String?>,
    // Layout
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    // Callbacks
    onSaveEditPage: (
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
        associate: Associate,
    ) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
) {
    requireNotNull(pageItems)

    val scope = rememberCoroutineScope()

    var currentPageItems by remember(pageItems) { mutableStateOf(pageItems) }
    val pageItemsToDelete = remember { mutableStateListOf<PageItem>() }
    var selectedId by remember(homeSettings.dockInitialPage) {
        mutableIntStateOf(homeSettings.dockInitialPage)
    }
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

    val cardHeight = homeSettings.dockHeight.dp

    EditPageScaffold(
        modifier = modifier,
        paddingValues = paddingValues,
        onAdd = {
            val nextId = (currentPageItems.maxOfOrNull { it.id } ?: -1) + 1

            currentPageItems = currentPageItems + PageItem(
                id = nextId,
                gridItems = emptyList(),
            )

            scope.launch {
                lazyListState.animateScrollToItem(currentPageItems.lastIndex)
            }
        },
        onSave = {
            onSaveEditPage(
                selectedId,
                currentPageItems,
                pageItemsToDelete,
                Associate.Dock,
            )
        },
        onCancel = {
            onUpdateScreen(Screen.Pager)
        },
        onUpdateScreen = onUpdateScreen,
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .dragColumnContainer(lazyColumnDragDropState = lazyColumnDragDropState)
                .fillMaxSize()
                .padding(contentPadding),
            state = lazyListState,
            contentPadding = PaddingValues(EblanSpacing.large),
            verticalArrangement = Arrangement.spacedBy(EblanSpacing.medium),
        ) {
            itemsIndexed(
                items = currentPageItems,
                key = { _, pageItem -> pageItem.id },
            ) { index, pageItem ->
                val isHome = pageItem.id == selectedId

                DraggableColumnItem(
                    modifier = Modifier.fillMaxWidth(),
                    lazyColumnDragDropState = lazyColumnDragDropState,
                    index = index,
                ) { isDragging ->
                    val dragScale by animateFloatAsState(
                        targetValue = if (isDragging) 1.03f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "DragDockPageScale",
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = dragScale
                                scaleY = dragScale
                            },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(
                            alpha = if (isDragging) 0.72f else 0.52f,
                        ),
                        border = BorderStroke(
                            width = if (isHome || isDragging) 2.dp else 1.dp,
                            color = when {
                                isHome -> MaterialTheme.colorScheme.primary
                                isDragging -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.outlineVariant
                            },
                        ),
                        tonalElevation = if (isDragging) 8.dp else 2.dp,
                    ) {
                        GridLayout(
                            gridItems = pageItem.gridItems,
                            columns = homeSettings.dockColumns,
                            rows = homeSettings.dockRows,
                            animate = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(cardHeight),
                            content = { currentGridItem ->
                                PageGridItem(
                                    gridItem = currentGridItem,
                                    gridItemSettings = homeSettings.dockGridItemSettings,
                                    folderPopupGridItems = folderPopupGridItems,
                                    hasShortcutHostPermission = hasShortcutHostPermission,
                                    textColor = textColor,
                                    systemTextColor = systemTextColor,
                                    systemCustomTextColor = systemCustomTextColor,
                                    folderBackgroundColor = folderBackgroundColor,
                                    customFolderBackgroundColor = customFolderBackgroundColor,
                                    folderBorderColor = folderBorderColor,
                                    customFolderBorderColor = customFolderBorderColor,
                                    folderBorderWidth = folderBorderWidth,
                                    folderCornerRadius = folderSettings.folderCornerRadius,
                                    iconPackFilePaths = iconPackFilePaths,
                                )
                            },
                        )
                    }

                    Spacer(modifier = Modifier.height(EblanSpacing.small))

                    PageButtons(
                        pageNumber = index + 1,
                        isHome = isHome,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
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
}

@Composable
private fun EditPageScaffold(
    paddingValues: PaddingValues,
    onAdd: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onUpdateScreen: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    val activity = LocalActivity.current as ComponentActivity
    val layoutDirection = LocalLayoutDirection.current
    val scope = rememberCoroutineScope()

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

    BackHandler(onBack = onCancel)

    HomeHandler {
        onCancel()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            BottomAppBar(
                windowInsets = WindowInsets(
                    left = paddingValues.calculateLeftPadding(layoutDirection),
                    right = paddingValues.calculateRightPadding(layoutDirection),
                    bottom = paddingValues.calculateBottomPadding(),
                ),
                actions = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = EblanLauncherIcons.Close,
                            contentDescription = stringResource(commonR.string.cancel),
                        )
                    }

                    IconButton(onClick = onSave) {
                        Icon(
                            imageVector = EblanLauncherIcons.Done,
                            contentDescription = stringResource(commonR.string.save),
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = onAdd,
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                    ) {
                        Icon(
                            imageVector = EblanLauncherIcons.Add,
                            contentDescription = stringResource(commonR.string.add),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateLeftPadding(layoutDirection),
                    top = paddingValues.calculateTopPadding(),
                    end = paddingValues.calculateRightPadding(layoutDirection),
                    bottom = innerPadding.calculateBottomPadding(),
                ),
        ) {
            content(PaddingValues())
        }
    }
}

@Composable
internal fun PageButtons(
    pageNumber: Int,
    isHome: Boolean,
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit,
    onHomeClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = EblanSpacing.small, vertical = EblanSpacing.extraSmall),
            horizontalArrangement = Arrangement.spacedBy(EblanSpacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = pageNumber.toString(),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = EblanSpacing.small),
            )

            IconButton(
                onClick = onHomeClick,
                enabled = !isHome,
                colors = IconButtonDefaults.iconButtonColors(
                    disabledContentColor = if (isHome) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                ),
            ) {
                Icon(
                    imageVector = EblanLauncherIcons.Home,
                    contentDescription = stringResource(commonR.string.home),
                )
            }

            IconButton(
                onClick = onDeleteClick,
                enabled = !isHome,
            ) {
                Icon(
                    imageVector = EblanLauncherIcons.Delete,
                    contentDescription = null,
                )
            }
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
