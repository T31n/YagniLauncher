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
package com.eblan.launcher.domain.usecase.cache

import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanIconPackInfoRepository
import com.eblan.launcher.domain.repository.FileReferenceRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import java.io.File

/** Cache directories whose files are tracked by a path column in the database. */
internal val referenceCheckedDirectoryNames = listOf(
    FileManager.ICONS_DIR,
    FileManager.TINTED_ICONS_DIR,
    FileManager.SHAPED_ICONS_DIR,
    FileManager.TINTED_SHAPED_ICONS_DIR,
    FileManager.WIDGETS_DIR,
    FileManager.SHORTCUTS_DIR,
    FileManager.SHORTCUT_INTENT_ICONS_DIR,
    FileManager.CUSTOM_ICONS_DIR,
)

/**
 * The result of scanning the cache.
 *
 * @property totalSize Total size in bytes of all files in the managed cache directories.
 * @property reclaimableSize Total size in bytes of unreferenced files that can be deleted.
 * @property unreferencedFiles The files that are not used anymore and can be deleted.
 */
internal data class CacheSnapshot(
    val totalSize: Long,
    val reclaimableSize: Long,
    val unreferencedFiles: List<File>,
)

/** Loads everything [scanCache] needs from the file system, the database and the settings. */
internal suspend fun getCacheSnapshot(
    fileManager: FileManager,
    fileReferenceRepository: FileReferenceRepository,
    eblanIconPackInfoRepository: EblanIconPackInfoRepository,
    eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    userDataRepository: UserDataRepository,
    iconKeyGenerator: IconKeyGenerator,
): CacheSnapshot {
    val activeIconPackPackageName =
        userDataRepository.userDataFlow.first().generalSettings.iconPackPackageName

    val keptIconPackPackageNames = buildSet {
        eblanIconPackInfoRepository.getEblanIconPacks().forEach { add(it.packageName) }

        if (activeIconPackPackageName.isNotEmpty()) add(activeIconPackPackageName)
    }

    val installedIconPackFileNames = buildSet {
        eblanApplicationInfoRepository.getEblanApplicationInfos().forEach {
            currentCoroutineContext().ensureActive()

            add(iconKeyGenerator.getHashedName(name = it.componentName))
        }
    }

    return scanCache(
        referenceCheckedDirectories = referenceCheckedDirectoryNames.map {
            fileManager.getFilesDirectory(name = it)
        },
        iconPacksDirectory = fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
        referencedPaths = fileReferenceRepository.getReferencedFilePaths(),
        keptIconPackPackageNames = keptIconPackPackageNames,
        installedIconPackFileNames = installedIconPackFileNames,
    )
}

/**
 * Splits the cached files into the ones that are still in use and the ones that can be deleted.
 *
 * @param referenceCheckedDirectories Flat directories whose files are tracked by the database.
 * @param iconPacksDirectory Directory containing one sub directory per icon pack.
 * @param referencedPaths File paths that are referenced by the database.
 * @param keptIconPackPackageNames Icon packs that must keep their cache directory.
 * @param installedIconPackFileNames File names an icon pack may contain for the installed activities.
 */
private fun scanCache(
    referenceCheckedDirectories: List<File>,
    iconPacksDirectory: File,
    referencedPaths: Collection<String>,
    keptIconPackPackageNames: Set<String>,
    installedIconPackFileNames: Set<String>,
): CacheSnapshot {
    fun File.toCacheKey(): String = "${parentFile?.name.orEmpty()}/$name"

    val referencedKeys = referencedPaths.mapTo(hashSetOf()) { File(it).toCacheKey() }

    var totalSize = 0L

    var reclaimableSize = 0L

    val unreferencedFiles = mutableListOf<File>()

    referenceCheckedDirectories.forEach { directory ->
        directory.listFiles()?.filter { it.isFile }?.forEach { file ->
            val fileSize = file.length()
            totalSize += fileSize

            if (file.toCacheKey() !in referencedKeys) {
                unreferencedFiles += file
                reclaimableSize += fileSize
            }
        }
    }

    iconPacksDirectory.listFiles()?.filter { it.isDirectory }?.forEach { iconPackDirectory ->
        val isKept = iconPackDirectory.name in keptIconPackPackageNames

        iconPackDirectory.listFiles()?.filter { it.isFile }?.forEach { file ->
            val fileSize = file.length()
            totalSize += fileSize

            val isInUse = isKept && file.name in installedIconPackFileNames

            if (!isInUse) {
                unreferencedFiles += file
                reclaimableSize += fileSize
            }
        }
    }

    return CacheSnapshot(
        totalSize = totalSize,
        reclaimableSize = reclaimableSize,
        unreferencedFiles = unreferencedFiles,
    )
}
