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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.application.GetEblanApplicationInfosByLabelAndTag
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData.ShortcutConfig
import com.eblan.launcher.domain.model.grid.GridItemData.ShortcutInfo
import com.eblan.launcher.domain.model.grid.MoveFolderEblanApplicationInfoGridItemResult
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.domain.model.home.PageItem
import com.eblan.launcher.domain.model.launcherapps.PinItemRequestType
import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.domain.platform.LauncherAppsWrapper
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.usecase.application.GetEblanAppWidgetProviderInfosByLabelUseCase
import com.eblan.launcher.domain.usecase.application.GetEblanApplicationInfosByLabelAndTagUseCase
import com.eblan.launcher.domain.usecase.application.GetEblanApplicationTagsUseCase
import com.eblan.launcher.domain.usecase.application.GetEblanShortcutConfigsByLabelUseCase
import com.eblan.launcher.domain.usecase.application.GetEblanShortcutInfosUseCase
import com.eblan.launcher.domain.usecase.folder.CleanupFolderGridItemsUseCase
import com.eblan.launcher.domain.usecase.folder.DeleteFolderEblanApplicationInfoGridItemsUseCase
import com.eblan.launcher.domain.usecase.folder.GetFolderEblanApplicationInfoPopupsUseCase
import com.eblan.launcher.domain.usecase.folder.GetRecursiveFolderEblanApplicationInfosUseCase
import com.eblan.launcher.domain.usecase.folder.MoveFolderEblanApplicationInfoGridItemUseCase
import com.eblan.launcher.domain.usecase.folder.MoveFolderGridItemUseCase
import com.eblan.launcher.domain.usecase.grid.DeleteGridItemUseCase
import com.eblan.launcher.domain.usecase.grid.MoveGridItemUseCase
import com.eblan.launcher.domain.usecase.grid.ResizeGridItemUseCase
import com.eblan.launcher.domain.usecase.grid.UpdateGridItemsAfterMoveUseCase
import com.eblan.launcher.domain.usecase.home.GetHomeDataUseCase
import com.eblan.launcher.domain.usecase.launcherapps.AddPackageUseCase
import com.eblan.launcher.domain.usecase.launcherapps.ChangePackageUseCase
import com.eblan.launcher.domain.usecase.launcherapps.ChangeShortcutsUseCase
import com.eblan.launcher.domain.usecase.launcherapps.RemovePackageUseCase
import com.eblan.launcher.domain.usecase.launcherapps.SyncDataUseCase
import com.eblan.launcher.domain.usecase.page.CachePageItemsUseCase
import com.eblan.launcher.domain.usecase.page.UpdatePageItemsUseCase
import com.eblan.launcher.domain.usecase.pin.GetPinGridItemUseCase
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.model.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    getHomeDataUseCase: GetHomeDataUseCase,
    private val moveGridItemUseCase: MoveGridItemUseCase,
    private val resizeGridItemUseCase: ResizeGridItemUseCase,
    private val cachePageItemsUseCase: CachePageItemsUseCase,
    private val updatePageItemsUseCase: UpdatePageItemsUseCase,
    private val updateGridItemsAfterMoveUseCase: UpdateGridItemsAfterMoveUseCase,
    private val getPinGridItemUseCase: GetPinGridItemUseCase,
    private val fileManager: FileManager,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    getEblanShortcutInfosUseCase: GetEblanShortcutInfosUseCase,
    eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    getEblanApplicationInfosByLabelAndTagUseCase: GetEblanApplicationInfosByLabelAndTagUseCase,
    getEblanAppWidgetProviderInfosByLabelUseCase: GetEblanAppWidgetProviderInfosByLabelUseCase,
    getEblanShortcutConfigsByLabelUseCase: GetEblanShortcutConfigsByLabelUseCase,
    private val gridRepository: GridRepository,
    private val syncDataUseCase: SyncDataUseCase,
    private val addPackageUseCase: AddPackageUseCase,
    private val removePackageUseCase: RemovePackageUseCase,
    private val changePackageUseCase: ChangePackageUseCase,
    private val changeShortcutsUseCase: ChangeShortcutsUseCase,
    private val moveFolderGridItemUseCase: MoveFolderGridItemUseCase,
    private val iconKeyGenerator: IconKeyGenerator,
    private val deleteGridItemUseCase: DeleteGridItemUseCase,
    getFolderEblanApplicationInfoPopupsUseCase: GetFolderEblanApplicationInfoPopupsUseCase,
    private val moveFolderEblanApplicationInfoGridItemUseCase: MoveFolderEblanApplicationInfoGridItemUseCase,
    getEblanApplicationTagsUseCase: GetEblanApplicationTagsUseCase,
    private val getRecursiveFolderEblanApplicationInfosUseCase: GetRecursiveFolderEblanApplicationInfosUseCase,
    private val deleteFolderEblanApplicationInfoGridItemsUseCase: DeleteFolderEblanApplicationInfoGridItemsUseCase,
    private val cleanupFolderGridItemsUseCase: CleanupFolderGridItemsUseCase,
) : ViewModel() {
    val homeUiState = getHomeDataUseCase().map(HomeUiState::Success).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading,
    )

    private val _screen = MutableStateFlow(Screen.Pager)
    val screen = _screen.asStateFlow()

    private val _moveGridItemResult = MutableStateFlow<MoveGridItemResult?>(null)
    val movedGridItemResult = _moveGridItemResult.asStateFlow()

    private val defaultDelay = 500L.milliseconds
    private val moveDelay = 50L.milliseconds

    private val _pageItems = MutableStateFlow<List<PageItem>?>(null)
    val pageItems = _pageItems.asStateFlow()

    private var moveGridItemJob: Job? = null

    private val _pinGridItem = MutableStateFlow<GridItem?>(null)
    val pinGridItem = _pinGridItem.asStateFlow()

    val eblanShortcutInfosGroup = getEblanShortcutInfosUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap(),
    )

    val eblanAppWidgetProviderInfosGroup =
        eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfosFlow.map { eblanAppWidgetProviderInfos ->
            eblanAppWidgetProviderInfos.groupBy { eblanAppWidgetProviderInfo ->
                eblanAppWidgetProviderInfo.packageName
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap(),
        )

    private val _eblanApplicationInfoLabel = MutableStateFlow("")
    private val _eblanApplicationInfoTagId = MutableStateFlow<Long?>(null)
    val getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTagUseCase(
        labelFlow = _eblanApplicationInfoLabel,
        eblanApplicationInfoTagIdFlow = _eblanApplicationInfoTagId,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GetEblanApplicationInfosByLabelAndTag(
            eblanApplicationInfos = emptyMap(),
            privateEblanUser = null,
            privateEblanApplicationInfos = emptyList(),
            iconPackFilePaths = emptyMap(),
        ),
    )

    private val _eblanAppWidgetProviderInfoLabel = MutableStateFlow("")
    val eblanAppWidgetProviderInfos =
        getEblanAppWidgetProviderInfosByLabelUseCase(labelFlow = _eblanAppWidgetProviderInfoLabel).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap(),
        )

    private val _eblanShortcutConfigLabel = MutableStateFlow("")
    val eblanShortcutConfigs =
        getEblanShortcutConfigsByLabelUseCase(labelFlow = _eblanShortcutConfigLabel).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap(),
        )

    val eblanApplicationInfoTags = getEblanApplicationTagsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private var syncDataJob: Job? = null
    private var packageRemovedJob: Job? = null
    private var packageAddedJob: Job? = null
    private var packageChangedJob: Job? = null
    private var shortcutsChangedJob: Job? = null

    private val _folderGridItemEntries = MutableStateFlow<List<FolderEntry>>(emptyList())
    val folderGridItemEntries = combine(
        _folderGridItemEntries,
        homeUiState.filterIsInstance<HomeUiState.Success>(),
    ) { folderEntries, currentHomeUiState ->
        val filteredFolderEntries = folderEntries.filter { folderEntry ->
            folderEntry.id in currentHomeUiState.homeData.folderPopups
        }

        if (filteredFolderEntries.size != folderEntries.size) {
            _folderGridItemEntries.update {
                filteredFolderEntries
            }
        }

        filteredFolderEntries
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _resizeGridItem = MutableStateFlow<GridItem?>(null)
    val resizeGridItem = _resizeGridItem.asStateFlow()

    private val _gridItemSource = MutableStateFlow<GridItemSource?>(null)
    val gridItemSource = _gridItemSource.asStateFlow()

    private val _isVisibleOverlay = MutableStateFlow(false)
    val isVisibleOverlay = _isVisibleOverlay.asStateFlow()

    private val _folderEblanApplicationInfoPopups =
        getFolderEblanApplicationInfoPopupsUseCase().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val folderEblanApplicationInfoPopups =
        _folderEblanApplicationInfoPopups.filterNotNull().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap(),
        )

    private val _folderEblanApplicationInfoEntries =
        MutableStateFlow<List<FolderEntry>>(emptyList())
    val folderEblanApplicationInfoEntries = combine(
        _folderEblanApplicationInfoEntries,
        _folderEblanApplicationInfoPopups.filterNotNull(),
    ) { folderEntries, currentFolderPopups ->
        val filteredFolderEntries = folderEntries.filter { folderEntry ->
            folderEntry.id in currentFolderPopups
        }

        if (filteredFolderEntries.size != folderEntries.size) {
            _folderEblanApplicationInfoEntries.update {
                filteredFolderEntries
            }
        }

        filteredFolderEntries
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _moveFolderEblanApplicationInfoGridItemResult =
        MutableStateFlow<MoveFolderEblanApplicationInfoGridItemResult?>(null)
    val moveFolderEblanApplicationInfoGridItemResult =
        _moveFolderEblanApplicationInfoGridItemResult.asStateFlow()

    fun moveGridItem(
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) {
        moveGridItemJob?.cancel()

        moveGridItemJob = viewModelScope.launch {
            delay(moveDelay)

            val moveGridItemResult = moveGridItemUseCase(
                movingGridItem = movingGridItem,
                x = x,
                y = y,
                columns = columns,
                rows = rows,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
            )

            _moveGridItemResult.update {
                moveGridItemResult
            }
        }
    }

    fun cancelPendingMoveGridItem() {
        moveGridItemJob?.cancel()
    }

    fun resizeGridItem(
        resizingGridItem: GridItem,
        columns: Int,
        rows: Int,
    ) {
        moveGridItemJob?.cancel()

        moveGridItemJob = viewModelScope.launch {
            delay(moveDelay)

            _resizeGridItem.update {
                resizeGridItemUseCase(
                    resizingGridItem = resizingGridItem,
                    columns = columns,
                    rows = rows,
                )
            }
        }
    }

    fun showPageCache(
        gridItems: List<GridItem>,
        associate: Associate,
    ) {
        viewModelScope.launch {
            _screen.update {
                Screen.Loading
            }

            _pageItems.update {
                cachePageItemsUseCase(
                    gridItems = gridItems,
                    associate = associate,
                )
            }

            delay(defaultDelay)

            _screen.update {
                when (associate) {
                    Associate.Grid -> Screen.EditGridPage
                    Associate.Dock -> Screen.EditDockGridPage
                }
            }
        }
    }

    fun saveEditPage(
        id: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
        associate: Associate,
    ) {
        viewModelScope.launch {
            _screen.update {
                Screen.Loading
            }

            updatePageItemsUseCase(
                id = id,
                pageItems = pageItems,
                pageItemsToDelete = pageItemsToDelete,
                associate = associate,
            )

            delay(defaultDelay)

            _screen.update {
                Screen.Pager
            }
        }
    }

    fun updateScreen(screen: Screen) {
        _screen.update {
            screen
        }
    }

    fun resetGridAfterResize() {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            _moveGridItemResult.update {
                null
            }

            _resizeGridItem.update {
                null
            }

            _gridItemSource.update {
                null
            }
        }
    }

    fun updateGridItemsAfterMove(moveGridItemResult: MoveGridItemResult) {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            updateGridItemsAfterMoveUseCase(moveGridItemResult = moveGridItemResult)

            if (moveGridItemResult.conflictingGridItem == null) {
                resetGrid()
            }
        }
    }

    fun resetGrid() {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            _isVisibleOverlay.update {
                false
            }

            _moveGridItemResult.update {
                null
            }

            _moveFolderEblanApplicationInfoGridItemResult.update {
                null
            }

            _gridItemSource.update {
                null
            }
        }
    }

    fun resetGridAfterDeleteGridItem(gridItem: GridItem) {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            deleteGridItemUseCase(gridItem = gridItem)

            _moveGridItemResult.update {
                null
            }

            _gridItemSource.update {
                null
            }
        }
    }

    fun getEblanApplicationInfosByLabel(label: String) {
        _eblanApplicationInfoLabel.update {
            label
        }
    }

    fun getEblanAppWidgetProviderInfosByLabel(label: String) {
        _eblanAppWidgetProviderInfoLabel.update {
            label
        }
    }

    fun getEblanShortcutConfigsByLabel(label: String) {
        _eblanShortcutConfigLabel.update {
            label
        }
    }

    fun deleteGridItem(gridItem: GridItem) {
        viewModelScope.launch {
            deleteGridItemUseCase(gridItem = gridItem)
        }
    }

    fun getPinGridItem(pinItemRequestType: PinItemRequestType) {
        viewModelScope.launch {
            _pinGridItem.update {
                getPinGridItemUseCase(pinItemRequestType = pinItemRequestType)
            }
        }
    }

    fun resetPinGridItem() {
        _pinGridItem.update {
            null
        }
    }

    fun updateShortcutConfigIntoShortcutInfoGridItem(
        moveGridItemResult: MoveGridItemResult,
        pinItemRequestType: PinItemRequestType.ShortcutInfo,
    ) {
        viewModelScope.launch {
            val movingGridItem = moveGridItemResult.movingGridItem

            gridRepository.deleteGridItemById(gridItem = movingGridItem)

            val eblanApplicationInfoIcon =
                launcherAppsWrapper.getFastActivityList(
                    serialNumber = pinItemRequestType.serialNumber,
                    packageName = pinItemRequestType.packageName,
                ).firstOrNull()?.componentName
                    ?.let { componentName ->
                        val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

                        val file = File(
                            directory,
                            iconKeyGenerator.getActivityIconKey(
                                serialNumber = pinItemRequestType.serialNumber,
                                componentName = componentName,
                            ),
                        )

                        file.absolutePath
                    }

            val icon = pinItemRequestType.icon?.let { byteArray ->
                val directory = fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR)

                fileManager.updateAndGetFilePath(
                    directory = directory,
                    name = iconKeyGenerator.getShortcutIconKey(
                        serialNumber = pinItemRequestType.serialNumber,
                        packageName = pinItemRequestType.packageName,
                        id = pinItemRequestType.shortcutId,
                    ),
                    byteArray = byteArray,
                )
            }

            val data = ShortcutInfo(
                shortcutId = pinItemRequestType.shortcutId,
                packageName = pinItemRequestType.packageName,
                serialNumber = pinItemRequestType.serialNumber,
                shortLabel = pinItemRequestType.shortLabel,
                longLabel = pinItemRequestType.longLabel,
                icon = icon,
                isEnabled = pinItemRequestType.isEnabled,
                eblanApplicationInfoIcon = eblanApplicationInfoIcon,
                customIcon = null,
                customShortLabel = null,
                index = -1,
                folderId = null,
            )

            gridRepository.insertGridItem(gridItem = movingGridItem.copy(data = data))

            updateGridItemsAfterMove(moveGridItemResult = moveGridItemResult)

            resetGrid()
        }
    }

    fun updateShortcutConfigGridItem(
        moveGridItemResult: MoveGridItemResult,
        shortcutIntentName: String?,
        shortcutIntentIcon: ByteArray?,
        shortcutIntentUri: String?,
    ) {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            val movingGridItem = moveGridItemResult.movingGridItem

            val movingData = movingGridItem.data as ShortcutConfig

            val shortcutIntentIconPath = shortcutIntentIcon?.let { byteArray ->
                fileManager.updateAndGetFilePath(
                    directory = fileManager.getFilesDirectory(FileManager.SHORTCUT_INTENT_ICONS_DIR),
                    name = movingGridItem.id,
                    byteArray = byteArray,
                )
            }

            val newData = movingData.copy(
                shortcutIntentName = shortcutIntentName,
                shortcutIntentIcon = shortcutIntentIconPath,
                shortcutIntentUri = shortcutIntentUri,
            )

            val newMovingGridItem = movingGridItem.copy(data = newData)

            updateGridItemsAfterMoveUseCase(
                moveGridItemResult = moveGridItemResult.copy(movingGridItem = newMovingGridItem),
            )

            resetGrid()
        }
    }

    fun getEblanApplicationInfosByTagId(id: Long?) {
        _eblanApplicationInfoTagId.update {
            id
        }
    }

    fun startSyncData() {
        syncDataJob = viewModelScope.launch {
            syncDataUseCase()
        }
    }

    fun stopSyncData() {
        syncDataJob?.cancel()
        packageRemovedJob?.cancel()
        packageAddedJob?.cancel()
        packageChangedJob?.cancel()
        shortcutsChangedJob?.cancel()

        syncDataJob = null
        packageRemovedJob = null
        packageAddedJob = null
        packageChangedJob = null
        shortcutsChangedJob = null
    }

    fun upsertFolderGridItemEntry(folderEntry: FolderEntry) {
        _folderGridItemEntries.update { folderEntries ->
            folderEntries
                .filterNot { currentFolderEntry -> currentFolderEntry.id == folderEntry.id }
                .plus(folderEntry)
        }
    }

    fun moveFolderGridItem(
        columns: Int,
        rows: Int,
        folderGridItems: List<GridItem>,
        movingGridItem: GridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) {
        moveGridItemJob?.cancel()

        moveGridItemJob = viewModelScope.launch {
            delay(moveDelay)

            val folderMoveGridItemResult = moveFolderGridItemUseCase(
                columns = columns,
                rows = rows,
                folderGridItems = folderGridItems,
                movingGridItem = movingGridItem,
                dragX = dragX,
                dragY = dragY,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                currentPage = currentPage,
            )

            _moveGridItemResult.update {
                folderMoveGridItemResult
            }
        }
    }

    fun resetGridAfterMoveFolder() {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            cleanupFolderGridItemsUseCase()

            _isVisibleOverlay.update {
                false
            }

            _moveGridItemResult.update {
                null
            }

            _gridItemSource.update {
                null
            }
        }
    }

    fun moveFolderGridItemOutsideFolder(movingGridItem: GridItem) {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            closeFolderGridItemEntries()

            gridRepository.updateGridItem(gridItem = movingGridItem)

            _moveGridItemResult.update {
                MoveGridItemResult(
                    isSuccess = false,
                    movingGridItem = movingGridItem,
                    conflictingGridItem = null,
                )
            }

            _gridItemSource.update {
                GridItemSource.ExistingFolder
            }
        }
    }

    fun updateGridItemSource(gridItemSource: GridItemSource) {
        _gridItemSource.update {
            gridItemSource
        }
    }

    fun updateIsVisibleOverlay(isVisibleOverlay: Boolean) {
        _isVisibleOverlay.update {
            isVisibleOverlay
        }
    }

    fun showFolderWhenDragging(
        folderEntry: FolderEntry,
        movingGridItem: GridItem,
    ) {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            gridRepository.updateGridItem(gridItem = movingGridItem)

            _folderGridItemEntries.update { currentFolderEntries ->
                currentFolderEntries + folderEntry
            }

            _moveGridItemResult.update {
                MoveGridItemResult(
                    isSuccess = false,
                    movingGridItem = movingGridItem,
                    conflictingGridItem = null,
                )
            }

            _gridItemSource.update {
                null
            }
        }
    }

    fun updateMoveGridItemResult(moveGridItemResult: MoveGridItemResult) {
        _moveGridItemResult.update {
            moveGridItemResult
        }
    }

    fun updateResizeGridItem(resizeGridItem: GridItem) {
        _resizeGridItem.update {
            resizeGridItem
        }
    }

    fun deleteFolderGridItemEntry(folderEntry: FolderEntry) {
        _folderGridItemEntries.update { folderEntries ->
            folderEntries.filterNot { currentFolderEntry -> currentFolderEntry.id == folderEntry.id }
        }
    }

    fun packageRemove(
        serialNumber: Long,
        packageName: String,
    ) {
        packageRemovedJob?.cancel()

        packageRemovedJob = viewModelScope.launch {
            removePackageUseCase(
                serialNumber = serialNumber,
                packageName = packageName,
            )
        }
    }

    fun packageAdded(
        serialNumber: Long,
        packageName: String,
    ) {
        packageAddedJob?.cancel()

        packageAddedJob = viewModelScope.launch {
            addPackageUseCase(
                serialNumber = serialNumber,
                packageName = packageName,
            )
        }
    }

    fun packageChanged(
        serialNumber: Long,
        packageName: String,
    ) {
        packageChangedJob?.cancel()

        packageChangedJob = viewModelScope.launch {
            changePackageUseCase(
                serialNumber = serialNumber,
                packageName = packageName,
            )
        }
    }

    fun shortcutsChanged(
        serialNumber: Long,
        packageName: String,
    ) {
        shortcutsChangedJob?.cancel()

        shortcutsChangedJob = viewModelScope.launch {
            changeShortcutsUseCase(
                serialNumber = serialNumber,
                packageName = packageName,
            )
        }
    }

    fun resetFolderGridItemEntries() {
        _folderGridItemEntries.update { emptyList() }
    }

    fun closeFolderGridItemEntries() {
        _folderGridItemEntries.update { folderEntries ->
            folderEntries.map { folderEntry ->
                folderEntry.copy(isCloseFolder = true)
            }
        }
    }

    fun upsertFolderEblanApplicationInfoEntry(folderEntry: FolderEntry) {
        _folderEblanApplicationInfoEntries.update { folderEntries ->
            folderEntries
                .filterNot { currentFolderEntry -> currentFolderEntry.id == folderEntry.id }
                .plus(folderEntry)
        }
    }

    fun deleteFolderEblanApplicationInfoEntry(folderEntry: FolderEntry) {
        _folderEblanApplicationInfoEntries.update { folderEntries ->
            folderEntries.filterNot { currentFolderEntry -> currentFolderEntry.id == folderEntry.id }
        }
    }

    fun resetFolderEblanApplicationInfoEntries() {
        _folderEblanApplicationInfoEntries.update { emptyList() }
    }

    fun closeFolderEblanApplicationInfoEntries() {
        _folderEblanApplicationInfoEntries.update { folderEntries ->
            folderEntries.map { folderEntry ->
                folderEntry.copy(isCloseFolder = true)
            }
        }
    }

    fun moveFolderEblanApplicationInfoGridItem(
        columns: Int,
        rows: Int,
        folderEblanApplicationInfoGridItems: List<FolderEblanApplicationInfoGridItem>,
        folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
        dragX: Int,
        dragY: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) {
        moveGridItemJob?.cancel()

        moveGridItemJob = viewModelScope.launch {
            delay(moveDelay)

            val moveFolderEblanApplicationInfoGridItemResult =
                moveFolderEblanApplicationInfoGridItemUseCase(
                    columns = columns,
                    rows = rows,
                    folderEblanApplicationInfoGridItems = folderEblanApplicationInfoGridItems,
                    folderEblanApplicationInfoGridItem = folderEblanApplicationInfoGridItem,
                    dragX = dragX,
                    dragY = dragY,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    currentPage = currentPage,
                )

            _moveFolderEblanApplicationInfoGridItemResult.update {
                moveFolderEblanApplicationInfoGridItemResult
            }
        }
    }

    fun updateMoveFolderEblanApplicationInfoGridItemResult(
        moveFolderEblanApplicationInfoGridItemResult: MoveFolderEblanApplicationInfoGridItemResult,
    ) {
        _moveFolderEblanApplicationInfoGridItemResult.update {
            moveFolderEblanApplicationInfoGridItemResult
        }
    }

    fun resetGridAfterMoveFolderEblanApplicationInfo() {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            _isVisibleOverlay.update {
                false
            }

            _moveFolderEblanApplicationInfoGridItemResult.update {
                null
            }

            _gridItemSource.update {
                null
            }
        }
    }

    fun moveFolderEblanApplicationInfoGridItemOutsideFolder(
        folderId: String,
        isFolder: Boolean,
        movingGridItem: GridItem,
    ) {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            closeFolderEblanApplicationInfoEntries()

            _moveGridItemResult.update {
                MoveGridItemResult(
                    isSuccess = false,
                    movingGridItem = movingGridItem,
                    conflictingGridItem = null,
                )
            }

            if (isFolder) {
                _gridItemSource.update {
                    GridItemSource.NewFolder(
                        folderGridItems = getRecursiveFolderEblanApplicationInfosUseCase(
                            id = movingGridItem.id,
                            folderId = folderId,
                        ),
                    )
                }
            } else {
                _gridItemSource.update {
                    GridItemSource.New
                }
            }
        }
    }

    fun dragFolderEblanApplicationInfoToGrid(
        folderId: String,
        movingGridItem: GridItem,
    ) {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            _moveGridItemResult.update {
                MoveGridItemResult(
                    isSuccess = false,
                    movingGridItem = movingGridItem,
                    conflictingGridItem = null,
                )
            }

            _gridItemSource.update {
                GridItemSource.NewFolder(
                    folderGridItems = getRecursiveFolderEblanApplicationInfosUseCase(
                        id = movingGridItem.id,
                        folderId = folderId,
                    ),
                )
            }
        }
    }

    fun deleteFolderEblanApplicationInfoGridItems(folderId: String) {
        viewModelScope.launch {
            deleteFolderEblanApplicationInfoGridItemsUseCase(folderId = folderId)
        }
    }

    fun updateGridItemsAfterMoveNewFolder(
        folderGridItems: List<GridItem>,
        moveGridItemResult: MoveGridItemResult,
    ) {
        viewModelScope.launch {
            moveGridItemJob?.cancelAndJoin()

            gridRepository.withTransaction {
                gridRepository.upsertGridItems(gridItems = folderGridItems)

                updateGridItemsAfterMoveUseCase(moveGridItemResult = moveGridItemResult)
            }

            if (moveGridItemResult.conflictingGridItem == null) {
                resetGrid()
            }
        }
    }
}
