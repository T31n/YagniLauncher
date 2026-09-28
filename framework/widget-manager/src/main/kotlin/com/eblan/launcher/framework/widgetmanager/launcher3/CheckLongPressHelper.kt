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
package com.eblan.launcher.framework.widgetmanager.launcher3

import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

internal class CheckLongPressHelper(
    private val view: View,
    private val listener: View.OnLongClickListener,
) {
    private val slop = ViewConfiguration.get(view.context).scaledTouchSlop.toFloat()

    private var hasPerformedLongPress = false

    private var pendingCheckForLongPress: Runnable? = null

    private val downPoint = PointF()

    fun onTouchEvent(ev: MotionEvent) {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downPoint.set(ev.x, ev.y)

                cancelLongPress()

                postCheckForLongPress()
            }

            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP,
            -> cancelLongPress()

            MotionEvent.ACTION_MOVE -> {
                val dx = ev.x - downPoint.x
                val dy = ev.y - downPoint.y

                if (dx * dx + dy * dy > slop * slop) {
                    cancelLongPress()
                }
            }
        }
    }

    fun cancelLongPress() {
        hasPerformedLongPress = false

        clearCallbacks()
    }

    fun hasPerformedLongPress(): Boolean = hasPerformedLongPress

    private fun postCheckForLongPress() {
        hasPerformedLongPress = false

        if (pendingCheckForLongPress == null) {
            pendingCheckForLongPress = Runnable { triggerLongPress() }
        }

        view.postDelayed(
            pendingCheckForLongPress,
            ViewConfiguration.getLongPressTimeout().toLong(),
        )
    }

    private fun triggerLongPress() {
        if (view.parent != null &&
            view.hasWindowFocus() &&
            !view.isPressed &&
            !hasPerformedLongPress
        ) {
            listener.onLongClick(view)
        }
    }

    private fun clearCallbacks() {
        if (pendingCheckForLongPress != null) {
            view.removeCallbacks(pendingCheckForLongPress)

            pendingCheckForLongPress = null
        }
    }
}
