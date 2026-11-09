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
import com.eblan.launcher.domain.model.userdata.AiOrganizerSettings
import com.eblan.launcher.domain.repository.AiOrganizerRepository
import javax.inject.Inject

internal class DefaultAiOrganizerRepository @Inject constructor(
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
}
