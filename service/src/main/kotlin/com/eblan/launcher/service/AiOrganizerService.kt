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
import com.eblan.launcher.domain.framework.AiOrganizerOperationState
import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerOperation
import com.eblan.launcher.domain.model.aiorganizer.ApplyAiOrganizationResult
import com.eblan.launcher.domain.model.aiorganizer.OrganizeHomeWithAiResult
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.aiorganizer.ApplyAiOrganizationUseCase
import com.eblan.launcher.domain.usecase.aiorganizer.OrganizeHomeWithAiUseCase
import com.eblan.launcher.framework.notificationmanager.AndroidNotificationManagerWrapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Runs AI organize and apply in the foreground. Only one operation runs at a time:
 * - Organize is rejected while an apply is running.
 * - Apply cancels a running organize, then runs to completion without being cancelled by organize.
 */
@AndroidEntryPoint
class AiOrganizerService : Service() {
    companion object {
        const val ACTION_ORGANIZE = "com.eblan.launcher.service.action.ORGANIZE"
        const val ACTION_APPLY = "com.eblan.launcher.service.action.APPLY"
    }

    @Inject
    lateinit var organizeHomeWithAiUseCase: OrganizeHomeWithAiUseCase

    @Inject
    lateinit var applyAiOrganizationUseCase: ApplyAiOrganizationUseCase

    @Inject
    lateinit var userDataRepository: UserDataRepository

    @Inject
    lateinit var notificationManagerWrapper: AndroidNotificationManagerWrapper

    @Inject
    lateinit var aiOrganizerOperationState: AiOrganizerOperationState

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private var organizeJob: Job? = null

    private var applyJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_APPLY -> startApply()
            else -> startOrganize()
        }

        return START_NOT_STICKY
    }

    private fun startOrganize() {
        if (applyJob?.isActive == true) {
            promoteToForeground(
                title = getString(R.string.ai_organizer_applying_title),
                text = getString(R.string.ai_organizer_applying_text),
            )

            return
        }

        promoteToForeground(
            title = getString(R.string.ai_organizer_organizing_title),
            text = getString(R.string.ai_organizer_organizing_text),
        )

        organizeJob?.cancel()

        aiOrganizerOperationState.setOperation(AiOrganizerOperation.ORGANIZING)

        val job = serviceScope.launch(start = CoroutineStart.LAZY) {
            val result = try {
                val defaultSystemInstructions = getString(R.string.ai_organizer_default_system_instructions)
                val customSystemInstructions = userDataRepository.userDataFlow.first()
                    .aiOrganizerSettings.systemInstructions.trim()
                val systemInstructions = if (customSystemInstructions.isBlank()) {
                    defaultSystemInstructions
                } else {
                    "$defaultSystemInstructions\n\n$customSystemInstructions"
                }

                organizeHomeWithAiUseCase(systemInstructions = systemInstructions)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                OrganizeHomeWithAiResult.Failed
            }

            // A cancelled or superseded organize must not stop the service.
            if (organizeJob === coroutineContext[Job]) {
                organizeJob = null

                finishOrganize(result)
            }
        }

        organizeJob = job

        job.start()
    }

    private fun startApply() {
        if (applyJob?.isActive == true) {
            promoteToForeground(
                title = getString(R.string.ai_organizer_applying_title),
                text = getString(R.string.ai_organizer_applying_text),
            )

            return
        }

        promoteToForeground(
            title = getString(R.string.ai_organizer_applying_title),
            text = getString(R.string.ai_organizer_applying_text),
        )

        // Detached before joining so the cancelled organize cannot stop the service.
        val organizeToCancel = organizeJob
        organizeJob = null

        applyJob = serviceScope.launch(start = CoroutineStart.LAZY) {
            organizeToCancel?.cancelAndJoin()

            aiOrganizerOperationState.setApplyResult(null)
            aiOrganizerOperationState.setOperation(AiOrganizerOperation.APPLYING)

            try {
                aiOrganizerOperationState.setApplyResult(applyAiOrganizationUseCase())
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                aiOrganizerOperationState.setApplyResult(ApplyAiOrganizationResult.FAILED)
            } finally {
                aiOrganizerOperationState.setOperation(null)

                stopForegroundAndSelf()
            }
        }

        applyJob?.start()
    }

    private fun promoteToForeground(title: String, text: String) {
        val notification =
            NotificationCompat.Builder(this, AndroidNotificationManagerWrapper.CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_import_export_24)
                .setContentTitle(title)
                .setContentText(text)
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
    }

    private fun finishOrganize(result: OrganizeHomeWithAiResult) {
        aiOrganizerOperationState.setOperation(null)

        when (result) {
            OrganizeHomeWithAiResult.Ready -> notify(
                notificationId = AndroidNotificationManagerWrapper.AI_ORGANIZER_READY_NOTIFICATION_ID,
                notificationTitle = getString(R.string.ai_organizer_ready_title),
                notificationText = getString(R.string.ai_organizer_ready_text),
            )

            else -> notify(
                notificationId = AndroidNotificationManagerWrapper.AI_ORGANIZER_ERROR_NOTIFICATION_ID,
                notificationTitle = getString(R.string.ai_organizer_failed_title),
                notificationText = result.messageText(),
            )
        }

        stopForegroundAndSelf()
    }

    private fun OrganizeHomeWithAiResult.messageText(): String = when (this) {
        OrganizeHomeWithAiResult.Ready -> getString(R.string.ai_organizer_ready_text)
        OrganizeHomeWithAiResult.EndpointNotSet -> getString(R.string.ai_organizer_endpoint_not_set_text)
        OrganizeHomeWithAiResult.ModelNotSet -> getString(R.string.ai_organizer_model_not_set_text)
        is OrganizeHomeWithAiResult.RequestFailed -> "${getString(R.string.ai_organizer_request_failed_text)}: $detail"
        is OrganizeHomeWithAiResult.InvalidResponse -> "${getString(R.string.ai_organizer_invalid_response_text)}: $detail"
        OrganizeHomeWithAiResult.Failed -> getString(R.string.ai_organizer_unexpected_error_text)
    }

    private fun notify(notificationId: Int, notificationTitle: String, notificationText: String) {
        notificationManagerWrapper.notify(
            id = notificationId,
            notification = NotificationCompat.Builder(
                this,
                AndroidNotificationManagerWrapper.CHANNEL_ID,
            )
                .setSmallIcon(R.drawable.baseline_import_export_24)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setAutoCancel(true)
                .build(),
        )
    }

    private fun stopForegroundAndSelf() {
        stopForeground(STOP_FOREGROUND_REMOVE)

        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()

        aiOrganizerOperationState.setOperation(null)
    }
}
