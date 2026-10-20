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
package com.eblan.launcher.feature.home.screen.application.horizontal

import android.os.Build
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.application.GetEblanApplicationInfosByLabelAndTag
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.launcherapps.EblanUser
import com.eblan.launcher.domain.model.launcherapps.EblanUserPageKey
import com.eblan.launcher.domain.model.launcherapps.EblanUserType
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.SearchBarPosition
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.HorizontalAppDrawerGridLayout
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.application.ApplicationScreenEffect
import com.eblan.launcher.feature.home.screen.application.ApplicationSearchBar
import com.eblan.launcher.feature.home.screen.application.EblanApplicationInfoItem
import com.eblan.launcher.feature.home.screen.application.EblanApplicationInfoTabRow
import com.eblan.launcher.feature.home.screen.application.PrivateSpaceEblanApplicationInfoItem
import com.eblan.launcher.feature.home.screen.application.QuiteModeScreen
import com.eblan.launcher.feature.home.screen.application.TagElevatedFilterChip
import com.eblan.launcher.feature.home.screen.application.rememberIsQuietModeEnabled
import com.eblan.launcher.feature.home.util.getApplicationScreenTextColor
import com.eblan.launcher.ui.local.LocalUserManager
import com.eblan.launcher.ui.settings.rememberIsDefaultLauncher
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HorizontalApplicationScreen(
    // Layout and Sizing
    sharedTransitionScope: SharedTransitionScope,
    paddingValues: PaddingValues,
    screenHeight: Int,
    // Application Data
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    // Appearance and Styling
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    animations: Boolean,
    // State
    appDrawerSettings: AppDrawerSettings,
    drag: Drag,
    swipeY: Float,
    isVisibleOverlay: Boolean,
    // Layout and Sizing
    modifier: Modifier = Modifier,
    // Callbacks
    onDismiss: () -> Unit,
    onDragEnd: () -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByTagId: (Long?) -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onDragApplicationInfo: (GridItem) -> Unit,
    onLongPressApplicationInfo: (
        eblanApplicationInfo: EblanApplicationInfo,
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
            )
            .pointerInput(
                key1 = onVerticalDrag,
                key2 = onDragEnd,
            ) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        onVerticalDrag(dragAmount)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd,
                )
            },
    ) {
        DragHandle(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = getApplicationScreenTextColor(
                backgroundColor = appDrawerSettings.backgroundColor,
                customBackgroundColor = appDrawerSettings.customBackgroundColor,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                defaultColor = MaterialTheme.colorScheme.onSurface,
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
                systemCustomTextColor = systemCustomTextColor,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            if (eblanApplicationInfoTags.isNotEmpty()) {
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(eblanApplicationInfoTags) { eblanApplicationInfoTag ->
                        TagElevatedFilterChip(
                            eblanApplicationInfoTag = eblanApplicationInfoTag,
                            selectedEblanApplicationInfoTag = selectedEblanApplicationInfoTagId,
                            onUpdateEblanApplicationInfoTag = {
                                selectedEblanApplicationInfoTagId = it
                            },
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
                    onAnimateScrollToPage = horizontalPagerState::animateScrollToPage,
                )
            }

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = horizontalPagerState,
                userScrollEnabled = !isVisibleOverlay,
            ) { index ->
                EblanApplicationInfosPage(
                    sharedTransitionScope = sharedTransitionScope,
                    appDrawerSettings = appDrawerSettings,
                    drag = drag,
                    getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
                    index = index,
                    paddingValues = paddingValues,
                    swipeY = swipeY,
                    isVisibleOverlay = isVisibleOverlay,
                    isScrollInProgress = horizontalPagerState.isScrollInProgress,
                    systemTextColor = systemTextColor,
                    systemCustomTextColor = systemCustomTextColor,
                    animations = animations,
                    onDragEnd = onDragEnd,
                    onVerticalDrag = onVerticalDrag,
                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                    onDragApplicationInfo = onDragApplicationInfo,
                    onLongPressApplicationInfo = onLongPressApplicationInfo,
                    onLongPressPrivateSpaceApplicationInfoItem = onLongPressPrivateSpaceApplicationInfoItem,
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
                systemCustomTextColor = systemCustomTextColor,
            )
        }
    }
}

@Composable
private fun EblanApplicationInfosPage(
    // Required Parameters
    sharedTransitionScope: SharedTransitionScope,
    appDrawerSettings: AppDrawerSettings,
    drag: Drag,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    index: Int,
    paddingValues: PaddingValues,
    swipeY: Float,
    isVisibleOverlay: Boolean,
    isScrollInProgress: Boolean,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    animations: Boolean,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onDragEnd: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onDragApplicationInfo: (GridItem) -> Unit,
    onLongPressApplicationInfo: (
        eblanApplicationInfo: EblanApplicationInfo,
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
                onVerticalDrag = onVerticalDrag,
            )
        } else {
            EblanApplicationInfos(
                eblanUserPageKey = eblanUserPageKey,
                sharedTransitionScope = sharedTransitionScope,
                appDrawerSettings = appDrawerSettings,
                paddingValues = paddingValues,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                animations = animations,
                getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
                isVisibleOverlay = isVisibleOverlay,
                drag = drag,
                swipeY = swipeY,
                isScrollInProgress = isScrollInProgress,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onDragApplicationInfo = onDragApplicationInfo,
                onLongPressApplicationInfo = onLongPressApplicationInfo,
                onLongPressPrivateSpaceApplicationInfoItem = onLongPressPrivateSpaceApplicationInfoItem,
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
    // Navigation and State
    eblanUserPageKey: EblanUserPageKey,
    sharedTransitionScope: SharedTransitionScope,
    appDrawerSettings: AppDrawerSettings,
    // Layout and Appearance
    paddingValues: PaddingValues,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    animations: Boolean,
    // Interaction State
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    isVisibleOverlay: Boolean,
    drag: Drag,
    swipeY: Float,
    isScrollInProgress: Boolean,
    // Layout and Appearance
    modifier: Modifier = Modifier,
    // Callbacks
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onDragApplicationInfo: (GridItem) -> Unit,
    onLongPressApplicationInfo: (
        eblanApplicationInfo: EblanApplicationInfo,
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
) {
    HorizontalAppDrawerGridLayout(
        eblanApplicationInfos = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos[eblanUserPageKey],
        rows = appDrawerSettings.horizontalAppDrawerRows,
        columns = appDrawerSettings.horizontalAppDrawerColumns,
        modifier = modifier.fillMaxSize(),
        content = {
            when (eblanUserPageKey.eblanUser.eblanUserType) {
                EblanUserType.Personal,
                EblanUserType.Clone,
                EblanUserType.Work,
                -> {
                    EblanApplicationInfoItem(
                        eblanApplicationInfo = it,
                        sharedTransitionScope = sharedTransitionScope,
                        drag = drag,
                        paddingValues = paddingValues,
                        animations = animations,
                        appDrawerSettings = appDrawerSettings,
                        isVisibleOverlay = isVisibleOverlay,
                        isScrollInProgress = isScrollInProgress,
                        isSwiping = swipeY > 0f,
                        systemTextColor = systemTextColor,
                        systemCustomTextColor = systemCustomTextColor,
                        iconPackFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackFilePaths,
                        modifier = Modifier.fillMaxSize(),
                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                        onDragApplicationInfo = onDragApplicationInfo,
                        onLongPressApplicationInfo = onLongPressApplicationInfo,
                    )
                }

                EblanUserType.Private -> {
                    PrivateSpaceEblanApplicationInfoItem(
                        eblanApplicationInfo = it,
                        appDrawerSettings = appDrawerSettings,
                        iconPackFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackFilePaths,
                        isVisibleOverlay = isVisibleOverlay,
                        systemTextColor = systemTextColor,
                        systemCustomTextColor = systemCustomTextColor,
                        paddingValues = paddingValues,
                        onLongPressPrivateSpaceApplicationInfoItem = onLongPressPrivateSpaceApplicationInfoItem,
                    )
                }
            }
        },
    )
}
