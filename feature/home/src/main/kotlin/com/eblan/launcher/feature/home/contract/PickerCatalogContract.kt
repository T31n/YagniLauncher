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
package com.eblan.launcher.feature.home.contract

import androidx.compose.runtime.Stable
import com.eblan.launcher.domain.model.application.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.launcherapps.EblanUser
import com.eblan.launcher.domain.model.shortcutconfig.EblanShortcutConfig
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfo
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfoByGroup
import com.eblan.launcher.domain.model.widget.EblanAppWidgetProviderInfo
import kotlinx.coroutines.flow.StateFlow

@Stable
internal interface WidgetCatalogContract {
    val eblanAppWidgetProviderInfos: StateFlow<Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>>>

    fun getEblanAppWidgetProviderInfosByLabel(label: String)
}

@Stable
internal interface AppWidgetCatalogContract {
    val eblanAppWidgetProviderInfosGroup: StateFlow<Map<String, List<EblanAppWidgetProviderInfo>>>
}

@Stable
internal interface ShortcutConfigCatalogContract {
    val eblanShortcutConfigs: StateFlow<Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>>

    fun getEblanShortcutConfigsByLabel(label: String)
}

@Stable
internal interface ShortcutInfoCatalogContract {
    val eblanShortcutInfosGroup: StateFlow<Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>>
}
