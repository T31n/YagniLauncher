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
import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.model.application.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.application.GetEblanApplicationInfosByLabelAndTag
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.folder.FolderPopupEntry
import com.eblan.launcher.domain.model.folder.PreviewFolder
import com.eblan.launcher.domain.model.folder.PreviewFolderEblanApplicationInfo
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.FolderGridItemPopup
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
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
    modifier: Modifier = Modifier,
    configureResultCode: Int?,
    viewModel: HomeViewModel = hiltViewModel(),
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onEditGridItem: (String) -> Unit,
    onResetConfigureResultCode: () -> Unit,
    onSettings: () -> Unit,
    onEditFolderApplicationInfo: (String) -> Unit,
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

    val folderGridItemPopups by viewModel.folderGridItemPopups.collectAsStateWithLifecycle()

    val resizeGridItem by viewModel.resizeGridItem.collectAsStateWithLifecycle()

    val gridItemSource by viewModel.gridItemSource.collectAsStateWithLifecycle()

    val isVisibleOverlay by viewModel.isVisibleOverlay.collectAsStateWithLifecycle()

    val textColor by viewModel.textColor.collectAsStateWithLifecycle()

    val previewFolderGridItems by viewModel.previewFolderGridItems.collectAsStateWithLifecycle()

    val previewFolderEblanApplicationInfos by viewModel.previewFolderEblanApplicationInfos.collectAsStateWithLifecycle()

    val folderEblanApplicationInfoPopups by viewModel.folderEblanApplicationInfoPopups.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        configureResultCode = configureResultCode,
        eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
        eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
        eblanApplicationInfoTags = eblanApplicationInfoTags,
        eblanShortcutConfigs = eblanShortcutConfigs,
        eblanShortcutInfosGroup = eblanShortcutInfosGroup,
        pageItems = pageItems,
        folderGridItemPopups = folderGridItemPopups,
        getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfos,
        homeUiState = homeUiState,
        movedGridItemResult = movedGridItemResult,
        pinGridItem = pinGridItem,
        screen = screen,
        resizeGridItem = resizeGridItem,
        gridItemSource = gridItemSource,
        isVisibleOverlay = isVisibleOverlay,
        textColor = textColor,
        previewFolderGridItems = previewFolderGridItems,
        previewFolderEblanApplicationInfos = previewFolderEblanApplicationInfos,
        folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
        onResetGrid = viewModel::resetGrid,
        onDeleteGridItem = viewModel::deleteGridItem,
        onResetGridAfterDeleteGridItem = viewModel::resetGridAfterDeleteGridItem,
        onEditApplicationInfo = onEditApplicationInfo,
        onEditGridItem = onEditGridItem,
        onEditPage = viewModel::showPageCache,
        onGetEblanAppWidgetProviderInfosByLabel = viewModel::getEblanAppWidgetProviderInfosByLabel,
        onGetEblanApplicationInfosByLabel = viewModel::getEblanApplicationInfosByLabel,
        onGetEblanApplicationInfosByTagId = viewModel::getEblanApplicationInfosByTagId,
        onGetEblanShortcutConfigsByLabel = viewModel::getEblanShortcutConfigsByLabel,
        onGetPinGridItem = viewModel::getPinGridItem,
        onMoveFolderGridItem = viewModel::moveFolderGridItem,
        onMoveFolderGridItemOutsideFolder = viewModel::moveFolderGridItemOutsideFolder,
        onMoveGridItem = viewModel::moveGridItem,
        onResetConfigureResultCode = onResetConfigureResultCode,
        onUpdateGridItemsAfterMove = viewModel::updateGridItemsAfterMove,
        onUpdateGridItemsAfterMoveFolder = viewModel::resetGridAfterMoveFolder,
        onResetGridAfterResize = viewModel::resetGridAfterResize,
        onResetPinGridItem = viewModel::resetPinGridItem,
        onResizeGridItem = viewModel::resizeGridItem,
        onSaveEditPage = viewModel::saveEditPage,
        onSettings = onSettings,
        onStartSyncData = viewModel::startSyncData,
        onStopSyncData = viewModel::stopSyncData,
        onUpsertFolderGridItemPopupEntry = viewModel::upsertFolderGridItemPopupEntry,
        onDeleteFolderPopupEntry = viewModel::deleteFolderGridItemPopupEntry,
        onShowFolderWhenDragging = viewModel::showFolderWhenDragging,
        onUpdateScreen = viewModel::updateScreen,
        onUpdateShortcutConfigIntoShortcutInfoGridItem = viewModel::updateShortcutConfigIntoShortcutInfoGridItem,
        onUpdateGridItemSource = viewModel::updateGridItemSource,
        onUpdateIsVisibleOverlay = viewModel::updateIsVisibleOverlay,
        onUpdateMoveGridItemResult = viewModel::updateMoveGridItemResult,
        onUpdateResizeGridItem = viewModel::updateResizeGridItem,
        onPackageRemoved = viewModel::packageRemove,
        onPackageAdded = viewModel::packageAdded,
        onPackageChanged = viewModel::packageChanged,
        onShortcutsChanged = viewModel::shortcutsChanged,
        onResetFolderPopupEntries = viewModel::resetFolderGridItemPopupEntries,
        onUpsertFolderEblanApplicationInfoPopupEntry = viewModel::upsertFolderEblanApplicationInfoPopupEntry,
        onDeleteFolderEblanApplicationInfoPopupEntry = viewModel::deleteFolderEblanApplicationInfoPopupEntry,
        onEditFolderApplicationInfo = onEditFolderApplicationInfo,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    configureResultCode: Int?,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    eblanShortcutConfigs: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    pageItems: List<PageItem>?,
    folderGridItemPopups: List<FolderGridItemPopup>,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    homeUiState: HomeUiState,
    movedGridItemResult: MoveGridItemResult?,
    pinGridItem: GridItem?,
    screen: Screen,
    resizeGridItem: GridItem?,
    gridItemSource: GridItemSource?,
    isVisibleOverlay: Boolean,
    textColor: TextColor,
    previewFolderGridItems: Map<String, PreviewFolder>,
    previewFolderEblanApplicationInfos: Map<String, PreviewFolderEblanApplicationInfo>,
    folderEblanApplicationInfoPopups: List<FolderEblanApplicationInfoPopup>,
    onResetGrid: () -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onResetGridAfterDeleteGridItem: (GridItem) -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onEditGridItem: (String) -> Unit,
    onEditPage: (
        gridItems: List<GridItem>,
        associate: Associate,
    ) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByTagId: (Long?) -> Unit,
    onGetEblanShortcutConfigsByLabel: (String) -> Unit,
    onGetPinGridItem: (PinItemRequestType) -> Unit,
    onMoveFolderGridItem: (
        folderGridItemPopup: FolderGridItemPopup,
        movingGridItem: GridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onMoveFolderGridItemOutsideFolder: (GridItem) -> Unit,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onResetConfigureResultCode: () -> Unit,
    onUpdateGridItemsAfterMove: (MoveGridItemResult) -> Unit,
    onUpdateGridItemsAfterMoveFolder: () -> Unit,
    onResetGridAfterResize: () -> Unit,
    onResetPinGridItem: () -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
    ) -> Unit,
    onSaveEditPage: (
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
        associate: Associate,
    ) -> Unit,
    onSettings: () -> Unit,
    onStartSyncData: () -> Unit,
    onStopSyncData: () -> Unit,
    onUpsertFolderGridItemPopupEntry: (FolderPopupEntry) -> Unit,
    onDeleteFolderPopupEntry: (FolderPopupEntry) -> Unit,
    onShowFolderWhenDragging: (
        folderPopupEntry: FolderPopupEntry,
        movingGridItem: GridItem,
    ) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
    onUpdateShortcutConfigIntoShortcutInfoGridItem: (
        moveGridItemResult: MoveGridItemResult,
        pinItemRequestType: PinItemRequestType.ShortcutInfo,
    ) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
    onUpdateResizeGridItem: (GridItem) -> Unit,
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
    onResetFolderPopupEntries: () -> Unit,
    onUpsertFolderEblanApplicationInfoPopupEntry: (FolderPopupEntry) -> Unit,
    onDeleteFolderEblanApplicationInfoPopupEntry: (FolderPopupEntry) -> Unit,
    onEditFolderApplicationInfo: (String) -> Unit,
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
                configureResultCode = configureResultCode,
                eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                eblanApplicationInfoTags = eblanApplicationInfoTags,
                eblanShortcutConfigs = eblanShortcutConfigs,
                eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                pageItems = pageItems,
                folderGridItemPopups = folderGridItemPopups,
                getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
                homeData = homeUiState.homeData,
                movedGridItemResult = movedGridItemResult,
                paddingValues = paddingValues,
                pinGridItem = pinGridItem,
                screen = screen,
                screenHeight = screenIntSize.height,
                screenWidth = screenIntSize.width,
                resizeGridItem = resizeGridItem,
                gridItemSource = gridItemSource,
                isVisibleOverlay = isVisibleOverlay,
                textColor = textColor,
                previewFolderGridItems = previewFolderGridItems,
                previewFolderEblanApplicationInfos = previewFolderEblanApplicationInfos,
                folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                onResetGrid = onResetGrid,
                onDeleteGridItem = onDeleteGridItem,
                onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
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
                onUpdateGridItemsAfterMove = onUpdateGridItemsAfterMove,
                onUpdateGridItemsAfterMoveFolder = onUpdateGridItemsAfterMoveFolder,
                onResetGridAfterResize = onResetGridAfterResize,
                onResetPinGridItem = onResetPinGridItem,
                onResizeGridItem = onResizeGridItem,
                onSaveEditPage = onSaveEditPage,
                onSettings = onSettings,
                onStartSyncData = onStartSyncData,
                onStopSyncData = onStopSyncData,
                onUpsertFolderGridItemPopupEntry = onUpsertFolderGridItemPopupEntry,
                onDeleteFolderPopupEntry = onDeleteFolderPopupEntry,
                onShowFolderWhenDragging = onShowFolderWhenDragging,
                onUpdateScreen = onUpdateScreen,
                onUpdateShortcutConfigIntoShortcutInfoGridItem = onUpdateShortcutConfigIntoShortcutInfoGridItem,
                onUpdateGridItemSource = onUpdateGridItemSource,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                onUpdateResizeGridItem = onUpdateResizeGridItem,
                onPackageRemoved = onPackageRemoved,
                onPackageAdded = onPackageAdded,
                onPackageChanged = onPackageChanged,
                onShortcutsChanged = onShortcutsChanged,
                onResetFolderPopupEntries = onResetFolderPopupEntries,
                onUpsertFolderEblanApplicationInfoPopupEntry = onUpsertFolderEblanApplicationInfoPopupEntry,
                onDeleteFolderEblanApplicationInfoPopupEntry = onDeleteFolderEblanApplicationInfoPopupEntry,
                onEditFolderApplicationInfo = onEditFolderApplicationInfo,
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun Success(
    modifier: Modifier = Modifier,
    configureResultCode: Int?,
    eblanAppWidgetProviderInfos: Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    eblanShortcutConfigs: Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    pageItems: List<PageItem>?,
    folderGridItemPopups: List<FolderGridItemPopup>,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    homeData: HomeData,
    movedGridItemResult: MoveGridItemResult?,
    paddingValues: PaddingValues,
    pinGridItem: GridItem?,
    screen: Screen,
    screenHeight: Int,
    screenWidth: Int,
    resizeGridItem: GridItem?,
    gridItemSource: GridItemSource?,
    isVisibleOverlay: Boolean,
    textColor: TextColor,
    previewFolderGridItems: Map<String, PreviewFolder>,
    previewFolderEblanApplicationInfos: Map<String, PreviewFolderEblanApplicationInfo>,
    folderEblanApplicationInfoPopups: List<FolderEblanApplicationInfoPopup>,
    onResetGrid: () -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onResetGridAfterDeleteGridItem: (GridItem) -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onEditGridItem: (String) -> Unit,
    onEditPage: (
        gridItems: List<GridItem>,
        associate: Associate,
    ) -> Unit,
    onGetEblanAppWidgetProviderInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByTagId: (Long?) -> Unit,
    onGetEblanShortcutConfigsByLabel: (String) -> Unit,
    onGetPinGridItem: (PinItemRequestType) -> Unit,
    onMoveFolderGridItem: (
        folderGridItemPopup: FolderGridItemPopup,
        movingGridItem: GridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onMoveFolderGridItemOutsideFolder: (GridItem) -> Unit,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onResetConfigureResultCode: () -> Unit,
    onUpdateGridItemsAfterMove: (MoveGridItemResult) -> Unit,
    onUpdateGridItemsAfterMoveFolder: () -> Unit,
    onResetGridAfterResize: () -> Unit,
    onResetPinGridItem: () -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
    ) -> Unit,
    onSaveEditPage: (
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
        associate: Associate,
    ) -> Unit,
    onSettings: () -> Unit,
    onStartSyncData: () -> Unit,
    onStopSyncData: () -> Unit,
    onUpsertFolderGridItemPopupEntry: (FolderPopupEntry) -> Unit,
    onDeleteFolderPopupEntry: (FolderPopupEntry) -> Unit,
    onShowFolderWhenDragging: (
        folderPopupEntry: FolderPopupEntry,
        movingGridItem: GridItem,
    ) -> Unit,
    onUpdateScreen: (Screen) -> Unit,
    onUpdateShortcutConfigIntoShortcutInfoGridItem: (
        moveGridItemResult: MoveGridItemResult,
        pinItemRequestType: PinItemRequestType.ShortcutInfo,
    ) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
    onUpdateResizeGridItem: (GridItem) -> Unit,
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
    onResetFolderPopupEntries: () -> Unit,
    onUpsertFolderEblanApplicationInfoPopupEntry: (FolderPopupEntry) -> Unit,
    onDeleteFolderEblanApplicationInfoPopupEntry: (FolderPopupEntry) -> Unit,
    onEditFolderApplicationInfo: (String) -> Unit,
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
                    folderGridItemPopups = folderGridItemPopups,
                    gestureSettings = homeData.userData.gestureSettings,
                    getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
                    gridItems = homeData.gridItems,
                    gridItemsByPage = homeData.gridItemsByPage,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    hasSystemFeatureAppWidgets = homeData.hasSystemFeatureAppWidgets,
                    homeSettings = homeData.userData.homeSettings,
                    moveGridItemResult = movedGridItemResult,
                    paddingValues = paddingValues,
                    pinGridItem = pinGridItem,
                    screenHeight = screenHeight,
                    screenWidth = screenWidth,
                    textColor = textColor,
                    resizeGridItem = resizeGridItem,
                    gridItemSource = gridItemSource,
                    isVisibleOverlay = isVisibleOverlay,
                    previewFolderGridItems = previewFolderGridItems,
                    iconPackInfoFilePaths = homeData.iconPackInfoFilePaths,
                    previewFolderEblanApplicationInfos = previewFolderEblanApplicationInfos,
                    folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                    onDeleteGridItem = onDeleteGridItem,
                    onResetGridAfterDeleteGridItem = onResetGridAfterDeleteGridItem,
                    onUpdateGridItemsAfterMove = onUpdateGridItemsAfterMove,
                    onDragEndAfterMoveFolder = onUpdateGridItemsAfterMoveFolder,
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
                    onResetFolderPopupEntries = onResetFolderPopupEntries,
                    onUpsertFolderEblanApplicationInfoPopupEntry = onUpsertFolderEblanApplicationInfoPopupEntry,
                    onDeleteFolderEblanApplicationInfoPopupEntry = onDeleteFolderEblanApplicationInfoPopupEntry,
                    onEditFolderApplicationInfo = onEditFolderApplicationInfo,
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
                    previewFolderGridItems = previewFolderGridItems,
                    iconPackInfoFilePaths = homeData.iconPackInfoFilePaths,
                    folderBackgroundColor = homeData.userData.homeSettings.folderBackgroundColor,
                    customFolderBackgroundColor = homeData.userData.homeSettings.customFolderBackgroundColor,
                    systemTextColor = textColor,
                    systemCustomTextColor = homeData.userData.homeSettings.gridItemSettings.customTextColor,
                    onSaveEditPage = onSaveEditPage,
                    onUpdateScreen = onUpdateScreen,
                )
            }

            Screen.EditDockGridPage -> {
                EditDockGridPageScreen(
                    pageItems = pageItems,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    homeSettings = homeData.userData.homeSettings,
                    paddingValues = paddingValues,
                    textColor = textColor,
                    previewFolderGridItems = previewFolderGridItems,
                    iconPackInfoFilePaths = homeData.iconPackInfoFilePaths,
                    folderBackgroundColor = homeData.userData.homeSettings.folderBackgroundColor,
                    customFolderBackgroundColor = homeData.userData.homeSettings.customFolderBackgroundColor,
                    systemTextColor = textColor,
                    systemCustomTextColor = homeData.userData.homeSettings.gridItemSettings.customTextColor,
                    onSaveEditPage = onSaveEditPage,
                    onUpdateScreen = onUpdateScreen,
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
            modifier = modifier,
            title = stringResource(R.string.request_permissions),
            text = stringResource(R.string.allow_permissions_so_we_can_inform_you_about_important_crash_reports_and_make_phone_shortcuts),
            onClick = {
                notificationsPermissionState.launchMultiplePermissionRequest()

                showTextDialog = false
            },
            onDismissRequest = {
                showTextDialog = false
            },
        )
    }
}
