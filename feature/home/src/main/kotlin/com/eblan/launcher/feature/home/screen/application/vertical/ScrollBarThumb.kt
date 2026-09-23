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
import androidx.compose.foundation.layout.Row
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
import com.eblan.launcher.domain.model.userdata.SearchBarPosition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun ScrollBarThumb(
    modifier: Modifier = Modifier,
    appDrawerColumns: Int,
    lazyGridState: LazyGridState,
    paddingValues: PaddingValues,
    searchBarPosition: SearchBarPosition,
    onScrollToItem: suspend (Int) -> Unit,
) {
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
        val totalContentHeight = totalRows * avgItemHeight
        val scrollableHeight = (totalContentHeight - viewportHeight).coerceAtLeast(0)
        val targetScrollY = (progress * scrollableHeight).coerceIn(0f, scrollableHeight.toFloat())
        val targetRow = (targetScrollY / avgItemHeight)
            .coerceIn(0f, (totalRows - 1).toFloat())
        val targetIndex = (targetRow.toInt() * appDrawerColumns)
            .coerceIn(0, totalItems - 1)

        thumbY = newThumbY
        scope.launch {
            onScrollToItem(targetIndex)
        }
    }

    Row(modifier = modifier.fillMaxHeight()) {
        Box(
            modifier = Modifier
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
}

@Composable
internal fun AlphabeticalScrollBar(
    modifier: Modifier = Modifier,
    alphabeticalScrollBarItems: List<AlphabeticalScrollBarItem>,
    paddingValues: PaddingValues,
    searchBarPosition: SearchBarPosition,
    onScrollToItem: suspend (Int) -> Unit,
) {
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

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(28.dp),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
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
}

private fun getViewPortThumbY(
    lazyGridState: LazyGridState,
    appDrawerColumns: Int,
    density: Density,
    thumbHeight: Dp,
    bottomPadding: Int,
): Float {
    val layoutInfo = lazyGridState.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo

    val firstItem = visibleItems.first()

    val avgItemHeight = visibleItems.sumOf { it.size.height } / visibleItems.size

    val totalItems = layoutInfo.totalItemsCount
    val totalRows = (totalItems + appDrawerColumns - 1) / appDrawerColumns

    val viewportHeight = layoutInfo.viewportSize.height.toFloat()

    val totalContentHeight = totalRows * avgItemHeight

    val firstRow = firstItem.index / appDrawerColumns

    val scrollY = (firstRow * avgItemHeight) - firstItem.offset.y

    val scrollableHeight = (totalContentHeight - viewportHeight).coerceAtLeast(0f)

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
