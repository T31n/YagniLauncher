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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

internal class OffsetNestedScrollConnection(
    private val onVerticalDrag: (Float) -> Unit,
    private val onDragEnd: () -> Unit,
) : NestedScrollConnection {
    private var swipeY = 0f
    private var canScrollBackward = false

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (available.y < 0f && swipeY > 0f) {
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
            available.y > 0f && !canScrollBackward
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
        onDragEnd()

        return super.onPostFling(consumed, available)
    }

    fun updateSwipeY(value: Float) {
        swipeY = value
    }

    fun updateCanScrollBackward(value: Boolean) {
        canScrollBackward = value
    }
}
