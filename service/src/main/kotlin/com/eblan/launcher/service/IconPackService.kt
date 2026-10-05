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
import com.eblan.launcher.domain.usecase.iconpack.UpdateIconPacksUseCase
import com.eblan.launcher.framework.notificationmanager.AndroidNotificationManagerWrapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IconPackService : Service() {
    @Inject
    lateinit var updateIconPacksUseCase: UpdateIconPacksUseCase

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private var iconPackInfoJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val iconPackPackageName =
            intent?.getStringExtra(ICON_PACK_PACKAGE_NAME)

        val iconPackLabel = intent?.getStringExtra(ICON_PACK_LABEL)

        if (iconPackPackageName != null && iconPackLabel != null) {
            iconPackInfoJob?.cancel()

            val notification =
                NotificationCompat.Builder(this, AndroidNotificationManagerWrapper.CHANNEL_ID)
                    .setSmallIcon(R.drawable.baseline_import_export_24)
                    .setContentTitle(getString(R.string.importing_into_cache, iconPackLabel))
                    .setContentText(getString(R.string.loading_icons_from_cache_is_faster))
                    .setOngoing(true)
                    .setProgress(0, 0, true)
                    .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                startForeground(
                    AndroidNotificationManagerWrapper.ICON_PACK_SERVICE_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
                )
            } else {
                startForeground(
                    AndroidNotificationManagerWrapper.ICON_PACK_SERVICE_NOTIFICATION_ID,
                    notification,
                )
            }

            iconPackInfoJob = serviceScope.launch {
                updateIconPacksUseCase(iconPackPackageName = iconPackPackageName)

                stopForeground(STOP_FOREGROUND_REMOVE)

                stopSelf()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()
    }

    companion object {
        const val ICON_PACK_PACKAGE_NAME = "iconPackPackageName"

        const val ICON_PACK_LABEL = "label"
    }
}
