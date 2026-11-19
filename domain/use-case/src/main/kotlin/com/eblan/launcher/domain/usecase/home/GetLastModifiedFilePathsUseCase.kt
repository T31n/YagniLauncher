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
package com.eblan.launcher.domain.usecase.home

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.platform.FileManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/** Collects last-modified timestamps for cached icon and preview files managed by [FileManager]. */
class GetLastModifiedFilePathsUseCase @Inject constructor(
    private val fileManager: FileManager,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Scans all [FileManager] icon and preview directories and maps each file's absolute path to
     * its `lastModified` timestamp.
     *
     * @return A map of absolute file paths to last-modified timestamps in milliseconds.
     */
    suspend operator fun invoke(): Map<String, Long> = withContext(ioDispatcher) {
        val directories = listOf(
            FileManager.ICONS_DIR,
            FileManager.TINTED_ICONS_DIR,
            FileManager.SHAPED_ICONS_DIR,
            FileManager.TINTED_SHAPED_ICONS_DIR,
            FileManager.APPLICATION_ICONS_DIR,
            FileManager.SHORTCUT_CONFIG_ICONS_DIR,
            FileManager.WIDGETS_DIR,
            FileManager.SHORTCUTS_DIR,
            FileManager.ICON_PACKS_DIR,
            FileManager.SHORTCUT_INTENT_ICONS_DIR,
            FileManager.CUSTOM_ICONS_DIR,
        )

        buildMap {
            directories.forEach { directoryName ->
                currentCoroutineContext().ensureActive()

                val directory = fileManager.getFilesDirectory(name = directoryName)

                directory.walkTopDown()
                    .filter(File::isFile)
                    .forEach { file ->
                        currentCoroutineContext().ensureActive()

                        put(file.absolutePath, file.lastModified())
                    }
            }
        }
    }
}
