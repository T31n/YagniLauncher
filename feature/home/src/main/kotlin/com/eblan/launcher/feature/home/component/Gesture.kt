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

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

internal enum class HomeGesture {
    SwipeLeft,
    SwipeRight,
    DoubleTapSwipeUp,
    DoubleTapSwipeDown,
    TwoFingerSwipeUp,
    TwoFingerSwipeDown,
    PinchIn,
    PinchOut,
    RotateCounterClockwise,
    RotateClockwise,
}

private data class TapRecord(
    val position: Offset,
    val uptimeMillis: Long,
)

private data class TwoFingerSample(
    val distance: Float,
    val angle: Float,
    val centroid: Offset,
)

private const val PINCH_SCALE_THRESHOLD = 0.25f
private const val ROTATION_THRESHOLD_DEGREES = 25f

private fun PointerInputChange.isDownChange(): Boolean = pressed && !previousPressed

private fun PointerInputChange.isUpChange(): Boolean = !pressed && previousPressed

@Composable
internal fun Modifier.homeGestures(
    // State
    pagerState: PagerState,
    // Vertical Drag
    onVerticalDragStart: () -> Unit,
    onVerticalDrag: (dragAmount: Float) -> Unit,
    onVerticalDragEnd: () -> Unit,
    onVerticalDragCancel: () -> Unit,
    // Taps
    onDoubleTap: () -> Unit,
    // Gestures
    onGesture: (HomeGesture) -> Unit,
): Modifier {
    val viewConfiguration = LocalViewConfiguration.current

    val currentPagerState by rememberUpdatedState(pagerState)
    val currentOnVerticalDragStart by rememberUpdatedState(onVerticalDragStart)
    val currentOnVerticalDrag by rememberUpdatedState(onVerticalDrag)
    val currentOnVerticalDragEnd by rememberUpdatedState(onVerticalDragEnd)
    val currentOnVerticalDragCancel by rememberUpdatedState(onVerticalDragCancel)
    val currentOnDoubleTap by rememberUpdatedState(onDoubleTap)
    val currentOnGesture by rememberUpdatedState(onGesture)

    return pointerInput(key1 = viewConfiguration) {
        val touchSlop = viewConfiguration.touchSlop
        val swipeThreshold = 48.dp.toPx()
        val doubleTapDistance = 100.dp.toPx()
        var lastTap: TapRecord? = null

        awaitEachGesture {
            val firstDown = awaitFirstDown(requireUnconsumed = false)
            val canTap = !firstDown.isConsumed
            val isDoubleTapCandidate = canTap && lastTap?.let { tap ->
                firstDown.uptimeMillis - tap.uptimeMillis < viewConfiguration.doubleTapTimeoutMillis &&
                    (firstDown.position - tap.position).getDistance() < doubleTapDistance
            } == true
            lastTap = null

            val firstId = firstDown.id
            var secondId: PointerId? = null
            var firstPosition = firstDown.position
            var lastFirstPosition = firstDown.position
            var secondPosition = Offset.Zero
            var initialTwoFinger: TwoFingerSample? = null
            var isTwoFingerResolved = false
            var isModeResolved = false
            var isVerticalDrag = false
            var isHorizontalDrag = false
            var isDoubleTapSwipe = false
            var isUp = false

            while (!isUp) {
                val changes = awaitPointerEvent().changes

                val secondDown = changes.firstOrNull { it.isDownChange() && it.id != firstId }
                if (secondId == null && secondDown != null) {
                    if (isVerticalDrag) currentOnVerticalDragCancel()
                    secondId = secondDown.id
                }

                changes.forEach { change ->
                    if (change.id == firstId) {
                        firstPosition = change.position
                    } else if (change.id == secondId) {
                        secondPosition = change.position
                    }
                }

                if (secondId != null) {
                    val sample = sampleTwoFingers(first = firstPosition, second = secondPosition)
                    val initial = initialTwoFinger ?: sample.also { initialTwoFinger = it }

                    if (!isTwoFingerResolved && changes.any { it.isUpChange() }) {
                        isTwoFingerResolved = true

                        classifyTwoFingerGesture(
                            initial = initial,
                            latest = sample,
                            swipeThreshold = swipeThreshold,
                        )?.let { currentOnGesture(it) }
                    }
                } else {
                    val totalDelta = firstPosition - firstDown.position
                    val frameDelta = firstPosition - lastFirstPosition
                    lastFirstPosition = firstPosition

                    val isFirstConsumed = changes.any { it.id == firstId && it.isConsumed }

                    if (!isModeResolved && totalDelta.getDistance() > touchSlop) {
                        isModeResolved = true
                        when {
                            abs(totalDelta.y) <= abs(totalDelta.x) -> isHorizontalDrag = true

                            isDoubleTapCandidate -> isDoubleTapSwipe = true

                            // Resolved with no flag set: the gesture is ignored until release.
                            isFirstConsumed -> Unit

                            else -> {
                                currentOnVerticalDragStart()
                                currentOnVerticalDrag(totalDelta.y)
                                isVerticalDrag = true
                            }
                        }
                    } else if (isVerticalDrag) {
                        currentOnVerticalDrag(frameDelta.y)
                    }

                    if (isVerticalDrag) {
                        changes.forEach { change ->
                            if (change.id == firstId) change.consume()
                        }
                    }

                    val releasedChange = changes.firstOrNull { it.id == firstId && it.isUpChange() }
                    if (releasedChange != null) {
                        when {
                            !isModeResolved -> {
                                val pressDuration = releasedChange.uptimeMillis - firstDown.uptimeMillis
                                if (canTap && pressDuration < viewConfiguration.longPressTimeoutMillis) {
                                    if (isDoubleTapCandidate) {
                                        currentOnDoubleTap()
                                    } else {
                                        lastTap = TapRecord(
                                            position = firstDown.position,
                                            uptimeMillis = releasedChange.uptimeMillis,
                                        )
                                    }
                                }
                            }

                            isVerticalDrag -> currentOnVerticalDragEnd()

                            isDoubleTapSwipe -> when {
                                totalDelta.y <= -swipeThreshold -> currentOnGesture(HomeGesture.DoubleTapSwipeUp)
                                totalDelta.y >= swipeThreshold -> currentOnGesture(HomeGesture.DoubleTapSwipeDown)
                            }

                            isHorizontalDrag -> {
                                val isHorizontalSwipe = abs(totalDelta.x) >= swipeThreshold &&
                                    abs(totalDelta.x) > abs(totalDelta.y)

                                if (isHorizontalSwipe) {
                                    when {
                                        totalDelta.x < 0f && !currentPagerState.canScrollForward -> {
                                            currentOnGesture(HomeGesture.SwipeLeft)
                                        }

                                        totalDelta.x > 0f && !currentPagerState.canScrollBackward -> {
                                            currentOnGesture(HomeGesture.SwipeRight)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                val isFirstReleased = changes.any { it.id == firstId && it.isUpChange() }
                isUp = isFirstReleased || changes.none { it.pressed }
            }
        }
    }
}

private fun sampleTwoFingers(first: Offset, second: Offset): TwoFingerSample = TwoFingerSample(
    distance = (second - first).getDistance(),
    angle = atan2(second.y - first.y, second.x - first.x),
    centroid = Offset(
        x = (first.x + second.x) / 2f,
        y = (first.y + second.y) / 2f,
    ),
)

// Angles use screen coordinates (y points down), so a positive rotation is clockwise.
private fun classifyTwoFingerGesture(
    initial: TwoFingerSample,
    latest: TwoFingerSample,
    swipeThreshold: Float,
): HomeGesture? {
    if (initial.distance <= 0f) return null

    val pinchScale = latest.distance / initial.distance
    val pinchScore = abs(pinchScale - 1f) / PINCH_SCALE_THRESHOLD

    val rotationDelta = latest.angle - initial.angle
    val rotationDegrees = Math.toDegrees(atan2(sin(rotationDelta), cos(rotationDelta)).toDouble()).toFloat()
    val rotationScore = abs(rotationDegrees) / ROTATION_THRESHOLD_DEGREES

    val deltaX = latest.centroid.x - initial.centroid.x
    val deltaY = latest.centroid.y - initial.centroid.y
    val swipeScore = if (abs(deltaY) > abs(deltaX)) abs(deltaY) / swipeThreshold else 0f

    val maxScore = maxOf(pinchScore, rotationScore, swipeScore)

    return when {
        maxScore < 1f -> null

        swipeScore == maxScore -> {
            if (deltaY < 0f) HomeGesture.TwoFingerSwipeUp else HomeGesture.TwoFingerSwipeDown
        }

        pinchScore == maxScore -> {
            if (pinchScale < 1f) HomeGesture.PinchIn else HomeGesture.PinchOut
        }

        else -> {
            if (rotationDegrees > 0f) HomeGesture.RotateClockwise else HomeGesture.RotateCounterClockwise
        }
    }
}
