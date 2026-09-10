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
package com.eblan.launcher.domain.common

import java.io.File

interface FileManager {
    suspend fun getFilesDirectory(name: String): File

    suspend fun updateAndGetFilePath(
        directory: File,
        name: String,
        byteArray: ByteArray,
    ): String?

    companion object {
        const val ICONS_DIR = "icons"

        const val WIDGETS_DIR = "widgets"

        const val SHORTCUTS_DIR = "shortcuts"

        const val ICON_PACKS_DIR = "iconpacks"

        const val SHORTCUT_INTENT_ICONS_DIR = "shortcutsintenticons"

        const val CUSTOM_ICONS_DIR = "customicons"
    }
}
