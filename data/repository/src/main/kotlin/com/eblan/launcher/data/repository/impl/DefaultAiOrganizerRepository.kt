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
package com.eblan.launcher.data.repository.impl

import com.eblan.launcher.data.network.AiChatClient
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerFile
import com.eblan.launcher.domain.model.userdata.AiOrganizerSettings
import com.eblan.launcher.domain.repository.AiOrganizerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

internal class DefaultAiOrganizerRepository @Inject constructor(
    private val fileManager: FileManager,
    private val aiChatClient: AiChatClient,
) : AiOrganizerRepository {
    override suspend fun complete(
        settings: AiOrganizerSettings,
        systemPrompt: String,
        userPrompt: String,
    ): String = aiChatClient.complete(
        endpoint = settings.endpoint,
        apiKey = settings.apiKey,
        model = settings.model,
        systemPrompt = systemPrompt,
        userPrompt = userPrompt,
    )

    override suspend fun readFile(file: AiOrganizerFile): ByteArray? = fileManager.readFileBytes(
        directory = homeDirectory(),
        name = file.fileName,
    )

    override suspend fun writeFile(
        file: AiOrganizerFile,
        content: ByteArray,
    ): Unit = withContext(Dispatchers.IO) {
        val directory = homeDirectory()
        val target = File(directory, file.fileName)
        val temp = File(directory, "${file.fileName}.tmp")

        try {
            temp.writeBytes(content)

            if (!temp.renameTo(target)) {
                throw IOException("Unable to move ${temp.name} to ${target.name}")
            }
        } finally {
            temp.delete()
        }
    }

    override suspend fun deleteFile(file: AiOrganizerFile): Boolean {
        val target = File(homeDirectory(), file.fileName)

        return !target.exists() || target.delete()
    }

    override suspend fun fileExists(file: AiOrganizerFile): Boolean = File(homeDirectory(), file.fileName).exists()

    private suspend fun homeDirectory(): File = fileManager.getFilesDirectory(FileManager.HOME_DIR)
}
