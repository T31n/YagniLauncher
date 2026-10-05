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
package com.eblan.launcher.framework.notificationmanager

import android.app.Notification
import android.os.Build
import androidx.annotation.RequiresApi

interface AndroidNotificationManagerWrapper {

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(
        channelId: String,
        name: String,
        importance: Int,
    )

    fun notify(id: Int, notification: Notification)

    companion object {
        const val CHANNEL_ID = "Eblan Launcher"
        const val ICON_PACK_SERVICE_NOTIFICATION_ID = 1
        const val CRASH_NOTIFICATION_ID = 2
    }
}
