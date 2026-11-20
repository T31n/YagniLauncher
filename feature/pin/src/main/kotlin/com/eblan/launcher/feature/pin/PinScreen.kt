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

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ClipData
import android.content.Context
import android.content.pm.LauncherApps.PinItemRequest
import android.content.pm.ShortcutInfo
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.platform.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.platform.usermanager.AndroidUserManagerWrapper
import com.eblan.launcher.platform.widgetmanager.AndroidAppWidgetManagerWrapper
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalPinItemRequest
import com.eblan.launcher.ui.local.LocalUserManager
import kotlinx.coroutines.launch
import com.eblan.launcher.common.R as commonR

@Composable
fun PinScreen(
    // Data and State
    pinItemRequest: PinItemRequest,
    modifier: Modifier = Modifier,
    viewModel: PinScreenViewModel = hiltViewModel(),
    // Callbacks
    onDragStart: () -> Unit,
    onFinish: () -> Unit,
) {
    val gridItem by viewModel.gridItem.collectAsStateWithLifecycle()
    val isBoundWidget by viewModel.isBoundWidget.collectAsStateWithLifecycle()
    val isFinished by viewModel.isFinished.collectAsStateWithLifecycle()

    when (pinItemRequest.requestType) {
        PinItemRequest.REQUEST_TYPE_APPWIDGET -> {
            PinWidgetScreen(
                // Widget State
                gridItem = gridItem,
                pinItemRequest = pinItemRequest,
                isBoundWidget = isBoundWidget,
                isFinished = isFinished,
                modifier = modifier,
                // Widget Actions
                onAddPinWidgetToHomeScreen = viewModel::addPinWidgetToHomeScreen,
                onDeleteGridItemCache = viewModel::deleteGridItem,
                onDragStart = onDragStart,
                onFinish = onFinish,
                onUpdateGridItemCache = viewModel::updateGridItemData,
                onUpdateGridItems = viewModel::updateGridItems,
            )
        }

        PinItemRequest.REQUEST_TYPE_SHORTCUT -> {
            PinShortcutScreen(
                // Item Data
                gridItem = gridItem,
                pinItemRequest = pinItemRequest,
                // State
                isFinished = isFinished,
                // Layout
                modifier = modifier,
                // Actions
                onAddPinShortcutToHomeScreen = viewModel::addPinShortcutToHomeScreen,
                onDeleteShortcutGridItem = viewModel::deleteGridItem,
                onDragStart = onDragStart,
                onFinish = onFinish,
                onUpdateGridItems = viewModel::updateGridItems,
            )
        }
    }
}

@Composable
private fun PinShortcutScreen(
    // Item Data
    gridItem: GridItem?,
    pinItemRequest: PinItemRequest,
    // State
    isFinished: Boolean,
    // Layout
    modifier: Modifier = Modifier,
    // Actions
    onAddPinShortcutToHomeScreen: (
        serialNumber: Long,
        shortcutId: String,
        packageName: String,
        shortLabel: String,
        longLabel: String,
        isEnabled: Boolean,
        icon: ByteArray?,
    ) -> Unit,
    onDeleteShortcutGridItem: (GridItem) -> Unit,
    onDragStart: () -> Unit,
    onFinish: () -> Unit,
    onUpdateGridItems: () -> Unit,
) {
    val shortcutInfo = pinItemRequest.shortcutInfo ?: return

    val pinItemRequestWrapper = LocalPinItemRequest.current
    val androidLauncherAppsWrapper = LocalLauncherApps.current
    val androidUserManagerWrapper = LocalUserManager.current
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val icon by produceState<ByteArray?>(
        initialValue = null,
        key1 = shortcutInfo,
        key2 = androidLauncherAppsWrapper,
    ) {
        value = androidLauncherAppsWrapper.getShortcutBadgedIconByteArray(
            shortcutInfo = shortcutInfo,
            density = 0,
        )
    }

    LaunchedEffect(key1 = gridItem) {
        handleGridItem(
            // Context and Item
            context = context,
            gridItem = gridItem,
            pinItemRequest = pinItemRequest,
            // Callbacks
            onUpdateGridItems = onUpdateGridItems,
            onDeleteShortcutGridItem = onDeleteShortcutGridItem,
        )
    }

    LaunchedEffect(key1 = isFinished) {
        if (isFinished) {
            onFinish()
        }
    }

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            PinBottomSheet(
                // Content
                icon = icon,
                label = shortcutInfo.shortLabel.toString(),
                // Actions
                onAdd = {
                    scope.launch {
                        addPinShortcutToHomeScreen(
                            // Shortcut Information
                            shortcutInfo = shortcutInfo,
                            // System Wrappers
                            androidUserManagerWrapper = androidUserManagerWrapper,
                            androidLauncherAppsWrapper = androidLauncherAppsWrapper,
                            // Callbacks
                            onAddPinShortcutToHomeScreen = onAddPinShortcutToHomeScreen,
                        )
                    }
                },
                onFinish = onFinish,
                onLongPress = {
                    pinItemRequestWrapper.updatePinItemRequest(
                        pinItemRequest = pinItemRequest,
                    )

                    onDragStart()

                    onFinish()
                },
            )
        }
    }
}

@Composable
private fun PinWidgetScreen(
    // Widget State
    gridItem: GridItem?,
    pinItemRequest: PinItemRequest,
    isBoundWidget: Boolean,
    isFinished: Boolean,
    modifier: Modifier = Modifier,
    // Widget Actions
    onAddPinWidgetToHomeScreen: (
        serialNumber: Long,
        componentName: String,
        configure: String?,
        packageName: String,
        targetCellHeight: Int,
        targetCellWidth: Int,
        minWidth: Int,
        minHeight: Int,
        resizeMode: Int,
        minResizeWidth: Int,
        minResizeHeight: Int,
        maxResizeWidth: Int,
        maxResizeHeight: Int,
        rootWidth: Int,
        rootHeight: Int,
        preview: ByteArray?,
    ) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onDragStart: () -> Unit,
    onFinish: () -> Unit,
    onUpdateGridItemCache: (GridItem) -> Unit,
    onUpdateGridItems: () -> Unit,
) {
    val context = LocalContext.current

    val appWidgetProviderInfo = pinItemRequest.getAppWidgetProviderInfo(context) ?: return

    val pinItemRequestWrapper = LocalPinItemRequest.current
    val appWidgetHostWrapper = LocalAppWidgetHost.current
    val appWidgetManager = LocalAppWidgetManager.current
    val userManager = LocalUserManager.current

    val paddingValues = WindowInsets.safeDrawing.asPaddingValues()

    var appWidgetId by remember { mutableIntStateOf(AppWidgetManager.INVALID_APPWIDGET_ID) }

    var deleteAppWidgetId by remember { mutableStateOf(false) }

    val icon by produceState<ByteArray?>(
        initialValue = null,
        key1 = appWidgetProviderInfo,
        key2 = appWidgetManager,
    ) {
        value = appWidgetManager.loadPreviewImageByteArray(
            appWidgetProviderInfo = appWidgetProviderInfo,
            density = 0,
        )
    }

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleAppWidgetLauncherResult(
            // Result
            result = result,
            gridItem = gridItem,
            // Callbacks
            onDeleteAppWidgetId = {
                deleteAppWidgetId = true
            },
            onUpdateGridItemCache = onUpdateGridItemCache,
        )
    }

    LaunchedEffect(key1 = gridItem) {
        handleGridItem(
            // Item
            gridItem = gridItem,
            // Managers
            appWidgetManager = appWidgetManager,
            appWidgetHostWrapper = appWidgetHostWrapper,
            userHandle = appWidgetProviderInfo.profile,
            // Callbacks
            onAddedToHomeScreenToast = { currentAddedToHomeScreenMessage ->
                Toast.makeText(
                    context,
                    currentAddedToHomeScreenMessage,
                    Toast.LENGTH_LONG,
                ).show()
            },
            onLaunch = appWidgetLauncher::launch,
            onUpdateAppWidgetId = { currentAppWidgetId ->
                appWidgetId = currentAppWidgetId
            },
            onUpdateGridItemCache = onUpdateGridItemCache,
        )
    }

    LaunchedEffect(key1 = deleteAppWidgetId) {
        handleDeleteAppWidgetId(
            // App Widget
            appWidgetId = appWidgetId,
            deleteAppWidgetId = deleteAppWidgetId,
            // Grid Item
            gridItem = gridItem,
            // Callbacks
            onDeleteGridItem = onDeleteGridItemCache,
        )
    }

    LaunchedEffect(key1 = isBoundWidget) {
        handleIsBoundWidget(
            // Widget Info
            appWidgetId = appWidgetId,
            isBoundWidget = isBoundWidget,
            // Grid and Pin Items
            gridItem = gridItem,
            pinItemRequest = pinItemRequest,
            // Callbacks
            onDeleteGridItem = onDeleteGridItemCache,
            onUpdateGridItems = onUpdateGridItems,
        )
    }

    LaunchedEffect(key1 = isFinished) {
        if (isFinished) {
            onFinish()
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        PinBottomSheet(
            // Content
            icon = icon,
            label = appWidgetProviderInfo.loadLabel(context.packageManager),
            // Actions
            onAdd = {
                addPinWidgetToHomeScreen(
                    // Widget Info
                    appWidgetProviderInfo = appWidgetProviderInfo,
                    // Dimensions
                    maxWidth = constraints.maxWidth,
                    maxHeight = constraints.maxHeight,
                    // Dependencies
                    appWidgetManager = appWidgetManager,
                    userManager = userManager,
                    // Callback
                    onAddPinWidgetToHomeScreen = onAddPinWidgetToHomeScreen,
                )
            },
            onFinish = onFinish,
            onLongPress = {
                pinItemRequestWrapper.updatePinItemRequest(
                    pinItemRequest = pinItemRequest,
                )

                onDragStart()

                onFinish()
            },
        )
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PinBottomSheet(
    // Content
    icon: Any?,
    label: String,
    // Layout
    modifier: Modifier = Modifier,
    // Actions
    onAdd: suspend () -> Unit,
    onFinish: () -> Unit,
    onLongPress: () -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(true) }

    val sheetState = rememberModalBottomSheetState()

    val scope = rememberCoroutineScope()

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false

                onFinish()
            },
            sheetState = sheetState,
        ) {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.touch_and_hold_the_widget_to_move_it_around_the_home_screen),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(10.dp))

                AsyncImage(
                    modifier = Modifier
                        .dragAndDropSource(
                            block = {
                                detectTapGestures(
                                    onLongPress = {
                                        startTransfer(
                                            DragAndDropTransferData(
                                                clipData = ClipData.newPlainText(
                                                    "PinItemRequest",
                                                    "PinItemRequest",
                                                ),
                                                flags = View.DRAG_FLAG_GLOBAL,
                                            ),
                                        )

                                        onLongPress()
                                    },
                                )
                            },
                        )
                        .size(100.dp),
                    model = icon,
                    contentDescription = null,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false

                                    onFinish()
                                }
                            }
                        },
                    ) {
                        Text(text = stringResource(commonR.string.cancel))
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                onAdd()
                            }
                        },
                    ) {
                        Text(text = stringResource(commonR.string.add))
                    }
                }
            }
        }
    }
}

private fun handleGridItem(
    // Context and Item
    context: Context,
    gridItem: GridItem?,
    pinItemRequest: PinItemRequest,
    // Callbacks
    onUpdateGridItems: () -> Unit,
    onDeleteShortcutGridItem: (GridItem) -> Unit,
) {
    if (gridItem == null) return

    if (pinItemRequest.isValid && pinItemRequest.accept()) {
        Toast.makeText(
            context,
            """
                ${gridItem.page}
                ${gridItem.startRow}
                ${gridItem.startColumn}
            """.trimIndent(),
            Toast.LENGTH_LONG,
        ).show()

        onUpdateGridItems()
    } else {
        onDeleteShortcutGridItem(gridItem)
    }
}

private suspend fun addPinShortcutToHomeScreen(
    // Shortcut Information
    shortcutInfo: ShortcutInfo,
    // System Wrappers
    androidUserManagerWrapper: AndroidUserManagerWrapper,
    androidLauncherAppsWrapper: AndroidLauncherAppsWrapper,
    // Callbacks
    onAddPinShortcutToHomeScreen: (
        serialNumber: Long,
        shortcutId: String,
        packageName: String,
        shortLabel: String,
        longLabel: String,
        isEnabled: Boolean,
        icon: ByteArray?,
    ) -> Unit,
) {
    val serialNumber =
        androidUserManagerWrapper.getSerialNumberForUser(userHandle = shortcutInfo.userHandle)

    val icon = androidLauncherAppsWrapper.getShortcutBadgedIconByteArray(
        shortcutInfo = shortcutInfo,
        density = 0,
    )

    onAddPinShortcutToHomeScreen(
        serialNumber,
        shortcutInfo.id,
        shortcutInfo.`package`,
        shortcutInfo.shortLabel.toString(),
        shortcutInfo.longLabel.toString(),
        shortcutInfo.isEnabled,
        icon,
    )
}

private suspend fun addPinWidgetToHomeScreen(
    // Widget Info
    appWidgetProviderInfo: AppWidgetProviderInfo,
    // Dimensions
    maxWidth: Int,
    maxHeight: Int,
    // Dependencies
    appWidgetManager: AndroidAppWidgetManagerWrapper,
    userManager: AndroidUserManagerWrapper,
    // Callback
    onAddPinWidgetToHomeScreen: (
        serialNumber: Long,
        componentName: String,
        configure: String?,
        packageName: String,
        targetCellHeight: Int,
        targetCellWidth: Int,
        minWidth: Int,
        minHeight: Int,
        resizeMode: Int,
        minResizeWidth: Int,
        minResizeHeight: Int,
        maxResizeWidth: Int,
        maxResizeHeight: Int,
        rootWidth: Int,
        rootHeight: Int,
        preview: ByteArray?,
    ) -> Unit,
) {
    val serialNumber =
        userManager.getSerialNumberForUser(userHandle = appWidgetProviderInfo.profile)

    val componentName = appWidgetProviderInfo.provider.flattenToString()

    val preview = appWidgetManager.loadPreviewImageByteArray(
        appWidgetProviderInfo = appWidgetProviderInfo,
        density = 0,
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        onAddPinWidgetToHomeScreen(
            serialNumber,
            componentName,
            appWidgetProviderInfo.configure?.flattenToString(),
            appWidgetProviderInfo.provider.packageName,
            appWidgetProviderInfo.targetCellHeight,
            appWidgetProviderInfo.targetCellWidth,
            appWidgetProviderInfo.minWidth,
            appWidgetProviderInfo.minHeight,
            appWidgetProviderInfo.resizeMode,
            appWidgetProviderInfo.minResizeWidth,
            appWidgetProviderInfo.minResizeHeight,
            appWidgetProviderInfo.maxResizeWidth,
            appWidgetProviderInfo.maxResizeHeight,
            maxWidth,
            maxHeight,
            preview,
        )
    } else {
        onAddPinWidgetToHomeScreen(
            serialNumber,
            componentName,
            appWidgetProviderInfo.configure?.flattenToString(),
            appWidgetProviderInfo.provider.packageName,
            0,
            0,
            appWidgetProviderInfo.minWidth,
            appWidgetProviderInfo.minHeight,
            appWidgetProviderInfo.resizeMode,
            appWidgetProviderInfo.minResizeWidth,
            appWidgetProviderInfo.minResizeHeight,
            0,
            0,
            maxWidth,
            maxHeight,
            preview,
        )
    }
}
