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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.designsystem.theme.EblanSpacing
import com.eblan.launcher.domain.model.application.AppDrawerGridItem
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.application.GetEblanApplicationInfosByLabelAndTag
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.folder.FolderEntry
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.launcherapps.EblanUserPageKey
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.AppDrawerType
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.R
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.component.getEblanUserTypeTitle
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
    val horizontalPagerState = rememberPagerState(
        pageCount = {
            getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos.keys.size
        },
    )

    val textFieldState = rememberTextFieldState()

    val focusRequester = remember { FocusRequester() }

    var selectedEblanApplicationInfoTagId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(key1 = eblanApplicationInfoTags) {
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
        showKeyboard = appDrawerSettings.showKeyboard,
        focusRequester = focusRequester,
        // Callbacks
        onDismiss = onDismiss,
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
                    pointerInput(key1 = Unit) {
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
        when (appDrawerSettings.appDrawerType) {
            AppDrawerType.Vertical -> {
                VerticalApplicationScreen(
                    // Layout and Transition
                    sharedTransitionScope = sharedTransitionScope,
                    screenHeight = screenHeight,
                    paddingValues = paddingValues,
                    // Application Info
                    eblanApplicationInfoTags = eblanApplicationInfoTags,
                    getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
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
                    appDrawerSettings = appDrawerSettings,
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
                    getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
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
                    appDrawerSettings = appDrawerSettings,
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
            .pointerInput(key1 = Unit) {
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
    eblanUserPageKeys: List<EblanUserPageKey>,
    eblanApplicationInfos: Map<EblanUserPageKey, List<AppDrawerGridItem>>,
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
        eblanApplicationInfos.keys.toList().getOrNull(currentPage) ?: return

    val selectedTabIndex = remember(
        key1 = eblanUserPageKeys,
        key2 = currentEblanUserPageKey,
    ) {
        eblanUserPageKeys.indexOfFirst { currentIndexedEblanUserPageKey ->
            currentIndexedEblanUserPageKey.eblanUser.serialNumber == currentEblanUserPageKey.eblanUser.serialNumber
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
        eblanUserPageKeys.forEach { eblanUserPageKey ->
            Tab(
                selected = currentEblanUserPageKey.eblanUser.serialNumber == eblanUserPageKey.eblanUser.serialNumber,
                onClick = {
                    scope.launch {
                        val targetPage = eblanApplicationInfos.keys.indexOfFirst { currentKeyEblanUserPageKey ->
                            currentKeyEblanUserPageKey.eblanUser.serialNumber == eblanUserPageKey.eblanUser.serialNumber
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
                        text = eblanUserPageKey.eblanUser.eblanUserType.getEblanUserTypeTitle(),
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

    DisposableEffect(key1 = window) {
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
    showKeyboard: Boolean,
    focusRequester: FocusRequester,
    // Callbacks
    onDismiss: () -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByTagId: (Long?) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusManager = LocalFocusManager.current

    val isScreenDismissed = swipeY == screenHeight.toFloat()
    val shouldFocusSearch = swipeY == 0f && showKeyboard

    LaunchedEffect(key1 = textFieldState) {
        snapshotFlow { textFieldState.text }.debounce(500L.milliseconds).onEach { currentCharSequence ->
            onGetEblanApplicationInfosByLabel(currentCharSequence.toString())
        }.collect()
    }

    LaunchedEffect(key1 = selectedEblanApplicationInfoTagId) {
        onGetEblanApplicationInfosByTagId(selectedEblanApplicationInfoTagId)
    }

    LaunchedEffect(
        key1 = isScreenDismissed,
        key2 = shouldFocusSearch,
    ) {
        when {
            isScreenDismissed -> {
                textFieldState.clearText()

                horizontalPagerState.scrollToPage(0)

                focusManager.clearFocus()

                keyboardController?.hide()
            }

            shouldFocusSearch -> {
                focusRequester.requestFocus()

                keyboardController?.show()
            }
        }
    }

    DisposableEffect(key1 = Unit) {
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
