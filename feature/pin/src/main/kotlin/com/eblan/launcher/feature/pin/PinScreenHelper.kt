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
package com.eblan.launcher.feature.pin

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.LauncherApps.PinItemRequest
import android.os.Build
import android.os.Bundle
import android.os.UserHandle
import androidx.activity.result.ActivityResult
import androidx.annotation.RequiresApi
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper

internal fun handleGridItem(
    appWidgetHostWrapper: AndroidAppWidgetHostWrapper,
    appWidgetManager: AndroidAppWidgetManagerWrapper,
    gridItem: GridItem?,
    userHandle: UserHandle,
    onAddedToHomeScreenToast: (String) -> Unit,
    onLaunch: (Intent) -> Unit,
    onUpdateAppWidgetId: (Int) -> Unit,
    onUpdateGridItemCache: (GridItem) -> Unit,
) {
    val data = gridItem?.data ?: return

    if (data is GridItemData.Widget) {
        val appWidgetId = appWidgetHostWrapper.allocateAppWidgetId()

        onUpdateAppWidgetId(appWidgetId)

        onAddPinWidget(
            appWidgetId = appWidgetId,
            appWidgetManager = appWidgetManager,
            data = data,
            gridItem = gridItem,
            userHandle = userHandle,
            onLaunch = onLaunch,
            onUpdateGridItemCache = onUpdateGridItemCache,
        )

        onAddedToHomeScreenToast(
            """
                ${gridItem.page}
                ${gridItem.startRow}
                ${gridItem.startColumn}
            """.trimIndent(),
        )
    }
}

internal fun onAddPinWidget(
    appWidgetId: Int,
    appWidgetManager: AndroidAppWidgetManagerWrapper,
    data: GridItemData.Widget,
    gridItem: GridItem,
    userHandle: UserHandle,
    onLaunch: (Intent) -> Unit,
    onUpdateGridItemCache: (GridItem) -> Unit,
) {
    val provider = ComponentName.unflattenFromString(data.componentName)

    val bindAppWidgetIdIfAllowed = appWidgetManager.bindAppWidgetIdIfAllowed(
        appWidgetId = appWidgetId,
        provider = provider,
        userHandle = userHandle,
    )

    if (bindAppWidgetIdIfAllowed) {
        val newData = data.copy(appWidgetId = appWidgetId)

        onUpdateGridItemCache(gridItem.copy(data = newData))
    } else {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        }

        onLaunch(intent)
    }
}

internal fun handleAppWidgetLauncherResult(
    gridItem: GridItem?,
    result: ActivityResult,
    onDeleteAppWidgetId: () -> Unit,
    onUpdateGridItemCache: (GridItem) -> Unit,
) {
    val data = gridItem?.data as? GridItemData.Widget ?: return

    if (result.resultCode == Activity.RESULT_OK) {
        val appWidgetId =
            result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

        val newData = data.copy(appWidgetId = appWidgetId)

        onUpdateGridItemCache(gridItem.copy(data = newData))
    } else {
        onDeleteAppWidgetId()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
internal fun handleIsBoundWidget(
    appWidgetId: Int,
    gridItem: GridItem?,
    isBoundWidget: Boolean,
    pinItemRequest: PinItemRequest?,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateGridItems: () -> Unit,
) {
    if (gridItem == null || !isBoundWidget || pinItemRequest == null) return

    val extras = Bundle().apply {
        putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }

    if (pinItemRequest.isValid &&
        pinItemRequest.accept(extras)
    ) {
        onUpdateGridItems()
    } else {
        onDeleteGridItem(gridItem)
    }
}

internal fun handleDeleteAppWidgetId(
    appWidgetId: Int,
    deleteAppWidgetId: Boolean,
    gridItem: GridItem?,
    onDeleteGridItem: (GridItem) -> Unit,
) {
    val data = gridItem?.data as? GridItemData.Widget ?: return

    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && deleteAppWidgetId) {
        val newData = data.copy(appWidgetId = appWidgetId)

        onDeleteGridItem(gridItem.copy(data = newData))
    }
}
