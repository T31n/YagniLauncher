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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.eblan.launcher.feature.home.model.ScrollBarItemLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun ScrollBarType(
    modifier: Modifier = Modifier,
    alphabeticalScrollBarItems: List<AlphabeticalScrollBarItem>,
    appDrawerSettings: AppDrawerSettings,
    canScroll: Boolean,
    lazyGridState: LazyGridState,
    paddingValues: PaddingValues,
    itemLayout: ScrollBarItemLayout,
) {
    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.TopCenter,
    ) {
        when (appDrawerSettings.scrollBarType) {
            ScrollBarType.ScrollBar -> {
                ScrollBarThumb(
                    appDrawerColumns = appDrawerSettings.appDrawerColumns,
                    lazyGridState = lazyGridState,
                    paddingValues = paddingValues,
                    searchBarPosition = appDrawerSettings.searchBarPosition,
                    canScroll = canScroll,
                    scrollBarItemLayout = itemLayout,
                    onScrollToItem = lazyGridState::scrollToItem,
                )
            }

            ScrollBarType.Alphabetical -> {
                if (alphabeticalScrollBarItems.isNotEmpty()) {
                    AlphabeticalScrollBar(
                        alphabeticalScrollBarItems = alphabeticalScrollBarItems,
                        paddingValues = paddingValues,
                        searchBarPosition = appDrawerSettings.searchBarPosition,
                        canScroll = canScroll,
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
    modifier: Modifier = Modifier,
    alphabeticalScrollBarItems: List<AlphabeticalScrollBarItem>,
    paddingValues: PaddingValues,
    searchBarPosition: SearchBarPosition,
    canScroll: Boolean,
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
    modifier: Modifier = Modifier,
    appDrawerColumns: Int,
    lazyGridState: LazyGridState,
    paddingValues: PaddingValues,
    searchBarPosition: SearchBarPosition,
    canScroll: Boolean,
    scrollBarItemLayout: ScrollBarItemLayout = ScrollBarItemLayout.Regular,
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

    val viewPortThumbY by remember(key1 = lazyGridState) {
        derivedStateOf {
            getViewPortThumbY(
                lazyGridState = lazyGridState,
                appDrawerColumns = appDrawerColumns,
                density = density,
                thumbHeight = thumbHeight,
                bottomPadding = bottomPaddingPx,
                itemLayout = scrollBarItemLayout,
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

    fun scrollToTap(offset: Offset) {
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

    fun scrollToDrag(deltaY: Float) {
        if (deltaY == 0f) return

        val layoutInfo = lazyGridState.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        val avgItemHeight = visibleItems.sumOf { it.size.height } / visibleItems.size
        val totalItems = layoutInfo.totalItemsCount
        val totalRows = (totalItems + appDrawerColumns - 1) / appDrawerColumns
        val viewportHeight = layoutInfo.viewportSize.height
        val thumbHeightPx = with(density) { thumbHeight.toPx() }
        val availableHeight = (viewportHeight - thumbHeightPx - bottomPaddingPx).coerceAtLeast(0f)
        val newThumbY = (thumbY + deltaY).coerceIn(0f, availableHeight)
        val progress = if (availableHeight > 0f) newThumbY / availableHeight else 0f
        val headerHeight = if (scrollBarItemLayout == ScrollBarItemLayout.StickyHeader) {
            visibleItems.firstOrNull { it.index == 0 }?.size?.height ?: 0
        } else {
            0
        }
        val appRows = if (headerHeight > 0) {
            (totalItems - 1 + appDrawerColumns - 1) / appDrawerColumns
        } else {
            totalRows
        }
        val totalContentHeight = headerHeight + appRows * avgItemHeight
        val scrollableHeight = (
            totalContentHeight - (viewportHeight - bottomPaddingPx)
            ).coerceAtLeast(0)
        val targetScrollY = (progress * scrollableHeight).coerceIn(0f, scrollableHeight.toFloat())
        val targetIndex = if (headerHeight > 0 && targetScrollY >= headerHeight) {
            val targetRow = ((targetScrollY - headerHeight) / avgItemHeight)
                .coerceIn(0f, (appRows - 1).coerceAtLeast(0).toFloat())
            (1 + targetRow.toInt() * appDrawerColumns).coerceIn(1, totalItems - 1)
        } else {
            0
        }

        thumbY = newThumbY
        scope.launch {
            onScrollToItem(targetIndex)
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
                    onTap = ::scrollToTap,
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
                            scrollToDrag(deltaY = dragAmount.y)
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

private fun getViewPortThumbY(
    lazyGridState: LazyGridState,
    appDrawerColumns: Int,
    density: Density,
    thumbHeight: Dp,
    bottomPadding: Int,
    itemLayout: ScrollBarItemLayout,
): Float {
    val layoutInfo = lazyGridState.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo

    val totalItems = layoutInfo.totalItemsCount
    val totalRows = (totalItems + appDrawerColumns - 1) / appDrawerColumns
    val header = if (itemLayout == ScrollBarItemLayout.StickyHeader) {
        visibleItems.firstOrNull { it.index == 0 }
    } else {
        null
    }
    val appItems = visibleItems.filter { it.index != 0 || header == null }
    val avgItemHeight = appItems.map { it.size.height }.average().toFloat()
    val appRows = if (header != null) {
        (totalItems - 1 + appDrawerColumns - 1) / appDrawerColumns
    } else {
        totalRows
    }

    val viewportHeight = layoutInfo.viewportSize.height.toFloat()

    val totalContentHeight = (header?.size?.height ?: 0) + appRows * avgItemHeight
    val firstItem = appItems.firstOrNull()
    val scrollY = if (header != null && firstItem != null) {
        header.size.height +
            (((firstItem.index - 1) / appDrawerColumns) * avgItemHeight) -
            firstItem.offset.y
    } else if (firstItem != null) {
        ((firstItem.index / appDrawerColumns) * avgItemHeight) - firstItem.offset.y
    } else {
        0f
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
