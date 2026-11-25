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
package com.eblan.launcher.feature.home.screen.application.vertical

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.theme.EblanSpacing
import com.eblan.launcher.domain.model.application.AppDrawerGridItemData
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.application.GetEblanApplicationInfosByLabelAndTag
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.launcherapps.EblanUser
import com.eblan.launcher.domain.model.launcherapps.EblanUserPageKey
import com.eblan.launcher.domain.model.launcherapps.EblanUserType
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.SearchBarType
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.rememberNestedScrollConnectionEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.application.ApplicationSearchBar
import com.eblan.launcher.feature.home.screen.application.EblanApplicationInfoItem
import com.eblan.launcher.feature.home.screen.application.EblanApplicationInfoTabRow
import com.eblan.launcher.feature.home.screen.application.FolderEblanApplicationInfoItem
import com.eblan.launcher.feature.home.screen.application.QuiteModeScreen
import com.eblan.launcher.feature.home.screen.application.TagElevatedFilterChip
import com.eblan.launcher.feature.home.screen.application.privateSpace
import com.eblan.launcher.feature.home.util.getApplicationScreenTextColor
import com.eblan.launcher.ui.local.LocalUserManager
import com.eblan.launcher.ui.settings.produceIsDefaultLauncherState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VerticalApplicationScreen(
    // Layout and Transition
    sharedTransitionScope: SharedTransitionScope,
    screenHeight: Int,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    // Application Info
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    horizontalPagerState: PagerState,
    textFieldState: TextFieldState,
    selectedEblanApplicationInfoTagId: Long?,
    focusRequester: FocusRequester,
    // Folder Configuration
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    folderBorderColor: BackgroundColor,
    customFolderBorderColor: Int,
    folderBorderWidth: Int,
    isVisibleFolderEblanApplicationInfos: Boolean,
    folderEblanApplicationInfoEntries: List<FolderEntry>,
    // System and Appearance
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    searchBarType: SearchBarType,
    appDrawerColumns: Int,
    appDrawerRowsHeight: Int,
    gridItemSettings: GridItemSettings,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    animations: Boolean,
    // Gesture and Interaction State
    drag: Drag,
    swipeY: Float,
    isVisibleOverlay: Boolean,
    // Callbacks
    onDismiss: () -> Unit,
    onDragEnd: () -> Unit,
    onUpdateSelectedEblanApplicationInfoTagId: (Long?) -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onDragFolderEblanApplicationInfo: (
        folderId: String,
        movingGridItem: GridItem,
    ) -> Unit,
    onDragApplicationInfo: (GridItem) -> Unit,
    onLongPressApplicationInfo: (
        eblanApplicationInfo: EblanApplicationInfo,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
    onLongPressFolderApplicationInfo: (
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
    onLongPressPrivateSpaceApplicationInfoItem: (
        eblanApplicationInfo: EblanApplicationInfo,
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onTapFolderApplicationInfo: (folderEntry: FolderEntry) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    val searchBarState = rememberSearchBarState()

    val eblanUserPageKeys =
        remember(key1 = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos) {
            getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos.keys.distinctBy { currentEblanUserPageKey ->
                currentEblanUserPageKey.eblanUser.serialNumber
            }
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = paddingValues.calculateStartPadding(layoutDirection),
                end = paddingValues.calculateEndPadding(layoutDirection),
            ),
    ) {
        DragHandle(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = getApplicationScreenTextColor(
                // Background Colors
                backgroundColor = backgroundColor,
                customBackgroundColor = customBackgroundColor,
                // Text Colors
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                // Defaults
                defaultColor = MaterialTheme.colorScheme.onSurface,
            ),
        )

        if (searchBarType == SearchBarType.Top) {
            ApplicationSearchBar(
                // State
                searchBarState = searchBarState,
                textFieldState = textFieldState,
                // Focus
                focusRequester = focusRequester,
                // Appearance
                backgroundColor = backgroundColor,
                customBackgroundColor = customBackgroundColor,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
            )
        }

        if (eblanApplicationInfoTags.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = EblanSpacing.medium),
                horizontalArrangement = Arrangement.spacedBy(EblanSpacing.small),
            ) {
                items(eblanApplicationInfoTags) { currentEblanApplicationInfoTag ->
                    TagElevatedFilterChip(
                        // Data
                        eblanApplicationInfoTag = currentEblanApplicationInfoTag,
                        selectedEblanApplicationInfoTag = selectedEblanApplicationInfoTagId,
                        // Callbacks
                        onUpdateEblanApplicationInfoTag = onUpdateSelectedEblanApplicationInfoTagId,
                    )
                }
            }
        }

        if (eblanUserPageKeys.size > 1) {
            EblanApplicationInfoTabRow(
                // Data
                eblanUserPageKeys = eblanUserPageKeys,
                eblanApplicationInfos = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos,
                // State
                currentPage = horizontalPagerState.currentPage,
                // Appearance
                backgroundColor = backgroundColor,
                customBackgroundColor = customBackgroundColor,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                // Callbacks
                onAnimateScrollToPage = horizontalPagerState::animateScrollToPage,
            )
        }

        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = horizontalPagerState,
            userScrollEnabled = !isVisibleOverlay,
        ) { index ->
            EblanApplicationInfosPage(
                // Scope and Layout
                sharedTransitionScope = sharedTransitionScope,
                paddingValues = paddingValues,
                screenHeight = screenHeight,
                index = index,
                // Animation and State
                isVisibleOverlay = isVisibleOverlay,
                swipeY = swipeY,
                animations = animations,
                isVisibleFolderEblanApplicationInfos = isVisibleFolderEblanApplicationInfos,
                // Settings and Data
                backgroundColor = backgroundColor,
                customBackgroundColor = customBackgroundColor,
                appDrawerColumns = appDrawerColumns,
                appDrawerRowsHeight = appDrawerRowsHeight,
                gridItemSettings = gridItemSettings,
                getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
                folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                // Folder Appearance
                folderCornerRadius = folderCornerRadius,
                folderBackgroundColor = folderBackgroundColor,
                customFolderBackgroundColor = customFolderBackgroundColor,
                folderBorderColor = folderBorderColor,
                customFolderBorderColor = customFolderBorderColor,
                folderBorderWidth = folderBorderWidth,
                folderEblanApplicationInfoEntries = folderEblanApplicationInfoEntries,
                // System Colors
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                // Drag and Drop State
                drag = drag,
                // Drag and Drop Callbacks
                onDismiss = onDismiss,
                onDragEnd = onDragEnd,
                onVerticalDrag = onVerticalDrag,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onDragFolderEblanApplicationInfo = onDragFolderEblanApplicationInfo,
                onDragApplicationInfo = onDragApplicationInfo,
                // Long Press and Tap Callbacks
                onLongPressApplicationInfo = onLongPressApplicationInfo,
                onLongPressFolderApplicationInfo = onLongPressFolderApplicationInfo,
                onLongPressPrivateSpaceApplicationInfoItem = onLongPressPrivateSpaceApplicationInfoItem,
                onTapFolderApplicationInfo = onTapFolderApplicationInfo,
            )
        }
    }
}

@Composable
private fun EblanApplicationInfosPage(
    // Scope and Layout
    sharedTransitionScope: SharedTransitionScope,
    paddingValues: PaddingValues,
    screenHeight: Int,
    index: Int,
    modifier: Modifier = Modifier,
    // Animation and State
    isVisibleOverlay: Boolean,
    swipeY: Float,
    animations: Boolean,
    isVisibleFolderEblanApplicationInfos: Boolean,
    // Settings and Data
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    appDrawerColumns: Int,
    appDrawerRowsHeight: Int,
    gridItemSettings: GridItemSettings,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    // Folder Appearance
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    folderBorderColor: BackgroundColor,
    customFolderBorderColor: Int,
    folderBorderWidth: Int,
    folderEblanApplicationInfoEntries: List<FolderEntry>,
    // System Colors
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // Drag and Drop State
    drag: Drag,
    // Drag and Drop Callbacks
    onDismiss: () -> Unit,
    onDragEnd: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onDragFolderEblanApplicationInfo: (
        folderId: String,
        movingGridItem: GridItem,
    ) -> Unit,
    onDragApplicationInfo: (GridItem) -> Unit,
    // Long Press and Tap Callbacks
    onLongPressApplicationInfo: (
        eblanApplicationInfo: EblanApplicationInfo,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
    onLongPressFolderApplicationInfo: (
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
    onLongPressPrivateSpaceApplicationInfoItem: (
        eblanApplicationInfo: EblanApplicationInfo,
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onTapFolderApplicationInfo: (folderEntry: FolderEntry) -> Unit,
) {
    val userManager = LocalUserManager.current

    val eblanUserPageKey = remember(key1 = getEblanApplicationInfosByLabelAndTag, key2 = index) {
        getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos.keys.toList()
            .getOrElse(
                index = index,
                defaultValue = {
                    EblanUserPageKey(
                        eblanUser = EblanUser(
                            serialNumber = 0L,
                            eblanUserType = EblanUserType.Personal,
                            isPrivateSpaceEntryPointHidden = false,
                            isPrivateSpaceLocked = false,
                            isQuietModeEnabled = false,
                        ),
                        page = 0,
                    )
                },
            )
    }

    val userHandle =
        userManager.getUserForSerialNumber(serialNumber = eblanUserPageKey.eblanUser.serialNumber)

    val isQuietModeEnabled = eblanUserPageKey.eblanUser.isQuietModeEnabled

    Box(modifier = modifier.fillMaxSize()) {
        if (isQuietModeEnabled) {
            QuiteModeScreen(
                // User
                userHandle = userHandle,
                // Appearance
                backgroundColor = backgroundColor,
                customBackgroundColor = customBackgroundColor,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                // Callbacks
                onDragEnd = onDragEnd,
                onVerticalDrag = onVerticalDrag,
            )
        } else {
            EblanApplicationInfosGrid(
                // Layout
                sharedTransitionScope = sharedTransitionScope,
                paddingValues = paddingValues,
                screenHeight = screenHeight,
                // State
                swipeY = swipeY,
                animations = animations,
                backgroundColor = backgroundColor,
                customBackgroundColor = customBackgroundColor,
                appDrawerColumns = appDrawerColumns,
                appDrawerRowsHeight = appDrawerRowsHeight,
                gridItemSettings = gridItemSettings,
                eblanUserPageKey = eblanUserPageKey,
                getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
                isVisibleOverlay = isVisibleOverlay,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                folderCornerRadius = folderCornerRadius,
                folderBackgroundColor = folderBackgroundColor,
                customFolderBackgroundColor = customFolderBackgroundColor,
                folderBorderColor = folderBorderColor,
                customFolderBorderColor = customFolderBorderColor,
                folderBorderWidth = folderBorderWidth,
                folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                isVisibleFolderEblanApplicationInfos = isVisibleFolderEblanApplicationInfos,
                folderEblanApplicationInfoEntries = folderEblanApplicationInfoEntries,
                drag = drag,
                // Callbacks
                onDismiss = onDismiss,
                onDragEnd = onDragEnd,
                onVerticalDrag = onVerticalDrag,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onDragFolderEblanApplicationInfo = onDragFolderEblanApplicationInfo,
                onDragApplicationInfo = onDragApplicationInfo,
                onLongPressApplicationInfo = onLongPressApplicationInfo,
                onLongPressFolderApplicationInfo = onLongPressFolderApplicationInfo,
                onLongPressPrivateSpaceApplicationInfoItem = onLongPressPrivateSpaceApplicationInfoItem,
                onTapFolderApplicationInfo = onTapFolderApplicationInfo,
            )
        }
    }
}

@Composable
private fun EblanApplicationInfosGrid(
    // Layout
    sharedTransitionScope: SharedTransitionScope,
    paddingValues: PaddingValues,
    screenHeight: Int,
    modifier: Modifier = Modifier,
    // State
    swipeY: Float,
    animations: Boolean,
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    appDrawerColumns: Int,
    appDrawerRowsHeight: Int,
    gridItemSettings: GridItemSettings,
    eblanUserPageKey: EblanUserPageKey,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    isVisibleOverlay: Boolean,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    folderBorderColor: BackgroundColor,
    customFolderBorderColor: Int,
    folderBorderWidth: Int,
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    isVisibleFolderEblanApplicationInfos: Boolean,
    folderEblanApplicationInfoEntries: List<FolderEntry>,
    drag: Drag,
    // Callbacks
    onDismiss: () -> Unit,
    onDragEnd: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onDragFolderEblanApplicationInfo: (
        folderId: String,
        movingGridItem: GridItem,
    ) -> Unit,
    onDragApplicationInfo: (GridItem) -> Unit,
    onLongPressApplicationInfo: (
        eblanApplicationInfo: EblanApplicationInfo,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
    onLongPressFolderApplicationInfo: (
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
    onLongPressPrivateSpaceApplicationInfoItem: (
        eblanApplicationInfo: EblanApplicationInfo,
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onTapFolderApplicationInfo: (folderEntry: FolderEntry) -> Unit,
) {
    val lazyGridState = rememberLazyGridState()

    val canScroll = lazyGridState.canScrollForward || lazyGridState.canScrollBackward

    val nestedScrollConnection = rememberNestedScrollConnectionEffect(
        // Scroll State
        canScrollBackward = lazyGridState.canScrollBackward,
        swipeY = swipeY,
        // Drag Callbacks
        onVerticalDrag = onVerticalDrag,
        onDragEnd = onDragEnd,
    )

    val privateIsQuiteModeEnabled =
        getEblanApplicationInfosByLabelAndTag.privateEblanUser?.isQuietModeEnabled == true

    val isDefaultLauncher by produceIsDefaultLauncherState()
    val isScreenDismissed = swipeY.toInt() == screenHeight

    LaunchedEffect(key1 = isScreenDismissed) {
        if (isScreenDismissed) {
            lazyGridState.scrollToItem(0)
        }
    }

    Row(
        modifier = modifier
            .nestedScroll(nestedScrollConnection)
            .fillMaxSize(),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(count = appDrawerColumns),
            state = lazyGridState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
            userScrollEnabled = !isVisibleOverlay,
        ) {
            when (eblanUserPageKey.eblanUser.eblanUserType) {
                EblanUserType.Personal -> {
                    items(
                        items = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos[eblanUserPageKey].orEmpty(),
                        key = { currentAppDrawerGridItem ->
                            currentAppDrawerGridItem.id
                        },
                    ) { currentAppDrawerGridItem ->
                        when (val data = currentAppDrawerGridItem.data) {
                            is AppDrawerGridItemData.ApplicationInfo -> {
                                EblanApplicationInfoItem(
                                    // Application Data and Settings
                                    eblanApplicationInfo = data.eblanApplicationInfo,
                                    gridItemSettings = gridItemSettings,
                                    backgroundColor = backgroundColor,
                                    customBackgroundColor = customBackgroundColor,
                                    iconPackFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackFilePaths,
                                    // Layout and UI State
                                    sharedTransitionScope = sharedTransitionScope,
                                    drag = drag,
                                    paddingValues = paddingValues,
                                    animations = animations,
                                    isVisibleOverlay = isVisibleOverlay,
                                    isScrollInProgress = lazyGridState.isScrollInProgress,
                                    isSwiping = swipeY != 0f,
                                    systemTextColor = systemTextColor,
                                    systemCustomTextColor = systemCustomTextColor,
                                    modifier = Modifier.height(appDrawerRowsHeight.dp),
                                    // Callbacks
                                    onDismiss = onDismiss,
                                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                    onDragApplicationInfo = onDragApplicationInfo,
                                    onLongPressApplicationInfo = onLongPressApplicationInfo,
                                )
                            }

                            is AppDrawerGridItemData.Folder -> {
                                FolderEblanApplicationInfoItem(
                                    // Core Data
                                    folderEblanApplicationInfo = data.folderEblanApplicationInfo,
                                    gridItemSettings = gridItemSettings,
                                    backgroundColor = backgroundColor,
                                    customBackgroundColor = customBackgroundColor,
                                    folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                                    folderEblanApplicationInfoEntries = folderEblanApplicationInfoEntries,
                                    iconPackFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackFilePaths,
                                    // Animation and Transition
                                    sharedTransitionScope = sharedTransitionScope,
                                    animations = animations,
                                    // Visibility and State
                                    isVisibleOverlay = isVisibleOverlay,
                                    isVisibleFolderEblanApplicationInfos = isVisibleFolderEblanApplicationInfos,
                                    isScrollInProgress = lazyGridState.isScrollInProgress,
                                    isSwiping = swipeY != 0f,
                                    // Styling and Appearance
                                    systemTextColor = systemTextColor,
                                    systemCustomTextColor = systemCustomTextColor,
                                    folderCornerRadius = folderCornerRadius,
                                    folderBackgroundColor = folderBackgroundColor,
                                    customFolderBackgroundColor = customFolderBackgroundColor,
                                    folderBorderColor = folderBorderColor,
                                    customFolderBorderColor = customFolderBorderColor,
                                    folderBorderWidth = folderBorderWidth,
                                    // Interaction
                                    drag = drag,
                                    modifier = Modifier.height(appDrawerRowsHeight.dp),
                                    // Callbacks
                                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                    onDragFolderEblanApplicationInfo = onDragFolderEblanApplicationInfo,
                                    onLongPressFolderApplicationInfo = onLongPressFolderApplicationInfo,
                                    onTapFolderApplicationInfo = onTapFolderApplicationInfo,
                                )
                            }
                        }
                    }

                    privateSpace(
                        // User and Application Data
                        privateEblanUser = getEblanApplicationInfosByLabelAndTag.privateEblanUser,
                        privateEblanApplicationInfos = getEblanApplicationInfosByLabelAndTag.privateEblanApplicationInfos,
                        // App Drawer and Appearance Settings
                        gridItemSettings = gridItemSettings,
                        appDrawerRowsHeight = appDrawerRowsHeight,
                        iconPackFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackFilePaths,
                        // Space State
                        isQuietModeEnabled = privateIsQuiteModeEnabled,
                        isVisibleOverlay = isVisibleOverlay,
                        // Layout and Colors
                        paddingValues = paddingValues,
                        backgroundColor = backgroundColor,
                        customBackgroundColor = customBackgroundColor,
                        systemTextColor = systemTextColor,
                        systemCustomTextColor = systemCustomTextColor,
                        // Callbacks
                        onDismiss = onDismiss,
                        onLongPressPrivateSpaceApplicationInfoItem = onLongPressPrivateSpaceApplicationInfoItem,
                    )
                }

                else -> {
                    items(
                        items = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos[eblanUserPageKey].orEmpty(),
                        key = { currentAppDrawerGridItem ->
                            currentAppDrawerGridItem.id
                        },
                    ) { currentAppDrawerGridItem ->
                        when (val data = currentAppDrawerGridItem.data) {
                            is AppDrawerGridItemData.ApplicationInfo -> {
                                EblanApplicationInfoItem(
                                    // Application Data and Settings
                                    eblanApplicationInfo = data.eblanApplicationInfo,
                                    gridItemSettings = gridItemSettings,
                                    backgroundColor = backgroundColor,
                                    customBackgroundColor = customBackgroundColor,
                                    iconPackFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackFilePaths,
                                    // Layout and UI State
                                    sharedTransitionScope = sharedTransitionScope,
                                    drag = drag,
                                    paddingValues = paddingValues,
                                    animations = animations,
                                    isVisibleOverlay = isVisibleOverlay,
                                    isScrollInProgress = lazyGridState.isScrollInProgress,
                                    isSwiping = swipeY != 0f,
                                    systemTextColor = systemTextColor,
                                    systemCustomTextColor = systemCustomTextColor,
                                    modifier = Modifier.height(appDrawerRowsHeight.dp),
                                    // Callbacks
                                    onDismiss = onDismiss,
                                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                                    onDragApplicationInfo = onDragApplicationInfo,
                                    onLongPressApplicationInfo = onLongPressApplicationInfo,
                                )
                            }

                            is AppDrawerGridItemData.Folder -> Unit
                        }
                    }
                }
            }
        }

        ScrollBarThumb(
            // Scroll and List
            lazyGridState = lazyGridState,
            canScroll = canScroll,
            appDrawerColumns = appDrawerColumns,
            // UI Settings
            isPrivateSpaceEntryPointHidden = isDefaultLauncher &&
                eblanUserPageKey.eblanUser.eblanUserType == EblanUserType.Personal &&
                getEblanApplicationInfosByLabelAndTag.privateEblanUser?.let { privateEblanUser ->
                    !(
                        privateEblanUser.isPrivateSpaceEntryPointHidden &&
                            privateEblanUser.isPrivateSpaceLocked
                        )
                } == true,
        )
    }
}
