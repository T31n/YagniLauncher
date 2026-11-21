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

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanIconPackInfoRepository
import com.eblan.launcher.domain.repository.FileReferenceRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Deletes cached files that no database row or icon pack uses anymore.
 *
 * This is the only place that removes cache files, so other use cases only need to care about
 * database transactions.
 */
class CleanUpCacheUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val fileReferenceRepository: FileReferenceRepository,
    private val eblanIconPackInfoRepository: EblanIconPackInfoRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val userDataRepository: UserDataRepository,
    private val iconKeyGenerator: IconKeyGenerator,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    /** Deletes the unreferenced files. */
    suspend operator fun invoke() = withContext(ioDispatcher) {
        val cacheSnapshot = getCacheSnapshot(
            fileManager = fileManager,
            fileReferenceRepository = fileReferenceRepository,
            eblanIconPackInfoRepository = eblanIconPackInfoRepository,
            eblanApplicationInfoRepository = eblanApplicationInfoRepository,
            userDataRepository = userDataRepository,
            iconKeyGenerator = iconKeyGenerator,
        )

        cacheSnapshot.unreferencedFiles.forEach { file ->
            currentCoroutineContext().ensureActive()

            file.delete()
        }

        fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR)
            .listFiles()
            ?.filter(File::isDirectory)
            ?.filter { currentFile -> currentFile.list().isNullOrEmpty() }
            ?.forEach { currentFile -> currentFile.delete() }
    }
}
