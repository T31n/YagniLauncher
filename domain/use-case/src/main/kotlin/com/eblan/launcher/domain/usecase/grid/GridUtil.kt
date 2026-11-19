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
package com.eblan.launcher.domain.usecase.grid

import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.usecase.util.deleteCustomIconsIfUnreferenced

/** Deletes the custom icon file referenced by a supported grid-item type when unreferenced. */
internal suspend fun deleteGridItemCustomIconFile(
    gridItem: GridItem,
    eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    gridRepository: GridRepository,
) {
    val customIconPath = when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo -> data.customIcon
        is GridItemData.ShortcutConfig -> data.customIcon
        is GridItemData.ShortcutInfo -> data.customIcon
        is GridItemData.Folder -> data.icon
        else -> null
    } ?: return

    deleteCustomIconsIfUnreferenced(
        iconPaths = listOf(customIconPath),
        eblanApplicationInfoRepository = eblanApplicationInfoRepository,
        folderEblanApplicationInfoRepository = folderEblanApplicationInfoRepository,
        gridRepository = gridRepository,
    )
}
