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

import com.eblan.launcher.domain.model.iconpackinfo.IconPackComponent
import com.eblan.launcher.domain.model.userdata.GeneralSettings
import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.domain.platform.IconPackManager
import java.io.File

/** Caches the icon-pack drawable for a component when the pack defines a matching entry. */
internal suspend fun cacheIconPackFile(
    iconPackManager: IconPackManager,
    appFilter: List<IconPackComponent>,
    iconPackPackageName: String,
    file: File,
    componentName: String,
    generalSettings: GeneralSettings,
) {
    appFilter.find { currentIconPackComponent ->
        componentName == currentIconPackComponent.componentName.removePrefix("ComponentInfo{")
            .removeSuffix("}")
    }?.let { currentIconPackComponent ->
        iconPackManager.createIconPackPath(
            packageName = iconPackPackageName,
            drawableName = currentIconPackComponent.drawableName,
            file = file,
            iconTint = generalSettings.iconTint,
            colorSource = generalSettings.colorSource,
            customSeedColor = generalSettings.customSeedColor,
            iconShape = generalSettings.iconShape,
            theme = generalSettings.theme,
            colorSchemeSettings = generalSettings.colorSchemeSettings,
        )
    }
}

/**
 * Resolves cached icon-pack file paths for the requested components.
 *
 * @return A map from component name to an existing cached file path, or `null` when absent.
 */
internal suspend fun getIconPackFilePaths(
    iconPackPackageName: String,
    componentNames: List<String>,
    fileManager: FileManager,
    iconKeyGenerator: IconKeyGenerator,
): Map<String, String?> {
    if (iconPackPackageName.isEmpty()) {
        return emptyMap()
    }

    val iconPacksDirectory = fileManager.getFilesDirectory(
        FileManager.ICON_PACKS_DIR,
    )

    val iconPackDirectory = File(
        iconPacksDirectory,
        iconPackPackageName,
    )

    return componentNames.associateWith { currentComponentName ->
        val iconPackInfoFile = File(
            iconPackDirectory,
            iconKeyGenerator.getHashedName(name = currentComponentName),
        )

        iconPackInfoFile
            .takeIf(File::exists)
            ?.absolutePath
    }.toMap()
}
