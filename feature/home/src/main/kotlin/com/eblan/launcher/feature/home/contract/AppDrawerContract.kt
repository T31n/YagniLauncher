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
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.application.GetEblanApplicationInfosByLabelAndTag
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.MoveFolderEblanApplicationInfoGridItemResult
import kotlinx.coroutines.flow.StateFlow

@Stable
internal interface AppDrawerContract {
    val getEblanApplicationInfosByLabelAndTag: StateFlow<GetEblanApplicationInfosByLabelAndTag>
    val eblanApplicationInfoTags: StateFlow<List<EblanApplicationInfoTag>>

    fun getEblanApplicationInfosByLabel(label: String)

    fun getEblanApplicationInfosByTagId(id: Long?)
}

@Stable
internal interface AppDrawerFolderContract {
    val folderEblanApplicationInfoPopups: StateFlow<Map<String, FolderEblanApplicationInfoPopup>>
    val folderEblanApplicationInfoEntries: StateFlow<List<FolderEntry>>
    val moveFolderEblanApplicationInfoGridItemResult: StateFlow<MoveFolderEblanApplicationInfoGridItemResult?>

    fun upsertFolderEblanApplicationInfoEntry(folderEntry: FolderEntry)

    fun deleteFolderEblanApplicationInfoEntry(folderEntry: FolderEntry)

    fun resetFolderEblanApplicationInfoEntries()

    fun closeFolderEblanApplicationInfoEntries()

    fun moveFolderEblanApplicationInfoGridItem(
        folderGridItemPopup: FolderEblanApplicationInfoPopup,
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    )

    fun updateMoveFolderEblanApplicationInfoGridItemResult(
        moveFolderEblanApplicationInfoGridItemResult: MoveFolderEblanApplicationInfoGridItemResult,
    )

    fun resetGridAfterMoveFolderEblanApplicationInfo()

    fun moveFolderEblanApplicationInfoGridItemOutsideFolder(
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        movingGridItem: GridItem,
    )

    fun dragFolderEblanApplicationInfoToGrid(
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        movingGridItem: GridItem,
    )

    fun deleteFolderEblanApplicationInfoGridItems(folderId: String)
}
