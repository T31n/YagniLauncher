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

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException

/**
 * Sends a single chat-completion request to an OpenAI-compatible endpoint.
 * The endpoint is the full chat-completions URL.
 */
@Singleton
class AiChatClient @Inject constructor(private val okHttpClient: OkHttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun complete(
        endpoint: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String,
    ): String {
        val url = endpoint.trim().toHttpUrlOrNull()
            ?: throw IOException("Invalid endpoint URL")

        val body = json.encodeToString(
            AiChatRequest(
                model = model,
                messages = listOf(
                    AiChatMessage(role = "system", content = systemPrompt),
                    AiChatMessage(role = "user", content = userPrompt),
                ),
            ),
        ).toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("Content-Type", "application/json")
            .apply { if (apiKey.isNotBlank()) header("Authorization", "Bearer ${apiKey.trim()}") }
            .build()

        val responseBody = okHttpClient.newCall(request).await()

        return json.decodeFromString<AiChatResponse>(responseBody)
            .choices
            .firstOrNull()
            ?.message
            ?.content
            ?.takeIf { it.isNotBlank() }
            ?: throw IOException("The endpoint returned no content")
    }

    private suspend fun Call.await(): String = suspendCancellableCoroutine { continuation ->
        enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        val content = it.body?.string().orEmpty()
                        if (it.isSuccessful) {
                            if (continuation.isActive) {
                                continuation.resumeWith(
                                    Result.success(
                                        content,
                                    ),
                                )
                            }
                        } else {
                            continuation.resumeWithException(
                                IOException("HTTP ${it.code}: ${content.take(300)}"),
                            )
                        }
                    }
                }
            },
        )

        continuation.invokeOnCancellation { cancel() }
    }
}
