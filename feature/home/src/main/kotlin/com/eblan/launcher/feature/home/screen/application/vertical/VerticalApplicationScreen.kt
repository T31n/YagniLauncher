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
    // Transition and Layout
    sharedTransitionScope: SharedTransitionScope,
    screenHeight: Int,
    paddingValues: PaddingValues,
    // Application Data
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    // Folder
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    isVisibleFolderEblanApplicationInfos: Boolean,
    folderEblanApplicationInfoEntries: List<FolderEntry>,
    // Appearance
    appDrawerSettings: AppDrawerSettings,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    animations: Boolean,
    // Interaction State
    drag: Drag,
    swipeY: Float,
    isVisibleOverlay: Boolean,
    // Modifier
    modifier: Modifier = Modifier,
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
    onTapFolderApplicationInfo: (folderEntry: FolderEntry) -> Unit
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
        horizontalPagerState = horizontalPagerState,
        screenHeight = screenHeight,
        selectedEblanApplicationInfoTagId = selectedEblanApplicationInfoTagId,
        swipeY = swipeY,
        textFieldState = textFieldState,
        showKeyboard = appDrawerSettings.showKeyboard &&
                appDrawerSettings.searchBarPosition != SearchBarPosition.None,
        focusRequester = focusRequester,
        onDismiss = onDismiss,
        onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
        onGetEblanApplicationInfosByTagId = onGetEblanApplicationInfosByTagId
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
                backgroundColor = appDrawerSettings.backgroundColor,
                customBackgroundColor = appDrawerSettings.customBackgroundColor,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                defaultColor = MaterialTheme.colorScheme.onSurface
            ),
        )

        if (appDrawerSettings.searchBarPosition == SearchBarPosition.Top) {
            ApplicationSearchBar(
                searchBarState = searchBarState,
                textFieldState = textFieldState,
                focusRequester = focusRequester,
                backgroundColor = appDrawerSettings.backgroundColor,
                customBackgroundColor = appDrawerSettings.customBackgroundColor,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            if (eblanApplicationInfoTags.isNotEmpty()) {
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(eblanApplicationInfoTags) {
                        TagElevatedFilterChip(
                            eblanApplicationInfoTag = it,
                            selectedEblanApplicationInfoTag = selectedEblanApplicationInfoTagId,
                            onUpdateEblanApplicationInfoTag = { id ->
                                selectedEblanApplicationInfoTagId = id
                            }
                        )
                    }
                }
            }

            if (eblanUserPageKeys.size > 1) {
                EblanApplicationInfoTabRow(
                    eblanUserPageKeys = eblanUserPageKeys,
                    eblanApplicationInfos = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos,
                    currentPage = horizontalPagerState.currentPage,
                    backgroundColor = appDrawerSettings.backgroundColor,
                    customBackgroundColor = appDrawerSettings.customBackgroundColor,
                    systemTextColor = systemTextColor,
                    systemCustomTextColor = systemCustomTextColor,
                    onAnimateScrollToPage = horizontalPagerState::animateScrollToPage
                )
            }

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = horizontalPagerState,
                userScrollEnabled = !isVisibleOverlay,
            ) { index ->
                EblanApplicationInfosPage(
                    sharedTransitionScope = sharedTransitionScope,
                    paddingValues = paddingValues,
                    screenHeight = screenHeight,
                    index = index,
                    isVisibleOverlay = isVisibleOverlay,
                    swipeY = swipeY,
                    animations = animations,
                    isVisibleFolderEblanApplicationInfos = isVisibleFolderEblanApplicationInfos,
                    appDrawerSettings = appDrawerSettings,
                    getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
                    folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                    folderCornerRadius = folderCornerRadius,
                    folderBackgroundColor = folderBackgroundColor,
                    customFolderBackgroundColor = customFolderBackgroundColor,
                    folderEblanApplicationInfoEntries = folderEblanApplicationInfoEntries,
                    systemTextColor = systemTextColor,
                    systemCustomTextColor = systemCustomTextColor,
                    drag = drag,
                    onDragEnd = onDragEnd,
                    onVerticalDrag = onVerticalDrag,
                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                    onDragFolderEblanApplicationInfo = onDragFolderEblanApplicationInfo,
                    onDragApplicationInfo = onDragApplicationInfo,
                    onLongPressApplicationInfo = onLongPressApplicationInfo,
                    onLongPressFolderApplicationInfo = onLongPressFolderApplicationInfo,
                    onLongPressPrivateSpaceApplicationInfoItem = onLongPressPrivateSpaceApplicationInfoItem,
                    onTapFolderApplicationInfo = onTapFolderApplicationInfo
                )
            }
        }

        if (appDrawerSettings.searchBarPosition == SearchBarPosition.Bottom) {
            ApplicationSearchBar(
                searchBarState = searchBarState,
                textFieldState = textFieldState,
                focusRequester = focusRequester,
                backgroundColor = appDrawerSettings.backgroundColor,
                customBackgroundColor = appDrawerSettings.customBackgroundColor,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor
            )
        }
    }
}

@Composable
private fun EblanApplicationInfosPage(
    // Layout
    sharedTransitionScope: SharedTransitionScope,
    paddingValues: PaddingValues,
    screenHeight: Int,
    // State
    index: Int,
    isVisibleOverlay: Boolean,
    swipeY: Float,
    animations: Boolean,
    isVisibleFolderEblanApplicationInfos: Boolean,
    // Content & Data
    appDrawerSettings: AppDrawerSettings,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    folderEblanApplicationInfoEntries: List<FolderEntry>,
    // Styling
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // Interactions
    drag: Drag,
    // Layout
    modifier: Modifier = Modifier,
    // Interactions
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
    onTapFolderApplicationInfo: (folderEntry: FolderEntry) -> Unit
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
                userHandle = userHandle,
                backgroundColor = appDrawerSettings.backgroundColor,
                customBackgroundColor = appDrawerSettings.customBackgroundColor,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                onDragEnd = onDragEnd,
                onVerticalDrag = onVerticalDrag
            )
        } else {
            EblanApplicationInfos(
                sharedTransitionScope = sharedTransitionScope,
                paddingValues = paddingValues,
                screenHeight = screenHeight,
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
                onDragEnd = onDragEnd,
                onVerticalDrag = onVerticalDrag,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onDragFolderEblanApplicationInfo = onDragFolderEblanApplicationInfo,
                onDragApplicationInfo = onDragApplicationInfo,
                onLongPressApplicationInfo = onLongPressApplicationInfo,
                onLongPressFolderApplicationInfo = onLongPressFolderApplicationInfo,
                onLongPressPrivateSpaceApplicationInfoItem = onLongPressPrivateSpaceApplicationInfoItem,
                onTapFolderApplicationInfo = onTapFolderApplicationInfo
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
    // Layout & Animation
    sharedTransitionScope: SharedTransitionScope,
    paddingValues: PaddingValues,
    screenHeight: Int,
    swipeY: Float,
    animations: Boolean,
    // Navigation & State
    appDrawerSettings: AppDrawerSettings,
    eblanUserPageKey: EblanUserPageKey,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    isVisibleOverlay: Boolean,
    // Theme & Styling
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    // Folder & Content
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    isVisibleFolderEblanApplicationInfos: Boolean,
    folderEblanApplicationInfoEntries: List<FolderEntry>,
    // Interactions
    drag: Drag,
    // Layout & Animation
    modifier: Modifier = Modifier,
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
    onTapFolderApplicationInfo: (folderEntry: FolderEntry) -> Unit
) {
    val lazyGridState = rememberLazyGridState()

    val canScroll by remember(key1 = lazyGridState) {
        derivedStateOf {
            lazyGridState.canScrollForward || lazyGridState.canScrollBackward
        }
    }

    val nestedScrollConnection = rememberNestedScrollConnectionEffect(
        scrollableState = lazyGridState,
        swipeY = swipeY,
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
                            sharedTransitionScope = sharedTransitionScope,
                            folderEblanApplicationInfo = it,
                            appDrawerSettings = appDrawerSettings,
                            isVisibleOverlay = isVisibleOverlay,
                            isVisibleFolderEblanApplicationInfos = isVisibleFolderEblanApplicationInfos,
                            animations = animations,
                            isScrollInProgress = lazyGridState.isScrollInProgress,
                            isSwiping = swipeY > 0f,
                            drag = drag,
                            folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                            folderEblanApplicationInfoEntries = folderEblanApplicationInfoEntries,
                            iconPackFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackFilePaths,
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                            folderCornerRadius = folderCornerRadius,
                            folderBackgroundColor = folderBackgroundColor,
                            customFolderBackgroundColor = customFolderBackgroundColor,
                            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                            onDragFolderEblanApplicationInfo = onDragFolderEblanApplicationInfo,
                            onLongPressFolderApplicationInfo = onLongPressFolderApplicationInfo,
                            onTapFolderApplicationInfo = onTapFolderApplicationInfo
                        )
                    }

                    items(
                        items = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos[eblanUserPageKey].orEmpty(),
                        key = { it.serialNumber to it.componentName },
                    ) {
                        EblanApplicationInfoItem(
                            eblanApplicationInfo = it,
                            sharedTransitionScope = sharedTransitionScope,
                            drag = drag,
                            paddingValues = paddingValues,
                            animations = animations,
                            appDrawerSettings = appDrawerSettings,
                            isVisibleOverlay = isVisibleOverlay,
                            isScrollInProgress = lazyGridState.isScrollInProgress,
                            isSwiping = swipeY > 0f,
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                            iconPackFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackFilePaths,
                            modifier = Modifier.height(appDrawerSettings.appDrawerRowsHeight.dp),
                            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                            onDragApplicationInfo = onDragApplicationInfo,
                            onLongPressApplicationInfo = onLongPressApplicationInfo
                        )
                    }

                    privateSpace(
                        privateEblanUser = getEblanApplicationInfosByLabelAndTag.privateEblanUser,
                        privateEblanApplicationInfos = getEblanApplicationInfosByLabelAndTag.privateEblanApplicationInfos,
                        appDrawerSettings = appDrawerSettings,
                        iconPackFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackFilePaths,
                        isQuietModeEnabled = privateIsQuiteModeEnabled,
                        isVisibleOverlay = isVisibleOverlay,
                        paddingValues = paddingValues,
                        backgroundColor = appDrawerSettings.backgroundColor,
                        customBackgroundColor = appDrawerSettings.customBackgroundColor,
                        systemTextColor = systemTextColor,
                        systemCustomTextColor = systemCustomTextColor,
                        onLongPressPrivateSpaceApplicationInfoItem = onLongPressPrivateSpaceApplicationInfoItem
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
                            eblanApplicationInfo = it,
                            sharedTransitionScope = sharedTransitionScope,
                            drag = drag,
                            paddingValues = paddingValues,
                            animations = animations,
                            appDrawerSettings = appDrawerSettings,
                            isVisibleOverlay = isVisibleOverlay,
                            isScrollInProgress = lazyGridState.isScrollInProgress,
                            isSwiping = swipeY > 0f,
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                            iconPackFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackFilePaths,
                            modifier = Modifier.height(appDrawerSettings.appDrawerRowsHeight.dp),
                            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                            onDragApplicationInfo = onDragApplicationInfo,
                            onLongPressApplicationInfo = onLongPressApplicationInfo
                        )
                    }
                }
            }
        }

        ScrollBarType(
            alphabeticalScrollBarItems = alphabeticalScrollBarItems,
            lazyGridState = lazyGridState,
            canScroll = canScroll,
            appDrawerSettings = appDrawerSettings,
            paddingValues = paddingValues,
            isPrivateSpaceEntryPointHidden = isDefaultLauncher &&
                    eblanUserPageKey.eblanUser.eblanUserType == EblanUserType.Personal &&
                    getEblanApplicationInfosByLabelAndTag.privateEblanUser
                        ?.isPrivateSpaceEntryPointHidden == false
        )
    }
}
