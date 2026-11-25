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
package com.eblan.launcher.feature.home.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

@Composable
internal fun rememberNestedScrollConnectionEffect(
    // Scroll State
    canScrollBackward: Boolean,
    swipeY: Float,
    // Drag Callbacks
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
): OffsetNestedScrollConnection {
    val currentCanScrollBackward = rememberUpdatedState(canScrollBackward)
    val currentSwipeY = rememberUpdatedState(swipeY)

    return remember(
        key1 = onVerticalDrag,
        key2 = onDragEnd,
    ) {
        OffsetNestedScrollConnection(
            canScrollBackward = currentCanScrollBackward,
            swipeY = currentSwipeY,
            onVerticalDrag = onVerticalDrag,
            onDragEnd = onDragEnd,
        )
    }
}

internal class OffsetNestedScrollConnection(
    private val canScrollBackward: State<Boolean>,
    private val swipeY: State<Float>,
    private val onVerticalDrag: (Float) -> Unit,
    private val onDragEnd: () -> Unit,
) : NestedScrollConnection {
    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (
            source == NestedScrollSource.UserInput &&
            available.y < 0f && swipeY.value > 0f
        ) {
            onVerticalDrag(available.y)

            return Offset(
                0f,
                available.y,
            )
        }

        return Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (
            source == NestedScrollSource.UserInput &&
            available.y > 0f && !canScrollBackward.value
        ) {
            onVerticalDrag(available.y)

            return Offset(
                x = 0f,
                y = available.y,
            )
        }

        return Offset.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (swipeY.value > 0f) {
            onDragEnd()
        }

        return super.onPostFling(consumed, available)
    }
}
