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

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerFile
import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/** Reports whether `new_home.json` and `home.json` exist, so the UI can enable the matching actions. */
class GetAiOrganizerStatusUseCase @Inject constructor(
    private val fileManager: FileManager,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(): AiOrganizerStatus = withContext(ioDispatcher) {
        val directory = fileManager.getFilesDirectory(FileManager.HOME_DIR)

        AiOrganizerStatus(
            hasNewHome = File(directory, AiOrganizerFile.NEW_HOME.fileName).exists(),
            hasBackup = File(directory, AiOrganizerFile.HOME_BACKUP.fileName).exists(),
        )
    }
}
