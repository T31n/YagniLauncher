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
package com.eblan.launcher.data.room.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItemSettings

@Entity
data class WidgetGridItemEntity(
    @PrimaryKey
    val id: String,
    val page: Int,
    val startColumn: Int,
    val startRow: Int,
    val columnSpan: Int,
    val rowSpan: Int,
    val associate: Associate,
    val appWidgetId: Int,
    val packageName: String,
    val componentName: String,
    val configure: String?,
    val minWidth: Int,
    val minHeight: Int,
    val resizeMode: Int,
    val minResizeWidth: Int,
    val minResizeHeight: Int,
    val maxResizeWidth: Int,
    val maxResizeHeight: Int,
    val targetCellHeight: Int,
    val targetCellWidth: Int,
    val preview: String?,
    val label: String?,
    val icon: String?,
    val override: Boolean,
    val serialNumber: Long,
    @Embedded val gridItemSettings: GridItemSettings,
)
