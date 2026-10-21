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

import android.os.Build
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.application.GetEblanApplicationInfosByLabelAndTag
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.launcherapps.EblanUser
import com.eblan.launcher.domain.model.launcherapps.EblanUserPageKey
import com.eblan.launcher.domain.model.launcherapps.EblanUserType
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.SearchBarPosition
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.rememberNestedScrollConnectionEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.application.ApplicationScreenEffect
import com.eblan.launcher.feature.home.screen.application.ApplicationSearchBar
import com.eblan.launcher.feature.home.screen.application.EblanApplicationInfoItem
import com.eblan.launcher.feature.home.screen.application.EblanApplicationInfoTabRow
import com.eblan.launcher.feature.home.screen.application.FolderEblanApplicationInfoItem
import com.eblan.launcher.feature.home.screen.application.QuiteModeScreen
import com.eblan.launcher.feature.home.screen.application.TagElevatedFilterChip
import com.eblan.launcher.feature.home.screen.application.privateSpace
import com.eblan.launcher.feature.home.screen.application.rememberIsPrivateQuietModeEnabled
import com.eblan.launcher.feature.home.screen.application.rememberIsQuietModeEnabled
import com.eblan.launcher.feature.home.util.getApplicationScreenTextColor
import com.eblan.launcher.ui.local.LocalUserManager
import com.eblan.launcher.ui.settings.rememberIsDefaultLauncher
import kotlinx.coroutines.launch

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
    // Folder Configuration
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    isVisibleFolderEblanApplicationInfos: Boolean,
    folderEblanApplicationInfoEntries: List<FolderEntry>,
    // System and Appearance
    appDrawerSettings: AppDrawerSettings,
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
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByTagId: (Long?) -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onDragFolderEblanApplicationInfo: (
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
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

    val horizontalPagerState = rememberPagerState(
        pageCount = {
            getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos.keys.size
        },
    )

    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()

    var selectedEblanApplicationInfoTagId by remember { mutableStateOf<Long?>(null) }

    val eblanUserPageKeys =
        remember(key1 = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos) {
            getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos.keys.distinctBy { it.eblanUser.serialNumber }
        }

    val focusRequester = remember { FocusRequester() }

    ApplicationScreenEffect(
        // Pager and Dimensions
        horizontalPagerState = horizontalPagerState,
        screenHeight = screenHeight,
        swipeY = swipeY,
        // State and Focus
        selectedEblanApplicationInfoTagId = selectedEblanApplicationInfoTagId,
        textFieldState = textFieldState,
        showKeyboard = appDrawerSettings.showKeyboard &&
            appDrawerSettings.searchBarPosition != SearchBarPosition.None,
        focusRequester = focusRequester,
        // Callbacks
        onDismiss = onDismiss,
        onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
        onGetEblanApplicationInfosByTagId = onGetEblanApplicationInfosByTagId,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = paddingValues.calculateStartPadding(layoutDirection),
                end = paddingValues.calculateEndPadding(layoutDirection),
                bottom = if (appDrawerSettings.searchBarPosition == SearchBarPosition.Bottom) {
                    paddingValues.calculateBottomPadding()
                } else {
                    0.dp
                },
            ),
    ) {
        DragHandle(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = getApplicationScreenTextColor(
                // Background Colors
                backgroundColor = appDrawerSettings.backgroundColor,
                customBackgroundColor = appDrawerSettings.customBackgroundColor,
                // Text Colors
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                // Defaults
                defaultColor = MaterialTheme.colorScheme.onSurface,
            ),
        )

        if (appDrawerSettings.searchBarPosition == SearchBarPosition.Top) {
            ApplicationSearchBar(
                // State and Focus
                searchBarState = searchBarState,
                textFieldState = textFieldState,
                focusRequester = focusRequester,
                // Colors
                backgroundColor = appDrawerSettings.backgroundColor,
                customBackgroundColor = appDrawerSettings.customBackgroundColor,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            if (eblanApplicationInfoTags.isNotEmpty()) {
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(eblanApplicationInfoTags) {
                        TagElevatedFilterChip(
                            // Data
                            eblanApplicationInfoTag = it,
                            selectedEblanApplicationInfoTag = selectedEblanApplicationInfoTagId,
                            // Callbacks
                            onUpdateEblanApplicationInfoTag = { id ->
                                selectedEblanApplicationInfoTagId = id
                            },
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
                    backgroundColor = appDrawerSettings.backgroundColor,
                    customBackgroundColor = appDrawerSettings.customBackgroundColor,
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
                    appDrawerSettings = appDrawerSettings,
                    getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
                    folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                    // Folder Appearance
                    folderCornerRadius = folderCornerRadius,
                    folderBackgroundColor = folderBackgroundColor,
                    customFolderBackgroundColor = customFolderBackgroundColor,
                    folderEblanApplicationInfoEntries = folderEblanApplicationInfoEntries,
                    // System Colors
                    systemTextColor = systemTextColor,
                    systemCustomTextColor = systemCustomTextColor,
                    // Drag and Drop State
                    drag = drag,
                    // Drag and Drop Callbacks
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

        if (appDrawerSettings.searchBarPosition == SearchBarPosition.Bottom) {
            ApplicationSearchBar(
                // State and Focus
                searchBarState = searchBarState,
                textFieldState = textFieldState,
                focusRequester = focusRequester,
                // Colors
                backgroundColor = appDrawerSettings.backgroundColor,
                customBackgroundColor = appDrawerSettings.customBackgroundColor,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
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
    appDrawerSettings: AppDrawerSettings,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    // Folder Appearance
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    folderEblanApplicationInfoEntries: List<FolderEntry>,
    // System Colors
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // Drag and Drop State
    drag: Drag,
    // Drag and Drop Callbacks
    onDragEnd: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onDragFolderEblanApplicationInfo: (
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
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
    val scope = rememberCoroutineScope()

    val userManager = LocalUserManager.current

    val eblanUserPageKey =
        getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos.keys.toList()
            .getOrElse(
                index = index,
                defaultValue = {
                    EblanUserPageKey(
                        eblanUser = EblanUser(
                            serialNumber = 0L,
                            eblanUserType = EblanUserType.Personal,
                            isPrivateSpaceEntryPointHidden = false,
                        ),
                        page = 0,
                    )
                },
            )

    val userHandle =
        userManager.getUserForSerialNumber(serialNumber = eblanUserPageKey.eblanUser.serialNumber)

    val isDefaultLauncher by rememberIsDefaultLauncher()

    val isQuietModeEnabled by rememberIsQuietModeEnabled(userHandle = userHandle)

    Box(modifier = modifier.fillMaxSize()) {
        if (isQuietModeEnabled) {
            QuiteModeScreen(
                // User
                userHandle = userHandle,
                // Appearance
                backgroundColor = appDrawerSettings.backgroundColor,
                customBackgroundColor = appDrawerSettings.customBackgroundColor,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                // Callbacks
                onDragEnd = onDragEnd,
                onVerticalDrag = onVerticalDrag,
            )
        } else {
            EblanApplicationInfos(
                // Layout
                sharedTransitionScope = sharedTransitionScope,
                paddingValues = paddingValues,
                screenHeight = screenHeight,
                // State
                swipeY = swipeY,
                animations = animations,
                appDrawerSettings = appDrawerSettings,
                eblanUserPageKey = eblanUserPageKey,
                getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
                isVisibleOverlay = isVisibleOverlay,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                folderCornerRadius = folderCornerRadius,
                folderBackgroundColor = folderBackgroundColor,
                customFolderBackgroundColor = customFolderBackgroundColor,
                folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                isVisibleFolderEblanApplicationInfos = isVisibleFolderEblanApplicationInfos,
                folderEblanApplicationInfoEntries = folderEblanApplicationInfoEntries,
                drag = drag,
                // Callbacks
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isDefaultLauncher &&
                eblanUserPageKey.eblanUser.serialNumber > 0 && userHandle != null
            ) {
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            end = 10.dp,
                            bottom = paddingValues.calculateBottomPadding() + 10.dp,
                        ),
                    onClick = {
                        scope.launch {
                            userManager.requestQuietModeEnabled(
                                enableQuiteMode = true,
                                userHandle = userHandle,
                            )
                        }
                    },
                ) {
                    Icon(
                        imageVector = EblanLauncherIcons.WorkOff,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
private fun EblanApplicationInfos(
    // Layout
    sharedTransitionScope: SharedTransitionScope,
    paddingValues: PaddingValues,
    screenHeight: Int,
    modifier: Modifier = Modifier,
    // State
    swipeY: Float,
    animations: Boolean,
    appDrawerSettings: AppDrawerSettings,
    eblanUserPageKey: EblanUserPageKey,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    isVisibleOverlay: Boolean,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    isVisibleFolderEblanApplicationInfos: Boolean,
    folderEblanApplicationInfoEntries: List<FolderEntry>,
    drag: Drag,
    // Callbacks
    onDragEnd: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onDragFolderEblanApplicationInfo: (
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
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

    val canScroll by remember(key1 = lazyGridState) {
        derivedStateOf {
            lazyGridState.canScrollForward || lazyGridState.canScrollBackward
        }
    }

    val nestedScrollConnection = rememberNestedScrollConnectionEffect(
        // Scroll State
        scrollableState = lazyGridState,
        swipeY = swipeY,
        // Drag Callbacks
        onVerticalDrag = onVerticalDrag,
        onDragEnd = onDragEnd,
    )

    val privateIsQuiteModeEnabled by rememberIsPrivateQuietModeEnabled(eblanUser = getEblanApplicationInfosByLabelAndTag.privateEblanUser)

    val bottomPadding = if (appDrawerSettings.searchBarPosition == SearchBarPosition.Bottom) {
        0.dp
    } else {
        paddingValues.calculateBottomPadding()
    }

    val alphabeticalScrollBarItems =
        getEblanApplicationInfosByLabelAndTag.alphabeticalScrollBarItems[eblanUserPageKey].orEmpty()

    val isDefaultLauncher by rememberIsDefaultLauncher()

    LaunchedEffect(key1 = swipeY) {
        if (swipeY.toInt() == screenHeight) {
            lazyGridState.scrollToItem(0)
        }
    }

    Row(
        modifier = modifier
            .nestedScroll(nestedScrollConnection)
            .fillMaxSize(),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(count = appDrawerSettings.appDrawerColumns),
            state = lazyGridState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = bottomPadding),
            userScrollEnabled = !isVisibleOverlay,
        ) {
            when (eblanUserPageKey.eblanUser.eblanUserType) {
                EblanUserType.Personal -> {
                    items(
                        items = getEblanApplicationInfosByLabelAndTag.folderEblanApplicationInfos,
                        key = { it.id },
                    ) {
                        FolderEblanApplicationInfoItem(
                            // Core Data
                            folderEblanApplicationInfo = it,
                            appDrawerSettings = appDrawerSettings,
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
                            isSwiping = swipeY > 0f,
                            // Styling and Appearance
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                            folderCornerRadius = folderCornerRadius,
                            folderBackgroundColor = folderBackgroundColor,
                            customFolderBackgroundColor = customFolderBackgroundColor,
                            // Interaction
                            drag = drag,
                            // Callbacks
                            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                            onDragFolderEblanApplicationInfo = onDragFolderEblanApplicationInfo,
                            onLongPressFolderApplicationInfo = onLongPressFolderApplicationInfo,
                            onTapFolderApplicationInfo = onTapFolderApplicationInfo,
                        )
                    }

                    items(
                        items = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos[eblanUserPageKey].orEmpty(),
                        key = { it.serialNumber to it.componentName },
                    ) {
                        EblanApplicationInfoItem(
                            // Application Data and Settings
                            eblanApplicationInfo = it,
                            appDrawerSettings = appDrawerSettings,
                            iconPackFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackFilePaths,
                            // Layout and UI State
                            sharedTransitionScope = sharedTransitionScope,
                            drag = drag,
                            paddingValues = paddingValues,
                            animations = animations,
                            isVisibleOverlay = isVisibleOverlay,
                            isScrollInProgress = lazyGridState.isScrollInProgress,
                            isSwiping = swipeY > 0f,
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                            modifier = Modifier.height(appDrawerSettings.appDrawerRowsHeight.dp),
                            // Callbacks
                            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                            onDragApplicationInfo = onDragApplicationInfo,
                            onLongPressApplicationInfo = onLongPressApplicationInfo,
                        )
                    }

                    privateSpace(
                        // User and Application Data
                        privateEblanUser = getEblanApplicationInfosByLabelAndTag.privateEblanUser,
                        privateEblanApplicationInfos = getEblanApplicationInfosByLabelAndTag.privateEblanApplicationInfos,
                        // App Drawer and Appearance Settings
                        appDrawerSettings = appDrawerSettings,
                        iconPackFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackFilePaths,
                        // Space State
                        isQuietModeEnabled = privateIsQuiteModeEnabled,
                        isVisibleOverlay = isVisibleOverlay,
                        // Layout and Colors
                        paddingValues = paddingValues,
                        backgroundColor = appDrawerSettings.backgroundColor,
                        customBackgroundColor = appDrawerSettings.customBackgroundColor,
                        systemTextColor = systemTextColor,
                        systemCustomTextColor = systemCustomTextColor,
                        // Callbacks
                        onLongPressPrivateSpaceApplicationInfoItem = onLongPressPrivateSpaceApplicationInfoItem,
                    )
                }

                else -> {
                    items(
                        items = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos[eblanUserPageKey].orEmpty(),
                        key = {
                            it.serialNumber to
                                it.componentName
                        },
                    ) {
                        EblanApplicationInfoItem(
                            // Application Data and Settings
                            eblanApplicationInfo = it,
                            appDrawerSettings = appDrawerSettings,
                            iconPackFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackFilePaths,
                            // Layout and UI State
                            sharedTransitionScope = sharedTransitionScope,
                            drag = drag,
                            paddingValues = paddingValues,
                            animations = animations,
                            isVisibleOverlay = isVisibleOverlay,
                            isScrollInProgress = lazyGridState.isScrollInProgress,
                            isSwiping = swipeY > 0f,
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                            modifier = Modifier.height(appDrawerSettings.appDrawerRowsHeight.dp),
                            // Callbacks
                            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                            onDragApplicationInfo = onDragApplicationInfo,
                            onLongPressApplicationInfo = onLongPressApplicationInfo,
                        )
                    }
                }
            }
        }

        ScrollBarType(
            // Scroll and List
            alphabeticalScrollBarItems = alphabeticalScrollBarItems,
            lazyGridState = lazyGridState,
            canScroll = canScroll,
            // UI Settings
            appDrawerSettings = appDrawerSettings,
            paddingValues = paddingValues,
            isPrivateSpaceEntryPointHidden = isDefaultLauncher &&
                eblanUserPageKey.eblanUser.eblanUserType == EblanUserType.Personal &&
                getEblanApplicationInfosByLabelAndTag.privateEblanUser
                    ?.isPrivateSpaceEntryPointHidden == false,
        )
    }
}
