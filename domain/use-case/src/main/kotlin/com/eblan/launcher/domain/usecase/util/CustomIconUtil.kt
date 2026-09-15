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
package com.eblan.launcher.domain.usecase.util

import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.ContentResolverWrapper
import java.io.File
import java.io.IOException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal suspend fun getCustomIcon(
    contentResolverWrapper: ContentResolverWrapper,
    fileManager: FileManager,
    iconKeyGenerator: IconKeyGenerator,
    uri: String,
): String? = try {
    val bytes = contentResolverWrapper.openInputStream(uri = uri) ?: return null

    val customIconsDirectory =
        fileManager.getFilesDirectory(name = FileManager.CUSTOM_ICONS_DIR)

    val name = iconKeyGenerator.getHashedName(name = Uuid.random().toHexString())

    val newFile = File(customIconsDirectory, name)

    newFile.writeBytes(bytes)

    newFile.absolutePath
} catch (_: IOException) {
    null
}
