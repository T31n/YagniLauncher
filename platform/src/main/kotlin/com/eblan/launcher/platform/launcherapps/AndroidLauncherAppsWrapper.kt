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
package com.eblan.launcher.platform.launcherapps

import android.content.Intent
import android.content.IntentSender
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Rect
import android.os.Handler

interface AndroidLauncherAppsWrapper {

    fun getPinItemRequest(intent: Intent): LauncherApps.PinItemRequest

    fun startMainActivity(
        serialNumber: Long,
        componentName: String,
        sourceBounds: Rect,
    )

    fun startShortcut(
        serialNumber: Long,
        packageName: String,
        id: String,
        sourceBounds: Rect,
    )

    fun startShortcut(
        packageName: String,
        id: String,
        sourceBounds: Rect,
    )

    suspend fun getShortcutBadgedIconByteArray(
        shortcutInfo: ShortcutInfo?,
        density: Int,
    ): ByteArray?

    fun startAppDetailsActivity(
        serialNumber: Long,
        componentName: String,
        sourceBounds: Rect,
    )

    suspend fun getShortcutConfigIntent(
        serialNumber: Long,
        packageName: String,
        componentName: String,
    ): IntentSender?

    fun getPrivateSpaceSettingsIntent(): IntentSender?

    fun registerCallback(
        callback: LauncherApps.Callback,
        handler: Handler,
    )

    fun unregisterCallback(callback: LauncherApps.Callback)
}
