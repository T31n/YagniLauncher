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
package com.eblan.launcher.feature.home

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.domain.model.application.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.application.GetEblanApplicationInfosByLabelAndTag
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.folder.FolderPopup
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.MoveFolderEblanApplicationInfoGridItemResult
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.domain.model.home.PageItem
import com.eblan.launcher.domain.model.launcherapps.EblanUser
import com.eblan.launcher.domain.model.launcherapps.PinItemRequestType
import com.eblan.launcher.domain.model.shortcutconfig.EblanShortcutConfig
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfo
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfoByGroup
import com.eblan.launcher.domain.model.userdata.HomeData
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.domain.model.widget.EblanAppWidgetProviderInfo
import com.eblan.launcher.feature.home.dialog.TextDialog
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.screen.editpage.EditDockGridPageScreen
import com.eblan.launcher.feature.home.screen.editpage.EditGridPageScreen
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.feature.home.screen.pager.PagerScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@Composable
internal fun HomeRoute(
    // Configuration
    configureResultCode: Int?,
    // Modifier
    modifier: Modifier = Modifier,
    // Dependencies
    viewModel: HomeViewModel = hiltViewModel(),
    // Configuration actions
    onResetConfigureResultCode: () -> Unit,
    // Edit actions
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onEditGridItem: (String) -> Unit,
    // Navigation actions
    onSettings: () -> Unit,
    // Edit actions
    onEditFolderApplicationInfo: (String) -> Unit
) {
    val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val screen by viewModel.screen.collectAsStateWithLifecycle()
    val movedGridItemResult by viewModel.movedGridItemResult.collectAsStateWithLifecycle()
    val pageItems by viewModel.pageItems.collectAsStateWithLifecycle()
    val pinGridItem by viewModel.pinGridItem.collectAsStateWithLifecycle()
    val getEblanApplicationInfos by viewModel.getEblanApplicationInfosByLabelAndTag.collectAsStateWithLifecycle()
    val eblanShortcutConfigs by viewModel.eblanShortcutConfigs.collectAsStateWithLifecycle()
    val eblanAppWidgetProviderInfos by viewModel.eblanAppWidgetProviderInfos.collectAsStateWithLifecycle()
    val eblanShortcutInfosGroup by viewModel.eblanShortcutInfosGroup.collectAsStateWithLifecycle()
    val eblanAppWidgetProviderInfosGroup by viewModel.eblanAppWidgetProviderInfosGroup.collectAsStateWithLifecycle()
    val eblanApplicationInfoTags by viewModel.eblanApplicationInfoTags.collectAsStateWithLifecycle()
    val folderGridItemEntries by viewModel.folderGridItemEntries.collectAsStateWithLifecycle()
    val resizeGridItem by viewModel.resizeGridItem.collectAsStateWithLifecycle()
    val gridItemSource by viewModel.gridItemSource.collectAsStateWithLifecycle()
    val isVisibleOverlay by viewModel.isVisibleOverlay.collectAsStateWithLifecycle()
    val textColor by viewModel.textColor.collectAsStateWithLifecycle()
    val folderPopups by viewModel.previewFolderGridItems.collectAsStateWithLifecycle()
    val folderEblanApplicationInfoPopups by viewModel.folderEblanApplicationInfoPopups.collectAsStateWithLifecycle()
    val folderEblanApplicationInfoEntries by viewModel.folderEblanApplicationInfoEntries.collectAsStateWithLifecycle()
    val moveFolderEblanApplicationInfoGridItemResult by viewModel.moveFolderEblanApplicationInfoGridItemResult.collectAsStateWithLifecycle()

    HomeScreen(
        homeUiState = homeUiState,
        screen = screen,
        textColor = textColor,
        isVisibleOverlay = isVisibleOverlay,
        configureResultCode = configureResultCode,
        pageItems = pageItems,
        gridItemSource = gridItemSource,
        pinGridItem = pinGridItem,
        resizeGridItem = resizeGridItem,
        movedGridItemResult = movedGridItemResult,
        folderGridItemEntries = folderGridItemEntries,
        folderPopups = folderPopups,
        folderEblanApplicationInfoEntries = folderEblanApplicationInfoEntries,
        folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
        moveFolderEblanApplicationInfoGridItemResult = moveFolderEblanApplicationInfoGridItemResult,
        eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
        eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
        eblanApplicationInfoTags = eblanApplicationInfoTags,
        eblanShortcutConfigs = eblanShortcutConfigs,
        eblanShortcutInfosGroup = eblanShortcutInfosGroup,
        getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfos,
        modifier = modifier,
        onUpdateScreen = viewModel::updateScreen,
        onSettings = onSettings,
        onStartSyncData = viewModel::startSyncData,
        onStopSyncData = viewModel::stopSyncData,
        onResetConfigureResultCode = onResetConfigureResultCode,
        onPackageAdded = viewModel::packageAdded,
        onPackageChanged = viewModel::packageChanged,
        onPackageRemoved = viewModel::packageRemove,
        onShortcutsChanged = viewModel::shortcutsChanged,
        onEditPage = viewModel::showPageCache,
        onSaveEditPage = viewModel::saveEditPage,
        onGetEblanAppWidgetProviderInfosByLabel = viewModel::getEblanAppWidgetProviderInfosByLabel,
        onGetEblanApplicationInfosByLabel = viewModel::getEblanApplicationInfosByLabel,
        onGetEblanApplicationInfosByTagId = viewModel::getEblanApplicationInfosByTagId,
        onGetEblanShortcutConfigsByLabel = viewModel::getEblanShortcutConfigsByLabel,
        onEditApplicationInfo = onEditApplicationInfo,
        onResetGrid = viewModel::resetGrid,
        onDeleteGridItem = viewModel::deleteGridItem,
        onResetGridAfterDeleteGridItem = viewModel::resetGridAfterDeleteGridItem,
        onEditGridItem = onEditGridItem,
        onGetPinGridItem = viewModel::getPinGridItem,
        onResetPinGridItem = viewModel::resetPinGridItem,
        onMoveGridItem = viewModel::moveGridItem,
        onUpdateMoveGridItemResult = viewModel::updateMoveGridItemResult,
        onUpdateGridItemsAfterMove = viewModel::updateGridItemsAfterMove,
        onResizeGridItem = viewModel::resizeGridItem,
        onUpdateResizeGridItem = viewModel::updateResizeGridItem,
        onResetGridAfterResize = viewModel::resetGridAfterResize,
        onUpdateGridItemSource = viewModel::updateGridItemSource,
        onUpdateIsVisibleOverlay = viewModel::updateIsVisibleOverlay,
        onUpdateShortcutConfigIntoShortcutInfoGridItem = viewModel::updateShortcutConfigIntoShortcutInfoGridItem,
        onMoveFolderGridItem = viewModel::moveFolderGridItem,
        onMoveFolderGridItemOutsideFolder = viewModel::moveFolderGridItemOutsideFolder,
        onResetGridAfterMoveFolder = viewModel::resetGridAfterMoveFolder,
        onUpsertFolderGridItemPopupEntry = viewModel::upsertFolderGridItemEntry,
        onDeleteFolderPopupEntry = viewModel::deleteFolderGridItemEntry,
        onResetFolderGridItemPopupEntries = viewModel::resetFolderGridItemEntries,
        onShowFolderWhenDragging = viewModel::showFolderWhenDragging,
        onUpsertFolderEblanApplicationInfoPopupEntry = viewModel::upsertFolderEblanApplicationInfoEntry,
        onDeleteFolderEblanApplicationInfoPopupEntry = viewModel::deleteFolderEblanApplicationInfoEntry,
        onEditFolderApplicationInfo = onEditFolderApplicationInfo,
        onUpdateMoveFolderEblanApplicationInfoGridItemResult = viewModel::updateMoveFolderEblanApplicationInfoGridItemResult,
        onMoveFolderEblanApplicationInfoGridItem = viewModel::moveFolderEblanApplicationInfoGridItem,
        onResetGridAfterMoveFolderEblanApplicationInfo = viewModel::resetGridAfterMoveFolderEblanApplicationInfo,
        onMoveFolderEblanApplicationInfoGridItemOutsideFolder = viewModel::moveFolderEblanApplicationInfoGridItemOutsideFolder,
        onDragFolderEblanApplicationInfo = viewModel::dragFolderEblanApplicationInfoToGrid,
        onResetFolderEblanApplicationInfoPopupEntries = viewModel::resetFolderEblanApplicationInfoEntries,
        onDeleteFolderEblanApplicationInfoGridItems = viewModel::deleteFolderEblanApplicationInfoGridItems,
        onUpdateGridItemsAfterMoveNewFolder = viewModel::updateGridItemsAfterMoveNewFolder
    )
}

@Composable
internal fun HomeScreen(
    // Screen and UI state
    homeUiState: HomeUiState,
    screen: Screen,
    textColor: TextColor,
    isVisibleOverlay: Boolean,
    configureResultCode: Int?,
    // Page and grid state
    pageItems: List<PageItem>?,
    gridItemSource: GridItemSource?,
    pinGridItem: GridItem?,
    resizeGridItem: GridItem?,
    movedGridItemResult: MoveGridItemResult?,
    // Folder state
    folderGridItemEntries: List<FolderEntry>,
    folderPopups: Map<String, FolderPopup>,
    folderEblanApplicationInfoEntries: List<FolderEntry>,
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    moveFolderEblanApplicationInfoGridItemResult: MoveFolderEblanApplicationInfoGridItemResult?,
    // Application and widget data
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    eblanShortcutConfigs: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    // Modifier
    modifier: Modifier = Modifier,
    // System and screen callbacks
    onUpdateScreen: (Screen) -> Unit,
    onSettings: () -> Unit,
    onStartSyncData: () -> Unit,
    onStopSyncData: () -> Unit,
    onResetConfigureResultCode: () -> Unit,
    onPackageAdded: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    onPackageChanged: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    onPackageRemoved: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    onShortcutsChanged: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    // Page and data query callbacks
    onEditPage: (
        gridItems: List<GridItem>,
        associate: Associate,
    ) -> Unit,
    onSaveEditPage: (
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
        associate: Associate,
    ) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByTagId: (Long?) -> Unit,
    onGetEblanShortcutConfigsByLabel: (String) -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    // Grid item callbacks
    onResetGrid: () -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onResetGridAfterDeleteGridItem: (GridItem) -> Unit,
    onEditGridItem: (String) -> Unit,
    onGetPinGridItem: (PinItemRequestType) -> Unit,
    onResetPinGridItem: () -> Unit,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
    onUpdateGridItemsAfterMove: (MoveGridItemResult) -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
    ) -> Unit,
    onUpdateResizeGridItem: (GridItem) -> Unit,
    onResetGridAfterResize: () -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateShortcutConfigIntoShortcutInfoGridItem: (
        moveGridItemResult: MoveGridItemResult,
        pinItemRequestType: PinItemRequestType.ShortcutInfo,
    ) -> Unit,
    // Folder grid item callbacks
    onMoveFolderGridItem: (
        folderPopup: FolderPopup,
        movingGridItem: GridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onMoveFolderGridItemOutsideFolder: (GridItem) -> Unit,
    onResetGridAfterMoveFolder: () -> Unit,
    onUpsertFolderGridItemPopupEntry: (FolderEntry) -> Unit,
    onDeleteFolderPopupEntry: (FolderEntry) -> Unit,
    onResetFolderGridItemPopupEntries: () -> Unit,
    onShowFolderWhenDragging: (
        folderEntry: FolderEntry,
        movingGridItem: GridItem,
    ) -> Unit,
    // Folder application info callbacks
    onUpsertFolderEblanApplicationInfoPopupEntry: (FolderEntry) -> Unit,
    onDeleteFolderEblanApplicationInfoPopupEntry: (FolderEntry) -> Unit,
    onEditFolderApplicationInfo: (String) -> Unit,
    onUpdateMoveFolderEblanApplicationInfoGridItemResult: (MoveFolderEblanApplicationInfoGridItemResult) -> Unit,
    onMoveFolderEblanApplicationInfoGridItem: (
        folderEblanApplicationInfoPopup: FolderEblanApplicationInfoPopup,
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onResetGridAfterMoveFolderEblanApplicationInfo: () -> Unit,
    onMoveFolderEblanApplicationInfoGridItemOutsideFolder: (
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        movingGridItem: GridItem,
    ) -> Unit,
    onDragFolderEblanApplicationInfo: (
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        movingGridItem: GridItem,
    ) -> Unit,
    onResetFolderEblanApplicationInfoPopupEntries: () -> Unit,
    onDeleteFolderEblanApplicationInfoGridItems: (
        icon: String?,
        folderId: String,
    ) -> Unit,
    // Grid item callbacks
    onUpdateGridItemsAfterMoveNewFolder: (
        folderGridItems: List<GridItem>,
        moveGridItemResult: MoveGridItemResult,
    ) -> Unit
) {
    val paddingValues = WindowInsets.safeDrawing.asPaddingValues()

    var screenIntSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { intSize ->
                screenIntSize = intSize
            },
    ) {
        if (homeUiState is HomeUiState.Success && screenIntSize != IntSize.Zero) {
            Success(
                homeData = homeUiState.homeData,
                screen = screen,
                screenWidth = screenIntSize.width,
                screenHeight = screenIntSize.height,
                paddingValues = paddingValues,
                textColor = textColor,
                isVisibleOverlay = isVisibleOverlay,
                configureResultCode = configureResultCode,
                pageItems = pageItems,
                eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                eblanApplicationInfoTags = eblanApplicationInfoTags,
                eblanShortcutConfigs = eblanShortcutConfigs,
                eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
                folderGridItemEntries = folderGridItemEntries,
                folderEblanApplicationInfoEntries = folderEblanApplicationInfoEntries,
                folderPopups = folderPopups,
                folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                pinGridItem = pinGridItem,
                resizeGridItem = resizeGridItem,
                gridItemSource = gridItemSource,
                movedGridItemResult = movedGridItemResult,
                moveFolderEblanApplicationInfoGridItemResult = moveFolderEblanApplicationInfoGridItemResult,
                onUpdateScreen = onUpdateScreen,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onResetConfigureResultCode = onResetConfigureResultCode,
                onSettings = onSettings,
                onStartSyncData = onStartSyncData,
                onStopSyncData = onStopSyncData,
                onResetGrid = onResetGrid,
                onEditPage = onEditPage,
                onSaveEditPage = onSaveEditPage,
                onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                onGetEblanApplicationInfosByTagId = onGetEblanApplicationInfosByTagId,
                onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
                onGetEblanShortcutConfigsByLabel = onGetEblanShortcutConfigsByLabel,
                onEditApplicationInfo = onEditApplicationInfo,
                onEditGridItem = onEditGridItem,
                onDeleteGridItem = onDeleteGridItem,
                onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
                onMoveGridItem = onMoveGridItem,
                onUpdateGridItemsAfterMove = onUpdateGridItemsAfterMove,
                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                onGetPinGridItem = onGetPinGridItem,
                onResetPinGridItem = onResetPinGridItem,
                onUpdateShortcutConfigIntoShortcutInfoGridItem = onUpdateShortcutConfigIntoShortcutInfoGridItem,
                onResizeGridItem = onResizeGridItem,
                onUpdateResizeGridItem = onUpdateResizeGridItem,
                onResetGridAfterResize = onResetGridAfterResize,
                onUpdateGridItemSource = onUpdateGridItemSource,
                onUpsertFolderGridItemPopupEntry = onUpsertFolderGridItemPopupEntry,
                onDeleteFolderPopupEntry = onDeleteFolderPopupEntry,
                onResetFolderGridItemPopupEntries = onResetFolderGridItemPopupEntries,
                onShowFolderWhenDragging = onShowFolderWhenDragging,
                onMoveFolderGridItem = onMoveFolderGridItem,
                onMoveFolderGridItemOutsideFolder = onMoveFolderGridItemOutsideFolder,
                onResetGridAfterMoveFolder = onResetGridAfterMoveFolder,
                onUpsertFolderEblanApplicationInfoPopupEntry = onUpsertFolderEblanApplicationInfoPopupEntry,
                onDeleteFolderEblanApplicationInfoPopupEntry = onDeleteFolderEblanApplicationInfoPopupEntry,
                onResetFolderEblanApplicationInfoPopupEntries = onResetFolderEblanApplicationInfoPopupEntries,
                onEditFolderApplicationInfo = onEditFolderApplicationInfo,
                onUpdateMoveFolderEblanApplicationInfoGridItemResult = onUpdateMoveFolderEblanApplicationInfoGridItemResult,
                onMoveFolderEblanApplicationInfoGridItem = onMoveFolderEblanApplicationInfoGridItem,
                onResetGridAfterMoveFolderEblanApplicationInfo = onResetGridAfterMoveFolderEblanApplicationInfo,
                onMoveFolderEblanApplicationInfoGridItemOutsideFolder = onMoveFolderEblanApplicationInfoGridItemOutsideFolder,
                onDragFolderEblanApplicationInfo = onDragFolderEblanApplicationInfo,
                onDeleteFolderEblanApplicationInfoGridItems = onDeleteFolderEblanApplicationInfoGridItems,
                onPackageAdded = onPackageAdded,
                onPackageChanged = onPackageChanged,
                onPackageRemoved = onPackageRemoved,
                onShortcutsChanged = onShortcutsChanged,
                onUpdateGridItemsAfterMoveNewFolder = onUpdateGridItemsAfterMoveNewFolder
            )
        }
    }
}

@Composable
private fun Success(
    // State
    homeData: HomeData,
    screen: Screen,
    screenWidth: Int,
    screenHeight: Int,
    paddingValues: PaddingValues,
    textColor: TextColor,
    isVisibleOverlay: Boolean,
    configureResultCode: Int?,
    // Data
    pageItems: List<PageItem>?,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    eblanShortcutConfigs: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    folderGridItemEntries: List<FolderEntry>,
    folderEblanApplicationInfoEntries: List<FolderEntry>,
    folderPopups: Map<String, FolderPopup>,
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    pinGridItem: GridItem?,
    resizeGridItem: GridItem?,
    gridItemSource: GridItemSource?,
    movedGridItemResult: MoveGridItemResult?,
    moveFolderEblanApplicationInfoGridItemResult: MoveFolderEblanApplicationInfoGridItemResult?,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onUpdateScreen: (Screen) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onResetConfigureResultCode: () -> Unit,
    onSettings: () -> Unit,
    onStartSyncData: () -> Unit,
    onStopSyncData: () -> Unit,
    onResetGrid: () -> Unit,
    onEditPage: (
        gridItems: List<GridItem>,
        associate: Associate,
    ) -> Unit,
    onSaveEditPage: (
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
        associate: Associate,
    ) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByTagId: (Long?) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onGetEblanShortcutConfigsByLabel: (String) -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onEditGridItem: (String) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onResetGridAfterDeleteGridItem: (GridItem) -> Unit,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onUpdateGridItemsAfterMove: (MoveGridItemResult) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
    onGetPinGridItem: (PinItemRequestType) -> Unit,
    onResetPinGridItem: () -> Unit,
    onUpdateShortcutConfigIntoShortcutInfoGridItem: (
        moveGridItemResult: MoveGridItemResult,
        pinItemRequestType: PinItemRequestType.ShortcutInfo,
    ) -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
    ) -> Unit,
    onUpdateResizeGridItem: (GridItem) -> Unit,
    onResetGridAfterResize: () -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpsertFolderGridItemPopupEntry: (FolderEntry) -> Unit,
    onDeleteFolderPopupEntry: (FolderEntry) -> Unit,
    onResetFolderGridItemPopupEntries: () -> Unit,
    onShowFolderWhenDragging: (
        folderEntry: FolderEntry,
        movingGridItem: GridItem,
    ) -> Unit,
    onMoveFolderGridItem: (
        folderPopup: FolderPopup,
        movingGridItem: GridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onMoveFolderGridItemOutsideFolder: (GridItem) -> Unit,
    onResetGridAfterMoveFolder: () -> Unit,
    onUpsertFolderEblanApplicationInfoPopupEntry: (FolderEntry) -> Unit,
    onDeleteFolderEblanApplicationInfoPopupEntry: (FolderEntry) -> Unit,
    onResetFolderEblanApplicationInfoPopupEntries: () -> Unit,
    onEditFolderApplicationInfo: (String) -> Unit,
    onUpdateMoveFolderEblanApplicationInfoGridItemResult: (MoveFolderEblanApplicationInfoGridItemResult) -> Unit,
    onMoveFolderEblanApplicationInfoGridItem: (
        folderEblanApplicationInfoPopup: FolderEblanApplicationInfoPopup,
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onResetGridAfterMoveFolderEblanApplicationInfo: () -> Unit,
    onMoveFolderEblanApplicationInfoGridItemOutsideFolder: (
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        movingGridItem: GridItem,
    ) -> Unit,
    onDragFolderEblanApplicationInfo: (
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        movingGridItem: GridItem,
    ) -> Unit,
    onDeleteFolderEblanApplicationInfoGridItems: (
        icon: String?,
        folderId: String,
    ) -> Unit,
    onPackageAdded: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    onPackageChanged: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    onPackageRemoved: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    onShortcutsChanged: (
        serialNumber: Long,
        packageName: String,
    ) -> Unit,
    onUpdateGridItemsAfterMoveNewFolder: (
        folderGridItems: List<GridItem>,
        moveGridItemResult: MoveGridItemResult,
    ) -> Unit
) {
    AnimatedContent(
        modifier = modifier,
        targetState = screen,
    ) { targetState ->
        when (targetState) {
            Screen.Pager -> {
                PagerScreen(
                    appDrawerSettings = homeData.userData.appDrawerSettings,
                    configureResultCode = configureResultCode,
                    dockGridItemsByPage = homeData.dockGridItemsByPage,
                    eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                    eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                    eblanApplicationInfoTags = eblanApplicationInfoTags,
                    eblanShortcutConfigs = eblanShortcutConfigs,
                    eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                    experimentalSettings = homeData.userData.experimentalSettings,
                    folderGridItemEntries = folderGridItemEntries,
                    folderEblanApplicationInfoEntries = folderEblanApplicationInfoEntries,
                    gestureSettings = homeData.userData.gestureSettings,
                    getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
                    gridItems = homeData.gridItems,
                    gridItemsByPage = homeData.gridItemsByPage,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    hasSystemFeatureAppWidgets = homeData.hasSystemFeatureAppWidgets,
                    homeSettings = homeData.userData.homeSettings,
                    folderSettings = homeData.userData.folderSettings,
                    moveGridItemResult = movedGridItemResult,
                    paddingValues = paddingValues,
                    pinGridItem = pinGridItem,
                    screenHeight = screenHeight,
                    screenWidth = screenWidth,
                    textColor = textColor,
                    resizeGridItem = resizeGridItem,
                    gridItemSource = gridItemSource,
                    isVisibleOverlay = isVisibleOverlay,
                    folderPopups = folderPopups,
                    iconPackFilePaths = homeData.iconPackFilePaths,
                    folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                    moveFolderEblanApplicationInfoGridItemResult = moveFolderEblanApplicationInfoGridItemResult,
                    onDeleteGridItem = onDeleteGridItem,
                    onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
                    onUpdateGridItemsAfterMove = onUpdateGridItemsAfterMove,
                    onResetGridAfterMoveFolder = onResetGridAfterMoveFolder,
                    onEditApplicationInfo = onEditApplicationInfo,
                    onEditGridItem = onEditGridItem,
                    onEditPage = onEditPage,
                    onGetEblanAppWidgetProviderInfosByLabel = onGetEblanAppWidgetProviderInfosByLabel,
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                    onGetEblanApplicationInfosByTagId = onGetEblanApplicationInfosByTagId,
                    onGetEblanShortcutConfigsByLabel = onGetEblanShortcutConfigsByLabel,
                    onGetPinGridItem = onGetPinGridItem,
                    onMoveFolderGridItem = onMoveFolderGridItem,
                    onMoveFolderGridItemOutsideFolder = onMoveFolderGridItemOutsideFolder,
                    onMoveGridItem = onMoveGridItem,
                    onResetConfigureResultCode = onResetConfigureResultCode,
                    onResetPinGridItem = onResetPinGridItem,
                    onResizeCancel = onResetGrid,
                    onResizeEnd = onResetGridAfterResize,
                    onResizeGridItem = onResizeGridItem,
                    onSettings = onSettings,
                    onStartSyncData = onStartSyncData,
                    onStopSyncData = onStopSyncData,
                    onUpsertFolderGridItemPopupEntry = onUpsertFolderGridItemPopupEntry,
                    onDeleteFolderGridItemPopupEntry = onDeleteFolderPopupEntry,
                    onUpsertFolderEblanApplicationInfoPopupEntry = onUpsertFolderEblanApplicationInfoPopupEntry,
                    onDeleteFolderEblanApplicationInfoPopupEntry = onDeleteFolderEblanApplicationInfoPopupEntry,
                    onShowFolderWhenDragging = onShowFolderWhenDragging,
                    onUpdateShortcutConfigIntoShortcutInfoGridItem = onUpdateShortcutConfigIntoShortcutInfoGridItem,
                    onUpdateGridItemSource = onUpdateGridItemSource,
                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                    onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                    onUpdateResizeGridItem = onUpdateResizeGridItem,
                    onResetGrid = onResetGrid,
                    onPackageRemoved = onPackageRemoved,
                    onPackageAdded = onPackageAdded,
                    onPackageChanged = onPackageChanged,
                    onShortcutsChanged = onShortcutsChanged,
                    onResetFolderGridItemPopupEntries = onResetFolderGridItemPopupEntries,
                    onEditFolderApplicationInfo = onEditFolderApplicationInfo,
                    onUpdateMoveFolderEblanApplicationInfoGridItemResult = onUpdateMoveFolderEblanApplicationInfoGridItemResult,
                    onMoveFolderEblanApplicationInfoGridItem = onMoveFolderEblanApplicationInfoGridItem,
                    onDragEndAfterMoveFolderEblanApplicationInfo = onResetGridAfterMoveFolderEblanApplicationInfo,
                    onMoveFolderEblanApplicationInfoGridItemOutsideFolder = onMoveFolderEblanApplicationInfoGridItemOutsideFolder,
                    onDragFolderEblanApplicationInfo = onDragFolderEblanApplicationInfo,
                    onResetFolderEblanApplicationInfoPopupEntries = onResetFolderEblanApplicationInfoPopupEntries,
                    onDeleteFolderEblanApplicationInfoGridItems = onDeleteFolderEblanApplicationInfoGridItems,
                    onUpdateGridItemsAfterMoveNewFolder = onUpdateGridItemsAfterMoveNewFolder
                )
            }

            Screen.Loading -> LoadingScreen()

            Screen.EditGridPage -> {
                EditGridPageScreen(
                    pageItems = pageItems,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    homeSettings = homeData.userData.homeSettings,
                    paddingValues = paddingValues,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    textColor = textColor,
                    folderPopupGridItems = folderPopups,
                    iconPackFilePaths = homeData.iconPackFilePaths,
                    folderBackgroundColor = homeData.userData.folderSettings.folderBackgroundColor,
                    customFolderBackgroundColor = homeData.userData.folderSettings.customFolderBackgroundColor,
                    systemTextColor = textColor,
                    systemCustomTextColor = homeData.userData.homeSettings.gridItemSettings.customTextColor,
                    folderSettings = homeData.userData.folderSettings,
                    onSaveEditPage = onSaveEditPage,
                    onUpdateScreen = onUpdateScreen
                )
            }

            Screen.EditDockGridPage -> {
                EditDockGridPageScreen(
                    pageItems = pageItems,
                    folderPopupGridItems = folderPopups,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    homeSettings = homeData.userData.homeSettings,
                    folderSettings = homeData.userData.folderSettings,
                    textColor = textColor,
                    systemTextColor = textColor,
                    systemCustomTextColor = homeData.userData.homeSettings.gridItemSettings.customTextColor,
                    folderBackgroundColor = homeData.userData.folderSettings.folderBackgroundColor,
                    customFolderBackgroundColor = homeData.userData.folderSettings.customFolderBackgroundColor,
                    iconPackFilePaths = homeData.iconPackFilePaths,
                    paddingValues = paddingValues,
                    onSaveEditPage = onSaveEditPage,
                    onUpdateScreen = onUpdateScreen
                )
            }
        }
    }

    RequestPermissionsEffect()
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RequestPermissionsEffect(modifier: Modifier = Modifier) {
    val notificationsPermissionState =
        rememberMultiplePermissionsState(
            permissions = buildList {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
                add(Manifest.permission.CALL_PHONE)
            },
        )

    var showTextDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = notificationsPermissionState) {
        if (notificationsPermissionState.shouldShowRationale) {
            showTextDialog = true
        } else {
            notificationsPermissionState.launchMultiplePermissionRequest()
        }
    }

    if (showTextDialog) {
        TextDialog(
            title = stringResource(R.string.request_permissions),
            text = stringResource(R.string.allow_permissions_so_we_can_inform_you_about_important_crash_reports_and_make_phone_shortcuts),
            modifier = modifier,
            onDismissRequest = {
                showTextDialog = false
            },
            onClick = {
                notificationsPermissionState.launchMultiplePermissionRequest()

                showTextDialog = false
            }
        )
    }
}
