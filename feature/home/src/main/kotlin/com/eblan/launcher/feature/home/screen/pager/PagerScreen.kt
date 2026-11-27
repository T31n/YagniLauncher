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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.grid.getDockHeight
import com.eblan.launcher.domain.grid.getPageIndicatorHeight
import com.eblan.launcher.domain.model.application.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.application.GetEblanApplicationInfosByLabelAndTag
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.folder.FolderPopup
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.MoveFolderEblanApplicationInfoGridItemResult
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.domain.model.launcherapps.EblanUser
import com.eblan.launcher.domain.model.launcherapps.PinItemRequestType
import com.eblan.launcher.domain.model.shortcutconfig.EblanShortcutConfig
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfo
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfoByGroup
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.domain.model.userdata.FolderSettings
import com.eblan.launcher.domain.model.userdata.GestureSettings
import com.eblan.launcher.domain.model.userdata.HomeSettings
import com.eblan.launcher.domain.model.userdata.PageIndicatorType
import com.eblan.launcher.domain.model.userdata.ScreenOrientation
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.domain.model.widget.EblanAppWidgetProviderInfo
import com.eblan.launcher.feature.home.component.GridLayout
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.component.PageIndicator
import com.eblan.launcher.feature.home.component.SparkleGridLayout
import com.eblan.launcher.feature.home.component.pagerTransitionEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
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
    // Screen Dimensions
    screenWidth: Int,
    screenHeight: Int,
    // Settings
    homeSettings: HomeSettings,
    folderSettings: FolderSettings,
    appDrawerSettings: AppDrawerSettings,
    gestureSettings: GestureSettings,
    syncData: Boolean,
    lockMovement: Boolean,
    gridItemAnimation: Boolean,
    textColor: TextColor,
    // System Features
    hasSystemFeatureAppWidgets: Boolean,
    hasShortcutHostPermission: Boolean,
    // Data
    gridItems: List<GridItem>,
    gridItemsByPage: Map<Int, List<GridItem>>,
    dockGridItemsByPage: Map<Int, List<GridItem>>,
    folderGridItemEntries: List<FolderEntry>,
    folderEblanApplicationInfoEntries: List<FolderEntry>,
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    eblanAppWidgetProviderInfos: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>>,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanShortcutConfigs: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    folderPopups: Map<String, FolderPopup>,
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    iconPackFilePaths: Map<String, String?>,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    // State
    configureResultCode: Int?,
    moveGridItemResult: MoveGridItemResult?,
    moveFolderEblanApplicationInfoGridItemResult: MoveFolderEblanApplicationInfoGridItemResult?,
    pinGridItem: GridItem?,
    resizeGridItem: GridItem?,
    gridItemSource: GridItemSource?,
    isVisibleOverlay: Boolean,
    // Layout
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    // Callbacks - Data Management
    onStartSyncData: () -> Unit,
    onStopSyncData: () -> Unit,
    onResetGrid: () -> Unit,
    onResetConfigureResultCode: () -> Unit,
    onResetPinGridItem: () -> Unit,
    onResetFolderGridItemEntries: () -> Unit,
    onCloseFolderGridItemEntries: () -> Unit,
    onResetFolderEblanApplicationInfoEntries: () -> Unit,
    onCloseFolderEblanApplicationInfoEntries: () -> Unit,
    // Callbacks - Grid Item Actions
    onDeleteGridItem: (GridItem) -> Unit,
    onResetGridAfterDeleteGridItem: (GridItem) -> Unit,
    onUpdateGridItemsAfterMove: (MoveGridItemResult) -> Unit,
    onEditGridItem: (String) -> Unit,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onCancelPendingMoveGridItem: () -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
    ) -> Unit,
    onResizeCancel: () -> Unit,
    onResizeEnd: () -> Unit,
    onUpdateResizeGridItem: (GridItem) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
    onUpdateGridItemsAfterMoveNewFolder: (
        folderGridItems: List<GridItem>,
        moveGridItemResult: MoveGridItemResult,
    ) -> Unit,
    onUpdateShortcutConfigIntoShortcutInfoGridItem: (
        moveGridItemResult: MoveGridItemResult,
        pinItemRequestType: PinItemRequestType.ShortcutInfo,
    ) -> Unit,
    onUpdateShortcutConfigGridItem: (
        moveGridItemResult: MoveGridItemResult,
        shortcutIntentName: String?,
        shortcutIntentIcon: ByteArray?,
        shortcutIntentUri: String?,
    ) -> Unit,
    // Callbacks - Folder Actions
    onResetGridAfterMoveFolder: () -> Unit,
    onUpsertFolderGridItemEntry: (FolderEntry) -> Unit,
    onDeleteFolderGridItemEntry: (FolderEntry) -> Unit,
    onUpsertFolderEblanApplicationInfoEntry: (FolderEntry) -> Unit,
    onDeleteFolderEblanApplicationInfoEntry: (FolderEntry) -> Unit,
    onMoveFolderGridItem: (
        columns: Int,
        rows: Int,
        folderGridItems: List<GridItem>,
        movingGridItem: GridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onMoveFolderGridItemOutsideFolder: (GridItem) -> Unit,
    onShowFolderWhenDragging: (
        folderEntry: FolderEntry,
        movingGridItem: GridItem,
    ) -> Unit,
    onEditFolderApplicationInfo: (String) -> Unit,
    onUpdateMoveFolderEblanApplicationInfoGridItemResult: (MoveFolderEblanApplicationInfoGridItemResult) -> Unit,
    onMoveFolderEblanApplicationInfoGridItem: (
        columns: Int,
        rows: Int,
        folderEblanApplicationInfoGridItems: List<FolderEblanApplicationInfoGridItem>,
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onDragEndAfterMoveFolderEblanApplicationInfo: () -> Unit,
    onMoveFolderEblanApplicationInfoGridItemOutsideFolder: (
        folderId: String,
        isFolder: Boolean,
        movingGridItem: GridItem,
    ) -> Unit,
    onDragFolderEblanApplicationInfo: (
        folderId: String,
        movingGridItem: GridItem,
    ) -> Unit,
    onDeleteFolderEblanApplicationInfoGridItems: (String) -> Unit,
    // Callbacks - Application Info Actions
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
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
    // Callbacks - Page and Widget Actions
    onEditPage: (
        gridItems: List<GridItem>,
        associate: Associate,
    ) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByTagId: (Long?) -> Unit,
    onGetEblanShortcutConfigsByLabel: (String) -> Unit,
    onGetPinGridItem: (PinItemRequestType) -> Unit,
    // Callbacks - General UI
    onSettings: () -> Unit,
) {
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
        onGetPinGridItem = onGetPinGridItem,
        onResetPinGridItem = onResetPinGridItem,
    )

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

    val pageIndicatorHeight = getPageIndicatorHeight(homeSettings.pageIndicatorType).dp

    val pageIndicatorHeightPx = with(density) {
        pageIndicatorHeight.roundToPx()
    }

    val dockHeightPx = getDockHeight(
        enableDock = homeSettings.enableDock,
        rows = homeSettings.rows,
        dockRows = homeSettings.dockRows,
        availableHeight = safeDrawingHeight - pageIndicatorHeightPx,
    )

    val dockHeight = with(density) {
        dockHeightPx.toDp()
    }

    val dockTopLeft = safeDrawingHeight - dockHeightPx

    val gridHeight = safeDrawingHeight - pageIndicatorHeightPx - dockHeightPx

    val currentGridItemSource = rememberUpdatedState(gridItemSource)
    val currentIsVisibleOverlay = rememberUpdatedState(isVisibleOverlay)
    val currentMoveGridItemResult = rememberUpdatedState(moveGridItemResult)
    val hasFolderGridItemEntries = rememberUpdatedState(folderGridItemEntries.isNotEmpty())
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
            density = density,
            rows = homeSettings.rows,
            safeDrawingWidth = safeDrawingWidth,
            safeDrawingHeight = safeDrawingHeight,
            dockHeightPx = dockHeightPx,
            pageIndicatorHeightPx = pageIndicatorHeightPx,
            // Callbacks
            onUpdateWidgetGridItem = pagerScreenState::updateWidgetGridItem,
            onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
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
                onDeleteGridItem = onResetGridAfterDeleteGridItem,
                onUpdateShortcutConfigGridItem = onUpdateShortcutConfigGridItem,
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
                onDeleteGridItem = onResetGridAfterDeleteGridItem,
                onUpdateShortcutConfigIntoShortcutInfoGridItem = onUpdateShortcutConfigIntoShortcutInfoGridItem,
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
                            !lockMovement
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
            onUpdateGridItemSource = onUpdateGridItemSource,
            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
            onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
        )
    }

    SyncDataEffect(
        // Sync Control
        syncData = syncData,
        // Sync Callbacks
        onStartSyncData = onStartSyncData,
        onStopSyncData = onStopSyncData,
        // Package Event Callbacks
        onPackageRemoved = onPackageRemoved,
        onPackageAdded = onPackageAdded,
        onPackageChanged = onPackageChanged,
        onShortcutsChanged = onShortcutsChanged,
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
            dockHeightPx = dockHeightPx,
            // Screen and Layout
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            pageIndicatorHeightPx = pageIndicatorHeightPx,
            layoutDirection = layoutDirection,
            leftPadding = leftPadding,
            topPadding = topPadding,
            rightPadding = rightPadding,
            bottomPadding = bottomPadding,
            // Drag State
            drag = pagerScreenState.drag,
            dragIntOffset = pagerScreenState.dragIntOffset,
            isDragging = pagerScreenState.isDragging,
            isVisibleOverlay = currentIsVisibleOverlay.value,
            lockMovement = lockMovement,
            // Grid and Scroll State
            gridItemSource = currentGridItemSource.value,
            moveGridItemResult = currentMoveGridItemResult.value,
            isGridScrollInProgress = gridHorizontalPagerState.isScrollInProgress,
            isDockScrollInProgress = dockGridHorizontalPagerState.isScrollInProgress,
            // Callbacks
            onMoveGridItem = onMoveGridItem,
            onCancelPendingMoveGridItem = onCancelPendingMoveGridItem,
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
            // Android System Wrappers
            androidAppWidgetHostWrapper = androidAppWidgetHostWrapper,
            androidAppWidgetManagerWrapper = androidAppWidgetManagerWrapper,
            androidLauncherAppsWrapper = androidLauncherAppsWrapper,
            androidUserManagerWrapper = androidUserManagerWrapper,
            // Grid and Layout Configuration
            columns = homeSettings.columns,
            rows = homeSettings.rows,
            safeDrawingHeight = safeDrawingHeight,
            safeDrawingWidth = safeDrawingWidth,
            dockHeightPx = dockHeightPx,
            pageIndicatorHeightPx = pageIndicatorHeightPx,
            // Drag and Drop State
            drag = pagerScreenState.drag,
            gridItemSource = currentGridItemSource.value,
            isDragging = pagerScreenState.isDragging,
            moveGridItemResult = currentMoveGridItemResult.value,
            lockMovement = lockMovement,
            isVisibleOverlay = currentIsVisibleOverlay.value,
            // Grid and Layout Callbacks
            onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
            onResetGrid = onResetGrid,
            onUpdateGridItemsAfterMove = onUpdateGridItemsAfterMove,
            onUpdateGridItemsAfterMoveNewFolder = onUpdateGridItemsAfterMoveNewFolder,
            // Intent and Configuration Callbacks
            onLaunchShortcutConfigIntent = shortcutConfigLauncher::launch,
            onLaunchShortcutConfigIntentSenderRequest = shortcutConfigIntentSenderLauncher::launch,
            onLaunchWidgetIntent = appWidgetLauncher::launch,
            onUpdateAppWidgetId = pagerScreenState::updateLastAppWidgetId,
            // State and UI Update Callbacks
            onUpdateIsDragging = pagerScreenState::updateIsDragging,
            onUpdateWidgetGridItem = pagerScreenState::updateWidgetGridItem,
            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
        )
    }

    LaunchedEffect(pagerScreenState.widgetGridItem) {
        handleBoundWidgetEffect(
            // Context and Host
            activity = activity,
            androidAppWidgetHostWrapper = androidAppWidgetHostWrapper,
            // Grid State
            gridItemSource = currentGridItemSource.value,
            moveGridItemResult = currentMoveGridItemResult.value,
            widgetGridItem = pagerScreenState.widgetGridItem,
            // Callbacks
            onDeleteGridItem = onResetGridAfterDeleteGridItem,
            onUpdateGridItemsAfterMove = onUpdateGridItemsAfterMove,
            onResetGrid = onResetGrid,
        )
    }

    LaunchedEffect(gridHorizontalPagerState) {
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
            leftPadding = leftPadding,
            rightPadding = rightPadding,
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
            onDeleteGridItem = onDeleteGridItem,
            onUpdateGridItemsAfterMove = onUpdateGridItemsAfterMove,
            onResetConfigureResultCode = onResetConfigureResultCode,
            onResetGrid = onResetGrid,
        )
    }

    LaunchedEffect(pagerScreenState.gridPageDirection) {
        handlePageDirection(
            // State
            hasFolderGridItemEntries = hasFolderGridItemEntries,
            // Pagination
            pageDirection = pagerScreenState.gridPageDirection,
            currentPage = gridHorizontalPagerState.currentPage,
            // Callbacks
            onAnimateScrollToPage = gridHorizontalPagerState::animateScrollToPage,
        )
    }

    LaunchedEffect(pagerScreenState.dockPageDirection) {
        handlePageDirection(
            // State
            hasFolderGridItemEntries = hasFolderGridItemEntries,
            // Pagination
            pageDirection = pagerScreenState.dockPageDirection,
            currentPage = dockGridHorizontalPagerState.currentPage,
            // Callbacks
            onAnimateScrollToPage = dockGridHorizontalPagerState::animateScrollToPage,
        )
    }

    LaunchedEffect(pagerScreenState.hasDoubleTap) {
        pagerScreenState.handleHasDoubleTap()
    }

    LaunchedEffect(homeSettings.infiniteScroll) {
        gridHorizontalPagerState.scrollToPage(
            if (homeSettings.infiniteScroll) {
                (Int.MAX_VALUE / 2) + homeSettings.initialPage
            } else {
                homeSettings.initialPage
            },
        )
    }

    LaunchedEffect(homeSettings.dockInfiniteScroll) {
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
            pagerScreenState.handleEblanActionIntent(currentIntent)
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
            .pointerInput(pagerScreenState) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { pointerOffset ->
                        val rootPointerOffset = requireNotNull(sharedTransitionLayoutCoordinates)
                            .localToRoot(pointerOffset)

                        pagerScreenState.dragStart(rootPointerOffset)
                    },
                    onDragEnd = {
                        pagerScreenState.updateDrag(Drag.End)
                    },
                    onDragCancel = {
                        pagerScreenState.updateDrag(Drag.Cancel)
                    },
                    onDrag = { _, dragAmount ->
                        pagerScreenState.drag(dragAmount)
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
                            pagerScreenState.verticalDrag(dragAmount)
                        },
                        onDragEnd = pagerScreenState::verticalDragEnd,
                        onDragCancel = pagerScreenState::verticalDragCancel,
                    )
                }
                .pointerInput(pagerScreenState) {
                    detectTapGestures(
                        onDoubleTap = {
                            pagerScreenState.updateHasDoubleTap(true)
                        },
                        onLongPress = { currentOffset ->
                            pagerScreenState.showSettingsMenu(currentOffset)
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
                        animate = (isVisibleOverlay || isResizing) && gridItemAnimation,
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
                            val currentFolderPopup = folderPopups[currentGridItem.id]

                            InteractiveGridItem(
                                // Item Data
                                gridItem = currentGridItem,
                                gridItemSettings = homeSettings.gridItemSettings,
                                sharedElementKey = SharedElementKey(
                                    id = currentGridItem.id,
                                    parent = SharedElementKey.Parent.Grid,
                                ),
                                hasNotifications = (currentGridItem.data as? GridItemData.ApplicationInfo)?.packageName?.let { packageName ->
                                    (statusBarNotifications[packageName] ?: 0) > 0
                                } == true,
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
                                animations = gridItemAnimation,
                                // State and Flags
                                drag = pagerScreenState.drag,
                                hasShortcutHostPermission = hasShortcutHostPermission,
                                isScrollInProgress = gridHorizontalPagerState.isScrollInProgress,
                                isVisibleOverlay = isVisibleOverlay,
                                isVisibleFolderGridItems = isVisibleFolderGridItems,
                                isVisibleFolder = isVisibleFolderGridItems && folderGridItemEntries.any { currentFolderEntry ->
                                    currentFolderEntry.id == currentGridItem.id
                                },
                                lockMovement = lockMovement,
                                isDragging = pagerScreenState.isDragging,
                                showGridItemMenu = pagerScreenState.showGridItemMenu,
                                folderGridItems = currentFolderPopup?.folderGridItems,
                                previewFolderGridItems = currentFolderPopup?.previewFolderGridItems,
                                // Callbacks
                                onOpenAppDrawer = pagerScreenState::openApplicationScreen,
                                onShowFolderWhenDragging = { folderEntry, gridItem ->
                                    pagerScreenState.showFolderWhenDragging(
                                        // Folder and item being dragged
                                        folderEntry = folderEntry,
                                        movingGridItem = gridItem,
                                        // Callbacks
                                        onShowFolderWhenDragging = onShowFolderWhenDragging,
                                    )
                                },
                                onResetGrid = onResetGrid,
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
                                        onUpdateGridItemSource = onUpdateGridItemSource,
                                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                        onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                                    )
                                },
                                onTapFolderGridItem = { folderEntry ->
                                    pagerScreenState.tapFolderGridItem(
                                        folderEntry = folderEntry,
                                        onUpsertFolderGridItemEntry = onUpsertFolderGridItemEntry,
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
                    .height(pageIndicatorHeight),
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
                            textColor = textColor,
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
                            animate = (isVisibleOverlay || isResizing) && gridItemAnimation,
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
                                val currentFolderPopup = folderPopups[currentDockGridItem.id]

                                InteractiveGridItem(
                                    // Item Data
                                    gridItem = currentDockGridItem,
                                    gridItemSettings = homeSettings.dockGridItemSettings,
                                    sharedElementKey = SharedElementKey(
                                        id = currentDockGridItem.id,
                                        parent = SharedElementKey.Parent.Dock,
                                    ),
                                    hasNotifications = (currentDockGridItem.data as? GridItemData.ApplicationInfo)?.packageName?.let { packageName ->
                                        (statusBarNotifications[packageName] ?: 0) > 0
                                    } == true,
                                    moveGridItemResult = moveGridItemResult,
                                    // Layout and Dimensions
                                    cellWidth = safeDrawingWidth / homeSettings.dockColumns,
                                    cellHeight = dockHeightPx / homeSettings.dockRows,
                                    leftPadding = leftPadding,
                                    topOffset = dockTopLeft,
                                    // Styling and Appearance
                                    textColor = textColor,
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
                                    animations = gridItemAnimation,
                                    // State and Flags
                                    drag = pagerScreenState.drag,
                                    hasShortcutHostPermission = hasShortcutHostPermission,
                                    isScrollInProgress = dockGridHorizontalPagerState.isScrollInProgress,
                                    isVisibleOverlay = isVisibleOverlay,
                                    isVisibleFolderGridItems = isVisibleFolderGridItems,
                                    isVisibleFolder = isVisibleFolderGridItems && folderGridItemEntries.any { currentFolderEntry ->
                                        currentFolderEntry.id == currentDockGridItem.id
                                    },
                                    lockMovement = lockMovement,
                                    isDragging = pagerScreenState.isDragging,
                                    showGridItemMenu = pagerScreenState.showGridItemMenu,
                                    folderGridItems = currentFolderPopup?.folderGridItems,
                                    previewFolderGridItems = currentFolderPopup?.previewFolderGridItems,
                                    // Callbacks
                                    onOpenAppDrawer = pagerScreenState::openApplicationScreen,
                                    onShowFolderWhenDragging = { folderEntry, gridItem ->
                                        pagerScreenState.showFolderWhenDragging(
                                            // Folder and item being dragged
                                            folderEntry = folderEntry,
                                            movingGridItem = gridItem,
                                            // Callbacks
                                            onShowFolderWhenDragging = onShowFolderWhenDragging,
                                        )
                                    },
                                    onResetGrid = onResetGrid,
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
                                            onUpdateGridItemSource = onUpdateGridItemSource,
                                            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                            onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                                        )
                                    },
                                    onTapFolderGridItem = { folderEntry ->
                                        pagerScreenState.tapFolderGridItem(
                                            folderEntry = folderEntry,
                                            onUpsertFolderGridItemEntry = onUpsertFolderGridItemEntry,
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
            val gridItemAppInfoData =
                pagerScreenState.selectedGridItem?.data as? GridItemData.ApplicationInfo

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
                hasAppWidgets = !eblanAppWidgetProviderInfosGroup[gridItemAppInfoData?.packageName].isNullOrEmpty(),
                eblanShortcutInfosByPackageName = gridItemAppInfoData?.let { data ->
                    eblanShortcutInfosGroup[
                        EblanShortcutInfoByGroup(
                            serialNumber = data.serialNumber,
                            packageName = data.packageName,
                        ),
                    ]
                },
                hasShortcutHostPermission = hasShortcutHostPermission,
                // Callbacks
                onDeleteGridItem = onDeleteGridItem,
                onDismissRequest = pagerScreenState::dismissGridItemMenu,
                onEdit = { id ->
                    onResetGrid()

                    onEditGridItem(id)
                },
                onResize = onUpdateResizeGridItem,
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
                        onUpdateGridItemSource = onUpdateGridItemSource,
                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
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
                onEditPage = onEditPage,
                onSettings = onSettings,
                onShortcutConfigActivities = pagerScreenState::openShortcutConfigScreen,
                onWidgets = pagerScreenState::openWidgetScreen,
            )
        }

        if (isVisibleFolderGridItems) {
            folderGridItemEntries.forEach { currentFolderEntry ->
                FolderScreen(
                    // Transition and Layout
                    sharedTransitionScope = this@SharedTransitionLayout,
                    paddingValues = paddingValues,
                    safeDrawingHeight = safeDrawingHeight,
                    safeDrawingWidth = safeDrawingWidth,
                    screenHeight = screenHeight,
                    screenWidth = screenWidth,
                    // Folder Content and Settings
                    folderEntry = currentFolderEntry,
                    folderGridItemEntries = folderGridItemEntries,
                    gridItemSettings = homeSettings.gridItemSettings,
                    folderCellWidth = folderSettings.folderCellWidth,
                    folderCellHeight = folderSettings.folderCellHeight,
                    folderPopups = folderPopups,
                    iconPackFilePaths = iconPackFilePaths,
                    statusBarNotifications = statusBarNotifications,
                    // Appearance and Style
                    animations = gridItemAnimation,
                    systemTextColor = textColor,
                    systemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
                    folderBackgroundColor = folderSettings.folderBackgroundColor,
                    folderCornerRadius = folderSettings.folderCornerRadius,
                    customFolderBackgroundColor = folderSettings.customFolderBackgroundColor,
                    folderBorderColor = folderSettings.folderBorderColor,
                    customFolderBorderColor = folderSettings.customFolderBorderColor,
                    folderBorderWidth = folderSettings.borderWidth,
                    // Drag and Drop State
                    drag = pagerScreenState.drag,
                    moveGridItemResult = moveGridItemResult,
                    isDragging = pagerScreenState.isDragging,
                    dragIntOffset = pagerScreenState.dragIntOffset,
                    lockMovement = lockMovement,
                    // Overlay and Permission State
                    isVisibleOverlay = isVisibleOverlay,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    showFolderGridItemMenu = pagerScreenState.showFolderGridItemMenu,
                    // Folder Actions
                    onCloseFolder = { folderEntry, isFirstFolderGridItem ->
                        pagerScreenState.closeFolderGridItem(
                            // Folder Item
                            folderEntry = folderEntry,
                            isFirstFolderGridItem = isFirstFolderGridItem,
                            // Callbacks
                            onDeleteFolderGridItemEntry = onDeleteFolderGridItemEntry,
                        )
                    },
                    onDismiss = onCloseFolderGridItemEntries,
                    onOpenAppDrawer = pagerScreenState::openApplicationScreen,
                    onResetGrid = onResetGrid,
                    onResetGridAfterMoveFolder = onResetGridAfterMoveFolder,
                    onUpdateIsDragging = pagerScreenState::updateIsDragging,
                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                    onDismissFolderGridItemMenu = pagerScreenState::dismissFolderGridItemMenu,
                    onUpsertFolderGridItemEntry = onUpsertFolderGridItemEntry,
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
                            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                            onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                        )
                    },
                    onMoveFolderGridItem = onMoveFolderGridItem,
                    onMoveFolderGridItemOutsideFolder = { gridItem, sharedElementKey ->
                        pagerScreenState.moveFolderGridItemOutsideFolder(
                            // Item to move
                            gridItem = gridItem,
                            // New location
                            newSharedElementKey = sharedElementKey,
                            // Callbacks
                            onMoveFolderGridItemOutsideFolder = onMoveFolderGridItemOutsideFolder,
                        )
                    },
                )
            }
        }

        if (isVisibleFolderGridItemMenu) {
            val folderGridItemAppInfoData =
                pagerScreenState.selectedGridItem?.data as? GridItemData.ApplicationInfo

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
                hasAppWidgets = !eblanAppWidgetProviderInfosGroup[folderGridItemAppInfoData?.packageName].isNullOrEmpty(),
                eblanShortcutInfosByPackageName = folderGridItemAppInfoData?.let { data ->
                    eblanShortcutInfosGroup[
                        EblanShortcutInfoByGroup(
                            serialNumber = data.serialNumber,
                            packageName = data.packageName,
                        ),
                    ]
                },
                // Actions
                onDeleteGridItem = onDeleteGridItem,
                onDismissRequest = pagerScreenState::dismissFolderGridItemMenu,
                onDismissScreen = onCloseFolderGridItemEntries,
                onEdit = { id ->
                    onResetGrid()

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
                        onUpdateGridItemSource = onUpdateGridItemSource,
                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                        onResetFolderGridItemEntries = onResetFolderGridItemEntries,
                    )
                },
            )
        }

        if (pagerScreenState.showApplicationScreen) {
            ApplicationScreen(
                // Layout and Transition
                sharedTransitionScope = this@SharedTransitionLayout,
                screenHeight = screenHeight,
                paddingValues = paddingValues,
                // Visual and Animation
                animations = gridItemAnimation,
                alpha = pagerScreenState.applicationScreenAlpha,
                systemTextColor = textColor,
                systemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
                // Application Data
                eblanApplicationInfoTags = eblanApplicationInfoTags,
                getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
                appDrawerSettings = appDrawerSettings,
                // Folder Settings
                folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                folderCornerRadius = folderSettings.folderCornerRadius,
                folderBackgroundColor = folderSettings.folderBackgroundColor,
                customFolderBackgroundColor = folderSettings.customFolderBackgroundColor,
                folderBorderColor = folderSettings.folderBorderColor,
                customFolderBorderColor = folderSettings.customFolderBorderColor,
                folderBorderWidth = folderSettings.borderWidth,
                isVisibleFolderEblanApplicationInfos = isVisibleFolderEblanApplicationInfos,
                folderEblanApplicationInfoEntries = folderEblanApplicationInfoEntries,
                // Gesture and State
                isVisibleOverlay = isVisibleOverlay,
                swipeY = pagerScreenState.applicationScreenSwipeY.value,
                drag = pagerScreenState.drag,
                // Actions and Callbacks
                onDismiss = pagerScreenState::dismissApplicationScreen,
                onDragEnd = pagerScreenState::handleOnDragEndApplicationScreen,
                onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                onGetEblanApplicationInfosByTagId = onGetEblanApplicationInfosByTagId,
                onVerticalDrag = pagerScreenState::verticalDragApplicationScreen,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onDragFolderEblanApplicationInfo = { folderId, movingGridItem ->
                    pagerScreenState.dragFolderEblanApplicationInfo(
                        // Data
                        folderId = folderId,
                        gridItem = movingGridItem,
                        // Callbacks
                        onDragFolderEblanApplicationInfoToGrid = onDragFolderEblanApplicationInfo,
                        onUpdateGridItemSource = onUpdateGridItemSource,
                        onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                    )
                },
                onDragApplicationInfo = { currentApplicationGridItem ->
                    pagerScreenState.dragEblanApplicationInfo(
                        gridItem = currentApplicationGridItem,
                        onUpdateGridItemSource = onUpdateGridItemSource,
                        onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
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
                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                    )
                },
                onLongPressFolderApplicationInfo = { folderEblanApplicationInfo: FolderEblanApplicationInfo, imageBitmap: ImageBitmap, intOffset: IntOffset, intSize: IntSize, sharedElementKey: SharedElementKey ->
                    pagerScreenState.longPressFolderEblanApplicationInfo(
                        // Target Data
                        folderEblanApplicationInfo = folderEblanApplicationInfo,
                        newSharedElementKey = sharedElementKey,
                        // Visual Data
                        imageBitmap = imageBitmap,
                        intOffset = intOffset,
                        intSize = intSize,
                        // Callbacks
                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
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
                        onUpsertFolderEblanApplicationInfoEntry = onUpsertFolderEblanApplicationInfoEntry,
                    )
                },
            )
        }

        val selectedEblanApplicationInfo = pagerScreenState.selectedEblanApplicationInfo

        if (pagerScreenState.showEblanApplicationInfoMenu &&
            selectedEblanApplicationInfo != null &&
            pagerScreenState.menuIntOffset != null &&
            pagerScreenState.menuIntSize != null
        ) {
            ApplicationInfoPopup(
                // Application Data
                eblanApplicationInfo = selectedEblanApplicationInfo,
                hasAppWidgets = !eblanAppWidgetProviderInfosGroup[selectedEblanApplicationInfo.packageName].isNullOrEmpty(),
                eblanShortcutInfosByPackageName = eblanShortcutInfosGroup[
                    EblanShortcutInfoByGroup(
                        serialNumber = selectedEblanApplicationInfo.serialNumber,
                        packageName = selectedEblanApplicationInfo.packageName,
                    ),
                ],
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
                        onUpdateGridItemSource = onUpdateGridItemSource,
                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                    )
                },
            )
        }

        if (pagerScreenState.showPrivateEblanApplicationInfoMenu &&
            selectedEblanApplicationInfo != null &&
            pagerScreenState.menuIntOffset != null &&
            pagerScreenState.menuIntSize != null
        ) {
            PrivateApplicationInfoPopup(
                // Application Info
                eblanApplicationInfo = selectedEblanApplicationInfo,
                eblanShortcutInfosByPackageName = eblanShortcutInfosGroup[
                    EblanShortcutInfoByGroup(
                        serialNumber = selectedEblanApplicationInfo.serialNumber,
                        packageName = selectedEblanApplicationInfo.packageName,
                    ),
                ],
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
                isVisibleOverlay = isVisibleOverlay,
                swipeY = pagerScreenState.widgetScreenSwipeY.value,
                eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                // Interaction
                drag = pagerScreenState.drag,
                // Actions
                onDismiss = pagerScreenState::dismissWidgetScreen,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onVerticalDrag = pagerScreenState::verticalDragWidgetScreen,
                onDragEnd = pagerScreenState::handleOnDragEndWidgetScreen,
                onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
                onDragWidget = { gridItem, imageBitmap, intOffset, intSize, sharedElementKey ->
                    pagerScreenState.dragWidget(
                        // Drag details
                        gridItem = gridItem,
                        newSharedElementKey = sharedElementKey,
                        imageBitmap = imageBitmap,
                        intOffset = intOffset,
                        intSize = intSize,
                        // Callbacks
                        onUpdateGridItemSource = onUpdateGridItemSource,
                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                    )
                },
            )
        }

        if (pagerScreenState.showShortcutConfigScreen) {
            ShortcutConfigScreen(
                // Configuration
                eblanShortcutConfigs = eblanShortcutConfigs,
                gridItemSettings = homeSettings.gridItemSettings,
                // Layout
                paddingValues = paddingValues,
                screenHeight = screenHeight,
                // State
                swipeY = pagerScreenState.shortcutConfigScreenSwipeY.value,
                alpha = pagerScreenState.shortcutConfigScreenAlpha,
                isVisibleOverlay = isVisibleOverlay,
                drag = pagerScreenState.drag,
                // Callbacks
                onDismiss = pagerScreenState::dismissShortcutConfigScreen,
                onGetEblanShortcutConfigsByLabel = onGetEblanShortcutConfigsByLabel,
                onVerticalDrag = pagerScreenState::verticalDragShortcutConfigScreen,
                onDragEnd = pagerScreenState::handleOnDragEndShortcutConfigScreen,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onDragShortcutConfig = { gridItem, imageBitmap, intOffset, intSize, sharedElementKey ->
                    pagerScreenState.dragShortcutConfig(
                        // Drag item details
                        gridItem = gridItem,
                        imageBitmap = imageBitmap,
                        intOffset = intOffset,
                        intSize = intSize,
                        newSharedElementKey = sharedElementKey,
                        // Callbacks
                        onUpdateGridItemSource = onUpdateGridItemSource,
                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                    )
                },
            )
        }

        if (isResizing) {
            ResizeScreen(
                // Layout
                paddingValues = paddingValues,
                // Content
                dockHeightPx = dockHeightPx,
                pageIndicatorHeightPx = pageIndicatorHeightPx,
                columns = homeSettings.columns,
                rows = homeSettings.rows,
                gridItemTextColor = homeSettings.gridItemSettings.textColor,
                gridItemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
                gridItemBackgroundColor = homeSettings.gridItemSettings.backgroundColor,
                gridItemCustomBackgroundColor = homeSettings.gridItemSettings.customBackgroundColor,
                dockColumns = homeSettings.dockColumns,
                dockRows = homeSettings.dockRows,
                dockGridItemTextColor = homeSettings.dockGridItemSettings.textColor,
                dockGridItemCustomTextColor = homeSettings.dockGridItemSettings.customTextColor,
                dockGridItemBackgroundColor = homeSettings.dockGridItemSettings.backgroundColor,
                dockGridItemCustomBackgroundColor = homeSettings.dockGridItemSettings.customBackgroundColor,
                resizeGridItem = resizeGridItem,
                // Appearance
                textColor = textColor,
                lockMovement = lockMovement,
                // Callbacks
                onResizeCancel = onResizeCancel,
                onResizeEnd = onResizeEnd,
                onResizeGridItem = onResizeGridItem,
                onUpdateIsResizing = pagerScreenState::updateIsResizing,
            )
        }

        if (isVisibleFolderEblanApplicationInfos) {
            folderEblanApplicationInfoEntries.forEach { currentFolderEntry ->
                FolderApplicationScreen(
                    // Core State and Layout
                    sharedTransitionScope = this,
                    folderEntry = currentFolderEntry,
                    paddingValues = paddingValues,
                    safeDrawingHeight = safeDrawingHeight,
                    safeDrawingWidth = safeDrawingWidth,
                    isVisibleOverlay = isVisibleOverlay,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    // Folder Content and Appearance
                    folderEblanApplicationInfoEntries = folderEblanApplicationInfoEntries,
                    animations = gridItemAnimation,
                    systemTextColor = textColor,
                    systemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
                    folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                    folderBackgroundColor = folderSettings.folderBackgroundColor,
                    customFolderBackgroundColor = folderSettings.customFolderBackgroundColor,
                    folderBorderColor = folderSettings.folderBorderColor,
                    customFolderBorderColor = folderSettings.customFolderBorderColor,
                    folderBorderWidth = folderSettings.borderWidth,
                    gridItemSettings = appDrawerSettings.gridItemSettings,
                    folderCornerRadius = folderSettings.folderCornerRadius,
                    folderCellWidth = folderSettings.folderCellWidth,
                    folderCellHeight = folderSettings.folderCellHeight,
                    iconPackFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackFilePaths,
                    // Drag and Drop State
                    moveFolderEblanApplicationInfoGridItemResult = moveFolderEblanApplicationInfoGridItemResult,
                    drag = pagerScreenState.drag,
                    isDragging = pagerScreenState.isDragging,
                    lockMovement = lockMovement,
                    dragIntOffset = pagerScreenState.dragIntOffset,
                    showFolderEblanApplicationInfoGridItemMenu = pagerScreenState.showFolderEblanApplicationInfoGridItemMenu,
                    // Actions and Callbacks
                    onUpsertFolderEblanApplicationInfoEntry = onUpsertFolderEblanApplicationInfoEntry,
                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                    onMoveFolderEblanApplicationInfoGridItem = onMoveFolderEblanApplicationInfoGridItem,
                    onDismissFolderEblanApplicationGridItemPopup = pagerScreenState::dismissFolderEblanApplicationInfoGridItemMenu,
                    onResetGrid = onResetGrid,
                    onResetGridAfterMoveFolderEblanApplicationInfo = onDragEndAfterMoveFolderEblanApplicationInfo,
                    onUpdateIsDragging = pagerScreenState::updateIsDragging,
                    onMoveFolderEblanApplicationInfoGridItemOutsideFolder = { folderId, isFolder, movingGridItem ->
                        pagerScreenState.moveFolderEblanApplicationInfoGridItemOutsideFolder(
                            // Grid Items
                            folderId = folderId,
                            isFolder = isFolder,
                            movingGridItem = movingGridItem,
                            // Actions
                            onMoveFolderEblanApplicationInfoGridItemOutsideFolder = onMoveFolderEblanApplicationInfoGridItemOutsideFolder,
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
                            onUpdateMoveFolderEblanApplicationInfoGridItemResult = onUpdateMoveFolderEblanApplicationInfoGridItemResult,
                            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                        )
                    },
                    onDragFolderEblanApplicationInfoGridItem = pagerScreenState::dragFolderEblanApplicationInfoGridItem,
                    onCloseFolder = { folderEntry, isFirstFolderGridItem ->
                        pagerScreenState.closeFolderEblanApplicationInfo(
                            // Folder Information
                            folderEntry = folderEntry,
                            isFirstFolderEblanApplicationInfo = isFirstFolderGridItem,
                            // Callbacks
                            onDeleteFolderEntry = onDeleteFolderEblanApplicationInfoEntry,
                        )
                    },
                    onDismiss = {
                        onCloseFolderEblanApplicationInfoEntries()

                        pagerScreenState.dismissApplicationScreen()
                    },
                    onTapFolderEblanApplicationInfoItem = { newFolderEntry ->
                        pagerScreenState.tapFolderEblanApplicationInfoGridItem(
                            folderEntry = newFolderEntry,
                            onUpsertFolderEntry = onUpsertFolderEblanApplicationInfoEntry,
                        )
                    },
                )
            }
        }

        val selectedFolderEblanApplicationInfo = pagerScreenState.selectedFolderEblanApplicationInfo

        if (pagerScreenState.showFolderEblanApplicationInfoMenu && selectedFolderEblanApplicationInfo != null) {
            FolderApplicationInfoPopup(
                // Application Info
                folderEblanApplicationInfoId = selectedFolderEblanApplicationInfo.id,
                isCloseFolderEblanApplicationInfoMenu = pagerScreenState.isCloseFolderEblanApplicationInfoMenu,
                // Layout and Positioning
                paddingValues = paddingValues,
                popupIntOffset = pagerScreenState.menuIntOffset,
                popupIntSize = pagerScreenState.menuIntSize,
                // Actions
                onDismissRequest = pagerScreenState::dismissFolderEblanApplicationMenu,
                onEditFolderApplicationInfo = onEditFolderApplicationInfo,
                onDeleteFolderEblanApplicationInfoGridItems = onDeleteFolderEblanApplicationInfoGridItems,
            )
        }

        if (isVisibleFolderEblanApplicationInfoGridItemMenu) {
            val selectedFolderEblanApplicationInfoGridItem =
                pagerScreenState.selectedFolderEblanApplicationInfoGridItem
            val folderAppInfoData =
                selectedFolderEblanApplicationInfoGridItem?.data as? FolderEblanApplicationInfoGridItemData.ApplicationInfo

            FolderApplicationInfoGridItemPopup(
                // Folder Data
                folderEblanApplicationInfoGridItemId = selectedFolderEblanApplicationInfoGridItem?.id,
                folderEblanApplicationInfoGridItemData = selectedFolderEblanApplicationInfoGridItem?.data,
                hasAppWidgets = !eblanAppWidgetProviderInfosGroup[folderAppInfoData?.packageName].isNullOrEmpty(),
                eblanShortcutInfosByPackageName = folderAppInfoData?.let { data ->
                    eblanShortcutInfosGroup[
                        EblanShortcutInfoByGroup(
                            serialNumber = data.serialNumber,
                            packageName = data.packageName,
                        ),
                    ]
                },
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
                    onCloseFolderEblanApplicationInfoEntries()

                    pagerScreenState.dismissApplicationScreen()
                },
                onEditFolderApplicationInfo = onEditFolderApplicationInfo,
                onEditApplicationInfo = onEditApplicationInfo,
                onWidgets = pagerScreenState::openAppWidgetScreen,
                onDeleteFolderEblanApplicationInfoGridItems = onDeleteFolderEblanApplicationInfoGridItems,
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
                        onUpdateGridItemSource = onUpdateGridItemSource,
                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                        onResetFolderEblanApplicationInfoEntries = onResetFolderEblanApplicationInfoEntries,
                    )
                },
            )
        }

        val selectedEblanApplicationInfoGroup = pagerScreenState.eblanApplicationInfoGroup

        if (selectedEblanApplicationInfoGroup != null) {
            AppWidgetScreen(
                // Data
                eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfosGroup[selectedEblanApplicationInfoGroup.packageName].orEmpty(),
                icon = selectedEblanApplicationInfoGroup.icon,
                label = selectedEblanApplicationInfoGroup.label,
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
                isVisibleOverlay = isVisibleOverlay,
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
                        onUpdateGridItemSource = onUpdateGridItemSource,
                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                        onResetFolderGridItemEntries = onResetFolderGridItemEntries,
                        onResetFolderEblanApplicationInfoEntries = onResetFolderEblanApplicationInfoEntries,
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
            animations = gridItemAnimation,
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

    LaunchedEffect(isVisibleOverlay) {
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
                        rememberSharedContentState(sharedElementKey),
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
                            launcherAppsWrapper.unregisterCallback(launcherAppsCallback)

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

            launcherAppsWrapper.unregisterCallback(launcherAppsCallback)

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

    LaunchedEffect(gridHorizontalPagerState) {
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
                userManagerWrapper.getSerialNumberForUser(user),
                packageName,
            )
        }
    }

    override fun onPackageAdded(packageName: String?, user: UserHandle?) {
        if (packageName != null && user != null) {
            onPackageAdded(
                userManagerWrapper.getSerialNumberForUser(user),
                packageName,
            )
        }
    }

    override fun onPackageChanged(packageName: String?, user: UserHandle?) {
        if (packageName != null && user != null) {
            onPackageChanged(
                userManagerWrapper.getSerialNumberForUser(user),
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
            userManagerWrapper.getSerialNumberForUser(user),
            packageName,
        )
    }
}
