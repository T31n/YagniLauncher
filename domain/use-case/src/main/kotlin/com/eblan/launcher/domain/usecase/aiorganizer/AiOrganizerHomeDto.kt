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
package com.eblan.launcher.domain.usecase.aiorganizer

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal val aiOrganizerJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@Serializable
internal data class AiOrganizerRequestDto(
    val gridItems: List<AiGridItemDto>,
    val appDrawer: List<AiAppDto>,
    val settings: AiRequestSettingsDto,
)

@Serializable
internal data class AiOrganizerHomeDto(
    val gridItems: List<AiGridItemDto> = emptyList(),
    val appDrawer: List<AiAppDto> = emptyList(),
    val tags: List<AiTagDto> = emptyList(),
    val tagCrossRefs: List<AiTagCrossRefDto> = emptyList(),
    val settings: AiHomeSettingsDto,
)

@Serializable
internal data class AiGridItemDto(
    val componentName: String,
    val packageName: String,
    val label: String,
    val page: Int,
    val position: Int,
)

@Serializable
internal data class AiAppDto(
    val componentName: String,
    val packageName: String,
    val serialNumber: Long,
    val label: String,
)

@Serializable
internal data class AiRequestSettingsDto(
    val pageCount: Int,
    val columns: Int,
    val rows: Int,
    val occupiedPositions: List<AiOccupiedPageDto> = emptyList(),
)

@Serializable
internal data class AiOccupiedPageDto(
    val page: Int,
    val positions: List<Int>,
)

@Serializable
internal data class AiHomeSettingsDto(
    val pageCount: Int,
    val columns: Int? = null,
    val rows: Int? = null,
)

@Serializable
internal data class AiTagDto(
    val id: Long,
    val name: String,
    val index: Int,
)

@Serializable
internal data class AiTagCrossRefDto(
    val componentName: String,
    val serialNumber: Long,
    val id: Long,
)
