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

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.UserHandle
import com.eblan.launcher.common.AndroidImageSerializer
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.AppWidgetManagerWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.widget.AppWidgetManagerAppWidgetProviderInfo
import com.eblan.launcher.framework.usermanager.AndroidUserManagerWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

internal class DefaultAppWidgetManagerWrapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val imageSerializer: AndroidImageSerializer,
    private val userManagerWrapper: AndroidUserManagerWrapper,
    private val fileManager: FileManager,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val iconKeyGenerator: IconKeyGenerator,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : AppWidgetManagerWrapper,
    AndroidAppWidgetManagerWrapper {
    private val appWidgetManager = AppWidgetManager.getInstance(context)

    override suspend fun getInstalledProvidersWithCacheIcons(): List<AppWidgetManagerAppWidgetProviderInfo> = withContext(ioDispatcher) {
        appWidgetManager.installedProviders.map {
            currentCoroutineContext().ensureActive()

            it.toEblanAppWidgetProviderInfo()
        }
    }

    override fun getAppWidgetInfo(appWidgetId: Int): AppWidgetProviderInfo? = appWidgetManager.getAppWidgetInfo(appWidgetId)

    override fun bindAppWidgetIdIfAllowed(appWidgetId: Int, provider: ComponentName?): Boolean = appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, provider)

    override fun bindAppWidgetIdIfAllowed(
        appWidgetId: Int,
        userHandle: UserHandle,
        provider: ComponentName?,
    ): Boolean = appWidgetManager.bindAppWidgetIdIfAllowed(
        appWidgetId,
        userHandle,
        provider,
        Bundle.EMPTY,
    )

    override fun updateAppWidgetOptions(appWidgetId: Int, options: Bundle) {
        appWidgetManager.updateAppWidgetOptions(appWidgetId, options)
    }

    private suspend fun AppWidgetProviderInfo.toEblanAppWidgetProviderInfo(): AppWidgetManagerAppWidgetProviderInfo {
        val serialNumber = userManagerWrapper.getSerialNumberForUser(userHandle = profile)

        val preview = loadPreviewImage(context, 0)?.let { drawable ->
            val directory = fileManager.getFilesDirectory(FileManager.WIDGETS_DIR)

            val file = File(
                directory,
                iconKeyGenerator.getHashedName(name = provider.flattenToString()),
            )

            imageSerializer.createDrawablePath(drawable = drawable, file = file)

            file.absolutePath
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AppWidgetManagerAppWidgetProviderInfo(
                serialNumber = serialNumber,
                packageName = provider.packageName,
                componentName = provider.flattenToString(),
                configure = configure?.flattenToString(),
                targetCellWidth = targetCellWidth,
                targetCellHeight = targetCellHeight,
                minWidth = minWidth,
                minHeight = minHeight,
                resizeMode = resizeMode,
                minResizeWidth = minResizeWidth,
                minResizeHeight = minResizeHeight,
                maxResizeWidth = maxResizeWidth,
                maxResizeHeight = maxResizeHeight,
                preview = preview,
                lastUpdateTime = packageManagerWrapper.getLastUpdateTime(packageName = provider.packageName),
                label = loadLabel(context.packageManager),
                description = loadDescription(context)?.let(CharSequence::toString),
            )
        } else {
            AppWidgetManagerAppWidgetProviderInfo(
                serialNumber = serialNumber,
                packageName = provider.packageName,
                componentName = provider.flattenToString(),
                configure = configure?.flattenToString(),
                targetCellWidth = 0,
                targetCellHeight = 0,
                minWidth = minWidth,
                minHeight = minHeight,
                resizeMode = resizeMode,
                minResizeWidth = minResizeWidth,
                minResizeHeight = minResizeHeight,
                maxResizeWidth = 0,
                maxResizeHeight = 0,
                preview = preview,
                lastUpdateTime = packageManagerWrapper.getLastUpdateTime(packageName = provider.packageName),
                label = loadLabel(context.packageManager),
                description = null,
            )
        }
    }
}
