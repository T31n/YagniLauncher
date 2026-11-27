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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.eblan.launcher.domain.model.userdata.PageIndicatorType
import com.eblan.launcher.feature.home.util.calculatePage
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
internal fun PageIndicator(
    // Pager State and Pagination
    gridHorizontalPagerState: PagerState,
    pageCount: Int,
    infiniteScroll: Boolean,
    // Appearance and Layout
    color: Color,
    pageIndicatorType: PageIndicatorType,
    modifier: Modifier = Modifier,
) {
    if (pageCount < 1) return

    when (pageIndicatorType) {
        PageIndicatorType.Dots -> DotsPageIndicator(
            gridHorizontalPagerState = gridHorizontalPagerState,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
            color = color,
            modifier = modifier,
        )

        PageIndicatorType.Line -> LinePageIndicator(
            gridHorizontalPagerState = gridHorizontalPagerState,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
            color = color,
            modifier = modifier,
        )

        PageIndicatorType.Worm -> WormPageIndicator(
            gridHorizontalPagerState = gridHorizontalPagerState,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
            color = color,
            modifier = modifier,
        )

        PageIndicatorType.JumpingDot -> JumpingDotPageIndicator(
            gridHorizontalPagerState = gridHorizontalPagerState,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
            color = color,
            modifier = modifier,
        )

        PageIndicatorType.Equalizer -> EqualizerPageIndicator(
            gridHorizontalPagerState = gridHorizontalPagerState,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
            color = color,
            modifier = modifier,
        )

        PageIndicatorType.Diamonds -> DiamondsPageIndicator(
            gridHorizontalPagerState = gridHorizontalPagerState,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
            color = color,
            modifier = modifier,
        )

        PageIndicatorType.Numbers -> NumbersPageIndicator(
            gridHorizontalPagerState = gridHorizontalPagerState,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
            color = color,
            modifier = modifier,
        )

        PageIndicatorType.PacMan -> PacManPageIndicator(
            gridHorizontalPagerState = gridHorizontalPagerState,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
            color = color,
            modifier = modifier,
        )

        PageIndicatorType.None -> Unit
    }
}

@Composable
private fun DotsPageIndicator(
    gridHorizontalPagerState: PagerState,
    pageCount: Int,
    infiniteScroll: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(pageCount) { index ->
            DotIndicatorItem(
                gridHorizontalPagerState = gridHorizontalPagerState,
                index = index,
                pageCount = pageCount,
                infiniteScroll = infiniteScroll,
                color = color,
            )
        }
    }
}

@Composable
private fun DotIndicatorItem(
    gridHorizontalPagerState: PagerState,
    index: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    color: Color,
) {
    val baseWidth = 8.dp
    val baseHeight = 8.dp
    val activeWidth = 16.dp

    fun distance(): Float = pageDistance(
        pagePosition = pagerPosition(
            gridHorizontalPagerState = gridHorizontalPagerState,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
        ),
        index = index,
        pageCount = pageCount,
        infiniteScroll = infiniteScroll,
    )

    Box(
        modifier = Modifier
            .padding(3.dp)
            .layout { measurable, constraints ->
                val currentDistance = distance()
                val activeFraction = 1f - abs(currentDistance).coerceIn(0f, 1f)
                val widthDp = baseWidth + ((activeWidth - baseWidth) * activeFraction)

                val widthPx = constraints.constrainWidth(widthDp.roundToPx())
                val heightPx = constraints.constrainHeight(baseHeight.roundToPx())

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

@Composable
private fun LinePageIndicator(
    gridHorizontalPagerState: PagerState,
    pageCount: Int,
    infiniteScroll: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val trackWidth = (pageCount * 28).coerceIn(64, 160).dp
    val trackHeight = 4.dp

    Canvas(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight),
    ) {
        drawWithLayoutDirection {
            val cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
            val trackPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(Offset.Zero, size),
                        cornerRadius = cornerRadius,
                    ),
                )
            }

            drawRoundRect(
                color = color.copy(alpha = 0.3f),
                cornerRadius = cornerRadius,
            )

            val visualPosition = visualPagerPosition(
                gridHorizontalPagerState = gridHorizontalPagerState,
                pageCount = pageCount,
                infiniteScroll = infiniteScroll,
            )
            val segmentWidth = size.width / pageCount
            val startX = visualPosition * segmentWidth

            clipPath(trackPath) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x = startX, y = 0f),
                    size = Size(width = segmentWidth, height = size.height),
                    cornerRadius = cornerRadius,
                )
            }
        }
    }
}

@Composable
private fun WormPageIndicator(
    gridHorizontalPagerState: PagerState,
    pageCount: Int,
    infiniteScroll: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val dotDiameter = 8.dp
    val dotSpacing = 6.dp
    val step = dotDiameter + dotSpacing
    val totalWidth = step * (pageCount - 1) + dotDiameter

    Canvas(
        modifier = modifier
            .width(totalWidth)
            .height(dotDiameter),
    ) {
        drawWithLayoutDirection {
            val radius = dotDiameter.toPx() / 2f
            val stepPx = step.toPx()
            val centerY = size.height / 2f

            repeat(pageCount) { index ->
                drawCircle(
                    color = color.copy(alpha = 0.4f),
                    radius = radius,
                    center = Offset(x = radius + index * stepPx, y = centerY),
                )
            }

            val (fromPage, toPage, fraction) = resolveTransition(
                pagePosition = pagerPosition(
                    gridHorizontalPagerState = gridHorizontalPagerState,
                    pageCount = pageCount,
                    infiniteScroll = infiniteScroll,
                ),
                pageCount = pageCount,
                infiniteScroll = infiniteScroll,
            )

            val fromX = radius + fromPage * stepPx
            val toX = radius + toPage * stepPx

            val headProgress = (fraction * 2f).coerceIn(0f, 1f)
            val tailProgress = ((fraction - 0.5f) * 2f).coerceIn(0f, 1f)

            val headX = lerp(fromX, toX, headProgress)
            val tailX = lerp(fromX, toX, tailProgress)

            val left = min(headX, tailX) - radius
            val right = max(headX, tailX) + radius

            drawRoundRect(
                color = color,
                topLeft = Offset(x = left, y = 0f),
                size = Size(width = right - left, height = size.height),
                cornerRadius = CornerRadius(radius, radius),
            )
        }
    }
}

@Composable
private fun JumpingDotPageIndicator(
    gridHorizontalPagerState: PagerState,
    pageCount: Int,
    infiniteScroll: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val dotDiameter = 8.dp
    val dotSpacing = 8.dp
    val jumpHeight = 11.dp
    val step = dotDiameter + dotSpacing
    val totalWidth = step * (pageCount - 1) + dotDiameter + 4.dp
    val totalHeight = dotDiameter + jumpHeight + 4.dp

    Canvas(
        modifier = modifier
            .width(totalWidth)
            .height(totalHeight),
    ) {
        drawWithLayoutDirection {
            val baseRadius = dotDiameter.toPx() / 2f
            val stepPx = step.toPx()
            val startOffsetX = baseRadius + 2.dp.toPx()
            val baselineY = size.height - baseRadius - 2.dp.toPx()

            repeat(pageCount) { index ->
                drawCircle(
                    color = color.copy(alpha = 0.4f),
                    radius = baseRadius,
                    center = Offset(x = startOffsetX + index * stepPx, y = baselineY),
                )
            }

            val (fromPage, toPage, fraction) = resolveTransition(
                pagePosition = pagerPosition(
                    gridHorizontalPagerState = gridHorizontalPagerState,
                    pageCount = pageCount,
                    infiniteScroll = infiniteScroll,
                ),
                pageCount = pageCount,
                infiniteScroll = infiniteScroll,
            )

            val fromX = startOffsetX + fromPage * stepPx
            val toX = startOffsetX + toPage * stepPx
            val arc = sin(fraction * PI).toFloat()

            val activeX = lerp(fromX, toX, fraction)
            val activeY = baselineY - arc * jumpHeight.toPx()
            val activeRadius = baseRadius * (1f + 0.15f * arc)

            drawCircle(
                color = color,
                radius = activeRadius,
                center = Offset(x = activeX, y = activeY),
            )
        }
    }
}

@Composable
private fun EqualizerPageIndicator(
    gridHorizontalPagerState: PagerState,
    pageCount: Int,
    infiniteScroll: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val barWidth = 4.dp
    val barSpacing = 5.dp
    val minBarHeight = 6.dp
    val maxBarHeight = 18.dp
    val step = barWidth + barSpacing
    val totalWidth = step * (pageCount - 1) + barWidth

    Canvas(
        modifier = modifier
            .width(totalWidth)
            .height(maxBarHeight),
    ) {
        drawWithLayoutDirection {
            val barWidthPx = barWidth.toPx()
            val stepPx = step.toPx()
            val minHeightPx = minBarHeight.toPx()
            val maxHeightPx = maxBarHeight.toPx()
            val cornerRadius = CornerRadius(barWidthPx / 2f, barWidthPx / 2f)

            val position = pagerPosition(
                gridHorizontalPagerState = gridHorizontalPagerState,
                pageCount = pageCount,
                infiniteScroll = infiniteScroll,
            )

            repeat(pageCount) { index ->
                val rawDistance = rawPageDistance(
                    pagePosition = position,
                    index = index,
                    pageCount = pageCount,
                    infiniteScroll = infiniteScroll,
                )
                val rippleFraction = (1f - abs(rawDistance) / 1.4f).coerceIn(0f, 1f)
                val heightPx = lerp(minHeightPx, maxHeightPx, rippleFraction)
                val topY = (size.height - heightPx) / 2f
                val barColor = lerp(
                    start = color.copy(alpha = 0.4f),
                    stop = color,
                    fraction = (1f - abs(rawDistance)).coerceIn(0f, 1f),
                )

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x = index * stepPx, y = topY),
                    size = Size(width = barWidthPx, height = heightPx),
                    cornerRadius = cornerRadius,
                )
            }
        }
    }
}

@Composable
private fun DiamondsPageIndicator(
    gridHorizontalPagerState: PagerState,
    pageCount: Int,
    infiniteScroll: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val slotSize = 16.dp
    val totalWidth = slotSize * pageCount

    Canvas(
        modifier = modifier
            .width(totalWidth)
            .height(slotSize),
    ) {
        drawWithLayoutDirection {
            val slotPx = slotSize.toPx()
            val centerY = size.height / 2f
            val minSide = 5.5.dp.toPx()
            val maxSide = 9.dp.toPx()

            val position = pagerPosition(
                gridHorizontalPagerState = gridHorizontalPagerState,
                pageCount = pageCount,
                infiniteScroll = infiniteScroll,
            )

            repeat(pageCount) { index ->
                val distance = pageDistance(
                    pagePosition = position,
                    index = index,
                    pageCount = pageCount,
                    infiniteScroll = infiniteScroll,
                )
                val activeFraction = 1f - abs(distance).coerceIn(0f, 1f)
                val side = lerp(minSide, maxSide, activeFraction)
                val centerX = slotPx * index + slotPx / 2f
                val rotationDegrees = 45f + activeFraction * 90f
                val diamondColor = lerp(
                    start = color.copy(alpha = 0.45f),
                    stop = color,
                    fraction = activeFraction,
                )

                rotate(
                    degrees = rotationDegrees,
                    pivot = Offset(x = centerX, y = centerY),
                ) {
                    drawRoundRect(
                        color = diamondColor,
                        topLeft = Offset(x = centerX - side / 2f, y = centerY - side / 2f),
                        size = Size(width = side, height = side),
                        cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx()),
                    )
                }
            }
        }
    }
}

@Composable
private fun NumbersPageIndicator(
    gridHorizontalPagerState: PagerState,
    pageCount: Int,
    infiniteScroll: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val currentPage by remember(gridHorizontalPagerState, pageCount, infiniteScroll) {
        derivedStateOf {
            calculatePage(
                index = gridHorizontalPagerState.currentPage,
                infiniteScroll = infiniteScroll,
                pageCount = pageCount,
            )
        }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color = color.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${currentPage + 1} / $pageCount",
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private class PacManDirectionState {
    var lastPosition: Float = Float.NaN
    var facingForward: Boolean = true
}

@Composable
private fun PacManPageIndicator(
    gridHorizontalPagerState: PagerState,
    pageCount: Int,
    infiniteScroll: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val pacManDiameter = 12.dp
    val pelletDiameter = 4.dp
    val step = 14.dp
    val totalWidth = step * (pageCount - 1) + pacManDiameter
    val directionState = remember { PacManDirectionState() }

    Canvas(
        modifier = modifier
            .width(totalWidth)
            .height(pacManDiameter),
    ) {
        drawWithLayoutDirection {
            val pacRadius = pacManDiameter.toPx() / 2f
            val pelletRadius = pelletDiameter.toPx() / 2f
            val stepPx = step.toPx()
            val centerY = size.height / 2f

            val (fromPage, toPage, fraction) = resolveTransition(
                pagePosition = pagerPosition(
                    gridHorizontalPagerState = gridHorizontalPagerState,
                    pageCount = pageCount,
                    infiniteScroll = infiniteScroll,
                ),
                pageCount = pageCount,
                infiniteScroll = infiniteScroll,
            )

            val visualPosition = lerp(fromPage.toFloat(), toPage.toFloat(), fraction)
            if (!directionState.lastPosition.isNaN() &&
                abs(visualPosition - directionState.lastPosition) > 0.005f
            ) {
                directionState.facingForward = visualPosition > directionState.lastPosition
            }
            directionState.lastPosition = visualPosition

            val pacX = pacRadius + visualPosition * stepPx

            repeat(pageCount) { index ->
                val pelletX = pacRadius + index * stepPx
                if (abs(pelletX - pacX) > pacRadius * 0.6f) {
                    drawCircle(
                        color = color.copy(alpha = 0.45f),
                        radius = pelletRadius,
                        center = Offset(x = pelletX, y = centerY),
                    )
                }
            }

            val travelSpan = abs(toPage - fromPage).coerceAtLeast(1)
            val chomp = abs(sin(fraction * 2f * PI * travelSpan)).toFloat()
            val mouthHalfAngle = lerp(18f, 46f, chomp)
            val baseAngle = if (directionState.facingForward) 0f else 180f
            val pacSize = Size(width = pacRadius * 2f, height = pacRadius * 2f)

            drawArc(
                color = color,
                startAngle = baseAngle + mouthHalfAngle,
                sweepAngle = 360f - 2f * mouthHalfAngle,
                useCenter = true,
                topLeft = Offset(x = pacX - pacRadius, y = centerY - pacRadius),
                size = pacSize,
            )
        }
    }
}

private inline fun DrawScope.drawWithLayoutDirection(
    block: DrawScope.() -> Unit,
) {
    if (layoutDirection == LayoutDirection.Rtl) {
        scale(
            scaleX = -1f,
            scaleY = 1f,
            pivot = Offset(x = size.width / 2f, y = size.height / 2f),
        ) {
            block()
        }
    } else {
        block()
    }
}

private data class PageTransition(
    val fromPage: Int,
    val toPage: Int,
    val fraction: Float,
)

private fun pagerPosition(
    gridHorizontalPagerState: PagerState,
    pageCount: Int,
    infiniteScroll: Boolean,
): Float {
    val currentPage = calculatePage(
        index = gridHorizontalPagerState.currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )
    return currentPage + gridHorizontalPagerState.currentPageOffsetFraction
}

private fun visualPagerPosition(
    gridHorizontalPagerState: PagerState,
    pageCount: Int,
    infiniteScroll: Boolean,
): Float {
    val (fromPage, toPage, fraction) = resolveTransition(
        pagePosition = pagerPosition(
            gridHorizontalPagerState = gridHorizontalPagerState,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
        ),
        pageCount = pageCount,
        infiniteScroll = infiniteScroll,
    )
    return lerp(fromPage.toFloat(), toPage.toFloat(), fraction)
}

private fun rawPageDistance(
    pagePosition: Float,
    index: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
): Float {
    var diff = pagePosition - index
    if (infiniteScroll && pageCount > 1) {
        val half = pageCount / 2f
        while (diff > half) diff -= pageCount
        while (diff < -half) diff += pageCount
    }
    return diff
}

private fun pageDistance(
    pagePosition: Float,
    index: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
): Float = rawPageDistance(
    pagePosition = pagePosition,
    index = index,
    pageCount = pageCount,
    infiniteScroll = infiniteScroll,
).coerceIn(-1f, 1f)

private fun resolveTransition(
    pagePosition: Float,
    pageCount: Int,
    infiniteScroll: Boolean,
): PageTransition {
    if (pageCount <= 1) {
        return PageTransition(fromPage = 0, toPage = 0, fraction = 0f)
    }

    return if (infiniteScroll) {
        val base = floor(pagePosition).toInt()
        val fraction = (pagePosition - base).coerceIn(0f, 1f)
        val fromPage = base.mod(pageCount)
        val toPage = (base + 1).mod(pageCount)
        PageTransition(
            fromPage = fromPage,
            toPage = toPage,
            fraction = fraction,
        )
    } else {
        val clamped = pagePosition.coerceIn(0f, (pageCount - 1).toFloat())
        val fromPage = floor(clamped).toInt().coerceIn(0, pageCount - 1)
        val toPage = (fromPage + 1).coerceAtMost(pageCount - 1)
        val fraction = (clamped - fromPage).coerceIn(0f, 1f)
        PageTransition(
            fromPage = fromPage,
            toPage = toPage,
            fraction = fraction,
        )
    }
}
