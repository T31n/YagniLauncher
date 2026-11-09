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
package com.eblan.launcher.data.network

import kotlinx.serialization.Serializable

@Serializable
data class AiChatRequest(
    val model: String,
    val messages: List<AiChatMessage>,
)

@Serializable
data class AiChatMessage(
    val role: String,
    val content: String,
)

@Serializable
data class AiChatResponse(
    val choices: List<AiChatChoice> = emptyList(),
)

@Serializable
data class AiChatChoice(
    val message: AiChatResponseMessage? = null,
)

@Serializable
data class AiChatResponseMessage(
    val content: String? = null,
)
