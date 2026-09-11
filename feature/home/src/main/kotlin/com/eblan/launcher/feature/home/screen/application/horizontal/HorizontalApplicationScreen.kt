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
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.application.GetEblanApplicationInfosByLabelAndTag
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.domain.model.launcherapps.EblanUser
import com.eblan.launcher.domain.model.launcherapps.EblanUserPageKey
import com.eblan.launcher.domain.model.launcherapps.EblanUserType
import com.eblan.launcher.domain.model.launcherapps.ManagedProfileResult
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfo
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfoByGroup
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.domain.model.widget.EblanAppWidgetProviderInfo
import com.eblan.launcher.feature.home.component.HorizontalAppDrawerGridLayout
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.application.ApplicationInfoPopup
import com.eblan.launcher.feature.home.screen.application.ApplicationScreenEffect
import com.eblan.launcher.feature.home.screen.application.ApplicationSearchBar
import com.eblan.launcher.feature.home.screen.application.EblanApplicationInfoItem
import com.eblan.launcher.feature.home.screen.application.EblanApplicationInfoTabRow
import com.eblan.launcher.feature.home.screen.application.PrivateApplicationInfoPopup
import com.eblan.launcher.feature.home.screen.application.PrivateSpaceEblanApplicationInfoItem
import com.eblan.launcher.feature.home.screen.application.QuiteModeScreen
import com.eblan.launcher.feature.home.screen.application.TagElevatedFilterChip
import com.eblan.launcher.feature.home.screen.application.rememberIsQuietModeEnabled
import com.eblan.launcher.ui.local.LocalUserManager
import com.eblan.launcher.ui.settings.rememberIsDefaultLauncher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class, FlowPreview::class)
@Composable
internal fun HorizontalApplicationScreen(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    appDrawerSettings: AppDrawerSettings,
    drag: Drag,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    hasShortcutHostPermission: Boolean,
    managedProfileResult: ManagedProfileResult?,
    paddingValues: PaddingValues,
    screenHeight: Int,
    swipeY: Float,
    isVisibleOverlay: Boolean,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    animations: Boolean,
    onDismiss: () -> Unit,
    onDragEnd: () -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByTagId: (Long?) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onWidgets: (EblanApplicationInfoGroup) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
) {
    var showPopupApplicationMenu by remember { mutableStateOf(false) }

    var showPrivatePopupApplicationMenu by remember { mutableStateOf(false) }

    var popupIntOffset by remember { mutableStateOf(IntOffset.Zero) }

    var popupIntSize by remember { mutableStateOf(IntSize.Zero) }

    val horizontalPagerState = rememberPagerState(
        pageCount = {
            getEblanApplicationInfosByLabelAndTag.eblanApplicationInfoWithIconPackInfos.keys.size
        },
    )

    val searchBarState = rememberSearchBarState()

    val textFieldState = rememberTextFieldState()

    var selectedEblanApplicationInfoTagId by remember { mutableStateOf<Long?>(null) }

    var selectedEblanApplicationInfo by remember { mutableStateOf<EblanApplicationInfo?>(null) }

    val eblanUserPageKeys =
        remember(key1 = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfoWithIconPackInfos) {
            getEblanApplicationInfosByLabelAndTag.eblanApplicationInfoWithIconPackInfos.keys.distinctBy { it.eblanUser.serialNumber }
        }

    val focusRequester = remember { FocusRequester() }

    ApplicationScreenEffect(
        horizontalPagerState = horizontalPagerState,
        screenHeight = screenHeight,
        selectedEblanApplicationInfoTagId = selectedEblanApplicationInfoTagId,
        showPopupApplicationMenu = showPopupApplicationMenu,
        swipeY = swipeY,
        textFieldState = textFieldState,
        showKeyboard = appDrawerSettings.showKeyboard,
        focusRequester = focusRequester,
        onDismiss = onDismiss,
        onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
        onGetEblanApplicationInfosByTagId = onGetEblanApplicationInfosByTagId,
        onShowPopupApplicationMenu = {
            showPopupApplicationMenu = it
        },
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        ApplicationSearchBar(
            focusRequester = focusRequester,
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            backgroundColor = appDrawerSettings.backgroundColor,
            customBackgroundColor = appDrawerSettings.customBackgroundColor,
            systemTextColor = systemTextColor,
            systemCustomTextColor = systemCustomTextColor,
        )

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
                currentPage = horizontalPagerState.currentPage,
                eblanUserPageKeys = eblanUserPageKeys,
                eblanApplicationInfos = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfoWithIconPackInfos,
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
                managedProfileResult = managedProfileResult,
                paddingValues = paddingValues,
                isVisibleOverlay = isVisibleOverlay,
                swipeY = swipeY,
                isScrollInProgress = horizontalPagerState.isScrollInProgress,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                animations = animations,
                onDismiss = onDismiss,
                onDragEnd = onDragEnd,
                onUpdateGridItemSource = onUpdateGridItemSource,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateIsDragging = onUpdateIsDragging,
                onUpdateOverlayBounds = { intOffset, intSize ->
                    onUpdateOverlayBounds(intOffset, intSize)

                    popupIntOffset = intOffset

                    popupIntSize = intSize
                },
                onUpdatePopupMenu = {
                    showPopupApplicationMenu = it
                },
                onUpdatePrivatePopupMenu = {
                    showPrivatePopupApplicationMenu = it
                },
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onVerticalDrag = onVerticalDrag,
                onUpdateEblanApplicationInfo = {
                    selectedEblanApplicationInfo = it
                },
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
            )
        }
    }

    if (showPopupApplicationMenu && selectedEblanApplicationInfo != null) {
        ApplicationInfoPopup(
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfosGroup,
            eblanShortcutInfosGroup = eblanShortcutInfosGroup,
            eblanApplicationInfo = selectedEblanApplicationInfo,
            gridItemSettings = appDrawerSettings.gridItemSettings,
            hasShortcutHostPermission = hasShortcutHostPermission,
            popupIntOffset = popupIntOffset,
            popupIntSize = popupIntSize,
            isVisibleOverlay = isVisibleOverlay,
            paddingValues = paddingValues,
            animations = animations,
            onDismissRequest = {
                showPopupApplicationMenu = false
            },
            onUpdateIsDragging = {
                showPopupApplicationMenu = false

                onDismiss()

                onUpdateIsDragging(it)
            },
            onEditApplicationInfo = onEditApplicationInfo,
            onUpdateGridItemSource = onUpdateGridItemSource,
            onUpdateImageBitmap = onUpdateImageBitmap,
            onUpdateOverlayBounds = onUpdateOverlayBounds,
            onUpdateSharedElementKey = onUpdateSharedElementKey,
            onWidgets = onWidgets,
            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
            onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
        )
    }

    if (showPrivatePopupApplicationMenu && selectedEblanApplicationInfo != null) {
        PrivateApplicationInfoPopup(
            eblanShortcutInfosGroup = eblanShortcutInfosGroup,
            eblanApplicationInfo = selectedEblanApplicationInfo,
            hasShortcutHostPermission = hasShortcutHostPermission,
            popupIntOffset = popupIntOffset,
            popupIntSize = popupIntSize,
            paddingValues = paddingValues,
            onDismissRequest = {
                showPrivatePopupApplicationMenu = false
            },
            onEditApplicationInfo = onEditApplicationInfo,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun EblanApplicationInfosPage(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    appDrawerSettings: AppDrawerSettings,
    drag: Drag,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    index: Int,
    managedProfileResult: ManagedProfileResult?,
    paddingValues: PaddingValues,
    swipeY: Float,
    isVisibleOverlay: Boolean,
    isScrollInProgress: Boolean,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    animations: Boolean,
    onDismiss: () -> Unit,
    onDragEnd: () -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdatePopupMenu: (Boolean) -> Unit,
    onUpdatePrivatePopupMenu: (Boolean) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val userManager = LocalUserManager.current

    val eblanUserPageKey =
        getEblanApplicationInfosByLabelAndTag.eblanApplicationInfoWithIconPackInfos.keys.toList()
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

    val isQuietModeEnabled by rememberIsQuietModeEnabled(
        userHandle = userHandle,
        managedProfileResult = managedProfileResult,
        eblanUser = eblanUserPageKey.eblanUser,
    )

    Box(modifier = modifier.fillMaxSize()) {
        if (isQuietModeEnabled) {
            QuiteModeScreen(
                userHandle = userHandle,
                backgroundColor = appDrawerSettings.backgroundColor,
                customBackgroundColor = appDrawerSettings.customBackgroundColor,
                systemCustomTextColor = systemCustomTextColor,
                systemTextColor = systemTextColor,
                onDragEnd = onDragEnd,
                onVerticalDrag = onVerticalDrag,
            )
        } else {
            EblanApplicationInfos(
                sharedTransitionScope = sharedTransitionScope,
                appDrawerSettings = appDrawerSettings,
                drag = drag,
                eblanUserPageKey = eblanUserPageKey,
                getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
                paddingValues = paddingValues,
                isVisibleOverlay = isVisibleOverlay,
                swipeY = swipeY,
                isScrollInProgress = isScrollInProgress,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                animations = animations,
                onDismiss = onDismiss,
                onDragEnd = onDragEnd,
                onUpdateGridItemSource = onUpdateGridItemSource,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateIsDragging = onUpdateIsDragging,
                onUpdateOverlayBounds = onUpdateOverlayBounds,
                onUpdatePopupMenu = onUpdatePopupMenu,
                onUpdatePrivatePopupMenu = onUpdatePrivatePopupMenu,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onVerticalDrag = onVerticalDrag,
                onUpdateEblanApplicationInfo = onUpdateEblanApplicationInfo,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun EblanApplicationInfos(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    appDrawerSettings: AppDrawerSettings,
    drag: Drag,
    eblanUserPageKey: EblanUserPageKey,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    paddingValues: PaddingValues,
    isVisibleOverlay: Boolean,
    swipeY: Float,
    isScrollInProgress: Boolean,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    animations: Boolean,
    onDismiss: () -> Unit,
    onDragEnd: () -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdatePopupMenu: (Boolean) -> Unit,
    onUpdatePrivatePopupMenu: (Boolean) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
) {
    HorizontalAppDrawerGridLayout(
        modifier = modifier
            .pointerInput(
                key1 = onVerticalDrag,
                key2 = onDragEnd,
            ) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        onVerticalDrag(dragAmount)
                    },
                    onDragEnd = onDragEnd,
                )
            }
            .fillMaxSize(),
        columns = appDrawerSettings.horizontalAppDrawerColumns,
        eblanApplicationInfoWithIconPackInfos = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfoWithIconPackInfos[eblanUserPageKey],
        rows = appDrawerSettings.horizontalAppDrawerRows,
        content = {
            when (eblanUserPageKey.eblanUser.eblanUserType) {
                EblanUserType.Personal,
                EblanUserType.Clone,
                EblanUserType.Work,
                -> {
                    EblanApplicationInfoItem(
                        sharedTransitionScope = sharedTransitionScope,
                        appDrawerSettings = appDrawerSettings,
                        drag = drag,
                        eblanApplicationInfo = it,
                        paddingValues = paddingValues,
                        isVisibleOverlay = isVisibleOverlay,
                        appDrawerType = appDrawerSettings.appDrawerType,
                        isSwiping = swipeY > 0f,
                        isScrollInProgress = isScrollInProgress,
                        systemTextColor = systemTextColor,
                        systemCustomTextColor = systemCustomTextColor,
                        iconPackInfoFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackInfoFilePaths,
                        animations = animations,
                        onDismiss = onDismiss,
                        onUpdateGridItemSource = onUpdateGridItemSource,
                        onUpdateImageBitmap = onUpdateImageBitmap,
                        onUpdateIsDragging = onUpdateIsDragging,
                        onUpdateOverlayBounds = onUpdateOverlayBounds,
                        onUpdatePopupMenu = onUpdatePopupMenu,
                        onUpdateSharedElementKey = onUpdateSharedElementKey,
                        onUpdateEblanApplicationInfo = onUpdateEblanApplicationInfo,
                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                    )
                }

                EblanUserType.Private -> {
                    PrivateSpaceEblanApplicationInfoItem(
                        appDrawerSettings = appDrawerSettings,
                        eblanApplicationInfo = it,
                        paddingValues = paddingValues,
                        isVisibleOverlay = isVisibleOverlay,
                        systemTextColor = systemTextColor,
                        systemCustomTextColor = systemCustomTextColor,
                        iconPackInfoFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackInfoFilePaths,
                        animations = animations,
                        onUpdateOverlayBounds = onUpdateOverlayBounds,
                        onUpdatePopupMenu = onUpdatePrivatePopupMenu,
                        onUpdateEblanApplicationInfo = onUpdateEblanApplicationInfo,
                    )
                }
            }
        },
    )
}
