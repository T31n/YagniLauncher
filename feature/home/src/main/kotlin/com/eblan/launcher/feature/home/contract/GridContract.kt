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
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.folder.FolderPopup
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.domain.model.launcherapps.PinItemRequestType
import com.eblan.launcher.feature.home.model.GridItemSource
import kotlinx.coroutines.flow.StateFlow

@Stable
internal interface GridMoveResultContract {
    val movedGridItemResult: StateFlow<MoveGridItemResult?>
}

@Stable
internal interface GridMoveContract {
    val gridItemSource: StateFlow<GridItemSource?>
    val pinGridItem: StateFlow<GridItem?>

    fun moveGridItem(
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    )

    fun cancelPendingMoveGridItem()

    fun updateMoveGridItemResult(moveGridItemResult: MoveGridItemResult)

    fun updateGridItemsAfterMove(moveGridItemResult: MoveGridItemResult)

    fun updateGridItemsAfterMoveNewFolder(
        folderGridItems: List<GridItem>,
        moveGridItemResult: MoveGridItemResult,
    )

    fun updateGridItemSource(gridItemSource: GridItemSource)

    fun deleteGridItem(gridItem: GridItem)

    fun resetGridAfterDeleteGridItem(gridItem: GridItem)

    fun getPinGridItem(pinItemRequestType: PinItemRequestType)

    fun resetPinGridItem()

    fun updateShortcutConfigIntoShortcutInfoGridItem(
        moveGridItemResult: MoveGridItemResult,
        pinItemRequestType: PinItemRequestType.ShortcutInfo,
    )

    fun updateShortcutConfigGridItem(
        moveGridItemResult: MoveGridItemResult,
        shortcutIntentName: String?,
        shortcutIntentIcon: ByteArray?,
        shortcutIntentUri: String?,
    )
}

@Stable
internal interface GridResizeContract {
    val resizeGridItem: StateFlow<GridItem?>

    fun resizeGridItem(
        resizingGridItem: GridItem,
        columns: Int,
        rows: Int,
    )

    fun updateResizeGridItem(resizeGridItem: GridItem)

    fun resetGridAfterResize()
}

@Stable
internal interface GridFolderContract {
    val folderGridItemEntries: StateFlow<List<FolderEntry>>

    fun upsertFolderGridItemEntry(folderEntry: FolderEntry)

    fun deleteFolderGridItemEntry(folderEntry: FolderEntry)

    fun resetFolderGridItemEntries()

    fun closeFolderGridItemEntries()

    fun showFolderWhenDragging(
        folderEntry: FolderEntry,
        movingGridItem: GridItem,
    )

    fun moveFolderGridItem(
        folderPopup: FolderPopup,
        movingGridItem: GridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    )

    fun moveFolderGridItemOutsideFolder(movingGridItem: GridItem)

    fun resetGridAfterMoveFolder()
}
