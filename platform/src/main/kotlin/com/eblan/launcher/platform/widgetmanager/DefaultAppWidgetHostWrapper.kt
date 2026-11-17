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
package com.eblan.launcher.platform.widgetmanager

import android.app.Activity
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.ActivityNotFoundException
import android.content.Context
import android.os.Bundle
import com.eblan.launcher.domain.platform.AppWidgetHostWrapper
import com.eblan.launcher.platform.widgetmanager.launcher3.LauncherAppWidgetHost
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class DefaultAppWidgetHostWrapper @Inject constructor(@param:ApplicationContext private val context: Context) :
    AppWidgetHostWrapper,
    AndroidAppWidgetHostWrapper {
    private val appWidgetHost = LauncherAppWidgetHost(context, 2814)

    override fun startListening() {
        appWidgetHost.startListening()
    }

    override fun stopListening() {
        appWidgetHost.stopListening()
    }

    override fun allocateAppWidgetId(): Int = appWidgetHost.allocateAppWidgetId()

    @Suppress("DEPRECATION")
    override fun createView(
        appWidgetId: Int,
        appWidgetProviderInfo: AppWidgetProviderInfo,
    ): AppWidgetHostView = appWidgetHost.createView(context, appWidgetId, appWidgetProviderInfo)

    override fun deleteAppWidgetId(appWidgetId: Int) {
        appWidgetHost.deleteAppWidgetId(appWidgetId)
    }

    @Throws(ActivityNotFoundException::class)
    override fun startAppWidgetConfigureActivityForResult(
        activity: Activity,
        appWidgetId: Int,
        intentFlags: Int,
        requestCode: Int,
        bundle: Bundle?,
    ) {
        appWidgetHost.startAppWidgetConfigureActivityForResult(
            activity,
            appWidgetId,
            intentFlags,
            requestCode,
            bundle,
        )
    }
}
