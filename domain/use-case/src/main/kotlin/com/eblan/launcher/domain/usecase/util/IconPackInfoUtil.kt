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
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.model.FastLauncherAppsActivityInfo
import com.eblan.launcher.domain.model.IconPackInfoComponent
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.File

internal suspend fun updateIconPackInfos(
    iconPackInfoPackageName: String,
    fileManager: FileManager,
    iconPackManager: IconPackManager,
    fastLauncherAppsActivityInfos: List<FastLauncherAppsActivityInfo>,
    iconKeyGenerator: IconKeyGenerator,
) {
    if (iconPackInfoPackageName.isEmpty()) return

    val iconPackInfoDirectory = File(
        fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
        iconPackInfoPackageName,
    ).apply { if (!exists()) mkdirs() }

    val appFilter = iconPackManager.getIconPackInfoComponents(packageName = iconPackInfoPackageName)

    val installedComponentHashedNames = buildSet {
        fastLauncherAppsActivityInfos.forEach {
            currentCoroutineContext().ensureActive()

            val file = File(
                iconPackInfoDirectory,
                iconKeyGenerator.getHashedName(name = it.componentName),
            )

            cacheIconPackFile(
                iconPackManager = iconPackManager,
                appFilter = appFilter,
                iconPackInfoPackageName = iconPackInfoPackageName,
                file = file,
                componentName = it.componentName,
            )

            add(iconKeyGenerator.getHashedName(name = it.componentName))
        }
    }

    iconPackInfoDirectory.listFiles()
        ?.filter {
            currentCoroutineContext().ensureActive()

            it.isFile && it.name !in installedComponentHashedNames
        }
        ?.forEach {
            currentCoroutineContext().ensureActive()

            it.delete()
        }
}

internal suspend fun cacheIconPackFile(
    iconPackManager: IconPackManager,
    appFilter: List<IconPackInfoComponent>,
    iconPackInfoPackageName: String,
    file: File,
    componentName: String,
) {
    appFilter.find {
        componentName == it.componentName.removePrefix("ComponentInfo{")
            .removeSuffix("}")
    }?.let {
        iconPackManager.createIconPackInfoPath(
            packageName = iconPackInfoPackageName,
            drawableName = it.drawableName,
            file = file,
        )
    }
}

internal suspend fun getIconPackInfoFilePaths(
    iconPackInfoPackageName: String,
    componentNames: List<String>,
    fileManager: FileManager,
    iconKeyGenerator: IconKeyGenerator,
): Map<String, String?> {
    if (iconPackInfoPackageName.isEmpty()) {
        return emptyMap()
    }

    val iconPacksDirectory = fileManager.getFilesDirectory(
        FileManager.ICON_PACKS_DIR,
    )

    val iconPackDirectory = File(
        iconPacksDirectory,
        iconPackInfoPackageName,
    )

    return componentNames.associateWith {
        val iconPackInfoFile = File(
            iconPackDirectory,
            iconKeyGenerator.getHashedName(name = it),
        )

        iconPackInfoFile
            .takeIf(File::exists)
            ?.absolutePath
    }.toMap()
}
