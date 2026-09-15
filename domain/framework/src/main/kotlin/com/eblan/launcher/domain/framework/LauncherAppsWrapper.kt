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
package com.eblan.launcher.domain.framework

import com.eblan.launcher.domain.model.launcherapps.EblanUser
import com.eblan.launcher.domain.model.launcherapps.FastLauncherAppsActivityInfo
import com.eblan.launcher.domain.model.launcherapps.FastLauncherAppsShortcutInfo
import com.eblan.launcher.domain.model.launcherapps.LauncherAppsActivityInfo
import com.eblan.launcher.domain.model.launcherapps.LauncherAppsShortcutInfo
import com.eblan.launcher.domain.model.launcherapps.ShortcutConfigActivityInfo
import com.eblan.launcher.domain.model.launcherapps.ShortcutQuery

interface LauncherAppsWrapper {
    val hasShortcutHostPermission: Boolean

    suspend fun getActivityListWithCacheIcons(): List<LauncherAppsActivityInfo>

    suspend fun getFastActivityList(): List<FastLauncherAppsActivityInfo>

    suspend fun getActivityListWithCacheIcons(
        serialNumber: Long,
        packageName: String,
    ): List<LauncherAppsActivityInfo>

    suspend fun getFastActivityList(
        serialNumber: Long,
        packageName: String,
    ): List<FastLauncherAppsActivityInfo>

    suspend fun getShortcutsWithCacheIcons(shortcutQuery: ShortcutQuery?): List<LauncherAppsShortcutInfo>?

    suspend fun getFastShortcuts(shortcutQuery: ShortcutQuery?): List<FastLauncherAppsShortcutInfo>?

    suspend fun getShortcutsByPackageNameWithCacheIcons(
        serialNumber: Long,
        packageName: String,
    ): List<LauncherAppsShortcutInfo>?

    suspend fun getShortcutConfigActivityListWithCacheIcons(
        serialNumber: Long,
        packageName: String,
    ): List<ShortcutConfigActivityInfo>

    suspend fun getUser(serialNumber: Long): EblanUser

    fun pinShortcuts(
        packageName: String,
        shortcutIds: List<String>,
        serialNumber: Long,
    )
}
