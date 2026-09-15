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
package com.eblan.launcher.ui.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.eblan.launcher.domain.model.iconpackinfo.PackageManagerIconPackInfo

sealed interface SettingsItem {
    data class Column(
        val title: String,
        val subtitle: String,
        val onClick: () -> Unit,
    ) : SettingsItem

    data class Row(
        val imageVector: ImageVector,
        val title: String,
        val subtitle: String,
        val onClick: () -> Unit,
    ) : SettingsItem

    data class Switch(
        val title: String,
        val subtitle: String,
        val checked: Boolean,
        val onClick: () -> Unit,
        val onCheckedChange: (Boolean) -> Unit,
    ) : SettingsItem

    data class CustomBackgroundColor(
        val title: String,
        val customBackgroundColor: Int,
        val onClick: () -> Unit,
    ) : SettingsItem

    data class CustomIcon(
        val customIcon: String?,
        val packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
        val onUpdateIconPackInfoPackageName: (
            packageName: String,
            label: String?,
        ) -> Unit,
        val onUpdateUri: (String) -> Unit,
        val onResetCustomIcon: () -> Unit,
    ) : SettingsItem
}
