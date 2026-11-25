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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import com.eblan.launcher.feature.home.util.calculatePage
import kotlin.math.abs

@Composable
internal fun PageIndicator(
    // Pager State and Pagination
    gridHorizontalPagerState: PagerState,
    pageCount: Int,
    infiniteScroll: Boolean,
    // Appearance and Layout
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (pageCount < 1) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(pageCount) { index ->
            PageIndicatorContent(
                // Pager
                gridHorizontalPagerState = gridHorizontalPagerState,
                index = index,
                pageCount = pageCount,
                infiniteScroll = infiniteScroll,
                // Styling
                color = color,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun PageIndicatorContent(
    // Pager
    gridHorizontalPagerState: PagerState,
    index: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    // Styling
    color: Color,
    modifier: Modifier = Modifier,
) {
    val baseWidth = 8.dp
    val baseHeight = 8.dp
    val activeWidth = 16.dp

    val currentPage = calculatePage(
        index = gridHorizontalPagerState.currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    fun distance(): Float = ((currentPage + gridHorizontalPagerState.currentPageOffsetFraction) - index)
        .coerceIn(-1f, 1f)

    Box(
        modifier = modifier
            .padding(3.dp)
            .layout { measurable, constraints ->
                val widthDp = when (val currentDistance = distance()) {
                    0f -> activeWidth
                    in -1f..0f -> baseWidth + (activeWidth - baseWidth) * (1f + currentDistance)
                    in 0f..1f -> baseWidth + (activeWidth - baseWidth) * (1f - currentDistance)
                    else -> baseWidth
                }

                val widthPx = widthDp.roundToPx()

                val heightPx = baseHeight.roundToPx()

                val placeable = measurable.measure(
                    constraints.copy(
                        minWidth = widthPx,
                        maxWidth = widthPx,
                        minHeight = heightPx,
                        maxHeight = heightPx,
                    ),
                )

                layout(widthPx, heightPx) {
                    placeable.placeRelative(0, 0)
                }
            }
            .clip(CircleShape)
            .drawBehind {
                drawRect(
                    color = lerp(
                        start = color.copy(alpha = 0.5f),
                        stop = color,
                        fraction = 1f - abs(distance()).coerceIn(0f, 1f),
                    ),
                )
            },
    )
}
