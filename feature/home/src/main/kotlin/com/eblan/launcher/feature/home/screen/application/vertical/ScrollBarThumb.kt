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
package com.eblan.launcher.feature.home.screen.application.vertical

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eblan.launcher.domain.model.application.AlphabeticalScrollBarItem
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.ScrollBarType
import com.eblan.launcher.domain.model.userdata.SearchBarPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun ScrollBarType(
    // Scroll and List
    alphabeticalScrollBarItems: List<AlphabeticalScrollBarItem>,
    lazyGridState: LazyGridState,
    canScroll: Boolean,
    // UI Settings
    appDrawerSettings: AppDrawerSettings,
    paddingValues: PaddingValues,
    isPrivateSpaceEntryPointHidden: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.TopCenter,
    ) {
        when (appDrawerSettings.scrollBarType) {
            ScrollBarType.ScrollBar -> {
                ScrollBarThumb(
                    // State and Scrolling
                    lazyGridState = lazyGridState,
                    canScroll = canScroll,
                    // Layout Configuration
                    appDrawerColumns = appDrawerSettings.appDrawerColumns,
                    paddingValues = paddingValues,
                    searchBarPosition = appDrawerSettings.searchBarPosition,
                    // Privacy and Modifiers
                    isPrivateSpaceEntryPointHidden = isPrivateSpaceEntryPointHidden,
                    // Callbacks
                    onScrollToItem = lazyGridState::scrollToItem,
                )
            }

            ScrollBarType.Alphabetical -> {
                if (alphabeticalScrollBarItems.isNotEmpty()) {
                    AlphabeticalScrollBar(
                        // Content and Layout
                        alphabeticalScrollBarItems = alphabeticalScrollBarItems,
                        paddingValues = paddingValues,
                        // Configuration
                        searchBarPosition = appDrawerSettings.searchBarPosition,
                        canScroll = canScroll,
                        // Actions
                        onScrollToItem = lazyGridState::scrollToItem,
                    )
                }
            }

            ScrollBarType.None -> Unit
        }
    }
}

@Composable
internal fun AlphabeticalScrollBar(
    // Content and Layout
    alphabeticalScrollBarItems: List<AlphabeticalScrollBarItem>,
    paddingValues: PaddingValues,
    // Configuration
    searchBarPosition: SearchBarPosition,
    canScroll: Boolean,
    modifier: Modifier = Modifier,
    // Actions
    onScrollToItem: suspend (Int) -> Unit,
) {
    if (!canScroll) return

    val scope = rememberCoroutineScope()

    val density = LocalDensity.current

    val edgeThreshold = with(density) { 28.dp.toPx() }

    val listState = rememberLazyListState()

    var selectedLetter by remember(key1 = alphabeticalScrollBarItems) {
        mutableStateOf<Char?>(null)
    }

    LaunchedEffect(key1 = selectedLetter) {
        if (selectedLetter != null) {
            delay(1000L.milliseconds)

            selectedLetter = null
        }
    }

    val bottomPadding = if (searchBarPosition == SearchBarPosition.Bottom) {
        0.dp
    } else {
        paddingValues.calculateBottomPadding()
    }

    fun selectItem(item: AlphabeticalScrollBarItem) {
        selectedLetter = item.letter
        scope.launch {
            onScrollToItem(item.index)

            val visibleItems = listState.layoutInfo.visibleItemsInfo
            if (visibleItems.none { it.index == alphabeticalScrollBarItems.indexOf(item) }) {
                listState.animateScrollToItem(alphabeticalScrollBarItems.indexOf(item))
            }
        }
    }

    fun selectItemAt(y: Float) {
        val visibleItems = listState.layoutInfo.visibleItemsInfo
        val target = visibleItems.minByOrNull {
            abs(it.offset + it.size / 2f - y)
        } ?: return

        selectItem(alphabeticalScrollBarItems[target.index])
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .width(28.dp)
            .fillMaxHeight()
            .pointerInput(alphabeticalScrollBarItems) {
                detectTapGestures(
                    onTap = { selectItemAt(it.y) },
                )
            }
            .pointerInput(alphabeticalScrollBarItems) {
                detectDragGestures(
                    onDragStart = { selectItemAt(it.y) },
                    onDrag = { change, dragAmount ->
                        val viewportHeight = listState.layoutInfo.viewportSize.height
                        val edgeScroll = when {
                            change.position.y < edgeThreshold &&
                                dragAmount.y < 0f -> dragAmount.y

                            change.position.y > viewportHeight - edgeThreshold &&
                                dragAmount.y > 0f -> dragAmount.y

                            else -> 0f
                        }

                        scope.launch {
                            if (edgeScroll != 0f) {
                                listState.scrollBy(edgeScroll)
                            }

                            selectItemAt(change.position.y)
                        }
                    },
                )
            },
        contentPadding = PaddingValues(bottom = bottomPadding),
        userScrollEnabled = false,
    ) {
        items(
            items = alphabeticalScrollBarItems,
            key = { it.letter },
        ) { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(
                        color = if (selectedLetter == item.letter) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        shape = RoundedCornerShape(14.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.letter.toString(),
                    modifier = Modifier.fillMaxWidth(),
                    color = if (selectedLetter == item.letter) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ScrollBarThumb(
    // State and Scrolling
    lazyGridState: LazyGridState,
    canScroll: Boolean,
    // Layout Configuration
    appDrawerColumns: Int,
    paddingValues: PaddingValues,
    searchBarPosition: SearchBarPosition,
    // Privacy and Modifiers
    isPrivateSpaceEntryPointHidden: Boolean,
    modifier: Modifier = Modifier,
    // Callbacks
    onScrollToItem: suspend (Int) -> Unit,
) {
    if (!canScroll) return

    val density = LocalDensity.current

    val scope = rememberCoroutineScope()

    val bottomPadding = if (searchBarPosition == SearchBarPosition.Bottom) {
        0.dp
    } else {
        paddingValues.calculateBottomPadding()
    }

    val bottomPaddingPx = with(density) {
        bottomPadding.roundToPx()
    }

    val thumbHeight by remember(lazyGridState) {
        derivedStateOf {
            with(density) {
                (lazyGridState.layoutInfo.viewportSize.height / 4).toDp()
            }
        }
    }

    val headerHeightPx by rememberHasPrivateSpaceStickyHeader(
        // State
        lazyGridState = lazyGridState,
        // Configuration
        isPrivateSpaceEntryPointHidden = isPrivateSpaceEntryPointHidden,
    )

    val viewPortThumbY by remember(key1 = lazyGridState) {
        derivedStateOf {
            getViewPortThumbY(
                // Grid State
                lazyGridState = lazyGridState,
                // Layout Dimensions
                appDrawerColumns = appDrawerColumns,
                headerHeightPx = headerHeightPx,
                bottomPadding = bottomPaddingPx,
                thumbHeight = thumbHeight,
                // Display Metrics
                density = density,
            )
        }
    }

    var isDraggingThumb by remember { mutableStateOf(false) }

    var thumbY by remember { mutableFloatStateOf(0f) }

    val animatedThumbY by remember {
        derivedStateOf {
            if (isDraggingThumb) thumbY else viewPortThumbY
        }
    }

    Box(
        modifier = modifier
            .width(10.dp)
            .fillMaxHeight()
            .padding(bottom = bottomPadding)
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
            )
            .pointerInput(lazyGridState) {
                detectTapGestures(
                    onTap = {
                        scrollToTap(
                            // State and Scope
                            lazyGridState = lazyGridState,
                            scope = scope,
                            // Layout and Dimensions
                            appDrawerColumns = appDrawerColumns,
                            bottomPaddingPx = bottomPaddingPx,
                            density = density,
                            thumbHeight = thumbHeight,
                            // Target
                            offset = it,
                            // Action
                            onScrollToItem = onScrollToItem,
                        )
                    },
                )
            },
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(thumbHeight)
                .offset {
                    IntOffset(
                        x = 0,
                        y = animatedThumbY.roundToInt(),
                    )
                }
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(10.dp),
                )
                .pointerInput(key1 = lazyGridState) {
                    detectDragGestures(
                        onDragStart = {
                            thumbY = viewPortThumbY

                            isDraggingThumb = true
                        },
                        onDrag = { _, dragAmount ->
                            scrollToDrag(
                                // State and Scope
                                lazyGridState = lazyGridState,
                                scope = scope,
                                // Layout and Dimensions
                                appDrawerColumns = appDrawerColumns,
                                headerHeightPx = headerHeightPx,
                                bottomPaddingPx = bottomPaddingPx,
                                thumbHeight = thumbHeight,
                                density = density,
                                // Scroll and Position
                                deltaY = dragAmount.y,
                                thumbY = thumbY,
                                // Callbacks
                                onScrollToItem = onScrollToItem,
                                onUpdateThumbY = {
                                    thumbY = it
                                },
                            )
                        },
                        onDragEnd = {
                            isDraggingThumb = false
                        },
                        onDragCancel = {
                            isDraggingThumb = false
                        },
                    )
                },
        )
    }
}

@Composable
private fun rememberHasPrivateSpaceStickyHeader(
    // State
    lazyGridState: LazyGridState,
    // Configuration
    isPrivateSpaceEntryPointHidden: Boolean,
): State<Int> = produceState(
    initialValue = 0,
    key1 = lazyGridState,
    key2 = isPrivateSpaceEntryPointHidden,
) {
    if (isPrivateSpaceEntryPointHidden) {
        snapshotFlow {
            lazyGridState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.index == 0 }
                ?.size
                ?.height
        }
            .filterNotNull()
            .collect { value = it }
    } else {
        value = 0
    }
}

private fun scrollToTap(
    // State and Scope
    lazyGridState: LazyGridState,
    scope: CoroutineScope,
    // Layout and Dimensions
    appDrawerColumns: Int,
    bottomPaddingPx: Int,
    density: Density,
    thumbHeight: Dp,
    // Target
    offset: Offset,
    // Action
    onScrollToItem: suspend (Int) -> Unit,
) {
    val viewportHeight = lazyGridState.layoutInfo.viewportSize.height - bottomPaddingPx

    val thumbHeightPx = with(density) { thumbHeight.roundToPx() }

    val maxThumbY = (viewportHeight - thumbHeightPx).coerceAtLeast(0)

    val targetThumbY = (offset.y - thumbHeightPx / 2f)
        .coerceIn(0f, maxThumbY.toFloat())

    val totalRows = ceil(
        lazyGridState.layoutInfo.totalItemsCount / appDrawerColumns.toFloat(),
    ).toInt()

    val row = targetThumbY / maxThumbY.coerceAtLeast(1) * (totalRows - 1)

    val targetIndex = (row.roundToInt() * appDrawerColumns)
        .coerceAtMost(lazyGridState.layoutInfo.totalItemsCount - 1)

    scope.launch {
        onScrollToItem(targetIndex)
    }
}

private fun scrollToDrag(
    // State and Scope
    lazyGridState: LazyGridState,
    scope: CoroutineScope,
    // Layout and Dimensions
    appDrawerColumns: Int,
    headerHeightPx: Int,
    bottomPaddingPx: Int,
    thumbHeight: Dp,
    density: Density,
    // Scroll and Position
    deltaY: Float,
    thumbY: Float,
    // Callbacks
    onScrollToItem: suspend (Int) -> Unit,
    onUpdateThumbY: (Float) -> Unit,
) {
    if (deltaY == 0f) return

    val layoutInfo = lazyGridState.layoutInfo

    val visibleItems = layoutInfo.visibleItemsInfo

    val totalItems = layoutInfo.totalItemsCount

    val hasHeader = headerHeightPx > 0

    val appItems = visibleItems.filter { it.index != 0 || !hasHeader }

    val avgItemHeight = appItems.map { it.size.height }.average().toFloat()

    val totalRows = (totalItems + appDrawerColumns - 1) / appDrawerColumns

    val appRows = if (hasHeader) {
        (totalItems - 1 + appDrawerColumns - 1) / appDrawerColumns
    } else {
        totalRows
    }

    val viewportHeight = layoutInfo.viewportSize.height

    val thumbHeightPx = with(density) { thumbHeight.toPx() }

    val availableHeight = (viewportHeight - thumbHeightPx - bottomPaddingPx).coerceAtLeast(0f)

    val newThumbY = (thumbY + deltaY).coerceIn(0f, availableHeight)

    val progress = if (availableHeight > 0f) newThumbY / availableHeight else 0f

    val totalContentHeight = headerHeightPx + appRows * avgItemHeight

    val scrollableHeight = (
        totalContentHeight - (viewportHeight - bottomPaddingPx)
        ).coerceAtLeast(0f)
    val targetScrollY = (progress * scrollableHeight).coerceIn(0f, scrollableHeight)

    val targetIndex = if (hasHeader && targetScrollY >= headerHeightPx) {
        val targetRow = ((targetScrollY - headerHeightPx) / avgItemHeight)
            .coerceIn(0f, (appRows - 1).coerceAtLeast(0).toFloat())
        (1 + targetRow.toInt() * appDrawerColumns).coerceIn(1, totalItems - 1)
    } else if (hasHeader) {
        0
    } else {
        val targetRow = (targetScrollY / avgItemHeight)
            .coerceIn(0f, (appRows - 1).coerceAtLeast(0).toFloat())
        (targetRow.toInt() * appDrawerColumns).coerceIn(0, totalItems - 1)
    }

    onUpdateThumbY(newThumbY)

    scope.launch {
        onScrollToItem(targetIndex)
    }
}

private fun getViewPortThumbY(
    // Grid State
    lazyGridState: LazyGridState,
    // Layout Dimensions
    appDrawerColumns: Int,
    headerHeightPx: Int,
    bottomPadding: Int,
    thumbHeight: Dp,
    // Display Metrics
    density: Density,
): Float {
    val layoutInfo = lazyGridState.layoutInfo
    val visibleItemsInfo = layoutInfo.visibleItemsInfo

    val totalItemsCount = layoutInfo.totalItemsCount

    val totalRows = (totalItemsCount + appDrawerColumns - 1) / appDrawerColumns

    val hasHeader = headerHeightPx > 0

    val appItems = visibleItemsInfo.filter { it.index != 0 || !hasHeader }

    val avgItemHeight = appItems.map { it.size.height }.average().toFloat()

    val appRows = if (hasHeader) {
        (totalItemsCount - 1 + appDrawerColumns - 1) / appDrawerColumns
    } else {
        totalRows
    }

    val viewportHeight = layoutInfo.viewportSize.height.toFloat()

    val totalContentHeight = headerHeightPx + appRows * avgItemHeight

    val firstItem = appItems.firstOrNull()

    val scrollY = if (firstItem == null) {
        0f
    } else if (hasHeader) {
        headerHeightPx +
            (((firstItem.index - 1) / appDrawerColumns) * avgItemHeight) -
            firstItem.offset.y
    } else {
        ((firstItem.index / appDrawerColumns) * avgItemHeight) - firstItem.offset.y
    }

    val scrollableHeight = (
        totalContentHeight - (viewportHeight - bottomPadding)
        ).coerceAtLeast(0f)

    val progress = if (scrollableHeight > 0f) {
        (scrollY / scrollableHeight).coerceIn(0f, 1f)
    } else {
        0f
    }

    val thumbHeightPx = with(density) { thumbHeight.toPx() }

    val availableHeight = (viewportHeight - thumbHeightPx - bottomPadding)
        .coerceAtLeast(0f)

    return (progress * availableHeight)
        .coerceIn(0f, availableHeight)
}
