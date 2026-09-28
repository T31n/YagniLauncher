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
package com.eblan.launcher.framework.widgetmanager

import android.app.Activity
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.ActivityNotFoundException
import android.os.Bundle
import kotlin.jvm.Throws

interface AndroidAppWidgetHostWrapper {
    fun startListening()

    fun stopListening()

    fun allocateAppWidgetId(): Int

    fun createView(
        appWidgetId: Int,
        appWidgetProviderInfo: AppWidgetProviderInfo,
    ): AppWidgetHostView

    @Throws(ActivityNotFoundException::class)
    fun startAppWidgetConfigureActivityForResult(
        activity: Activity,
        appWidgetId: Int,
        intentFlags: Int,
        requestCode: Int,
        bundle: Bundle?,
    )

    companion object {
        const val CONFIGURE_REQUEST_CODE = 1
    }
}
