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
package com.eblan.launcher.platform.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EblanNotificationListenerService : NotificationListenerService() {
    private val _statusBarNotifications =
        MutableStateFlow<Map<String, Int>>(emptyMap())

    val statusBarNotifications = _statusBarNotifications.asStateFlow()

    private var isListenerConnected = false

    override fun onBind(intent: Intent?): IBinder? = if (intent?.action == SERVICE_INTERFACE) {
        super.onBind(intent)
    } else {
        LocalBinder()
    }

    override fun onListenerConnected() {
        isListenerConnected = true
    }

    override fun onListenerDisconnected() {
        isListenerConnected = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        updateStatusBarNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        updateStatusBarNotifications()
    }

    private fun updateStatusBarNotifications() {
        if (isListenerConnected) {
            _statusBarNotifications.update {
                activeNotifications
                    ?.groupingBy { it.packageName }
                    ?.eachCount()
                    ?: emptyMap()
            }
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): EblanNotificationListenerService = this@EblanNotificationListenerService
    }
}
