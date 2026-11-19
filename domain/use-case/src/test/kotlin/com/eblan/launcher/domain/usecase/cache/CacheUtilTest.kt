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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class CacheUtilTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `scanCache marks unreferenced old files and keeps referenced, recent, and active icon pack files`() {
        val now = 1_000_000L
        val oldTimestamp = now - MINIMUM_UNREFERENCED_FILE_AGE_MILLIS - 1_000L
        val recentTimestamp = now - MINIMUM_UNREFERENCED_FILE_AGE_MILLIS + 1_000L

        val iconsDir = File(tempDir.toFile(), FileManager.ICONS_DIR).apply { mkdirs() }
        val customIconsDir = File(tempDir.toFile(), FileManager.CUSTOM_ICONS_DIR).apply { mkdirs() }
        val iconPacksDir = File(tempDir.toFile(), FileManager.ICON_PACKS_DIR).apply { mkdirs() }

        val referencedIcon = File(iconsDir, "referenced.png").apply {
            writeBytes(ByteArray(100))
            setLastModified(oldTimestamp)
        }
        val unreferencedOldIcon = File(iconsDir, "unreferenced_old.png").apply {
            writeBytes(ByteArray(200))
            setLastModified(oldTimestamp)
        }
        val unreferencedRecentCustomIcon = File(customIconsDir, "unreferenced_recent.png").apply {
            writeBytes(ByteArray(300))
            setLastModified(recentTimestamp)
        }

        val keptPackDir = File(iconPacksDir, "com.example.keptpack").apply { mkdirs() }
        val keptPackInstalledIcon = File(keptPackDir, "installed_hash").apply {
            writeBytes(ByteArray(400))
            setLastModified(oldTimestamp)
        }
        val keptPackStaleIcon = File(keptPackDir, "uninstalled_hash").apply {
            writeBytes(ByteArray(500))
            setLastModified(oldTimestamp)
        }

        val removedPackDir = File(iconPacksDir, "com.example.removedpack").apply { mkdirs() }
        val removedPackIcon = File(removedPackDir, "installed_hash").apply {
            writeBytes(ByteArray(600))
            setLastModified(oldTimestamp)
        }

        val snapshot = scanCache(
            referenceCheckedDirectories = listOf(iconsDir, customIconsDir),
            iconPacksDirectory = iconPacksDir,
            referencedPaths = listOf(referencedIcon.absolutePath),
            keptIconPackPackageNames = setOf("com.example.keptpack"),
            installedIconPackFileNames = setOf("installed_hash"),
            now = now,
        )

        assertEquals(
            setOf(
                referencedIcon,
                unreferencedOldIcon,
                unreferencedRecentCustomIcon,
                keptPackInstalledIcon,
                keptPackStaleIcon,
                removedPackIcon,
            ),
            snapshot.allFiles.toSet(),
        )
        assertEquals(
            setOf(
                unreferencedOldIcon,
                keptPackStaleIcon,
                removedPackIcon,
            ),
            snapshot.unreferencedFiles.toSet(),
        )
    }
}
