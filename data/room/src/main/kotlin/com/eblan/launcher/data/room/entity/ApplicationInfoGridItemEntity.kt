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
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.userdata.EblanAction

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = FolderGridItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("folderId"),
    ],
)
data class ApplicationInfoGridItemEntity(
    @PrimaryKey
    val id: String,
    val page: Int,
    val startColumn: Int,
    val startRow: Int,
    val columnSpan: Int,
    val rowSpan: Int,
    val associate: Associate,
    val componentName: String,
    val packageName: String,
    val icon: String?,
    val label: String,
    val override: Boolean,
    val serialNumber: Long,
    val customIcon: String?,
    val customLabel: String?,
    @Embedded val gridItemSettings: GridItemSettings,
    @Embedded(prefix = "doubleTap_") val doubleTap: EblanAction,
    @Embedded(prefix = "swipeUp_") val swipeUp: EblanAction,
    @Embedded(prefix = "swipeDown_") val swipeDown: EblanAction,
    val index: Int,
    val folderId: String?,
)
