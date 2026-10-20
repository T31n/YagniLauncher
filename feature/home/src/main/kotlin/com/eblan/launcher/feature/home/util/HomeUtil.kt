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
package com.eblan.launcher.feature.home.util

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process.myUserHandle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.HorizontalAlignment
import com.eblan.launcher.domain.model.grid.HorizontalArrangement
import com.eblan.launcher.domain.model.grid.VerticalAlignment
import com.eblan.launcher.domain.model.grid.VerticalArrangement
import com.eblan.launcher.domain.model.home.GlobalAction
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.framework.statusbarmanager.AndroidStatusBarManagerWrapper
import com.eblan.launcher.framework.usermanager.AndroidUserManagerWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper
import kotlin.math.roundToInt

internal fun handleEblanAction(
    // Context
    context: Context,
    // Action
    eblanAction: EblanAction,
    // System Wrappers
    launcherApps: AndroidLauncherAppsWrapper,
    statusBarManagerWrapper: AndroidStatusBarManagerWrapper,
    // Callbacks
    onOpenAppDrawer: () -> Unit
) {
    when (eblanAction.eblanActionType) {
        EblanActionType.OpenApp -> launcherApps.startMainActivity(
            serialNumber = eblanAction.serialNumber,
            componentName = eblanAction.componentName,
            sourceBounds = Rect(),
        )

        EblanActionType.OpenNotificationPanel -> {
            val intent = Intent(GlobalAction.NAME).setPackage(context.packageName).putExtra(
                GlobalAction.GLOBAL_ACTION_TYPE,
                GlobalAction.Notifications.name,
            )

            context.sendBroadcast(intent)
        }

        EblanActionType.LockScreen -> {
            val intent = Intent(GlobalAction.NAME).setPackage(context.packageName).putExtra(
                GlobalAction.GLOBAL_ACTION_TYPE,
                GlobalAction.LockScreen.name,
            )

            context.sendBroadcast(intent)
        }

        EblanActionType.OpenQuickSettings -> {
            val intent = Intent(GlobalAction.NAME).setPackage(context.packageName).putExtra(
                GlobalAction.GLOBAL_ACTION_TYPE,
                GlobalAction.QuickSettings.name,
            )

            context.sendBroadcast(intent)
        }

        EblanActionType.OpenRecents -> {
            val intent = Intent(GlobalAction.NAME).setPackage(context.packageName).putExtra(
                GlobalAction.GLOBAL_ACTION_TYPE,
                GlobalAction.Recents.name,
            )

            context.sendBroadcast(intent)
        }

        EblanActionType.OpenAppDrawer -> onOpenAppDrawer()

        EblanActionType.OpenNotificationPanelByReflection -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                statusBarManagerWrapper.expandNotificationsPanel()
            }
        }

        EblanActionType.None -> Unit
    }
}

internal fun getHorizontalAlignment(horizontalAlignment: HorizontalAlignment): Alignment.Horizontal = when (horizontalAlignment) {
    HorizontalAlignment.Start -> Alignment.Start
    HorizontalAlignment.CenterHorizontally -> Alignment.CenterHorizontally
    HorizontalAlignment.End -> Alignment.End
}

internal fun getVerticalArrangement(verticalArrangement: VerticalArrangement): Arrangement.Vertical = when (verticalArrangement) {
    VerticalArrangement.Top -> Arrangement.Top
    VerticalArrangement.Center -> Arrangement.Center
    VerticalArrangement.Bottom -> Arrangement.Bottom
}

internal fun getHorizontalArrangement(horizontalArrangement: HorizontalArrangement): Arrangement.Horizontal = when (horizontalArrangement) {
    HorizontalArrangement.Start -> Arrangement.Start
    HorizontalArrangement.Center -> Arrangement.Center
    HorizontalArrangement.End -> Arrangement.End
}

internal fun getVerticalAlignment(verticalAlignment: VerticalAlignment): Alignment.Vertical = when (verticalAlignment) {
    VerticalAlignment.Top -> Alignment.Top
    VerticalAlignment.CenterVertically -> Alignment.CenterVertically
    VerticalAlignment.Bottom -> Alignment.Bottom
}

internal fun updateAppWidgetOptions(
    // Manager
    androidAppWidgetManagerWrapper: AndroidAppWidgetManagerWrapper,
    // Data
    data: GridItemData.Widget,
    // Dimensions
    width: Int,
    height: Int,
    density: Density,
    // Grid
    columns: Int,
    rows: Int,
    gridWidth: Int,
    gridHeight: Int,
    startColumn: Int,
    startRow: Int
) {
    val cellWidthPx = gridWidth.toFloat() / columns
    val cellHeightPx = gridHeight.toFloat() / rows

    val maxSpanColumns = (columns - startColumn).coerceAtLeast(1)
    val maxSpanRows = (rows - startRow).coerceAtLeast(1)

    val maxWidthByGridPx = (maxSpanColumns * cellWidthPx).roundToInt()
    val maxHeightByGridPx = (maxSpanRows * cellHeightPx).roundToInt()

    val maxWidthByGridDp = with(density) { maxWidthByGridPx.toDp().value.roundToInt() }
    val maxHeightByGridDp = with(density) { maxHeightByGridPx.toDp().value.roundToInt() }

    val maxWidthDp = if (data.maxResizeWidth > 0) {
        minOf(
            data.maxResizeWidth,
            maxWidthByGridDp,
        )
    } else {
        maxWidthByGridDp
    }
    val maxHeightDp = if (data.maxResizeHeight > 0) {
        minOf(
            data.maxResizeHeight,
            maxHeightByGridDp,
        )
    } else {
        maxHeightByGridDp
    }

    val minWidthDp = if (data.minResizeWidth > 0) {
        data.minResizeWidth
    } else {
        with(density) { width.toDp().value.roundToInt() }
    }
    val minHeightDp = if (data.minResizeHeight > 0) {
        data.minResizeHeight
    } else {
        with(density) { height.toDp().value.roundToInt() }
    }

    val boundedMinWidthDp = minWidthDp.coerceAtLeast(1)
    val boundedMinHeightDp = minHeightDp.coerceAtLeast(1)

    val boundedMaxWidthDp = maxWidthDp.coerceAtLeast(boundedMinWidthDp)
    val boundedMaxHeightDp = maxHeightDp.coerceAtLeast(boundedMinHeightDp)

    val options = Bundle().apply {
        putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, boundedMinWidthDp)
        putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, boundedMinHeightDp)
        putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, boundedMaxWidthDp)
        putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, boundedMaxHeightDp)
    }

    androidAppWidgetManagerWrapper.updateAppWidgetOptions(
        appWidgetId = data.appWidgetId,
        options = options,
    )
}

internal fun handleOnUninstall(
    // Context & System
    context: Context,
    launcherApps: AndroidLauncherAppsWrapper,
    userManager: AndroidUserManagerWrapper,
    // Target Identification
    serialNumber: Long,
    componentName: String,
    packageName: String,
    // Popup Layout
    popupIntOffset: IntOffset,
    popupIntSize: IntSize
) {
    val userHandle =
        userManager.getUserForSerialNumber(serialNumber = serialNumber) ?: return

    if (userHandle != myUserHandle()) {
        launcherApps.startAppDetailsActivity(
            serialNumber = serialNumber,
            componentName = componentName,
            sourceBounds = Rect(
                popupIntOffset.x,
                popupIntOffset.y,
                popupIntOffset.x + popupIntSize.width,
                popupIntOffset.y + popupIntSize.height,
            ),
        )
    } else {
        context.startActivity(
            Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts(
                    "package",
                    packageName,
                    null,
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                sourceBounds = Rect(
                    popupIntOffset.x,
                    popupIntOffset.y,
                    popupIntOffset.x + popupIntSize.width,
                    popupIntOffset.y + popupIntSize.height,
                )
            },
        )
    }
}

internal val PAGE_INDICATOR_HEIGHT = 30.dp
internal val DRAG_HANDLE_SIZE = 30.dp
