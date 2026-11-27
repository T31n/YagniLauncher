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
package com.eblan.launcher.feature.home.screen.application

import android.os.Build
import android.os.UserHandle
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.designsystem.theme.EblanSpacing
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
import com.eblan.launcher.domain.model.userdata.AppDrawerType
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.SearchBarType
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.R
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.component.getEblanUserTypeTitle
import com.eblan.launcher.feature.home.dialog.SearchApplicationsDialog
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.application.horizontal.HorizontalApplicationScreen
import com.eblan.launcher.feature.home.screen.application.vertical.VerticalApplicationScreen
import com.eblan.launcher.feature.home.util.getApplicationScreenTextColor
import com.eblan.launcher.feature.home.util.getBackgroundColor
import com.eblan.launcher.ui.local.LocalUserManager
import com.eblan.launcher.ui.settings.produceIsDefaultLauncherState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun ApplicationScreen(
    // Layout and Transition
    sharedTransitionScope: SharedTransitionScope,
    screenHeight: Int,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    // Visual and Animation
    animations: Boolean,
    alpha: Float,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // Application Data
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    appDrawerSettings: AppDrawerSettings,
    // Folder Settings
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    folderCornerRadius: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    folderBorderColor: BackgroundColor,
    customFolderBorderColor: Int,
    folderBorderWidth: Int,
    isVisibleFolderEblanApplicationInfos: Boolean,
    folderEblanApplicationInfoEntries: List<FolderEntry>,
    // Gesture and State
    isVisibleOverlay: Boolean,
    swipeY: Float,
    drag: Drag,
    // Actions and Callbacks
    onDismiss: () -> Unit,
    onDragEnd: () -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByTagId: (Long?) -> Unit,
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

    val scope = rememberCoroutineScope()

    val userManager = LocalUserManager.current

    val isDefaultLauncher by produceIsDefaultLauncherState()

    val horizontalPagerState = rememberPagerState(
        pageCount = {
            getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos.keys.size
        },
    )

    val textFieldState = rememberTextFieldState()

    val focusRequester = remember { FocusRequester() }

    var selectedEblanApplicationInfoTagId by remember { mutableStateOf<Long?>(null) }

    var showSearchDialog by remember { mutableStateOf(false) }

    val currentEblanUserPageKey = remember(
        key1 = getEblanApplicationInfosByLabelAndTag,
        key2 = horizontalPagerState.currentPage,
    ) {
        getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos.keys.toList()
            .getOrNull(horizontalPagerState.currentPage)
    }

    val currentUserHandle = remember(key1 = currentEblanUserPageKey, key2 = userManager) {
        currentEblanUserPageKey?.let { eblanUserPageKey ->
            userManager.getUserForSerialNumber(eblanUserPageKey.eblanUser.serialNumber)
        }
    }

    val isQuietModeEnabled = currentEblanUserPageKey?.eblanUser?.isQuietModeEnabled == true

    val canPauseWorkProfile = isDefaultLauncher &&
        currentEblanUserPageKey != null &&
        currentEblanUserPageKey.eblanUser.eblanUserType == EblanUserType.Work &&
        currentUserHandle != null

    LaunchedEffect(eblanApplicationInfoTags) {
        if (selectedEblanApplicationInfoTagId != null &&
            eblanApplicationInfoTags.none { tag -> tag.id == selectedEblanApplicationInfoTagId }
        ) {
            selectedEblanApplicationInfoTagId = null
        }
    }

    BlurBehindEffect(
        // Effect
        blurBehind = appDrawerSettings.blurBehind,
        // Layout
        screenHeight = screenHeight,
        swipeY = swipeY,
    )

    ApplicationScreenEffect(
        // Pager and Dimensions
        horizontalPagerState = horizontalPagerState,
        screenHeight = screenHeight,
        swipeY = swipeY,
        // State and Focus
        selectedEblanApplicationInfoTagId = selectedEblanApplicationInfoTagId,
        textFieldState = textFieldState,
        searchBarType = appDrawerSettings.searchBarType,
        showKeyboard = appDrawerSettings.showKeyboard,
        focusRequester = focusRequester,
        // Callbacks
        onDismiss = onDismiss,
        onShowSearchDialog = {
            showSearchDialog = true
        },
        onHideSearchDialog = {
            showSearchDialog = false
        },
        onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
        onGetEblanApplicationInfosByTagId = onGetEblanApplicationInfosByTagId,
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding())
            .graphicsLayer {
                translationY = swipeY
                this.alpha = alpha
                clip = true
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                )
            }
            .run {
                if (!isVisibleOverlay) {
                    pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->
                                onVerticalDrag(dragAmount)
                            },
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragEnd,
                        )
                    }
                } else {
                    this
                }
            },
        color = getBackgroundColor(
            backgroundColor = appDrawerSettings.backgroundColor,
            customBackgroundColor = appDrawerSettings.customBackgroundColor,
            defaultColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (appDrawerSettings.appDrawerType) {
                AppDrawerType.Vertical -> {
                    val privateEblanUser = getEblanApplicationInfosByLabelAndTag.privateEblanUser
                    val isPrivateSpaceVisible = privateEblanUser != null &&
                        !(
                            privateEblanUser.isPrivateSpaceEntryPointHidden &&
                                privateEblanUser.isPrivateSpaceLocked
                            )

                    VerticalApplicationScreen(
                        // Layout and Transition
                        sharedTransitionScope = sharedTransitionScope,
                        screenHeight = screenHeight,
                        paddingValues = paddingValues,
                        // Application Info
                        eblanApplicationInfoTags = eblanApplicationInfoTags,
                        eblanApplicationInfos = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos,
                        iconPackFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackFilePaths,
                        privateSerialNumber = privateEblanUser?.serialNumber,
                        isPrivateSpaceVisible = isPrivateSpaceVisible,
                        isPrivateQuietModeEnabled = privateEblanUser?.isQuietModeEnabled == true,
                        privateEblanApplicationInfos = getEblanApplicationInfosByLabelAndTag.privateEblanApplicationInfos,
                        horizontalPagerState = horizontalPagerState,
                        textFieldState = textFieldState,
                        selectedEblanApplicationInfoTagId = selectedEblanApplicationInfoTagId,
                        focusRequester = focusRequester,
                        // Folder Configuration
                        folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                        folderCornerRadius = folderCornerRadius,
                        folderBackgroundColor = folderBackgroundColor,
                        customFolderBackgroundColor = customFolderBackgroundColor,
                        folderBorderColor = folderBorderColor,
                        customFolderBorderColor = customFolderBorderColor,
                        folderBorderWidth = folderBorderWidth,
                        isVisibleFolderEblanApplicationInfos = isVisibleFolderEblanApplicationInfos,
                        folderEblanApplicationInfoEntries = folderEblanApplicationInfoEntries,
                        // System and Appearance
                        appDrawerColumns = appDrawerSettings.appDrawerColumns,
                        appDrawerRowsHeight = appDrawerSettings.appDrawerRowsHeight,
                        backgroundColor = appDrawerSettings.backgroundColor,
                        customBackgroundColor = appDrawerSettings.customBackgroundColor,
                        searchBarType = appDrawerSettings.searchBarType,
                        gridItemSettings = appDrawerSettings.gridItemSettings,
                        systemTextColor = systemTextColor,
                        systemCustomTextColor = systemCustomTextColor,
                        animations = animations,
                        // Gesture and Interaction State
                        drag = drag,
                        swipeY = swipeY,
                        isVisibleOverlay = isVisibleOverlay,
                        // Callbacks
                        onDismiss = onDismiss,
                        onDragEnd = onDragEnd,
                        onUpdateSelectedEblanApplicationInfoTagId = { currentSelectedEblanApplicationInfoTagId ->
                            selectedEblanApplicationInfoTagId = currentSelectedEblanApplicationInfoTagId
                        },
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

                AppDrawerType.Horizontal -> {
                    HorizontalApplicationScreen(
                        // Layout and Transition
                        sharedTransitionScope = sharedTransitionScope,
                        paddingValues = paddingValues,
                        // Application Info and Data
                        eblanApplicationInfoTags = eblanApplicationInfoTags,
                        eblanApplicationInfos = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos,
                        iconPackFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackFilePaths,
                        horizontalPagerState = horizontalPagerState,
                        textFieldState = textFieldState,
                        selectedEblanApplicationInfoTagId = selectedEblanApplicationInfoTagId,
                        focusRequester = focusRequester,
                        // Folder Configuration
                        folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                        folderCornerRadius = folderCornerRadius,
                        folderBackgroundColor = folderBackgroundColor,
                        customFolderBackgroundColor = customFolderBackgroundColor,
                        folderBorderColor = folderBorderColor,
                        customFolderBorderColor = customFolderBorderColor,
                        folderBorderWidth = folderBorderWidth,
                        isVisibleFolderEblanApplicationInfos = isVisibleFolderEblanApplicationInfos,
                        folderEblanApplicationInfoEntries = folderEblanApplicationInfoEntries,
                        // Theme and Appearance
                        systemTextColor = systemTextColor,
                        systemCustomTextColor = systemCustomTextColor,
                        // Settings and Interaction
                        animations = animations,
                        horizontalAppDrawerRows = appDrawerSettings.horizontalAppDrawerRows,
                        horizontalAppDrawerColumns = appDrawerSettings.horizontalAppDrawerColumns,
                        appDrawerRowsHeight = appDrawerSettings.appDrawerRowsHeight,
                        backgroundColor = appDrawerSettings.backgroundColor,
                        customBackgroundColor = appDrawerSettings.customBackgroundColor,
                        searchBarType = appDrawerSettings.searchBarType,
                        gridItemSettings = appDrawerSettings.gridItemSettings,
                        drag = drag,
                        swipeY = swipeY,
                        isVisibleOverlay = isVisibleOverlay,
                        onUpdateSelectedEblanApplicationInfoTagId = { currentSelectedEblanApplicationInfoTagId ->
                            selectedEblanApplicationInfoTagId = currentSelectedEblanApplicationInfoTagId
                        },
                        onDismiss = onDismiss,
                        onVerticalDrag = onVerticalDrag,
                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                        onDragEnd = onDragEnd,
                        onDragFolderEblanApplicationInfo = onDragFolderEblanApplicationInfo,
                        onDragApplicationInfo = onDragApplicationInfo,
                        onLongPressApplicationInfo = onLongPressApplicationInfo,
                        onLongPressFolderApplicationInfo = onLongPressFolderApplicationInfo,
                        onLongPressPrivateSpaceApplicationInfoItem = onLongPressPrivateSpaceApplicationInfoItem,
                        onTapFolderApplicationInfo = onTapFolderApplicationInfo,
                    )
                }
            }

            ApplicationFloatingActionButton(
                searchBarType = appDrawerSettings.searchBarType,
                hasSearchQuery = textFieldState.text.isNotEmpty(),
                canPauseWorkProfile = canPauseWorkProfile,
                isQuietModeEnabled = isQuietModeEnabled,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = paddingValues.calculateEndPadding(layoutDirection) + EblanSpacing.large,
                        bottom = paddingValues.calculateBottomPadding() + EblanSpacing.large,
                    ),
                onShowSearchDialog = {
                    showSearchDialog = true
                },
                onClearSearch = {
                    textFieldState.clearText()
                    onGetEblanApplicationInfosByLabel("")
                },
                onPauseWorkProfile = {
                    if (currentUserHandle != null) {
                        scope.launch {
                            userManager.requestQuietModeEnabled(
                                enableQuiteMode = true,
                                userHandle = currentUserHandle,
                            )
                        }
                    }
                },
            )
        }
    }

    if (showSearchDialog) {
        SearchApplicationsDialog(
            initialLabel = textFieldState.text.toString(),
            onDismissRequest = {
                showSearchDialog = false
            },
            onSearch = { query ->
                textFieldState.setTextAndPlaceCursorAtEnd(query)
                onGetEblanApplicationInfosByLabel(query)
            },
        )
    }
}

@Composable
internal fun QuiteModeScreen(
    // User
    userHandle: UserHandle?,
    // Appearance
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // Layout
    modifier: Modifier = Modifier,
    // Callbacks
    onDragEnd: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val userManager = LocalUserManager.current

    val isDefaultLauncher by produceIsDefaultLauncherState()

    val textColor = getApplicationScreenTextColor(
        // Background Colors
        backgroundColor = backgroundColor,
        customBackgroundColor = customBackgroundColor,
        // Text Colors
        systemTextColor = systemTextColor,
        systemCustomTextColor = systemCustomTextColor,
        // Defaults
        defaultColor = MaterialTheme.colorScheme.onSurface,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(EblanSpacing.medium)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        onVerticalDrag(dragAmount)
                    },
                    onDragEnd = onDragEnd,
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.work_apps_are_paused),
            color = textColor,
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.you_won_t_receive_notifications_from_your_work_apps),
            color = textColor,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )

        if (isDefaultLauncher && userHandle != null) {
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        userManager.requestQuietModeEnabled(
                            enableQuiteMode = false,
                            userHandle = userHandle,
                        )
                    }
                },
            ) {
                Text(text = stringResource(R.string.unpause))
            }
        }
    }
}

@Composable
internal fun TagElevatedFilterChip(
    // Data
    eblanApplicationInfoTag: EblanApplicationInfoTag,
    selectedEblanApplicationInfoTag: Long?,
    // Layout
    modifier: Modifier = Modifier,
    // Callbacks
    onUpdateEblanApplicationInfoTag: (Long?) -> Unit,
) {
    ElevatedFilterChip(
        modifier = modifier,
        onClick = {
            if (eblanApplicationInfoTag.id == selectedEblanApplicationInfoTag) {
                onUpdateEblanApplicationInfoTag(null)
            } else {
                onUpdateEblanApplicationInfoTag(eblanApplicationInfoTag.id)
            }
        },
        label = {
            Text(text = eblanApplicationInfoTag.name)
        },
        selected = eblanApplicationInfoTag.id == selectedEblanApplicationInfoTag,
        leadingIcon = if (eblanApplicationInfoTag.id == selectedEblanApplicationInfoTag) {
            {
                Icon(
                    imageVector = EblanLauncherIcons.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        } else {
            null
        },
    )
}

@Composable
internal fun EblanApplicationInfoTabRow(
    // Data
    eblanUsers: List<EblanUser>,
    eblanApplicationInfoKeys: Set<EblanUserPageKey>,
    // State
    currentPage: Int,
    // Appearance
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    modifier: Modifier = Modifier,
    // Callbacks
    onAnimateScrollToPage: suspend (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val currentEblanUserPageKey =
        eblanApplicationInfoKeys.toList().getOrNull(currentPage) ?: return

    val selectedTabIndex = remember(
        key1 = eblanUsers,
        key2 = currentEblanUserPageKey,
    ) {
        eblanUsers.indexOfFirst { currentIndexedEblanUser ->
            currentIndexedEblanUser.serialNumber == currentEblanUserPageKey.eblanUser.serialNumber
        }.coerceAtLeast(0)
    }

    val containerColor = getBackgroundColor(
        backgroundColor = backgroundColor,
        customBackgroundColor = customBackgroundColor,
        defaultColor = MaterialTheme.colorScheme.surface,
    )

    val contentColor = getApplicationScreenTextColor(
        // Background Colors
        backgroundColor = backgroundColor,
        customBackgroundColor = customBackgroundColor,
        // Text Colors
        systemTextColor = systemTextColor,
        systemCustomTextColor = systemCustomTextColor,
        // Defaults
        defaultColor = MaterialTheme.colorScheme.onSurface,
    )

    val secondaryIndicatorContentColor = getApplicationScreenTextColor(
        // Background Colors
        backgroundColor = backgroundColor,
        customBackgroundColor = customBackgroundColor,
        // Text Colors
        systemTextColor = systemTextColor,
        systemCustomTextColor = systemCustomTextColor,
        // Defaults
        defaultColor = MaterialTheme.colorScheme.primary,
    )

    SecondaryTabRow(
        modifier = modifier,
        selectedTabIndex = selectedTabIndex,
        containerColor = containerColor,
        contentColor = contentColor,
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                color = secondaryIndicatorContentColor,
            )
        },
    ) {
        eblanUsers.forEach { eblanUser ->
            Tab(
                selected = currentEblanUserPageKey.eblanUser.serialNumber == eblanUser.serialNumber,
                onClick = {
                    scope.launch {
                        val targetPage = eblanApplicationInfoKeys.indexOfFirst { currentKeyEblanUserPageKey ->
                            currentKeyEblanUserPageKey.eblanUser.serialNumber == eblanUser.serialNumber
                        }

                        if (targetPage >= 0) {
                            onAnimateScrollToPage(targetPage)
                        }
                    }
                },
                selectedContentColor = contentColor,
                unselectedContentColor = contentColor.copy(alpha = 0.6f),
                text = {
                    Text(
                        text = eblanUser.eblanUserType.getEblanUserTypeTitle(),
                        maxLines = 1,
                    )
                },
            )
        }
    }
}

@Composable
private fun BlurBehindEffect(
    // Effect
    blurBehind: Boolean,
    // Layout
    screenHeight: Int,
    swipeY: Float,
) {
    if (!blurBehind) return

    val activity = LocalActivity.current ?: return

    val window = activity.window

    val progress = 1f - (swipeY / screenHeight).coerceIn(0f, 1f)

    val radius = (progress * 20f).roundToInt()

    DisposableEffect(window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
            )
        }

        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.attributes = window.attributes.apply {
                    blurBehindRadius = 0
                }

                window.clearFlags(
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                )
            }
        }
    }

    SideEffect {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.attributes = window.attributes.apply {
                blurBehindRadius = radius
            }
        }
    }
}

@OptIn(FlowPreview::class)
@Composable
private fun ApplicationScreenEffect(
    // Pager and Dimensions
    horizontalPagerState: PagerState,
    screenHeight: Int,
    swipeY: Float,
    // State and Focus
    selectedEblanApplicationInfoTagId: Long?,
    textFieldState: TextFieldState,
    searchBarType: SearchBarType,
    showKeyboard: Boolean,
    focusRequester: FocusRequester,
    // Callbacks
    onDismiss: () -> Unit,
    onShowSearchDialog: () -> Unit,
    onHideSearchDialog: () -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByTagId: (Long?) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusManager = LocalFocusManager.current

    val isScreenDismissed = swipeY == screenHeight.toFloat()
    val shouldFocusSearch = swipeY == 0f && showKeyboard

    LaunchedEffect(key1 = textFieldState, key2 = searchBarType) {
        if (searchBarType == SearchBarType.Top) {
            snapshotFlow { textFieldState.text }.debounce(500L.milliseconds).onEach { currentCharSequence ->
                onGetEblanApplicationInfosByLabel(currentCharSequence.toString())
            }.collect()
        }
    }

    LaunchedEffect(selectedEblanApplicationInfoTagId) {
        onGetEblanApplicationInfosByTagId(selectedEblanApplicationInfoTagId)
    }

    LaunchedEffect(
        key1 = isScreenDismissed,
        key2 = shouldFocusSearch,
        key3 = searchBarType,
    ) {
        when {
            isScreenDismissed -> {
                onHideSearchDialog()

                textFieldState.clearText()

                onGetEblanApplicationInfosByLabel("")

                horizontalPagerState.scrollToPage(0)

                focusManager.clearFocus()

                keyboardController?.hide()
            }

            shouldFocusSearch -> {
                when (searchBarType) {
                    SearchBarType.Top -> {
                        focusRequester.requestFocus()

                        keyboardController?.show()
                    }

                    SearchBarType.Floating -> {
                        onShowSearchDialog()
                    }

                    SearchBarType.None -> Unit
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onGetEblanApplicationInfosByLabel("")
            onGetEblanApplicationInfosByTagId(null)
        }
    }

    BackHandler(enabled = swipeY < screenHeight.toFloat()) {
        onDismiss()
    }

    HomeHandler(enabled = swipeY < screenHeight.toFloat()) {
        onDismiss()
    }
}
