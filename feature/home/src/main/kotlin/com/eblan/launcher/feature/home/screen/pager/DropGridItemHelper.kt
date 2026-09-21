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
package com.eblan.launcher.feature.home.screen.pager

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.PinItemRequest
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.common.AndroidImageSerializer
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.domain.model.launcherapps.PinItemRequestType
import com.eblan.launcher.feature.home.R
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.util.PAGE_INDICATOR_HEIGHT
import com.eblan.launcher.feature.home.util.updateAppWidgetOptions
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.framework.usermanager.AndroidUserManagerWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper
import java.io.File

internal suspend fun handleDropGridItem(
    androidAppWidgetHostWrapper: AndroidAppWidgetHostWrapper,
    androidAppWidgetManagerWrapper: AndroidAppWidgetManagerWrapper,
    androidLauncherAppsWrapper: AndroidLauncherAppsWrapper,
    androidUserManagerWrapper: AndroidUserManagerWrapper,
    context: Context,
    drag: Drag,
    gridItemSource: GridItemSource?,
    isDragging: Boolean,
    moveGridItemResult: MoveGridItemResult?,
    lockMovement: Boolean,
    isVisibleOverlay: Boolean,
    columns: Int,
    density: Density,
    rows: Int,
    paddingValues: PaddingValues,
    screenHeight: Int,
    screenWidth: Int,
    dockHeight: Int,
    layoutDirection: LayoutDirection,
    onResetGridAfterDeleteGridItem: (GridItem) -> Unit,
    onResetGrid: () -> Unit,
    onUpdateGridItemsAfterMove: (MoveGridItemResult) -> Unit,
    onLaunchShortcutConfigIntent: (Intent) -> Unit,
    onLaunchShortcutConfigIntentSenderRequest: (IntentSenderRequest) -> Unit,
    onLaunchWidgetIntent: (Intent) -> Unit,
    onUpdateAppWidgetId: (Int) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateWidgetGridItem: (GridItem) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateGridItemsAfterMoveNewFolder: (
        folderGridItems: List<GridItem>,
        moveGridItemResult: MoveGridItemResult,
    ) -> Unit,
) {
    val currentGridItemSource = gridItemSource ?: return

    val currentMoveGridItemResult = moveGridItemResult ?: return

    if (drag == Drag.None ||
        drag == Drag.Start ||
        drag == Drag.Dragging
    ) {
        return
    }

    val gridSize = calculateGridSize(
        density = density,
        paddingValues = paddingValues,
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        dockHeight = dockHeight,
        layoutDirection = layoutDirection,
        associate = currentMoveGridItemResult.movingGridItem.associate,
    )

    val isLongPress = isVisibleOverlay && !isDragging

    val isMoveGridItemResultFailed = drag == Drag.Cancel ||
        !currentMoveGridItemResult.isSuccess

    when (currentGridItemSource) {
        is GridItemSource.Existing ->
            handleExistingGridItemSource(
                currentMoveGridItemResult = currentMoveGridItemResult,
                isLongPress = isLongPress,
                isMoveGridItemResultFailed = isMoveGridItemResultFailed,
                isVisibleOverlay = isVisibleOverlay,
                lockMovement = lockMovement,
                onResetGrid = onResetGrid,
                onUpdateGridItemsAfterMove = onUpdateGridItemsAfterMove,
                onUpdateIsDragging = onUpdateIsDragging,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
            )

        is GridItemSource.ExistingFolder ->
            handleExistingFolderGridItemSource(
                currentMoveGridItemResult = currentMoveGridItemResult,
                isLongPress = isLongPress,
                isMoveGridItemResultFailed = isMoveGridItemResultFailed,
                isVisibleOverlay = isVisibleOverlay,
                lockMovement = lockMovement,
                onResetGrid = onResetGrid,
                onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
                onUpdateGridItemsAfterMove = onUpdateGridItemsAfterMove,
                onUpdateIsDragging = onUpdateIsDragging,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
            )

        is GridItemSource.New ->
            handleNewGridItemSource(
                androidAppWidgetHostWrapper = androidAppWidgetHostWrapper,
                androidAppWidgetManagerWrapper = androidAppWidgetManagerWrapper,
                androidLauncherAppsWrapper = androidLauncherAppsWrapper,
                androidUserManagerWrapper = androidUserManagerWrapper,
                columns = columns,
                context = context,
                currentMoveGridItemResult = currentMoveGridItemResult,
                density = density,
                gridSize = gridSize,
                isDragging = isDragging,
                isMoveGridItemResultFailed = isMoveGridItemResultFailed,
                isVisibleOverlay = isVisibleOverlay,
                lockMovement = lockMovement,
                rows = rows,
                onLaunchShortcutConfigIntent = onLaunchShortcutConfigIntent,
                onLaunchShortcutConfigIntentSenderRequest = onLaunchShortcutConfigIntentSenderRequest,
                onLaunchWidgetIntent = onLaunchWidgetIntent,
                onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
                onUpdateAppWidgetId = onUpdateAppWidgetId,
                onUpdateGridItemsAfterMove = onUpdateGridItemsAfterMove,
                onUpdateIsDragging = onUpdateIsDragging,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateWidgetGridItem = onUpdateWidgetGridItem,
            )

        is GridItemSource.NewFolder ->
            handleNewFolderGridItemSource(
                context = context,
                currentMoveGridItemResult = currentMoveGridItemResult,
                isDragging = isDragging,
                isMoveGridItemResultFailed = isMoveGridItemResultFailed,
                isVisibleOverlay = isVisibleOverlay,
                lockMovement = lockMovement,
                folderGridItems = currentGridItemSource.folderGridItems,
                onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
                onUpdateGridItemsAfterMoveNewFolder = onUpdateGridItemsAfterMoveNewFolder,
                onUpdateIsDragging = onUpdateIsDragging,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
            )

        is GridItemSource.Pin ->
            handlePinGridItemSource(
                androidAppWidgetHostWrapper = androidAppWidgetHostWrapper,
                androidAppWidgetManagerWrapper = androidAppWidgetManagerWrapper,
                columns = columns,
                context = context,
                currentGridItemSource = currentGridItemSource,
                currentMoveGridItemResult = currentMoveGridItemResult,
                density = density,
                gridSize = gridSize,
                isDragging = isDragging,
                isMoveGridItemResultFailed = isMoveGridItemResultFailed,
                isVisibleOverlay = isVisibleOverlay,
                lockMovement = lockMovement,
                rows = rows,
                onLaunchWidgetIntent = onLaunchWidgetIntent,
                onResetGrid = onResetGrid,
                onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
                onUpdateAppWidgetId = onUpdateAppWidgetId,
                onUpdateGridItemsAfterMove = onUpdateGridItemsAfterMove,
                onUpdateIsDragging = onUpdateIsDragging,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateWidgetGridItem = onUpdateWidgetGridItem,
            )
    }
}

internal fun handleAppWidgetLauncherResult(
    androidAppWidgetManagerWrapper: AndroidAppWidgetManagerWrapper,
    moveGridItemResult: MoveGridItemResult?,
    result: ActivityResult,
    columns: Int,
    density: Density,
    rows: Int,
    screenWidth: Int,
    screenHeight: Int,
    paddingValues: PaddingValues,
    layoutDirection: LayoutDirection,
    dockHeight: Int,
    lastAppWidgetId: Int,
    onUpdateWidgetGridItem: (GridItem) -> Unit,
    onResetGridAfterDeleteGridItem: (GridItem) -> Unit,
    onUpdateLastAppWidgetId: (Int) -> Unit,
) {
    val movingGridItem = requireNotNull(moveGridItemResult?.movingGridItem)

    val data = movingGridItem.data as GridItemData.Widget

    val gridSize = calculateGridSize(
        density = density,
        paddingValues = paddingValues,
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        dockHeight = dockHeight,
        layoutDirection = layoutDirection,
        associate = movingGridItem.associate,
    )

    if (result.resultCode == Activity.RESULT_OK) {
        val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

        val newData = data.copy(appWidgetId = appWidgetId)

        updateAppWidgetOptions(
            height = data.minHeight,
            width = data.minWidth,
            androidAppWidgetManagerWrapper = androidAppWidgetManagerWrapper,
            columns = columns,
            data = newData,
            density = density,
            gridHeight = gridSize.height,
            gridWidth = gridSize.width,
            rows = rows,
            startColumn = movingGridItem.startColumn,
            startRow = movingGridItem.startRow,
        )

        onUpdateWidgetGridItem(movingGridItem.copy(data = newData))
    } else {
        val newData = data.copy(appWidgetId = lastAppWidgetId)

        onResetGridAfterDeleteGridItem(movingGridItem.copy(data = newData))
    }

    onUpdateLastAppWidgetId(AppWidgetManager.INVALID_APPWIDGET_ID)
}

internal fun handleConfigureLauncherResultEffect(
    moveGridItemResult: MoveGridItemResult?,
    resultCode: Int?,
    widgetGridItem: GridItem?,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateGridItemsAfterMove: (MoveGridItemResult) -> Unit,
    onResetConfigureResultCode: () -> Unit,
    onResetGrid: () -> Unit,
) {
    if (resultCode == null) return

    val currentMoveGridItemResult = requireNotNull(moveGridItemResult)

    requireNotNull(widgetGridItem)

    check(widgetGridItem.data is GridItemData.Widget)

    if (resultCode == Activity.RESULT_OK) {
        onUpdateGridItemsAfterMove(currentMoveGridItemResult.copy(movingGridItem = widgetGridItem))

        onResetGrid()
    } else {
        onDeleteGridItem(widgetGridItem)
    }

    onResetConfigureResultCode()
}

internal fun handleBoundWidgetEffect(
    activity: Activity?,
    androidAppWidgetHostWrapper: AndroidAppWidgetHostWrapper,
    gridItemSource: GridItemSource?,
    moveGridItemResult: MoveGridItemResult?,
    widgetGridItem: GridItem?,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateGridItemsAfterMove: (MoveGridItemResult) -> Unit,
    onResetGrid: () -> Unit,
) {
    if (widgetGridItem == null) return

    val currentGridItemSource = requireNotNull(gridItemSource)

    val currentMoveGridItemResult = requireNotNull(moveGridItemResult)

    val data = widgetGridItem.data as GridItemData.Widget

    when (currentGridItemSource) {
        is GridItemSource.New -> {
            startAppWidgetConfigureActivityForResult(
                activity = activity,
                androidAppWidgetHostWrapper = androidAppWidgetHostWrapper,
                appWidgetId = data.appWidgetId,
                configure = data.configure,
                moveGridItemResult = currentMoveGridItemResult,
                updatedWidgetGridItem = widgetGridItem,
                onDeleteGridItem = onDeleteGridItem,
                onUpdateGridItemsAfterMove = onUpdateGridItemsAfterMove,
                onResetGrid = onResetGrid,
            )
        }

        is GridItemSource.Pin -> {
            bindPinWidget(
                appWidgetId = data.appWidgetId,
                moveGridItemResult = currentMoveGridItemResult,
                pinItemRequest = currentGridItemSource.pinItemRequest,
                updatedWidgetGridItem = widgetGridItem,
                onDeleteGridItem = onDeleteGridItem,
                onUpdateGridItemsAfterMove = onUpdateGridItemsAfterMove,
                onResetGrid = onResetGrid,
            )
        }

        else -> Unit
    }
}

@Suppress("DEPRECATION")
internal suspend fun handleShortcutConfigLauncherResult(
    androidImageSerializer: AndroidImageSerializer,
    moveGridItemResult: MoveGridItemResult?,
    result: ActivityResult,
    fileManager: FileManager,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateGridItemsAfterMove: (MoveGridItemResult) -> Unit,
    onResetGrid: () -> Unit,
) {
    val currentMoveGridItemResult = requireNotNull(moveGridItemResult)

    val movingGridItem = currentMoveGridItemResult.movingGridItem

    if (result.resultCode == Activity.RESULT_CANCELED) {
        onDeleteGridItem(movingGridItem)

        return
    }

    val name = result.data?.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)

    val icon = result.data?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            it.getParcelableExtra(
                Intent.EXTRA_SHORTCUT_ICON,
                Bitmap::class.java,
            )
        } else {
            it.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON)
        }
    }?.let { bitmap ->
        androidImageSerializer.createByteArray(bitmap = bitmap)
    }

    val shortcutIntentUri = result.data?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            it.getParcelableExtra(
                Intent.EXTRA_SHORTCUT_INTENT,
                Intent::class.java,
            )
        } else {
            it.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT)
        }
    }?.toUri(Intent.URI_INTENT_SCHEME)

    val movingData = movingGridItem.data as GridItemData.ShortcutConfig

    val shortcutIntentIcon = icon?.let {
        fileManager.updateAndGetFilePath(
            fileManager.getFilesDirectory(FileManager.SHORTCUT_INTENT_ICONS_DIR),
            movingGridItem.id,
            it,
        )
    }

    val newData = movingData.copy(
        shortcutIntentName = name,
        shortcutIntentIcon = shortcutIntentIcon,
        shortcutIntentUri = shortcutIntentUri,
    )

    val newMovingGridItem = movingGridItem.copy(data = newData)

    onUpdateGridItemsAfterMove(currentMoveGridItemResult.copy(movingGridItem = newMovingGridItem))

    onResetGrid()
}

@Suppress("DEPRECATION")
internal suspend fun handleShortcutConfigIntentSenderLauncherResult(
    androidImageSerializer: AndroidImageSerializer,
    androidLauncherAppsWrapper: AndroidLauncherAppsWrapper,
    androidUserManagerWrapper: AndroidUserManagerWrapper,
    fileManager: FileManager,
    moveGridItemResult: MoveGridItemResult?,
    result: ActivityResult,
    iconKeyGenerator: IconKeyGenerator,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateShortcutConfigIntoShortcutInfoGridItem: (
        moveGridItemResult: MoveGridItemResult,
        pinItemRequestType: PinItemRequestType.ShortcutInfo,
    ) -> Unit,
) {
    val currentMoveGridItemResult = requireNotNull(moveGridItemResult)

    val movingGridItem = currentMoveGridItemResult.movingGridItem

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || result.resultCode == Activity.RESULT_CANCELED) {
        onDeleteGridItem(movingGridItem)

        return
    }

    val pinItemRequest = result.data?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            it.getParcelableExtra(
                LauncherApps.EXTRA_PIN_ITEM_REQUEST,
                PinItemRequest::class.java,
            )
        } else {
            it.getParcelableExtra(LauncherApps.EXTRA_PIN_ITEM_REQUEST)
        }
    }

    val shortcutInfo = pinItemRequest?.shortcutInfo

    if (pinItemRequest != null &&
        shortcutInfo != null &&
        pinItemRequest.isValid &&
        pinItemRequest.accept()
    ) {
        val serialNumber =
            androidUserManagerWrapper.getSerialNumberForUser(userHandle = shortcutInfo.userHandle)

        val icon = androidLauncherAppsWrapper.getShortcutBadgedIconDrawable(
            shortcutInfo = shortcutInfo,
            density = 0,
        )?.let {
            val directory = fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR)

            val file = File(
                directory,
                iconKeyGenerator.getShortcutIconKey(
                    serialNumber = serialNumber,
                    packageName = shortcutInfo.`package`,
                    id = shortcutInfo.id,
                ),
            )

            androidImageSerializer.createDrawablePath(drawable = it, file = file)

            file.absolutePath
        }

        val pinItemRequestType = PinItemRequestType.ShortcutInfo(
            serialNumber = serialNumber,
            shortcutId = shortcutInfo.id,
            packageName = shortcutInfo.`package`,
            shortLabel = shortcutInfo.shortLabel.toString(),
            longLabel = shortcutInfo.longLabel.toString(),
            isEnabled = shortcutInfo.isEnabled,
            disabledMessage = shortcutInfo.disabledMessage?.toString(),
            icon = icon,
        )

        onUpdateShortcutConfigIntoShortcutInfoGridItem(
            currentMoveGridItemResult,
            pinItemRequestType,
        )
    } else {
        onDeleteGridItem(movingGridItem)
    }
}

private fun onDragEndWidget(
    androidAppWidgetHostWrapper: AndroidAppWidgetHostWrapper,
    androidAppWidgetManagerWrapper: AndroidAppWidgetManagerWrapper,
    data: GridItemData.Widget,
    gridItem: GridItem,
    columns: Int,
    density: Density,
    gridHeight: Int,
    gridWidth: Int,
    rows: Int,
    onLaunchWidgetIntent: (Intent) -> Unit,
    onUpdateAppWidgetId: (Int) -> Unit,
    onUpdateWidgetGridItem: (GridItem) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
) {
    val appWidgetId = androidAppWidgetHostWrapper.allocateAppWidgetId()

    onUpdateAppWidgetId(appWidgetId)

    val provider = ComponentName.unflattenFromString(data.componentName)

    val bindAppWidgetIdIfAllowed = androidAppWidgetManagerWrapper.bindAppWidgetIdIfAllowed(
        appWidgetId = appWidgetId,
        provider = provider,
    )

    if (bindAppWidgetIdIfAllowed) {
        val newData = data.copy(appWidgetId = appWidgetId)

        updateAppWidgetOptions(
            height = data.minWidth,
            width = data.minHeight,
            androidAppWidgetManagerWrapper = androidAppWidgetManagerWrapper,
            columns = columns,
            data = newData,
            density = density,
            gridHeight = gridHeight,
            gridWidth = gridWidth,
            rows = rows,
            startColumn = gridItem.startColumn,
            startRow = gridItem.startRow,
        )

        onUpdateWidgetGridItem(gridItem.copy(data = newData))
    } else {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        }

        onLaunchWidgetIntent(intent)
    }

    onUpdateIsDragging(false)

    onUpdateIsVisibleOverlay(false)
}

private fun onDragEndPinShortcut(
    gridItem: GridItem,
    moveGridItemResult: MoveGridItemResult,
    pinItemRequest: PinItemRequest?,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateGridItemsAfterMove: (MoveGridItemResult) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onResetGrid: () -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        pinItemRequest != null &&
        pinItemRequest.isValid &&
        pinItemRequest.accept()
    ) {
        onUpdateGridItemsAfterMove(moveGridItemResult)

        if (moveGridItemResult.conflictingGridItem == null) {
            onResetGrid()
        }
    } else {
        onDeleteGridItem(gridItem)
    }

    onUpdateIsDragging(false)

    onUpdateIsVisibleOverlay(false)
}

private fun bindPinWidget(
    appWidgetId: Int,
    moveGridItemResult: MoveGridItemResult,
    pinItemRequest: PinItemRequest,
    updatedWidgetGridItem: GridItem,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateGridItemsAfterMove: (MoveGridItemResult) -> Unit,
    onResetGrid: () -> Unit,
) {
    val extras = Bundle().apply {
        putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pinItemRequest.isValid && pinItemRequest.accept(
            extras,
        )
    ) {
        onUpdateGridItemsAfterMove(moveGridItemResult.copy(movingGridItem = updatedWidgetGridItem))

        onResetGrid()
    } else {
        onDeleteGridItem(updatedWidgetGridItem)
    }
}

private suspend fun onDragEndShortcutConfig(
    androidLauncherAppsWrapper: AndroidLauncherAppsWrapper,
    androidUserManagerWrapper: AndroidUserManagerWrapper,
    data: GridItemData.ShortcutConfig,
    gridItem: GridItem,
    onResetGridAfterDeleteGridItem: (GridItem) -> Unit,
    onLaunchShortcutConfigIntent: (Intent) -> Unit,
    onLaunchShortcutConfigIntentSenderRequest: (IntentSenderRequest) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
) {
    val serialNumber =
        androidUserManagerWrapper.getSerialNumberForUser(userHandle = Process.myUserHandle())

    if (serialNumber == data.serialNumber) {
        val intent = Intent(Intent.ACTION_CREATE_SHORTCUT).setComponent(
            ComponentName.unflattenFromString(data.componentName),
        )

        try {
            onLaunchShortcutConfigIntent(intent)
        } catch (_: ActivityNotFoundException) {
            onResetGridAfterDeleteGridItem(gridItem)
        }
    } else {
        val shortcutConfigIntent = androidLauncherAppsWrapper.getShortcutConfigIntent(
            serialNumber = data.serialNumber,
            packageName = data.packageName,
            componentName = data.componentName,
        )

        if (shortcutConfigIntent != null) {
            val intentSenderRequest = IntentSenderRequest.Builder(shortcutConfigIntent).build()

            onLaunchShortcutConfigIntentSenderRequest(intentSenderRequest)
        } else {
            onResetGridAfterDeleteGridItem(gridItem)
        }
    }

    onUpdateIsDragging(false)

    onUpdateIsVisibleOverlay(false)
}

private fun startAppWidgetConfigureActivityForResult(
    activity: Activity?,
    androidAppWidgetHostWrapper: AndroidAppWidgetHostWrapper,
    appWidgetId: Int,
    configure: String?,
    moveGridItemResult: MoveGridItemResult,
    updatedWidgetGridItem: GridItem,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateGridItemsAfterMove: (MoveGridItemResult) -> Unit,
    onResetGrid: () -> Unit,
) {
    val configureComponent = configure?.let(ComponentName::unflattenFromString)

    try {
        if (activity != null && configureComponent != null) {
            androidAppWidgetHostWrapper.startAppWidgetConfigureActivityForResult(
                activity,
                appWidgetId,
                0,
                AndroidAppWidgetHostWrapper.CONFIGURE_REQUEST_CODE,
                null,
            )
        } else {
            onUpdateGridItemsAfterMove(moveGridItemResult.copy(movingGridItem = updatedWidgetGridItem))

            onResetGrid()
        }
    } catch (_: ActivityNotFoundException) {
        onDeleteGridItem(updatedWidgetGridItem)
    } catch (_: SecurityException) {
        onDeleteGridItem(updatedWidgetGridItem)
    }
}

private fun calculateGridSize(
    density: Density,
    paddingValues: PaddingValues,
    screenWidth: Int,
    screenHeight: Int,
    dockHeight: Int,
    layoutDirection: LayoutDirection,
    associate: Associate,
): IntSize {
    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val rightPadding = with(density) {
        paddingValues.calculateRightPadding(layoutDirection).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val dockHeightPx = with(density) {
        dockHeight.dp.roundToPx()
    }

    val pageIndicatorHeightPx = with(density) {
        PAGE_INDICATOR_HEIGHT.roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding

    val verticalPadding = topPadding + bottomPadding

    val safeDrawingWidth = screenWidth - horizontalPadding

    val safeDrawingHeight = screenHeight - verticalPadding

    val gridHeight = when (associate) {
        Associate.Grid -> safeDrawingHeight - dockHeightPx - pageIndicatorHeightPx
        Associate.Dock -> dockHeightPx
    }

    return IntSize(
        width = safeDrawingWidth,
        height = gridHeight,
    )
}

private fun handleExistingGridItemSource(
    currentMoveGridItemResult: MoveGridItemResult,
    isLongPress: Boolean,
    isMoveGridItemResultFailed: Boolean,
    isVisibleOverlay: Boolean,
    lockMovement: Boolean,
    onResetGrid: () -> Unit,
    onUpdateGridItemsAfterMove: (MoveGridItemResult) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
) {
    fun cancel() {
        onUpdateIsVisibleOverlay(false)

        onUpdateIsDragging(false)

        onResetGrid()
    }

    if (isLongPress) {
        onUpdateIsVisibleOverlay(false)

        return
    }

    if (isVisibleOverlay &&
        isMoveGridItemResultFailed
    ) {
        return cancel()
    }

    if (lockMovement) return cancel()

    if (isVisibleOverlay) {
        onUpdateGridItemsAfterMove(currentMoveGridItemResult)

        onUpdateIsDragging(false)
    }
}

private fun handleExistingFolderGridItemSource(
    currentMoveGridItemResult: MoveGridItemResult,
    isLongPress: Boolean,
    isMoveGridItemResultFailed: Boolean,
    isVisibleOverlay: Boolean,
    lockMovement: Boolean,
    onResetGrid: () -> Unit,
    onResetGridAfterDeleteGridItem: (GridItem) -> Unit,
    onUpdateGridItemsAfterMove: (MoveGridItemResult) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
) {
    fun cancel() {
        onUpdateIsVisibleOverlay(false)

        onUpdateIsDragging(false)

        onResetGrid()

        onResetGridAfterDeleteGridItem(currentMoveGridItemResult.movingGridItem)
    }

    if (isLongPress) {
        onUpdateIsVisibleOverlay(false)

        return
    }

    if (isVisibleOverlay &&
        isMoveGridItemResultFailed
    ) {
        return cancel()
    }

    if (lockMovement) return cancel()

    if (isVisibleOverlay) {
        onUpdateGridItemsAfterMove(currentMoveGridItemResult)

        onUpdateIsDragging(false)
    }
}

private suspend fun handleNewGridItemSource(
    androidAppWidgetHostWrapper: AndroidAppWidgetHostWrapper,
    androidAppWidgetManagerWrapper: AndroidAppWidgetManagerWrapper,
    androidLauncherAppsWrapper: AndroidLauncherAppsWrapper,
    androidUserManagerWrapper: AndroidUserManagerWrapper,
    columns: Int,
    context: Context,
    currentMoveGridItemResult: MoveGridItemResult,
    density: Density,
    gridSize: IntSize,
    isDragging: Boolean,
    isMoveGridItemResultFailed: Boolean,
    isVisibleOverlay: Boolean,
    lockMovement: Boolean,
    rows: Int,
    onLaunchShortcutConfigIntent: (Intent) -> Unit,
    onLaunchShortcutConfigIntentSenderRequest: (IntentSenderRequest) -> Unit,
    onLaunchWidgetIntent: (Intent) -> Unit,
    onResetGridAfterDeleteGridItem: (GridItem) -> Unit,
    onUpdateAppWidgetId: (Int) -> Unit,
    onUpdateGridItemsAfterMove: (MoveGridItemResult) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateWidgetGridItem: (GridItem) -> Unit,
) {
    if (isVisibleOverlay &&
        isDragging &&
        isMoveGridItemResultFailed
    ) {
        return cancelAndDeleteGridItem(
            context = context,
            moveGridItemResult = currentMoveGridItemResult,
            onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
            onUpdateIsDragging = onUpdateIsDragging,
            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
        )
    }

    if (lockMovement) {
        return cancelAndDeleteGridItem(
            context = context,
            moveGridItemResult = currentMoveGridItemResult,
            onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
            onUpdateIsDragging = onUpdateIsDragging,
            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
        )
    }

    if (isVisibleOverlay &&
        isDragging
    ) {
        val movingGridItem = currentMoveGridItemResult.movingGridItem

        when (val data = movingGridItem.data) {
            is GridItemData.Widget -> {
                onDragEndWidget(
                    androidAppWidgetHostWrapper = androidAppWidgetHostWrapper,
                    androidAppWidgetManagerWrapper = androidAppWidgetManagerWrapper,
                    data = data,
                    gridItem = movingGridItem,
                    columns = columns,
                    density = density,
                    gridHeight = gridSize.height,
                    gridWidth = gridSize.width,
                    rows = rows,
                    onLaunchWidgetIntent = onLaunchWidgetIntent,
                    onUpdateAppWidgetId = onUpdateAppWidgetId,
                    onUpdateWidgetGridItem = onUpdateWidgetGridItem,
                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                    onUpdateIsDragging = onUpdateIsDragging,
                )
            }

            is GridItemData.ShortcutConfig -> {
                onDragEndShortcutConfig(
                    androidLauncherAppsWrapper = androidLauncherAppsWrapper,
                    androidUserManagerWrapper = androidUserManagerWrapper,
                    data = data,
                    gridItem = movingGridItem,
                    onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
                    onLaunchShortcutConfigIntent = onLaunchShortcutConfigIntent,
                    onLaunchShortcutConfigIntentSenderRequest = onLaunchShortcutConfigIntentSenderRequest,
                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                    onUpdateIsDragging = onUpdateIsDragging,
                )
            }

            is GridItemData.ApplicationInfo,
            is GridItemData.Folder,
            is GridItemData.ShortcutInfo,
            -> {
                onUpdateGridItemsAfterMove(currentMoveGridItemResult)

                onUpdateIsDragging(false)
            }
        }
    }
}

private fun handleNewFolderGridItemSource(
    context: Context,
    currentMoveGridItemResult: MoveGridItemResult,
    isDragging: Boolean,
    isMoveGridItemResultFailed: Boolean,
    isVisibleOverlay: Boolean,
    lockMovement: Boolean,
    folderGridItems: List<GridItem>,
    onResetGridAfterDeleteGridItem: (GridItem) -> Unit,
    onUpdateGridItemsAfterMoveNewFolder: (
        folderGridItems: List<GridItem>,
        moveGridItemResult: MoveGridItemResult,
    ) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
) {
    if (isVisibleOverlay &&
        isDragging &&
        isMoveGridItemResultFailed
    ) {
        return cancelAndDeleteGridItem(
            context = context,
            moveGridItemResult = currentMoveGridItemResult,
            onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
            onUpdateIsDragging = onUpdateIsDragging,
            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
        )
    }

    if (lockMovement) {
        return cancelAndDeleteGridItem(
            context = context,
            moveGridItemResult = currentMoveGridItemResult,
            onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
            onUpdateIsDragging = onUpdateIsDragging,
            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
        )
    }

    if (isVisibleOverlay &&
        isDragging
    ) {
        onUpdateGridItemsAfterMoveNewFolder(
            folderGridItems,
            currentMoveGridItemResult,
        )

        onUpdateIsDragging(false)
    }
}

private fun handlePinGridItemSource(
    androidAppWidgetHostWrapper: AndroidAppWidgetHostWrapper,
    androidAppWidgetManagerWrapper: AndroidAppWidgetManagerWrapper,
    columns: Int,
    context: Context,
    currentGridItemSource: GridItemSource.Pin,
    currentMoveGridItemResult: MoveGridItemResult,
    density: Density,
    gridSize: IntSize,
    isDragging: Boolean,
    isMoveGridItemResultFailed: Boolean,
    isVisibleOverlay: Boolean,
    lockMovement: Boolean,
    rows: Int,
    onLaunchWidgetIntent: (Intent) -> Unit,
    onResetGrid: () -> Unit,
    onResetGridAfterDeleteGridItem: (GridItem) -> Unit,
    onUpdateAppWidgetId: (Int) -> Unit,
    onUpdateGridItemsAfterMove: (MoveGridItemResult) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateWidgetGridItem: (GridItem) -> Unit,
) {
    if (isVisibleOverlay &&
        isDragging &&
        isMoveGridItemResultFailed
    ) {
        return cancelAndDeleteGridItem(
            context = context,
            moveGridItemResult = currentMoveGridItemResult,
            onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
            onUpdateIsDragging = onUpdateIsDragging,
            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
        )
    }

    if (lockMovement) {
        return cancelAndDeleteGridItem(
            context = context,
            moveGridItemResult = currentMoveGridItemResult,
            onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
            onUpdateIsDragging = onUpdateIsDragging,
            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
        )
    }

    if (isVisibleOverlay && isDragging) {
        val movingGridItem = currentMoveGridItemResult.movingGridItem

        when (val data = movingGridItem.data) {
            is GridItemData.ShortcutInfo -> onDragEndPinShortcut(
                gridItem = movingGridItem,
                moveGridItemResult = currentMoveGridItemResult,
                pinItemRequest = currentGridItemSource.pinItemRequest,
                onDeleteGridItem = onResetGridAfterDeleteGridItem,
                onUpdateGridItemsAfterMove = onUpdateGridItemsAfterMove,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateIsDragging = onUpdateIsDragging,
                onResetGrid = onResetGrid,
            )

            is GridItemData.Widget -> onDragEndWidget(
                androidAppWidgetHostWrapper = androidAppWidgetHostWrapper,
                androidAppWidgetManagerWrapper = androidAppWidgetManagerWrapper,
                data = data,
                gridItem = movingGridItem,
                columns = columns,
                density = density,
                gridHeight = gridSize.height,
                gridWidth = gridSize.width,
                rows = rows,
                onLaunchWidgetIntent = onLaunchWidgetIntent,
                onUpdateAppWidgetId = onUpdateAppWidgetId,
                onUpdateWidgetGridItem = onUpdateWidgetGridItem,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateIsDragging = onUpdateIsDragging,
            )

            else -> error("Expected ShortcutInfo or Widget")
        }
    }
}

private fun cancelAndDeleteGridItem(
    context: Context,
    moveGridItemResult: MoveGridItemResult,
    onResetGridAfterDeleteGridItem: (GridItem) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
) {
    onUpdateIsVisibleOverlay(false)

    onUpdateIsDragging(false)

    onResetGridAfterDeleteGridItem(moveGridItemResult.movingGridItem)

    Toast.makeText(
        context,
        context.getString(R.string.please_wait_for_the_white_box_indicator),
        Toast.LENGTH_LONG,
    ).show()
}
