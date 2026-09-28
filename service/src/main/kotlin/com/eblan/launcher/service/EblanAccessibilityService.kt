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
package com.eblan.launcher.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import com.eblan.launcher.domain.model.home.GlobalAction

class EblanAccessibilityService : AccessibilityService() {
    private val globalActionBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                GlobalAction.NAME -> {
                    when (intent.getStringExtra(GlobalAction.GLOBAL_ACTION_TYPE)) {
                        GlobalAction.Notifications.name ->
                            performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)

                        GlobalAction.QuickSettings.name ->
                            performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)

                        GlobalAction.LockScreen.name ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                            }

                        GlobalAction.Recents.name ->
                            performGlobalAction(GLOBAL_ACTION_RECENTS)
                    }
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override fun onServiceConnected() {
        ContextCompat.registerReceiver(
            this,
            globalActionBroadcastReceiver,
            IntentFilter(GlobalAction.NAME),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    override fun onDestroy() {
        unregisterReceiver(globalActionBroadcastReceiver)
    }
}
