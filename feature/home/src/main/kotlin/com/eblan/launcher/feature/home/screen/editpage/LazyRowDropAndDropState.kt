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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Composable
internal fun rememberLazyRowDragDropState(
    lazyListState: LazyListState,
    onMove: (Int, Int) -> Unit,
): LazyRowDragDropState {
    val scope = rememberCoroutineScope()

    val state = remember(lazyListState) {
        LazyRowDragDropState(
            state = lazyListState,
            onMove = onMove,
            scope = scope,
        )
    }

    LaunchedEffect(state) {
        state.scrollChannel
            .receiveAsFlow()
            .onEach(lazyListState::scrollBy)
            .collect()
    }

    return state
}

internal class LazyRowDragDropState(
    private val state: LazyListState,
    private val scope: CoroutineScope,
    private val onMove: (Int, Int) -> Unit,
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    internal val scrollChannel = Channel<Float>()

    private var draggingItemDraggedDelta by mutableFloatStateOf(0f)
    private var draggingItemInitialOffset by mutableIntStateOf(0)
    internal val draggingItemOffset: Float
        get() = draggingItemLayoutInfo?.let { item ->
            draggingItemInitialOffset + draggingItemDraggedDelta - item.offset
        } ?: 0f

    private val draggingItemLayoutInfo: LazyListItemInfo?
        get() = state.layoutInfo.visibleItemsInfo.firstOrNull { currentLazyListItemInfo ->
            currentLazyListItemInfo.index == draggingItemIndex
        }

    internal var previousIndexOfDraggedItem by mutableStateOf<Int?>(null)
        private set

    internal var previousItemOffset = Animatable(0f)
        private set

    internal fun onDragStart(offset: Offset) {
        val contentOffsetX = offset.x - state.layoutInfo.beforeContentPadding

        state.layoutInfo.visibleItemsInfo
            .firstOrNull { item ->
                contentOffsetX.toInt() in item.offset..(item.offset + item.size)
            }
            ?.also { currentLazyListItemInfo ->
                draggingItemIndex = currentLazyListItemInfo.index
                draggingItemInitialOffset = currentLazyListItemInfo.offset
            }
    }

    internal fun onDragInterrupted() {
        if (draggingItemIndex != null) {
            previousIndexOfDraggedItem = draggingItemIndex
            val startOffset = draggingItemOffset
            scope.launch {
                previousItemOffset.snapTo(startOffset)
                previousItemOffset.animateTo(
                    0f,
                    spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = 1f),
                )
                previousIndexOfDraggedItem = null
            }
        }
        draggingItemDraggedDelta = 0f
        draggingItemIndex = null
        draggingItemInitialOffset = 0
    }

    internal fun onDrag(offset: Offset) {
        draggingItemDraggedDelta += offset.x

        val draggingItem = draggingItemLayoutInfo ?: return

        val startOffset = draggingItem.offset + draggingItemOffset
        val endOffset = startOffset + draggingItem.size
        val middleOffset = startOffset + (endOffset - startOffset) / 2f

        val targetItem = state.layoutInfo.visibleItemsInfo.find { item ->
            middleOffset.toInt() in item.offset..item.offsetEnd &&
                draggingItem.index != item.index
        }

        if (targetItem != null) {
            if (
                draggingItem.index == state.firstVisibleItemIndex ||
                targetItem.index == state.firstVisibleItemIndex
            ) {
                state.requestScrollToItem(
                    state.firstVisibleItemIndex,
                    state.firstVisibleItemScrollOffset,
                )
            }

            onMove(draggingItem.index, targetItem.index)
            draggingItemIndex = targetItem.index
        } else {
            val overscroll = when {
                draggingItemDraggedDelta > 0 ->
                    (endOffset - state.layoutInfo.viewportEndOffset)
                        .coerceAtLeast(0f)

                draggingItemDraggedDelta < 0 ->
                    (startOffset - state.layoutInfo.viewportStartOffset)
                        .coerceAtMost(0f)

                else -> 0f
            }

            if (overscroll != 0f) {
                scrollChannel.trySend(overscroll)
            }
        }
    }

    private val LazyListItemInfo.offsetEnd: Int
        get() = this.offset + this.size
}

internal fun Modifier.dragRowContainer(
    lazyRowDragDropState: LazyRowDragDropState,
) = pointerInput(lazyRowDragDropState) {
    detectDragGesturesAfterLongPress(
        onDragStart = lazyRowDragDropState::onDragStart,
        onDragEnd = lazyRowDragDropState::onDragInterrupted,
        onDragCancel = lazyRowDragDropState::onDragInterrupted,
        onDrag = { change, offset ->
            change.consume()
            lazyRowDragDropState.onDrag(offset)
        },
    )
}

@Composable
internal fun LazyItemScope.DraggableRowItem(
    modifier: Modifier = Modifier,
    lazyRowDragDropState: LazyRowDragDropState,
    index: Int,
    content: @Composable ColumnScope.(isDragging: Boolean) -> Unit,
) {
    val dragging = index == lazyRowDragDropState.draggingItemIndex

    val draggingModifier =
        if (dragging) {
            Modifier
                .zIndex(1f)
                .graphicsLayer {
                    translationX = lazyRowDragDropState.draggingItemOffset
                }
        } else if (index == lazyRowDragDropState.previousIndexOfDraggedItem) {
            Modifier
                .zIndex(1f)
                .graphicsLayer {
                    translationX = lazyRowDragDropState.previousItemOffset.value
                }
        } else {
            Modifier.animateItem(
                fadeInSpec = null,
                fadeOutSpec = null,
            )
        }

    Column(modifier = modifier.then(draggingModifier)) { content(dragging) }
}
