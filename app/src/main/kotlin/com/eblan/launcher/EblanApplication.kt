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
package com.eblan.launcher

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.eblan.launcher.framework.notificationmanager.AndroidNotificationManagerWrapper
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class EblanApplication :
    Application(),
    Thread.UncaughtExceptionHandler {
    @Inject
    lateinit var notificationManagerWrapper: AndroidNotificationManagerWrapper

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManagerWrapper.createNotificationChannel(
                channelId = AndroidNotificationManagerWrapper.CHANNEL_ID,
                name = getString(R.string.app_name),
                importance = NotificationManager.IMPORTANCE_DEFAULT,
            )
        }
    }

    @Suppress("DEPRECATION")
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val file = File(filesDir, "last_crash.txt")

            val device = "${Build.MANUFACTURER} ${Build.MODEL}"

            val androidVersion = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"

            val appVersion = try {
                val packageInfo = packageManager.getPackageInfo(packageName, 0)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    "${packageInfo.versionName} (${packageInfo.longVersionCode})"
                } else {
                    packageInfo.versionCode
                }
            } catch (_: Exception) {
                "Unknown"
            }

            val crashInfo = buildString {
                appendLine("=== Crash Report ===")
                appendLine("Device: $device")
                appendLine("OS: $androidVersion")
                appendLine("App: $appVersion")
                appendLine("Time: ${java.util.Date()}")
                appendLine("GitHub: https://github.com/T31n/YagniLauncher")
                appendLine()
                appendLine(Log.getStackTraceString(throwable))
            }

            file.writeText(crashInfo)

            showCrashNotification(file = file)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun showCrashNotification(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            file,
        )

        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/plain")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification =
            NotificationCompat.Builder(this, AndroidNotificationManagerWrapper.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("Yagni Launcher has crashed")
                .setContentText("View the stack trace and report on GitHub")
                .setAutoCancel(true)
                .addAction(0, "Open stacktrace", pendingIntent)
                .build()

        notificationManagerWrapper.notify(
            id = AndroidNotificationManagerWrapper.CRASH_NOTIFICATION_ID,
            notification = notification,
        )
    }
}
