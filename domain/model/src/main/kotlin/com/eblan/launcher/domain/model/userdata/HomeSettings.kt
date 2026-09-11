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
package com.eblan.launcher.domain.model.userdata

import com.eblan.launcher.domain.model.grid.GridItemSettings

data class HomeSettings(
    val columns: Int,
    val rows: Int,
    val pageCount: Int,
    val infiniteScroll: Boolean,
    val dockColumns: Int,
    val dockRows: Int,
    val dockHeight: Int,
    val initialPage: Int,
    val wallpaperScroll: Boolean,
    val gridItemSettings: GridItemSettings,
    val lockScreenOrientation: Boolean,
    val dockPageCount: Int,
    val dockInfiniteScroll: Boolean,
    val dockInitialPage: Int,
    val addNewAppsToHomeScreen: Boolean,
    val folderCellWidth: Int,
    val folderCellHeight: Int,
    val maxFolderColumns: Int,
    val maxFolderRows: Int,
    val showPageIndicator: Boolean,
    val dockCustomBackgroundColor: Int,
    val dockPadding: Int,
    val dockTopStartCornerRadius: Int,
    val dockTopEndCornerRadius: Int,
    val dockBottomStartCornerRadius: Int,
    val dockBottomEndCornerRadius: Int,
    val folderCornerRadius: Int,
    val folderBackgroundColor: BackgroundColor,
    val customFolderBackgroundColor: Int,
)
