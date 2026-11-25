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

import android.content.ClipDescription
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.UserHandle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.folder.FolderPopup
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.domain.model.userdata.ExperimentalSettings
import com.eblan.launcher.domain.model.userdata.FolderSettings
import com.eblan.launcher.domain.model.userdata.GestureSettings
import com.eblan.launcher.domain.model.userdata.HomeSettings
import com.eblan.launcher.domain.model.userdata.PageIndicatorType
import com.eblan.launcher.domain.model.userdata.ScreenOrientation
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.GridLayout
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.component.PageIndicator
import com.eblan.launcher.feature.home.component.SparkleGridLayout
import com.eblan.launcher.feature.home.component.pagerTransitionEffect
import com.eblan.launcher.feature.home.contract.PagerScreenContract
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.application.ApplicationInfoPopup
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.application.PrivateApplicationInfoPopup
import com.eblan.launcher.feature.home.screen.application.folder.FolderApplicationInfoGridItemPopup
import com.eblan.launcher.feature.home.screen.application.folder.FolderApplicationInfoPopup
import com.eblan.launcher.feature.home.screen.application.folder.FolderApplicationScreen
import com.eblan.launcher.feature.home.screen.folder.FolderGridItemPopup
import com.eblan.launcher.feature.home.screen.folder.FolderScreen
import com.eblan.launcher.feature.home.screen.pager.griditem.InteractiveGridItem
import com.eblan.launcher.feature.home.screen.resize.ResizeScreen
import com.eblan.launcher.feature.home.screen.shortcutconfig.ShortcutConfigScreen
import com.eblan.launcher.feature.home.screen.widget.AppWidgetScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.feature.home.util.PAGE_INDICATOR_HEIGHT
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.getBackgroundColor
import com.eblan.launcher.feature.home.util.getTextColor
import com.eblan.launcher.platform.service.EblanNotificationListenerService
import com.eblan.launcher.platform.usermanager.AndroidUserManagerWrapper
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import com.eblan.launcher.ui.local.LocalImageSerializer
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalPinItemRequest
import com.eblan.launcher.ui.local.LocalUserManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun PagerScreen(
    // Contract
    contract: PagerScreenContract,
    // Screen Dimensions
    screenWidth: Int,
    screenHeight: Int,
    // Settings
    homeSettings: HomeSettings,
    folderSettings: FolderSettings,
    appDrawerSettings: AppDrawerSettings,
    gestureSettings: GestureSettings,
    experimentalSettings: ExperimentalSettings,
    textColor: TextColor,
    dockTextColor: TextColor,
    appDrawerTextColor: TextColor,
    // System Features
    hasSystemFeatureAppWidgets: Boolean,
    hasShortcutHostPermission: Boolean,
    // Data
    gridItems: List<GridItem>,
    gridItemsByPage: Map<Int, List<GridItem>>,
    dockGridItemsByPage: Map<Int, List<GridItem>>,
    folderPopups: Map<String, FolderPopup>,
    iconPackFilePaths: Map<String, String?>,
    // State
    configureResultCode: Int?,
    // Layout
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    // Callbacks - Navigation and Activity Actions
    onResetConfigureResultCode: () -> Unit,
    onEditGridItem: (String) -> Unit,
    onEditFolderApplicationInfo: (String) -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onSettings: () -> Unit,
) {
    val folderGridItemEntries by contract.folderGridItemEntries.collectAsStateWithLifecycle()
    val folderEblanApplicationInfoEntries by contract.folderEblanApplicationInfoEntries.collectAsStateWithLifecycle()
    val eblanAppWidgetProviderInfosGroup by contract.eblanAppWidgetProviderInfosGroup.collectAsStateWithLifecycle()
    val eblanShortcutInfosGroup by contract.eblanShortcutInfosGroup.collectAsStateWithLifecycle()
    val moveGridItemResult by contract.movedGridItemResult.collectAsStateWithLifecycle()
    val pinGridItem by contract.pinGridItem.collectAsStateWithLifecycle()
    val resizeGridItem by contract.resizeGridItem.collectAsStateWithLifecycle()
    val gridItemSource by contract.gridItemSource.collectAsStateWithLifecycle()
    val isVisibleOverlay by contract.isVisibleOverlay.collectAsStateWithLifecycle()

    val layoutDirection = LocalLayoutDirection.current
    val androidLauncherAppsWrapper = LocalLauncherApps.current
    val view = LocalView.current
    val activity = LocalActivity.current as ComponentActivity
    val density = LocalDensity.current
    val context = LocalContext.current
    val androidUserManagerWrapper = LocalUserManager.current
    val androidImageSerializer = LocalImageSerializer.current
    val androidAppWidgetManagerWrapper = LocalAppWidgetManager.current
    val androidAppWidgetHostWrapper = LocalAppWidgetHost.current

    val scope = rememberCoroutineScope()

    val pagerScreenState = rememberPagerScreenState(
        // Settings
        gestureSettings = gestureSettings,
        wallpaperScroll = homeSettings.wallpaperScroll,
        pageCount = homeSettings.pageCount,
        infiniteScroll = homeSettings.infiniteScroll,
        initialPage = homeSettings.initialPage,
        dockInfiniteScroll = homeSettings.dockInfiniteScroll,
        dockInitialPage = homeSettings.dockInitialPage,
        dockPageCount = homeSettings.dockPageCount,
        // Dimensions
        screenHeight = screenHeight,
        // Callbacks
        onGetPinGridItem = contract::getPinGridItem,
        onResetPinGridItem = contract::resetPinGridItem,
    )

    val visibleDockHeight = when (homeSettings.enableDock) {
        true -> homeSettings.dockHeight
        false -> 0
    }

    val dockHeight = visibleDockHeight.dp

    val dockHeightPx = with(density) {
        dockHeight.roundToPx()
    }

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

    val horizontalPadding = leftPadding + rightPadding

    val verticalPadding = topPadding + bottomPadding

    val safeDrawingWidth = screenWidth - horizontalPadding

    val safeDrawingHeight = screenHeight - verticalPadding

    val dockTopLeft = safeDrawingHeight - dockHeightPx

    val pageIndicatorHeightPx = with(density) {
        PAGE_INDICATOR_HEIGHT.roundToPx()
    }

    val gridHeight = safeDrawingHeight - pageIndicatorHeightPx - dockHeightPx

    val currentGridItemSource = rememberUpdatedState(gridItemSource)
    val currentIsVisibleOverlay = rememberUpdatedState(isVisibleOverlay)
    val currentMoveGridItemResult = rememberUpdatedState(moveGridItemResult)
    val currentFolderGridItemEntries = rememberUpdatedState(folderGridItemEntries)
    var sharedTransitionLayoutCoordinates by remember {
        mutableStateOf<LayoutCoordinates?>(null)
    }

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { currentActivityResult ->
        handleAppWidgetLauncherResult(
            // Result and Manager
            androidAppWidgetManagerWrapper = androidAppWidgetManagerWrapper,
            moveGridItemResult = currentMoveGridItemResult.value,
            result = currentActivityResult,
            lastAppWidgetId = pagerScreenState.lastAppWidgetId,
            // Screen and Grid Configuration
            columns = homeSettings.columns,
            rows = homeSettings.rows,
            dockColumns = homeSettings.dockColumns,
            dockRows = homeSettings.dockRows,
            density = density,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            paddingValues = paddingValues,
            layoutDirection = layoutDirection,
            dockHeight = visibleDockHeight,
            // Callbacks
            onUpdateWidgetGridItem = pagerScreenState::updateWidgetGridItem,
            onResetGridAfterDeleteGridItem = contract::resetGridAfterDeleteGridItem,
            onUpdateLastAppWidgetId = pagerScreenState::updateLastAppWidgetId,
        )
    }

    val shortcutConfigLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { currentActivityResult ->
        scope.launch {
            handleShortcutConfigLauncherResult(
                // Result Parameters
                result = currentActivityResult,
                moveGridItemResult = currentMoveGridItemResult.value,
                // Dependencies
                androidImageSerializer = androidImageSerializer,
                // Callbacks
                onDeleteGridItem = contract::resetGridAfterDeleteGridItem,
                onUpdateShortcutConfigGridItem = contract::updateShortcutConfigGridItem,
            )
        }
    }

    val shortcutConfigIntentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { currentActivityResult ->
        scope.launch {
            handleShortcutConfigIntentSenderLauncherResult(
                // Results
                moveGridItemResult = currentMoveGridItemResult.value,
                result = currentActivityResult,
                // Dependencies
                androidLauncherAppsWrapper = androidLauncherAppsWrapper,
                androidUserManagerWrapper = androidUserManagerWrapper,
                // Callbacks
                onDeleteGridItem = contract::resetGridAfterDeleteGridItem,
                onUpdateShortcutConfigIntoShortcutInfoGridItem = contract::updateShortcutConfigIntoShortcutInfoGridItem,
            )
        }
    }

    val gridHorizontalPagerState = rememberPagerState(
        initialPage = if (homeSettings.infiniteScroll) {
            (Int.MAX_VALUE / 2) + homeSettings.initialPage
        } else {
            homeSettings.initialPage
        },
        pageCount = {
            if (homeSettings.infiniteScroll) {
                Int.MAX_VALUE
            } else {
                homeSettings.pageCount
            }
        },
    )

    val dockGridHorizontalPagerState = rememberPagerState(
        initialPage = if (homeSettings.dockInfiniteScroll) {
            (Int.MAX_VALUE / 2) + homeSettings.dockInitialPage
        } else {
            homeSettings.dockInitialPage
        },
        pageCount = {
            if (homeSettings.dockInfiniteScroll) {
                Int.MAX_VALUE
            } else {
                homeSettings.dockPageCount
            }
        },
    )

    val hasMenuGeometry =
        pagerScreenState.menuIntOffset != null && pagerScreenState.menuIntSize != null

    val isVisibleGridItemMenu =
        pagerScreenState.selectedGridItem != null &&
            pagerScreenState.showGridItemMenu &&
            hasMenuGeometry

    val isVisibleSettingsMenu =
        pagerScreenState.showSettingsMenu && pagerScreenState.settingsMenuIntOffset != null

    val isVisibleFolderGridItems =
        pagerScreenState.isVisibleFolderGridItems && folderGridItemEntries.isNotEmpty()

    val isVisibleFolderEblanApplicationInfos =
        pagerScreenState.isVisibleFolderEblanApplicationInfos &&
            folderEblanApplicationInfoEntries.isNotEmpty()

    val isVisibleFolderGridItemMenu =
        pagerScreenState.selectedGridItem != null &&
            pagerScreenState.showFolderGridItemMenu &&
            hasMenuGeometry

    val isResizing = pagerScreenState.isResizing && resizeGridItem != null

    val shouldLockScreenOrientation =
        isVisibleGridItemMenu ||
            isVisibleSettingsMenu ||
            isVisibleFolderGridItems ||
            isVisibleFolderGridItemMenu ||
            isResizing ||
            pagerScreenState.showApplicationScreen ||
            pagerScreenState.showWidgetScreen ||
            pagerScreenState.showShortcutConfigScreen ||
            pagerScreenState.eblanApplicationInfoGroup != null

    val statusBarNotifications by produceStatusBarNotificationsState()

    val isVisibleFolderEblanApplicationInfoGridItemMenu =
        pagerScreenState.selectedFolderEblanApplicationInfoGridItem != null &&
            pagerScreenState.showFolderEblanApplicationInfoGridItemMenu &&
            hasMenuGeometry

    val isDragOnDock =
        dockHeightPx > 0 &&
            (pagerScreenState.dragIntOffset.y - topPadding) > safeDrawingHeight - dockHeightPx

    val isVisibleSparkle =
        moveGridItemResult != null &&
            (
                pagerScreenState.drag == Drag.Dragging ||
                    (
                        isVisibleOverlay &&
                            pagerScreenState.isDragging &&
                            !experimentalSettings.lockMovement
                        )
                ) &&
            !isVisibleFolderGridItems &&
            !isVisibleFolderEblanApplicationInfos

    val isVisibleSparkleGrid = isVisibleSparkle && !isDragOnDock

    val isVisibleSparkleDockGrid = isVisibleSparkle && isDragOnDock

    LaunchedEffect(
        key1 = pinGridItem,
        key2 = pagerScreenState,
    ) {
        pagerScreenState.handlePinGridItemEffect(
            // Item
            pinGridItem = pinGridItem,
            // Callbacks
            onUpdateGridItemSource = contract::updateGridItemSource,
            onUpdateIsVisibleOverlay = contract::updateIsVisibleOverlay,
            onUpdateMoveGridItemResult = contract::updateMoveGridItemResult,
        )
    }

    SyncDataEffect(
        // Sync Control
        syncData = experimentalSettings.syncData,
        // Sync Callbacks
        onStartSyncData = contract::startSyncData,
        onStopSyncData = contract::stopSyncData,
        // Package Event Callbacks
        onPackageRemoved = contract::packageRemove,
        onPackageAdded = contract::packageAdded,
        onPackageChanged = contract::packageChanged,
        onShortcutsChanged = contract::shortcutsChanged,
    )

    LaunchedEffect(
        key1 = pagerScreenState.dragIntOffset,
        key2 = pagerScreenState,
    ) {
        handleDragGridItem(
            // Grid Dimensions
            columns = homeSettings.columns,
            rows = homeSettings.rows,
            gridIndex = gridHorizontalPagerState.currentPage,
            infiniteScroll = homeSettings.infiniteScroll,
            pageCount = homeSettings.pageCount,
            // Dock Dimensions
            dockColumns = homeSettings.dockColumns,
            dockRows = homeSettings.dockRows,
            dockIndex = dockGridHorizontalPagerState.currentPage,
            dockInfiniteScroll = homeSettings.dockInfiniteScroll,
            dockPageCount = homeSettings.dockPageCount,
            dockHeight = visibleDockHeight,
            // Screen and Layout
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            density = density,
            layoutDirection = layoutDirection,
            paddingValues = paddingValues,
            // Drag State
            drag = pagerScreenState.drag,
            dragIntOffset = pagerScreenState.dragIntOffset,
            isDragging = pagerScreenState.isDragging,
            isVisibleOverlay = currentIsVisibleOverlay.value,
            lockMovement = experimentalSettings.lockMovement,
            // Grid and Scroll State
            gridItemSource = currentGridItemSource.value,
            moveGridItemResult = currentMoveGridItemResult.value,
            isGridScrollInProgress = gridHorizontalPagerState.isScrollInProgress,
            isDockScrollInProgress = dockGridHorizontalPagerState.isScrollInProgress,
            // Callbacks
            onMoveGridItem = contract::moveGridItem,
            onCancelPendingMoveGridItem = contract::cancelPendingMoveGridItem,
            onUpdateAssociate = pagerScreenState::updateAssociate,
            onUpdateSharedElementKey = pagerScreenState::updateSharedElementKey,
        )
    }

    LaunchedEffect(
        key1 = pagerScreenState.drag,
        key2 = pagerScreenState,
    ) {
        handleDropGridItem(
            // Context and Environment
            context = context,
            density = density,
            layoutDirection = layoutDirection,
            // Android System Wrappers
            androidAppWidgetHostWrapper = androidAppWidgetHostWrapper,
            androidAppWidgetManagerWrapper = androidAppWidgetManagerWrapper,
            androidLauncherAppsWrapper = androidLauncherAppsWrapper,
            androidUserManagerWrapper = androidUserManagerWrapper,
            // Grid and Layout Configuration
            columns = homeSettings.columns,
            rows = homeSettings.rows,
            dockColumns = homeSettings.dockColumns,
            dockRows = homeSettings.dockRows,
            screenHeight = screenHeight,
            screenWidth = screenWidth,
            dockHeight = visibleDockHeight,
            paddingValues = paddingValues,
            // Drag and Drop State
            drag = pagerScreenState.drag,
            gridItemSource = currentGridItemSource.value,
            isDragging = pagerScreenState.isDragging,
            moveGridItemResult = currentMoveGridItemResult.value,
            lockMovement = experimentalSettings.lockMovement,
            isVisibleOverlay = currentIsVisibleOverlay.value,
            // Grid and Layout Callbacks
            onResetGridAfterDeleteGridItem = contract::resetGridAfterDeleteGridItem,
            onResetGrid = contract::resetGrid,
            onUpdateGridItemsAfterMove = contract::updateGridItemsAfterMove,
            onUpdateGridItemsAfterMoveNewFolder = contract::updateGridItemsAfterMoveNewFolder,
            // Intent and Configuration Callbacks
            onLaunchShortcutConfigIntent = shortcutConfigLauncher::launch,
            onLaunchShortcutConfigIntentSenderRequest = shortcutConfigIntentSenderLauncher::launch,
            onLaunchWidgetIntent = appWidgetLauncher::launch,
            onUpdateAppWidgetId = pagerScreenState::updateLastAppWidgetId,
            // State and UI Update Callbacks
            onUpdateIsDragging = pagerScreenState::updateIsDragging,
            onUpdateWidgetGridItem = pagerScreenState::updateWidgetGridItem,
            onUpdateIsVisibleOverlay = contract::updateIsVisibleOverlay,
        )
    }

    LaunchedEffect(key1 = pagerScreenState.widgetGridItem) {
        handleBoundWidgetEffect(
            // Context and Host
            activity = activity,
            androidAppWidgetHostWrapper = androidAppWidgetHostWrapper,
            // Grid State
            gridItemSource = currentGridItemSource.value,
            moveGridItemResult = currentMoveGridItemResult.value,
            widgetGridItem = pagerScreenState.widgetGridItem,
            // Callbacks
            onDeleteGridItem = contract::resetGridAfterDeleteGridItem,
            onUpdateGridItemsAfterMove = contract::updateGridItemsAfterMove,
            onResetGrid = contract::resetGrid,
        )
    }

    LaunchedEffect(key1 = gridHorizontalPagerState) {
        pagerScreenState.handleWallpaperScrollEffect(
            horizontalPagerState = gridHorizontalPagerState,
            windowToken = view.windowToken,
        )
    }

    LaunchedEffect(
        key1 = pagerScreenState.dragIntOffset,
        key2 = pagerScreenState,
    ) {
        handleAnimateScrollToPage(
            // State
            isDragging = pagerScreenState.isDragging,
            dragIntOffset = pagerScreenState.dragIntOffset,
            associate = pagerScreenState.associate,
            // Layout
            screenWidth = screenWidth,
            density = density,
            layoutDirection = layoutDirection,
            paddingValues = paddingValues,
            gridItemSource = currentGridItemSource.value,
            // Callbacks
            onUpdateDockPageDirection = pagerScreenState::updateDockPageDirection,
            onUpdateGridPageDirection = pagerScreenState::updateGridPageDirection,
        )
    }

    LaunchedEffect(
        key1 = configureResultCode,
        key2 = pagerScreenState.widgetGridItem,
    ) {
        handleConfigureLauncherResultEffect(
            // Result Data
            resultCode = configureResultCode,
            moveGridItemResult = currentMoveGridItemResult.value,
            widgetGridItem = pagerScreenState.widgetGridItem,
            // Callbacks
            onDeleteGridItem = contract::deleteGridItem,
            onUpdateGridItemsAfterMove = contract::updateGridItemsAfterMove,
            onResetConfigureResultCode = onResetConfigureResultCode,
            onResetGrid = contract::resetGrid,
        )
    }

    LaunchedEffect(key1 = pagerScreenState.gridPageDirection) {
        handlePageDirection(
            // State
            folderGridItemEntries = currentFolderGridItemEntries,
            // Pagination
            pageDirection = pagerScreenState.gridPageDirection,
            currentPage = gridHorizontalPagerState.currentPage,
            // Callbacks
            onAnimateScrollToPage = gridHorizontalPagerState::animateScrollToPage,
        )
    }

    LaunchedEffect(key1 = pagerScreenState.dockPageDirection) {
        handlePageDirection(
            // State
            folderGridItemEntries = currentFolderGridItemEntries,
            // Pagination
            pageDirection = pagerScreenState.dockPageDirection,
            currentPage = dockGridHorizontalPagerState.currentPage,
            // Callbacks
            onAnimateScrollToPage = dockGridHorizontalPagerState::animateScrollToPage,
        )
    }

    LaunchedEffect(key1 = pagerScreenState.hasDoubleTap) {
        pagerScreenState.handleHasDoubleTap()
    }

    LaunchedEffect(key1 = homeSettings.infiniteScroll) {
        gridHorizontalPagerState.scrollToPage(
            if (homeSettings.infiniteScroll) {
                (Int.MAX_VALUE / 2) + homeSettings.initialPage
            } else {
                homeSettings.initialPage
            },
        )
    }

    LaunchedEffect(key1 = homeSettings.dockInfiniteScroll) {
        dockGridHorizontalPagerState.scrollToPage(
            if (homeSettings.dockInfiniteScroll) {
                (Int.MAX_VALUE / 2) + homeSettings.dockInitialPage
            } else {
                homeSettings.dockInitialPage
            },
        )
    }

    DisposableEffect(
        key1 = activity,
        key2 = pagerScreenState,
    ) {
        val listener = Consumer<Intent> { currentIntent ->
            pagerScreenState.handleEblanActionIntent(intent = currentIntent)
        }

        activity.addOnNewIntentListener(listener)

        onDispose {
            activity.removeOnNewIntentListener(listener)
        }
    }

    val requestedOrientation = when (homeSettings.screenOrientation) {
        ScreenOrientation.System -> {
            if (shouldLockScreenOrientation) {
                ActivityInfo.SCREEN_ORIENTATION_LOCKED
            } else {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }

        ScreenOrientation.Portrait -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        ScreenOrientation.Landscape -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    DisposableEffect(
        key1 = activity,
        key2 = requestedOrientation,
    ) {
        activity.requestedOrientation = requestedOrientation

        onDispose {
            if (!activity.isChangingConfigurations) {
                activity.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    BackHandler(enabled = pagerScreenState.isAvailableSystemNavigation) {
        pagerScreenState.handleSystemNavigation(
            dockGridHorizontalPagerState = dockGridHorizontalPagerState,
            gridHorizontalPagerState = gridHorizontalPagerState,
            windowToken = view.windowToken,
        )
    }

    HomeHandler(enabled = pagerScreenState.isAvailableSystemNavigation) {
        pagerScreenState.handleSystemNavigation(
            dockGridHorizontalPagerState = dockGridHorizontalPagerState,
            gridHorizontalPagerState = gridHorizontalPagerState,
            windowToken = view.windowToken,
        )
    }

    SharedTransitionLayout(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { currentLayoutCoordinates ->
                sharedTransitionLayoutCoordinates = currentLayoutCoordinates
            }
            .pointerInput(key1 = pagerScreenState) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { pointerOffset ->
                        val layoutCoordinates = sharedTransitionLayoutCoordinates
                            ?.takeIf(LayoutCoordinates::isAttached)
                            ?: return@detectDragGesturesAfterLongPress

                        val rootPointerOffset = layoutCoordinates.localToRoot(pointerOffset)

                        pagerScreenState.dragStart(rootPointerOffset)
                    },
                    onDragEnd = {
                        pagerScreenState.updateDrag(Drag.End)
                    },
                    onDragCancel = {
                        pagerScreenState.updateDrag(Drag.Cancel)
                    },
                    onDrag = { _, dragAmount ->
                        pagerScreenState.drag(dragAmount = dragAmount)
                    },
                )
            }
            .dragAndDropTarget(
                shouldStartDragAndDrop = { currentDragAndDropEvent ->
                    currentDragAndDropEvent.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                },
                target = pagerScreenState.target,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .alpha(pagerScreenState.pagerScreenAlpha)
                .pointerInput(
                    key1 = isVisibleOverlay,
                    key2 = pagerScreenState,
                ) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            pagerScreenState.verticalDragStart()
                        },
                        onVerticalDrag = { _, dragAmount ->
                            pagerScreenState.verticalDrag(dragAmount = dragAmount)
                        },
                        onDragEnd = pagerScreenState::verticalDragEnd,
                        onDragCancel = pagerScreenState::verticalDragCancel,
                    )
                }
                .pointerInput(key1 = pagerScreenState) {
                    detectTapGestures(
                        onDoubleTap = {
                            pagerScreenState.updateHasDoubleTap(value = true)
                        },
                        onLongPress = { currentOffset ->
                            pagerScreenState.showSettingsMenu(offset = currentOffset)
                        },
                    )
                },
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (isVisibleSparkleGrid) {
                    SparkleGridLayout(
                        // Grid Structure
                        columns = homeSettings.columns,
                        rows = homeSettings.rows,
                        // Grid Item Text Colors
                        gridItemTextColor = homeSettings.gridItemSettings.textColor,
                        gridItemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
                        // Drag and Style
                        dragIntOffset = pagerScreenState.dragIntOffset,
                        textColor = textColor,
                        // Modifier
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = paddingValues.calculateStartPadding(layoutDirection),
                                end = paddingValues.calculateEndPadding(layoutDirection),
                            ),
                    )
                }

                HorizontalPager(
                    state = gridHorizontalPagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = !isVisibleOverlay,
                ) { index ->
                    val page = calculatePage(
                        index = index,
                        infiniteScroll = homeSettings.infiniteScroll,
                        pageCount = homeSettings.pageCount,
                    )

                    GridLayout(
                        // Grid Properties
                        gridItems = gridItemsByPage[page],
                        columns = homeSettings.columns,
                        rows = homeSettings.rows,
                        // Layout Options
                        animate = (isVisibleOverlay || isResizing) && experimentalSettings.gridItemAnimation,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = paddingValues.calculateStartPadding(layoutDirection),
                                end = paddingValues.calculateEndPadding(layoutDirection),
                            )
                            .pagerTransitionEffect(
                                pagerState = gridHorizontalPagerState,
                                page = index,
                                effect = homeSettings.pagerTransitionEffect,
                            ),
                        // Content
                        content = { currentGridItem ->
                            InteractiveGridItem(
                                // Item Data
                                gridItem = currentGridItem,
                                gridItemSettings = homeSettings.gridItemSettings,
                                sharedElementKey = SharedElementKey(
                                    id = currentGridItem.id,
                                    parent = SharedElementKey.Parent.Grid,
                                ),
                                statusBarNotifications = statusBarNotifications,
                                moveGridItemResult = moveGridItemResult,
                                // Layout and Dimensions
                                cellWidth = safeDrawingWidth / homeSettings.columns,
                                cellHeight = gridHeight / homeSettings.rows,
                                leftPadding = leftPadding,
                                topOffset = topPadding,
                                // Styling and Appearance
                                textColor = textColor,
                                folderCornerRadius = folderSettings.folderCornerRadius,
                                folderBackgroundColor = folderSettings.folderBackgroundColor,
                                customFolderBackgroundColor = folderSettings.customFolderBackgroundColor,
                                folderBorderColor = folderSettings.folderBorderColor,
                                customFolderBorderColor = folderSettings.customFolderBorderColor,
                                folderBorderWidth = folderSettings.borderWidth,
                                systemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
                                iconPackFilePaths = iconPackFilePaths,
                                // Animation and Transition
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animations = experimentalSettings.gridItemAnimation,
                                // State and Flags
                                drag = pagerScreenState.drag,
                                hasShortcutHostPermission = hasShortcutHostPermission,
                                isScrollInProgress = gridHorizontalPagerState.isScrollInProgress,
                                isVisibleOverlay = isVisibleOverlay,
                                isVisibleFolderGridItems = isVisibleFolderGridItems,
                                lockMovement = experimentalSettings.lockMovement,
                                isDragging = pagerScreenState.isDragging,
                                showGridItemMenu = pagerScreenState.showGridItemMenu,
                                folderPopups = folderPopups,
                                folderGridItemEntries = folderGridItemEntries,
                                // Callbacks
                                onOpenAppDrawer = pagerScreenState::openApplicationScreen,
                                onShowFolderWhenDragging = { folderEntry, gridItem ->
                                    pagerScreenState.showFolderWhenDragging(
                                        // Folder and item being dragged
                                        folderEntry = folderEntry,
                                        movingGridItem = gridItem,
                                        // Callbacks
                                        onShowFolderWhenDragging = contract::showFolderWhenDragging,
                                    )
                                },
                                onResetGrid = contract::resetGrid,
                                onLongPressGridItem = { gridItem, imageBitmap, intOffset, intSize, sharedElementKey ->
                                    pagerScreenState.longPressGridItem(
                                        // Grid item details
                                        gridItem = gridItem,
                                        imageBitmap = imageBitmap,
                                        newSharedElementKey = sharedElementKey,
                                        // Grid item position and size
                                        intOffset = intOffset,
                                        intSize = intSize,
                                        // Callbacks
                                        onUpdateGridItemSource = contract::updateGridItemSource,
                                        onUpdateIsVisibleOverlay = contract::updateIsVisibleOverlay,
                                        onUpdateMoveGridItemResult = contract::updateMoveGridItemResult,
                                    )
                                },
                                onTapFolderGridItem = { folderEntry ->
                                    pagerScreenState.tapFolderGridItem(
                                        folderEntry = folderEntry,
                                        onUpsertFolderGridItemEntry = contract::upsertFolderGridItemEntry,
                                    )
                                },
                                onDragGridItem = pagerScreenState::dragGridItem,
                            )
                        },
                    )
                }
            }

            GridPagerIndicator(
                // Pager State
                gridHorizontalPagerState = gridHorizontalPagerState,
                pageCount = homeSettings.pageCount,
                // Indicator Appearance
                color = getTextColor(
                    // Text Colors
                    textColor = textColor,
                    customTextColor = homeSettings.gridItemSettings.customTextColor,
                ),
                pageIndicatorType = homeSettings.pageIndicatorType,
                // Scroll Behavior
                infiniteScroll = homeSettings.infiniteScroll,
                swipeUp = gestureSettings.swipeUp,
                swipeDown = gestureSettings.swipeDown,
                // Layout
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PAGE_INDICATOR_HEIGHT),
            )

            if (homeSettings.enableDock) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dockHeight + paddingValues.calculateBottomPadding()),
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(
                                start = homeSettings.dockPadding.dp,
                                top = homeSettings.dockPadding.dp,
                                end = homeSettings.dockPadding.dp,
                                bottom = if (homeSettings.addDockBottomPadding) {
                                    homeSettings.dockPadding.dp + paddingValues.calculateBottomPadding()
                                } else {
                                    homeSettings.dockPadding.dp
                                },
                            )
                            .background(
                                color = getBackgroundColor(
                                    backgroundColor = homeSettings.dockBackgroundColor,
                                    customBackgroundColor = homeSettings.dockCustomBackgroundColor,
                                ),
                                shape = RoundedCornerShape(
                                    topStart = homeSettings.dockTopStartCornerRadius.dp,
                                    topEnd = homeSettings.dockTopEndCornerRadius.dp,
                                    bottomStart = homeSettings.dockBottomStartCornerRadius.dp,
                                    bottomEnd = homeSettings.dockBottomEndCornerRadius.dp,
                                ),
                            ),
                    )

                    if (isVisibleSparkleDockGrid) {
                        SparkleGridLayout(
                            // Grid Structure
                            columns = homeSettings.dockColumns,
                            rows = homeSettings.dockRows,
                            // Grid Item Text Colors
                            gridItemTextColor = homeSettings.dockGridItemSettings.textColor,
                            gridItemCustomTextColor = homeSettings.dockGridItemSettings.customTextColor,
                            // Drag and Style
                            dragIntOffset = pagerScreenState.dragIntOffset,
                            textColor = dockTextColor,
                            // Modifier
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = paddingValues.calculateStartPadding(layoutDirection),
                                    end = paddingValues.calculateEndPadding(layoutDirection),
                                    bottom = paddingValues.calculateBottomPadding(),
                                ),
                        )
                    }

                    HorizontalPager(
                        state = dockGridHorizontalPagerState,
                        modifier = Modifier
                            .matchParentSize()
                            .padding(
                                bottom = paddingValues.calculateBottomPadding(),
                            ),
                        userScrollEnabled = !isVisibleOverlay,
                    ) { index ->
                        val page = calculatePage(
                            index = index,
                            infiniteScroll = homeSettings.dockInfiniteScroll,
                            pageCount = homeSettings.dockPageCount,
                        )

                        GridLayout(
                            // Grid Properties
                            gridItems = dockGridItemsByPage[page],
                            columns = homeSettings.dockColumns,
                            rows = homeSettings.dockRows,
                            // Layout Options
                            animate = (isVisibleOverlay || isResizing) && experimentalSettings.gridItemAnimation,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = paddingValues.calculateStartPadding(layoutDirection),
                                    end = paddingValues.calculateEndPadding(layoutDirection),
                                )
                                .pagerTransitionEffect(
                                    pagerState = dockGridHorizontalPagerState,
                                    page = index,
                                    effect = homeSettings.pagerTransitionEffect,
                                ),
                            // Content
                            content = { currentDockGridItem ->
                                InteractiveGridItem(
                                    // Item Data
                                    gridItem = currentDockGridItem,
                                    gridItemSettings = homeSettings.dockGridItemSettings,
                                    sharedElementKey = SharedElementKey(
                                        id = currentDockGridItem.id,
                                        parent = SharedElementKey.Parent.Dock,
                                    ),
                                    statusBarNotifications = statusBarNotifications,
                                    moveGridItemResult = moveGridItemResult,
                                    // Layout and Dimensions
                                    cellWidth = safeDrawingWidth / homeSettings.dockColumns,
                                    cellHeight = dockHeightPx / homeSettings.dockRows,
                                    leftPadding = leftPadding,
                                    topOffset = dockTopLeft,
                                    // Styling and Appearance
                                    textColor = dockTextColor,
                                    folderCornerRadius = folderSettings.folderCornerRadius,
                                    folderBackgroundColor = folderSettings.folderBackgroundColor,
                                    customFolderBackgroundColor = folderSettings.customFolderBackgroundColor,
                                    folderBorderColor = folderSettings.folderBorderColor,
                                    customFolderBorderColor = folderSettings.customFolderBorderColor,
                                    folderBorderWidth = folderSettings.borderWidth,
                                    systemCustomTextColor = homeSettings.dockGridItemSettings.customTextColor,
                                    iconPackFilePaths = iconPackFilePaths,
                                    // Animation and Transition
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animations = experimentalSettings.gridItemAnimation,
                                    // State and Flags
                                    drag = pagerScreenState.drag,
                                    hasShortcutHostPermission = hasShortcutHostPermission,
                                    isScrollInProgress = dockGridHorizontalPagerState.isScrollInProgress,
                                    isVisibleOverlay = isVisibleOverlay,
                                    isVisibleFolderGridItems = isVisibleFolderGridItems,
                                    lockMovement = experimentalSettings.lockMovement,
                                    isDragging = pagerScreenState.isDragging,
                                    showGridItemMenu = pagerScreenState.showGridItemMenu,
                                    folderPopups = folderPopups,
                                    folderGridItemEntries = folderGridItemEntries,
                                    // Callbacks
                                    onOpenAppDrawer = pagerScreenState::openApplicationScreen,
                                    onShowFolderWhenDragging = { folderEntry, gridItem ->
                                        pagerScreenState.showFolderWhenDragging(
                                            // Folder and item being dragged
                                            folderEntry = folderEntry,
                                            movingGridItem = gridItem,
                                            // Callbacks
                                            onShowFolderWhenDragging = contract::showFolderWhenDragging,
                                        )
                                    },
                                    onResetGrid = contract::resetGrid,
                                    onLongPressGridItem = { gridItem, imageBitmap, intOffset, intSize, sharedElementKey ->
                                        pagerScreenState.longPressGridItem(
                                            // Grid item details
                                            gridItem = gridItem,
                                            imageBitmap = imageBitmap,
                                            newSharedElementKey = sharedElementKey,
                                            // Grid item position and size
                                            intOffset = intOffset,
                                            intSize = intSize,
                                            // Callbacks
                                            onUpdateGridItemSource = contract::updateGridItemSource,
                                            onUpdateIsVisibleOverlay = contract::updateIsVisibleOverlay,
                                            onUpdateMoveGridItemResult = contract::updateMoveGridItemResult,
                                        )
                                    },
                                    onTapFolderGridItem = { folderEntry ->
                                        pagerScreenState.tapFolderGridItem(
                                            folderEntry = folderEntry,
                                            onUpsertFolderGridItemEntry = contract::upsertFolderGridItemEntry,
                                        )
                                    },
                                    onDragGridItem = pagerScreenState::dragGridItem,
                                )
                            },
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
            }
        }

        if (isVisibleGridItemMenu) {
            GridItemPopup(
                // Grid Item Details
                gridItem = pagerScreenState.selectedGridItem,
                gridItemSettings = when (pagerScreenState.selectedGridItem?.associate) {
                    Associate.Dock -> homeSettings.dockGridItemSettings
                    else -> homeSettings.gridItemSettings
                },
                isCloseGridItemPopup = pagerScreenState.isCloseGridItemMenu,
                popupIntOffset = pagerScreenState.menuIntOffset,
                popupIntSize = pagerScreenState.menuIntSize,
                isVisibleOverlay = isVisibleOverlay,
                paddingValues = paddingValues,
                // Shortcut and Widget Data
                eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                hasShortcutHostPermission = hasShortcutHostPermission,
                // Callbacks
                onDeleteGridItem = contract::deleteGridItem,
                onDismissRequest = pagerScreenState::dismissGridItemMenu,
                onEdit = { id ->
                    contract.resetGrid()

                    onEditGridItem(id)
                },
                onResize = contract::updateResizeGridItem,
                onWidgets = pagerScreenState::openAppWidgetScreen,
                onUpdateIsResizing = pagerScreenState::updateIsResizing,
                onDragShortcutInfo = { gridItem, imageBitmap, intOffset, intSize, sharedElementKey ->
                    pagerScreenState.dragShortcutInfoFromGridItemMenu(
                        // Drag source information
                        gridItem = gridItem,
                        imageBitmap = imageBitmap,
                        intOffset = intOffset,
                        intSize = intSize,
                        newSharedElementKey = sharedElementKey,
                        // Callbacks
                        onUpdateGridItemSource = contract::updateGridItemSource,
                        onUpdateIsVisibleOverlay = contract::updateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = contract::updateMoveGridItemResult,
                    )
                },
            )
        }

        if (isVisibleSettingsMenu) {
            SettingsPopup(
                // Content
                gridItems = gridItems,
                // State
                hasSystemFeatureAppWidgets = hasSystemFeatureAppWidgets,
                isEnableDock = homeSettings.enableDock,
                popupSettingsIntOffset = pagerScreenState.settingsMenuIntOffset,
                // Actions
                onDismissRequest = pagerScreenState::dismissSettingsMenu,
                onEditPage = contract::showPageCache,
                onSettings = onSettings,
                onShortcutConfigActivities = pagerScreenState::openShortcutConfigScreen,
                onWidgets = pagerScreenState::openWidgetScreen,
            )
        }

        if (isVisibleFolderGridItems) {
            folderGridItemEntries.forEach { currentFolderEntry ->
                val isDockFolder =
                    folderPopups[currentFolderEntry.id]?.folderGridItem?.associate == Associate.Dock

                val folderGridItemSettings = if (isDockFolder) {
                    homeSettings.dockGridItemSettings
                } else {
                    homeSettings.gridItemSettings
                }

                val folderSystemTextColor = if (isDockFolder) {
                    dockTextColor
                } else {
                    textColor
                }

                FolderScreen(
                    // Contract
                    contract = contract,
                    // Transition and Layout
                    sharedTransitionScope = this@SharedTransitionLayout,
                    paddingValues = paddingValues,
                    safeDrawingHeight = safeDrawingHeight,
                    safeDrawingWidth = safeDrawingWidth,
                    screenHeight = screenHeight,
                    screenWidth = screenWidth,
                    // Folder Content and Settings
                    folderEntry = currentFolderEntry,
                    gridItemSettings = folderGridItemSettings,
                    folderCellWidth = folderSettings.folderCellWidth,
                    folderCellHeight = folderSettings.folderCellHeight,
                    folderPopups = folderPopups,
                    iconPackFilePaths = iconPackFilePaths,
                    statusBarNotifications = statusBarNotifications,
                    // Appearance and Style
                    animations = experimentalSettings.gridItemAnimation,
                    systemTextColor = folderSystemTextColor,
                    systemCustomTextColor = folderGridItemSettings.customTextColor,
                    folderBackgroundColor = folderSettings.folderBackgroundColor,
                    folderCornerRadius = folderSettings.folderCornerRadius,
                    customFolderBackgroundColor = folderSettings.customFolderBackgroundColor,
                    folderBorderColor = folderSettings.folderBorderColor,
                    customFolderBorderColor = folderSettings.customFolderBorderColor,
                    folderBorderWidth = folderSettings.borderWidth,
                    // Drag and Drop State
                    drag = pagerScreenState.drag,
                    isDragging = pagerScreenState.isDragging,
                    dragIntOffset = pagerScreenState.dragIntOffset,
                    lockMovement = experimentalSettings.lockMovement,
                    // Overlay and Permission State
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    showFolderGridItemMenu = pagerScreenState.showFolderGridItemMenu,
                    // Folder Actions
                    onCloseFolder = { folderEntry, isFirstFolderGridItem ->
                        pagerScreenState.closeFolderGridItem(
                            // Folder Item
                            folderEntry = folderEntry,
                            isFirstFolderGridItem = isFirstFolderGridItem,
                            // Callbacks
                            onDeleteFolderGridItemEntry = contract::deleteFolderGridItemEntry,
                        )
                    },
                    onOpenAppDrawer = pagerScreenState::openApplicationScreen,
                    onUpdateIsDragging = pagerScreenState::updateIsDragging,
                    onDismissFolderGridItemMenu = pagerScreenState::dismissFolderGridItemMenu,
                    onDragFolderGridItem = pagerScreenState::dragFolderGridItem,
                    onLongPressFolderGridItem = { gridItem, imageBitmap, intOffset, intSize, sharedElementKey ->
                        pagerScreenState.longPressFolderGridItem(
                            // Item details
                            gridItem = gridItem,
                            imageBitmap = imageBitmap,
                            // Position and size
                            intOffset = intOffset,
                            intSize = intSize,
                            // Shared element
                            newSharedElementKey = sharedElementKey,
                            // Callbacks
                            onUpdateIsVisibleOverlay = contract::updateIsVisibleOverlay,
                            onUpdateMoveGridItemResult = contract::updateMoveGridItemResult,
                        )
                    },
                    onMoveFolderGridItemOutsideFolder = { gridItem, sharedElementKey ->
                        pagerScreenState.moveFolderGridItemOutsideFolder(
                            // Item to move
                            gridItem = gridItem,
                            // New location
                            newSharedElementKey = sharedElementKey,
                            // Callbacks
                            onMoveFolderGridItemOutsideFolder = contract::moveFolderGridItemOutsideFolder,
                        )
                    },
                )
            }
        }

        if (isVisibleFolderGridItemMenu) {
            FolderGridItemPopup(
                // Grid Item
                gridItemSettings = homeSettings.gridItemSettings,
                folderGridItem = pagerScreenState.selectedGridItem,
                // Permission and Visibility
                hasShortcutHostPermission = hasShortcutHostPermission,
                isVisibleOverlay = isVisibleOverlay,
                isCloseFolderGridItemPopup = pagerScreenState.isCloseFolderGridItemMenu,
                // Popup Layout
                popupIntOffset = pagerScreenState.menuIntOffset,
                popupIntSize = pagerScreenState.menuIntSize,
                paddingValues = paddingValues,
                // Widget and Shortcut Data
                eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                // Actions
                onDeleteGridItem = contract::deleteGridItem,
                onDismissRequest = pagerScreenState::dismissFolderGridItemMenu,
                onDismissScreen = contract::closeFolderGridItemEntries,
                onEdit = { id ->
                    contract.resetGrid()

                    onEditGridItem(id)
                },
                onWidgets = pagerScreenState::openAppWidgetScreen,
                onDragShortcutInfo = { gridItem, imageBitmap, intOffset, intSize, sharedElementKey ->
                    pagerScreenState.dragShortcutInfoFromFolderGridItemMenu(
                        // Grid item details
                        gridItem = gridItem,
                        imageBitmap = imageBitmap,
                        intOffset = intOffset,
                        intSize = intSize,
                        // Shared element key
                        newSharedElementKey = sharedElementKey,
                        // Callbacks
                        onUpdateGridItemSource = contract::updateGridItemSource,
                        onUpdateIsVisibleOverlay = contract::updateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = contract::updateMoveGridItemResult,
                        onResetFolderGridItemEntries = contract::resetFolderGridItemEntries,
                    )
                },
            )
        }

        if (pagerScreenState.showApplicationScreen) {
            ApplicationScreen(
                // Contract
                contract = contract,
                // Layout and Transition
                sharedTransitionScope = this@SharedTransitionLayout,
                screenHeight = screenHeight,
                paddingValues = paddingValues,
                // Visual and Animation
                animations = experimentalSettings.gridItemAnimation,
                alpha = pagerScreenState.applicationScreenAlpha,
                systemTextColor = appDrawerTextColor,
                systemCustomTextColor = appDrawerSettings.gridItemSettings.customTextColor,
                // Application Data
                appDrawerSettings = appDrawerSettings,
                // Folder Settings
                folderCornerRadius = folderSettings.folderCornerRadius,
                folderBackgroundColor = folderSettings.folderBackgroundColor,
                customFolderBackgroundColor = folderSettings.customFolderBackgroundColor,
                folderBorderColor = folderSettings.folderBorderColor,
                customFolderBorderColor = folderSettings.customFolderBorderColor,
                folderBorderWidth = folderSettings.borderWidth,
                isVisibleFolderEblanApplicationInfos = isVisibleFolderEblanApplicationInfos,
                // Gesture and State
                swipeY = pagerScreenState.applicationScreenSwipeY.value,
                drag = pagerScreenState.drag,
                // Actions and Callbacks
                onDismiss = pagerScreenState::dismissApplicationScreen,
                onDragEnd = pagerScreenState::handleOnDragEndApplicationScreen,
                onVerticalDrag = pagerScreenState::verticalDragApplicationScreen,
                onDragFolderEblanApplicationInfo = { folderId, movingGridItem ->
                    pagerScreenState.dragFolderEblanApplicationInfo(
                        // Data
                        folderId = folderId,
                        gridItem = movingGridItem,
                        // Callbacks
                        onDragFolderEblanApplicationInfoToGrid = contract::dragFolderEblanApplicationInfoToGrid,
                        onUpdateGridItemSource = contract::updateGridItemSource,
                        onUpdateMoveGridItemResult = contract::updateMoveGridItemResult,
                    )
                },
                onDragApplicationInfo = { currentApplicationGridItem ->
                    pagerScreenState.dragEblanApplicationInfo(
                        gridItem = currentApplicationGridItem,
                        onUpdateGridItemSource = contract::updateGridItemSource,
                        onUpdateMoveGridItemResult = contract::updateMoveGridItemResult,
                    )
                },
                onLongPressApplicationInfo = { eblanApplicationInfo, imageBitmap, intOffset, intSize, sharedElementKey ->
                    pagerScreenState.longPressEblanApplicationInfo(
                        // Application Info
                        eblanApplicationInfo = eblanApplicationInfo,
                        // Layout Bounds
                        imageBitmap = imageBitmap,
                        intOffset = intOffset,
                        intSize = intSize,
                        newSharedElementKey = sharedElementKey,
                        // Callbacks
                        onUpdateIsVisibleOverlay = contract::updateIsVisibleOverlay,
                    )
                },
                onLongPressFolderApplicationInfo = { folderEblanApplicationInfo, imageBitmap, intOffset, intSize, sharedElementKey ->
                    pagerScreenState.longPressFolderEblanApplicationInfo(
                        // Target Data
                        folderEblanApplicationInfo = folderEblanApplicationInfo,
                        newSharedElementKey = sharedElementKey,
                        // Visual Data
                        imageBitmap = imageBitmap,
                        intOffset = intOffset,
                        intSize = intSize,
                        // Callbacks
                        onUpdateIsVisibleOverlay = contract::updateIsVisibleOverlay,
                    )
                },
                onLongPressPrivateSpaceApplicationInfoItem = { eblanApplicationInfo, intOffset, intSize ->
                    pagerScreenState.longPressPrivateSpaceEblanApplicationInfoItem(
                        eblanApplicationInfo = eblanApplicationInfo,
                        intOffset = intOffset,
                        intSize = intSize,
                    )
                },
                onTapFolderApplicationInfo = { currentFolderEntry ->
                    pagerScreenState.tapFolderEblanApplicationInfo(
                        folderEntry = currentFolderEntry,
                        onUpsertFolderEblanApplicationInfoEntry = contract::upsertFolderEblanApplicationInfoEntry,
                    )
                },
            )
        }

        if (pagerScreenState.showEblanApplicationInfoMenu &&
            pagerScreenState.selectedEblanApplicationInfo != null &&
            pagerScreenState.menuIntOffset != null &&
            pagerScreenState.menuIntSize != null
        ) {
            ApplicationInfoPopup(
                // Application Data
                eblanApplicationInfo = pagerScreenState.selectedEblanApplicationInfo,
                eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfosGroup,
                eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                // Settings and State
                gridItemSettings = appDrawerSettings.gridItemSettings,
                hasShortcutHostPermission = hasShortcutHostPermission,
                isVisibleOverlay = isVisibleOverlay,
                isCloseEblanApplicationInfoMenu = pagerScreenState.isCloseEblanApplicationInfoMenu,
                // Layout and Positioning
                popupIntOffset = pagerScreenState.menuIntOffset,
                popupIntSize = pagerScreenState.menuIntSize,
                paddingValues = paddingValues,
                // Callbacks
                onDismissRequest = pagerScreenState::dismissEblanApplicationInfoMenu,
                onDismissScreen = pagerScreenState::dismissApplicationScreen,
                onEditApplicationInfo = onEditApplicationInfo,
                onWidgets = pagerScreenState::openAppWidgetScreen,
                onDragShortcutInfo = { gridItem, imageBitmap, intOffset, intSize, sharedElementKey ->
                    pagerScreenState.dragShortcutInfoFromApplicationMenu(
                        // Drag information
                        gridItem = gridItem,
                        imageBitmap = imageBitmap,
                        intOffset = intOffset,
                        intSize = intSize,
                        newSharedElementKey = sharedElementKey,
                        // Callbacks
                        onUpdateGridItemSource = contract::updateGridItemSource,
                        onUpdateIsVisibleOverlay = contract::updateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = contract::updateMoveGridItemResult,
                    )
                },
            )
        }

        if (pagerScreenState.showPrivateEblanApplicationInfoMenu &&
            pagerScreenState.selectedEblanApplicationInfo != null &&
            pagerScreenState.menuIntOffset != null &&
            pagerScreenState.menuIntSize != null
        ) {
            PrivateApplicationInfoPopup(
                // Application Info
                eblanApplicationInfo = pagerScreenState.selectedEblanApplicationInfo,
                eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                hasShortcutHostPermission = hasShortcutHostPermission,
                // Popup Layout
                popupIntOffset = pagerScreenState.menuIntOffset,
                popupIntSize = pagerScreenState.menuIntSize,
                paddingValues = paddingValues,
                isClosePrivateEblanApplicationInfoMenu = pagerScreenState.isClosePrivateEblanApplicationInfoMenu,
                // Callbacks
                onDismissRequest = pagerScreenState::dismissPrivateEblanApplicationInfoMenu,
                onDismissScreen = pagerScreenState::dismissApplicationScreen,
                onEditApplicationInfo = onEditApplicationInfo,
            )
        }

        if (pagerScreenState.showWidgetScreen) {
            WidgetScreen(
                // Contract
                contract = contract,
                // Grid Configuration
                columns = homeSettings.columns,
                rows = homeSettings.rows,
                gridItemSettings = homeSettings.gridItemSettings,
                // Dimensions
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                paddingValues = paddingValues,
                // State
                alpha = pagerScreenState.widgetScreenAlpha,
                swipeY = pagerScreenState.widgetScreenSwipeY.value,
                // Interaction
                drag = pagerScreenState.drag,
                // Actions
                onDismiss = pagerScreenState::dismissWidgetScreen,
                onVerticalDrag = pagerScreenState::verticalDragWidgetScreen,
                onDragEnd = pagerScreenState::handleOnDragEndWidgetScreen,
                onDragWidget = { gridItem, imageBitmap, intOffset, intSize, sharedElementKey ->
                    pagerScreenState.dragWidget(
                        // Drag details
                        gridItem = gridItem,
                        newSharedElementKey = sharedElementKey,
                        imageBitmap = imageBitmap,
                        intOffset = intOffset,
                        intSize = intSize,
                        // Callbacks
                        onUpdateGridItemSource = contract::updateGridItemSource,
                        onUpdateIsVisibleOverlay = contract::updateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = contract::updateMoveGridItemResult,
                    )
                },
            )
        }

        if (pagerScreenState.showShortcutConfigScreen) {
            ShortcutConfigScreen(
                // Contract
                contract = contract,
                // Configuration
                gridItemSettings = homeSettings.gridItemSettings,
                // Layout
                paddingValues = paddingValues,
                screenHeight = screenHeight,
                // State
                swipeY = pagerScreenState.shortcutConfigScreenSwipeY.value,
                alpha = pagerScreenState.shortcutConfigScreenAlpha,
                drag = pagerScreenState.drag,
                // Callbacks
                onDismiss = pagerScreenState::dismissShortcutConfigScreen,
                onVerticalDrag = pagerScreenState::verticalDragShortcutConfigScreen,
                onDragEnd = pagerScreenState::handleOnDragEndShortcutConfigScreen,
                onDragShortcutConfig = { gridItem, imageBitmap, intOffset, intSize, sharedElementKey ->
                    pagerScreenState.dragShortcutConfig(
                        // Drag item details
                        gridItem = gridItem,
                        imageBitmap = imageBitmap,
                        intOffset = intOffset,
                        intSize = intSize,
                        newSharedElementKey = sharedElementKey,
                        // Callbacks
                        onUpdateGridItemSource = contract::updateGridItemSource,
                        onUpdateIsVisibleOverlay = contract::updateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = contract::updateMoveGridItemResult,
                    )
                },
            )
        }

        if (isResizing) {
            ResizeScreen(
                // Contract
                contract = contract,
                // Layout
                paddingValues = paddingValues,
                // Dock Settings
                enableDock = homeSettings.enableDock,
                dockHeight = homeSettings.dockHeight,
                dockColumns = homeSettings.dockColumns,
                dockRows = homeSettings.dockRows,
                dockGridItemSettings = homeSettings.dockGridItemSettings,
                // Grid Settings
                columns = homeSettings.columns,
                rows = homeSettings.rows,
                gridItemSettings = homeSettings.gridItemSettings,
                // Appearance
                textColor = textColor,
                dockTextColor = dockTextColor,
                lockMovement = experimentalSettings.lockMovement,
                // Callbacks
                onUpdateIsResizing = pagerScreenState::updateIsResizing,
            )
        }

        if (isVisibleFolderEblanApplicationInfos) {
            folderEblanApplicationInfoEntries.forEach { currentFolderEntry ->
                FolderApplicationScreen(
                    // Contract
                    contract = contract,
                    // Core State and Layout
                    sharedTransitionScope = this,
                    folderEntry = currentFolderEntry,
                    paddingValues = paddingValues,
                    safeDrawingHeight = safeDrawingHeight,
                    safeDrawingWidth = safeDrawingWidth,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    // Folder Content and Appearance
                    animations = experimentalSettings.gridItemAnimation,
                    systemTextColor = appDrawerTextColor,
                    systemCustomTextColor = appDrawerSettings.gridItemSettings.customTextColor,
                    folderBackgroundColor = folderSettings.folderBackgroundColor,
                    customFolderBackgroundColor = folderSettings.customFolderBackgroundColor,
                    folderBorderColor = folderSettings.folderBorderColor,
                    customFolderBorderColor = folderSettings.customFolderBorderColor,
                    folderBorderWidth = folderSettings.borderWidth,
                    gridItemSettings = appDrawerSettings.gridItemSettings,
                    folderCornerRadius = folderSettings.folderCornerRadius,
                    folderCellWidth = folderSettings.folderCellWidth,
                    folderCellHeight = folderSettings.folderCellHeight,
                    // Drag and Drop State
                    drag = pagerScreenState.drag,
                    isDragging = pagerScreenState.isDragging,
                    lockMovement = experimentalSettings.lockMovement,
                    dragIntOffset = pagerScreenState.dragIntOffset,
                    showFolderEblanApplicationInfoGridItemMenu = pagerScreenState.showFolderEblanApplicationInfoGridItemMenu,
                    // Actions and Callbacks
                    onDismissFolderEblanApplicationGridItemPopup = pagerScreenState::dismissFolderEblanApplicationInfoGridItemMenu,
                    onUpdateIsDragging = pagerScreenState::updateIsDragging,
                    onMoveFolderEblanApplicationInfoGridItemOutsideFolder = { folderEblanApplicationInfoGridItem, movingGridItem ->
                        pagerScreenState.moveFolderEblanApplicationInfoGridItemOutsideFolder(
                            // Grid Items
                            folderEblanApplicationInfoGridItem = folderEblanApplicationInfoGridItem,
                            movingGridItem = movingGridItem,
                            // Actions
                            onMoveFolderEblanApplicationInfoGridItemOutsideFolder = contract::moveFolderEblanApplicationInfoGridItemOutsideFolder,
                        )
                    },
                    onLongPressFolderEblanApplicationInfoGridItem = { folderEblanApplicationInfoGridItem, imageBitmap, intOffset, intSize, sharedElementKey ->
                        pagerScreenState.longPressFolderEblanApplicationInfoGridItem(
                            // Item
                            folderEblanApplicationInfoGridItem = folderEblanApplicationInfoGridItem,
                            // Visuals
                            newSharedElementKey = sharedElementKey,
                            imageBitmap = imageBitmap,
                            // Layout
                            intOffset = intOffset,
                            intSize = intSize,
                            // Callbacks
                            onUpdateMoveFolderEblanApplicationInfoGridItemResult = contract::updateMoveFolderEblanApplicationInfoGridItemResult,
                            onUpdateIsVisibleOverlay = contract::updateIsVisibleOverlay,
                        )
                    },
                    onDragFolderEblanApplicationInfoGridItem = pagerScreenState::dragFolderEblanApplicationInfoGridItem,
                    onCloseFolder = { folderEntry, isFirstFolderGridItem ->
                        pagerScreenState.closeFolderEblanApplicationInfo(
                            // Folder Information
                            folderEntry = folderEntry,
                            isFirstFolderEblanApplicationInfo = isFirstFolderGridItem,
                            // Callbacks
                            onDeleteFolderEntry = contract::deleteFolderEblanApplicationInfoEntry,
                        )
                    },
                    onDismiss = {
                        contract.closeFolderEblanApplicationInfoEntries()

                        pagerScreenState.dismissApplicationScreen()
                    },
                    onTapFolderEblanApplicationInfoItem = { newFolderEntry ->
                        pagerScreenState.tapFolderEblanApplicationInfoGridItem(
                            folderEntry = newFolderEntry,
                            onUpsertFolderEntry = contract::upsertFolderEblanApplicationInfoEntry,
                        )
                    },
                )
            }
        }

        val selectedFolderEblanApplicationInfo =
            pagerScreenState.selectedFolderEblanApplicationInfo

        if (pagerScreenState.showFolderEblanApplicationInfoMenu && selectedFolderEblanApplicationInfo != null) {
            FolderApplicationInfoPopup(
                // Application Info
                folderId = selectedFolderEblanApplicationInfo.id,
                isCloseFolderEblanApplicationInfoMenu = pagerScreenState.isCloseFolderEblanApplicationInfoMenu,
                // Layout and Positioning
                paddingValues = paddingValues,
                popupIntOffset = pagerScreenState.menuIntOffset,
                popupIntSize = pagerScreenState.menuIntSize,
                // Actions
                onDismissRequest = pagerScreenState::dismissFolderEblanApplicationMenu,
                onEditFolderApplicationInfo = onEditFolderApplicationInfo,
                onDeleteFolderEblanApplicationInfoGridItems = contract::deleteFolderEblanApplicationInfoGridItems,
            )
        }

        if (isVisibleFolderEblanApplicationInfoGridItemMenu) {
            FolderApplicationInfoGridItemPopup(
                // Folder Data
                folderEblanApplicationInfoGridItem = pagerScreenState.selectedFolderEblanApplicationInfoGridItem,
                eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                gridItemSettings = appDrawerSettings.gridItemSettings,
                // Popup Configuration
                popupIntOffset = pagerScreenState.menuIntOffset,
                popupIntSize = pagerScreenState.menuIntSize,
                paddingValues = paddingValues,
                hasShortcutHostPermission = hasShortcutHostPermission,
                isVisibleOverlay = isVisibleOverlay,
                isCloseFolderEblanApplicationInfoGridItemMenu = pagerScreenState.isCloseFolderEblanApplicationInfoGridItemMenu,
                // Actions
                onDismissRequest = pagerScreenState::dismissFolderEblanApplicationInfoGridItemMenu,
                onDismissScreen = {
                    contract.closeFolderEblanApplicationInfoEntries()

                    pagerScreenState.dismissApplicationScreen()
                },
                onEditFolderApplicationInfo = onEditFolderApplicationInfo,
                onEditApplicationInfo = onEditApplicationInfo,
                onWidgets = pagerScreenState::openAppWidgetScreen,
                onDeleteFolderEblanApplicationInfoGridItems = contract::deleteFolderEblanApplicationInfoGridItems,
                onDragShortcutInfo = { gridItem, imageBitmap, intOffset, intSize, sharedElementKey ->
                    pagerScreenState.dragShortcutInfoFromFolderApplicationGridItemMenu(
                        // Grid item details
                        gridItem = gridItem,
                        imageBitmap = imageBitmap,
                        intOffset = intOffset,
                        intSize = intSize,
                        // Shared element key
                        newSharedElementKey = sharedElementKey,
                        // Callbacks
                        onUpdateGridItemSource = contract::updateGridItemSource,
                        onUpdateIsVisibleOverlay = contract::updateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = contract::updateMoveGridItemResult,
                        onResetFolderEblanApplicationInfoEntries = contract::resetFolderEblanApplicationInfoEntries,
                    )
                },
            )
        }

        if (pagerScreenState.eblanApplicationInfoGroup != null) {
            AppWidgetScreen(
                // Contract
                contract = contract,
                // Data
                eblanApplicationInfoGroup = pagerScreenState.eblanApplicationInfoGroup,
                // Grid
                columns = homeSettings.columns,
                rows = homeSettings.rows,
                gridItemSettings = homeSettings.gridItemSettings,
                // Layout
                paddingValues = paddingValues,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                // Interaction
                swipeY = pagerScreenState.appWidgetScreenSwipeY.value,
                // Callbacks
                onDismiss = pagerScreenState::dismissAppWidgetScreen,
                onVerticalDrag = pagerScreenState::verticalDragAppWidgetScreen,
                onDragEnd = pagerScreenState::handleOnDragEndAppWidgetScreen,
                onDragAppWidget = { gridItem, imageBitmap, intOffset, intSize, sharedElementKey ->
                    pagerScreenState.dragAppWidget(
                        // Widget details
                        gridItem = gridItem,
                        imageBitmap = imageBitmap,
                        // Drag position and size
                        intOffset = intOffset,
                        intSize = intSize,
                        // Shared element
                        newSharedElementKey = sharedElementKey,
                        // Callbacks
                        onUpdateGridItemSource = contract::updateGridItemSource,
                        onUpdateIsVisibleOverlay = contract::updateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = contract::updateMoveGridItemResult,
                        onResetFolderGridItemEntries = contract::resetFolderGridItemEntries,
                        onResetFolderEblanApplicationInfoEntries = contract::resetFolderEblanApplicationInfoEntries,
                    )
                },
            )
        }

        OverlayImage(
            // Overlay Image
            overlayImageBitmap = pagerScreenState.overlayImageBitmap,
            overlayIntOffset = pagerScreenState.overlayIntOffset,
            overlayIntSize = pagerScreenState.overlayIntSize,
            // Overlay State
            isVisibleOverlay = isVisibleOverlay,
            animations = experimentalSettings.gridItemAnimation,
            // Screen Dimensions
            screenWidth = screenWidth,
            // Shared Element
            sharedElementKey = pagerScreenState.sharedElementKey,
            // Callbacks
            onResetOverlay = pagerScreenState::resetOverlay,
        )
    }
}

@Composable
private fun SharedTransitionScope.OverlayImage(
    // Overlay Image
    overlayImageBitmap: ImageBitmap?,
    overlayIntOffset: IntOffset?,
    overlayIntSize: IntSize?,
    // Overlay State
    isVisibleOverlay: Boolean,
    animations: Boolean,
    // Screen Dimensions
    screenWidth: Int,
    // Shared Element
    sharedElementKey: SharedElementKey?,
    // Layout
    modifier: Modifier = Modifier,
    // Callbacks
    onResetOverlay: () -> Unit,
) {
    if (overlayImageBitmap == null ||
        sharedElementKey == null ||
        overlayIntOffset == null ||
        overlayIntSize == null
    ) {
        return
    }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val size = with(density) {
        DpSize(width = overlayIntSize.width.toDp(), height = overlayIntSize.height.toDp())
    }

    LaunchedEffect(key1 = isVisibleOverlay) {
        if (!isVisibleOverlay) {
            onResetOverlay()
        }
    }

    Image(
        modifier = modifier
            .offset {
                when (layoutDirection) {
                    LayoutDirection.Ltr -> overlayIntOffset

                    LayoutDirection.Rtl -> IntOffset(
                        x = screenWidth - overlayIntSize.width - overlayIntOffset.x,
                        y = overlayIntOffset.y,
                    )
                }
            }
            .size(size)
            .run {
                if (animations) {
                    sharedElementWithCallerManagedVisibility(
                        rememberSharedContentState(key = sharedElementKey),
                        visible = isVisibleOverlay,
                    )
                } else {
                    this
                }
            },
        bitmap = overlayImageBitmap,
        contentDescription = null,
    )
}

@Composable
private fun SyncDataEffect(
    // Sync Control
    syncData: Boolean,
    // Sync Callbacks
    onStartSyncData: () -> Unit,
    onStopSyncData: () -> Unit,
    // Package Event Callbacks
    onPackageRemoved: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    onPackageAdded: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    onPackageChanged: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    onShortcutsChanged: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val appWidgetHost = LocalAppWidgetHost.current
    val pinItemRequestWrapper = LocalPinItemRequest.current
    val launcherAppsWrapper = LocalLauncherApps.current
    val userManagerWrapper = LocalUserManager.current

    DisposableEffect(
        key1 = lifecycleOwner,
        key2 = syncData,
    ) {
        val launcherAppsCallback = getLauncherAppsCallback(
            // Dependencies
            userManagerWrapper = userManagerWrapper,
            // Package change callbacks
            onPackageRemoved = onPackageRemoved,
            onPackageAdded = onPackageAdded,
            onPackageChanged = onPackageChanged,
            // Shortcut change callbacks
            onShortcutsChanged = onShortcutsChanged,
        )

        val lifecycleEventObserver = LifecycleEventObserver { lifecycleOwner, event ->
            lifecycleOwner.lifecycleScope.launch {
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        if (syncData &&
                            pinItemRequestWrapper.getPinItemRequest() == null
                        ) {
                            launcherAppsWrapper.registerCallback(
                                callback = launcherAppsCallback,
                                handler = Handler(Looper.getMainLooper()),
                            )

                            onStartSyncData()
                        }

                        appWidgetHost.startListening()
                    }

                    Lifecycle.Event.ON_STOP -> {
                        if (syncData &&
                            pinItemRequestWrapper.getPinItemRequest() == null
                        ) {
                            launcherAppsWrapper.unregisterCallback(callback = launcherAppsCallback)

                            onStopSyncData()
                        }

                        appWidgetHost.stopListening()
                    }

                    else -> Unit
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleEventObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleEventObserver)

            launcherAppsWrapper.unregisterCallback(callback = launcherAppsCallback)

            onStopSyncData()

            appWidgetHost.stopListening()
        }
    }
}

@OptIn(FlowPreview::class)
@Composable
private fun GridPagerIndicator(
    // Pager State
    gridHorizontalPagerState: PagerState,
    pageCount: Int,
    // Indicator Appearance
    color: Color,
    pageIndicatorType: PageIndicatorType,
    // Scroll Behavior
    infiniteScroll: Boolean,
    swipeUp: EblanAction,
    swipeDown: EblanAction,
    // Layout
    modifier: Modifier = Modifier,
) {
    if (pageIndicatorType == PageIndicatorType.None) return

    var isScrollInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = gridHorizontalPagerState) {
        snapshotFlow { gridHorizontalPagerState.isScrollInProgress }
            .debounce(100L.milliseconds)
            .onEach { currentIsScrollInProgress -> isScrollInProgress = currentIsScrollInProgress }
            .collect()
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        if (isScrollInProgress) {
            PageIndicator(
                // Pager State and Pagination
                gridHorizontalPagerState = gridHorizontalPagerState,
                pageCount = pageCount,
                infiniteScroll = infiniteScroll,
                // Appearance and Layout
                color = color,
                pageIndicatorType = pageIndicatorType,
            )
        } else if (swipeUp.eblanActionType == EblanActionType.OpenAppDrawer) {
            Image(
                imageVector = EblanLauncherIcons.KeyboardArrowUp,
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = color),
            )
        } else if (swipeDown.eblanActionType == EblanActionType.OpenAppDrawer) {
            Image(
                imageVector = EblanLauncherIcons.KeyboardArrowDown,
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = color),
            )
        }
    }
}

@Composable
private fun produceStatusBarNotificationsState(): State<Map<String, Int>> {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    return produceState(
        initialValue = emptyMap(),
        key1 = context,
        key2 = lifecycleOwner,
    ) {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName,
                service: IBinder,
            ) {
                val listener =
                    (service as EblanNotificationListenerService.LocalBinder).getService()

                launch {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        listener.statusBarNotifications.collect { currentStatusBarNotifications ->
                            value = currentStatusBarNotifications
                        }
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName) = Unit
        }

        context.bindService(
            Intent(
                context,
                EblanNotificationListenerService::class.java,
            ),
            serviceConnection,
            Context.BIND_AUTO_CREATE,
        )

        awaitDispose {
            context.unbindService(serviceConnection)
        }
    }
}

private fun getLauncherAppsCallback(
    // Dependencies
    userManagerWrapper: AndroidUserManagerWrapper,
    // Package change callbacks
    onPackageRemoved: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    onPackageAdded: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    onPackageChanged: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    // Shortcut change callbacks
    onShortcutsChanged: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
): LauncherApps.Callback = object : LauncherApps.Callback() {
    override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
        if (packageName != null && user != null) {
            onPackageRemoved(
                userManagerWrapper.getSerialNumberForUser(userHandle = user),
                packageName,
            )
        }
    }

    override fun onPackageAdded(packageName: String?, user: UserHandle?) {
        if (packageName != null && user != null) {
            onPackageAdded(
                userManagerWrapper.getSerialNumberForUser(userHandle = user),
                packageName,
            )
        }
    }

    override fun onPackageChanged(packageName: String?, user: UserHandle?) {
        if (packageName != null && user != null) {
            onPackageChanged(
                userManagerWrapper.getSerialNumberForUser(userHandle = user),
                packageName,
            )
        }
    }

    override fun onPackagesAvailable(
        packageNames: Array<out String>?,
        user: UserHandle?,
        replacing: Boolean,
    ) {
    }

    override fun onPackagesUnavailable(
        packageNames: Array<out String>?,
        user: UserHandle?,
        replacing: Boolean,
    ) {
    }

    override fun onShortcutsChanged(
        packageName: String,
        shortcuts: MutableList<ShortcutInfo>,
        user: UserHandle,
    ) {
        onShortcutsChanged(
            userManagerWrapper.getSerialNumberForUser(userHandle = user),
            packageName,
        )
    }
}
