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

import com.eblan.launcher.domain.platform.ContentResolverWrapper
import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.File
import java.io.IOException
import kotlin.uuid.Uuid

/**
 * Copies an image referenced by [uri] to the custom-icon cache and returns its absolute path.
 *
 * @return The saved file path, or `null` if the URI cannot be opened or writing fails.
 */
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

/**
 * Deletes each candidate custom-icon, folder-icon, or shortcut-intent-icon file in [iconPaths]
 * when it is no longer referenced by any application, folder, or grid item.
 */
internal suspend fun deleteCustomIconsIfUnreferenced(
    iconPaths: Iterable<String?>,
    eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    gridRepository: GridRepository,
) {
    val candidates = iconPaths.filterNotNull().distinct()

    if (candidates.isEmpty()) return

    val referencedPaths = buildSet {
        eblanApplicationInfoRepository.getEblanApplicationInfos()
            .mapNotNullTo(this) { it.customIcon }

        folderEblanApplicationInfoRepository.getFolderEblanApplicationInfos()
            .mapNotNullTo(this) { it.icon }

        val gridItems = gridRepository.getGridItems()

        gridItems.applicationInfoGridItems.mapNotNullTo(this) { it.customIcon }
        gridItems.shortcutInfoGridItems.mapNotNullTo(this) { it.customIcon }
        gridItems.shortcutConfigGridItems.mapNotNullTo(this) { it.customIcon }
        gridItems.shortcutConfigGridItems.mapNotNullTo(this) { it.shortcutIntentIcon }
        gridItems.folderGridItems.mapNotNullTo(this) { it.icon }
    }

    candidates.forEach { path ->
        currentCoroutineContext().ensureActive()

        if (path !in referencedPaths) {
            val file = File(path)

            if (file.exists()) {
                file.delete()
            }
        }
    }
}
