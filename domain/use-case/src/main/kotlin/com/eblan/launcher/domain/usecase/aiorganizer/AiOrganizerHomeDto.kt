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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal val aiOrganizerJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
}

@Serializable
internal data class AiOrganizerRequestDto(
    val gridItems: List<AiRequestGridItemDto>,
    val dockGridItems: List<AiRequestGridItemDto>,
    val appDrawer: List<AiAppDto>,
    val settings: AiRequestSettingsDto,
)

@Serializable
internal data class AiOrganizerHomeDto(
    val gridItems: List<AiGridItemDto>,
    val dockGridItems: List<AiGridItemDto>? = null,
    val appDrawer: List<AiAppDto> = emptyList(),
    val appDrawerFolders: List<AiAppDrawerFolderDto> = emptyList(),
    val tags: List<AiTagDto>,
    val tagCrossRefs: List<AiTagCrossRefDto>,
    val settings: AiHomeSettingsDto,
)

@Serializable
internal enum class AiGridItemType {
    @SerialName("application")
    Application,

    @SerialName("folder")
    Folder,

    @SerialName("widget")
    Widget,

    @SerialName("shortcut")
    Shortcut,

    @SerialName("shortcutConfig")
    ShortcutConfig,
}

@Serializable
internal data class AiRequestGridItemDto(
    val id: String,
    val type: AiGridItemType,
    val componentName: String? = null,
    val packageName: String? = null,
    val serialNumber: Long? = null,
    val label: String? = null,
    val page: Int,
    val position: Int,
    val columnSpan: Int,
    val rowSpan: Int,
    val folderId: String? = null,
)

@Serializable
internal data class AiGridItemDto(
    val id: String,
    val type: AiGridItemType,
    val componentName: String? = null,
    val serialNumber: Long? = null,
    val label: String? = null,
    val page: Int,
    val position: Int,
    val folderId: String? = null,
)

@Serializable
internal data class AiAppDto(
    val componentName: String,
    val packageName: String,
    val serialNumber: Long,
    val label: String,
    val folderId: String? = null,
)

@Serializable
internal data class AiAppDrawerFolderDto(
    val id: String,
    val label: String,
)

@Serializable
internal data class AiRequestSettingsDto(
    val pageCount: Int,
    val columns: Int,
    val rows: Int,
    val dockPageCount: Int,
    val dockColumns: Int,
    val dockRows: Int,
)

@Serializable
internal data class AiHomeSettingsDto(
    val pageCount: Int,
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
