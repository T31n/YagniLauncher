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
package com.eblan.launcher.domain.model.grid

import com.eblan.launcher.domain.model.userdata.EblanAction

data class FolderGridItem(
    val id: String,
    val page: Int,
    val startColumn: Int,
    val startRow: Int,
    val columnSpan: Int,
    val rowSpan: Int,
    val associate: Associate,
    val label: String,
    val override: Boolean,
    val icon: String?,
    val gridItemSettings: GridItemSettings,
    val doubleTap: EblanAction,
    val swipeUp: EblanAction,
    val swipeDown: EblanAction,
    val index: Int,
    val folderId: String?,
)
