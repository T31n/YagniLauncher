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
package com.eblan.launcher.feature.home.util

import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.grid.GridItemData

internal fun getEblanApplicationInfoIconPath(
    data: EblanApplicationInfo,
    iconPackFilePath: String? = null,
): String? = getApplicationInfoIconPath(
    customIcon = data.customIcon,
    iconPackFilePath = iconPackFilePath,
    icon = data.icon,
)

internal fun getGridItemIconPath(
    data: GridItemData,
    iconPackFilePath: String? = null,
): String? = when (data) {
    is GridItemData.ApplicationInfo -> getApplicationInfoIconPath(
        customIcon = data.customIcon,
        iconPackFilePath = iconPackFilePath,
        icon = data.icon,
    )

    is GridItemData.Folder -> data.icon

    is GridItemData.ShortcutConfig -> {
        data.customIcon
            ?: data.shortcutIntentIcon
            ?: data.activityIcon
            ?: data.applicationIcon
    }

    is GridItemData.ShortcutInfo -> data.customIcon ?: data.icon

    is GridItemData.Widget -> data.icon
}

internal fun getFolderGridItemIconPath(
    data: FolderEblanApplicationInfoGridItemData,
    iconPackFilePath: String? = null,
): String? = when (data) {
    is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> getApplicationInfoIconPath(
        customIcon = data.customIcon,
        iconPackFilePath = iconPackFilePath,
        icon = data.icon,
    )

    is FolderEblanApplicationInfoGridItemData.Folder -> data.icon
}

internal fun getGridItemLabel(data: GridItemData): String? = when (data) {
    is GridItemData.ApplicationInfo -> data.customLabel ?: data.label

    is GridItemData.Folder -> data.label

    is GridItemData.ShortcutConfig -> {
        data.customLabel
            ?: data.shortcutIntentName
            ?: data.activityLabel
            ?: data.applicationLabel
    }

    is GridItemData.ShortcutInfo -> data.customShortLabel ?: data.shortLabel

    is GridItemData.Widget -> data.label
}

internal fun getEblanApplicationInfoLabel(data: EblanApplicationInfo): String = data.customLabel ?: data.label

internal fun getFolderGridItemLabel(data: FolderEblanApplicationInfoGridItemData): String = when (data) {
    is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> data.customLabel ?: data.label
    is FolderEblanApplicationInfoGridItemData.Folder -> data.label
}

private fun getApplicationInfoIconPath(
    customIcon: String?,
    iconPackFilePath: String?,
    icon: String?,
): String? = customIcon ?: iconPackFilePath ?: icon
