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
package com.eblan.launcher.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.eblan.launcher.domain.model.aiorganizer.OrganizeHomeWithAiResult
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.aiorganizer.OrganizeHomeWithAiUseCase
import com.eblan.launcher.framework.notificationmanager.AndroidNotificationManagerWrapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.eblan.launcher.common.R as commonR

/**
 * Runs the AI organization in the foreground. Starting it while a run is active cancels the
 * previous run and starts a new one.
 */
@AndroidEntryPoint
class AiOrganizerService : Service() {
    @Inject
    lateinit var organizeHomeWithAiUseCase: OrganizeHomeWithAiUseCase

    @Inject
    lateinit var userDataRepository: UserDataRepository

    @Inject
    lateinit var notificationManagerWrapper: AndroidNotificationManagerWrapper

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private var organizeJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        organizeJob?.cancel()

        val notification =
            NotificationCompat.Builder(this, AndroidNotificationManagerWrapper.CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_import_export_24)
                .setContentTitle(getString(R.string.ai_organizer_organizing_title))
                .setContentText(getString(R.string.ai_organizer_organizing_text))
                .setOngoing(true)
                .setProgress(0, 0, true)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                AndroidNotificationManagerWrapper.AI_ORGANIZER_SERVICE_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            startForeground(
                AndroidNotificationManagerWrapper.AI_ORGANIZER_SERVICE_NOTIFICATION_ID,
                notification,
            )
        }

        organizeJob = serviceScope.launch {
            val defaultSystemInstructions = getString(commonR.string.ai_organizer_default_system_instructions)
            val customSystemInstructions = userDataRepository.userDataFlow.first()
                .aiOrganizerSettings.systemInstructions.trim()
            val systemInstructions = if (customSystemInstructions.isBlank()) {
                defaultSystemInstructions
            } else {
                "$defaultSystemInstructions\n\n$customSystemInstructions"
            }

            when (
                val result = organizeHomeWithAiUseCase(systemInstructions = systemInstructions)
            ) {
                OrganizeHomeWithAiResult.Ready -> notify(
                    id = AndroidNotificationManagerWrapper.AI_ORGANIZER_READY_NOTIFICATION_ID,
                    title = getString(R.string.ai_organizer_ready_title),
                    text = getString(R.string.ai_organizer_ready_text),
                )

                else -> notify(
                    id = AndroidNotificationManagerWrapper.AI_ORGANIZER_ERROR_NOTIFICATION_ID,
                    title = getString(R.string.ai_organizer_failed_title),
                    text = result.messageText(),
                )
            }

            stopForeground(STOP_FOREGROUND_REMOVE)

            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun OrganizeHomeWithAiResult.messageText(): String = when (this) {
        OrganizeHomeWithAiResult.Ready -> getString(R.string.ai_organizer_ready_text)
        OrganizeHomeWithAiResult.EndpointNotSet -> getString(R.string.ai_organizer_endpoint_not_set_text)
        OrganizeHomeWithAiResult.ModelNotSet -> getString(R.string.ai_organizer_model_not_set_text)
        is OrganizeHomeWithAiResult.RequestFailed -> "${getString(R.string.ai_organizer_request_failed_text)}: $detail"
        is OrganizeHomeWithAiResult.InvalidResponse -> "${getString(R.string.ai_organizer_invalid_response_text)}: $detail"
        OrganizeHomeWithAiResult.Failed -> getString(R.string.ai_organizer_unexpected_error_text)
    }

    private fun notify(id: Int, title: String, text: String) {
        notificationManagerWrapper.notify(
            id = id,
            notification = NotificationCompat.Builder(
                this,
                AndroidNotificationManagerWrapper.CHANNEL_ID,
            )
                .setSmallIcon(R.drawable.baseline_import_export_24)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .build(),
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()
    }
}
