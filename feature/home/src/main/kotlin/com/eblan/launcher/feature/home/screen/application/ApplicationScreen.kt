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
import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.AppDrawerType
import com.eblan.launcher.domain.model.BackgroundColor
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.EblanShortcutInfoByGroup
import com.eblan.launcher.domain.model.EblanUser
import com.eblan.launcher.domain.model.EblanUserPageKey
import com.eblan.launcher.domain.model.EblanUserType
import com.eblan.launcher.domain.model.GetEblanApplicationInfosByLabelAndTag
import com.eblan.launcher.domain.model.ManagedProfileResult
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.R
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.application.horizontal.HorizontalApplicationScreen
import com.eblan.launcher.feature.home.screen.application.list.ListApplicationScreen
import com.eblan.launcher.feature.home.screen.application.vertical.VerticalApplicationScreen
import com.eblan.launcher.feature.home.util.getApplicationScreenTextColor
import com.eblan.launcher.ui.local.LocalUserManager
import com.eblan.launcher.ui.settings.rememberIsDefaultLauncher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun ApplicationScreen(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    alpha: Float,
    appDrawerSettings: AppDrawerSettings,
    cornerSize: Dp,
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
    BlurBehindEffect(
        blurBehind = appDrawerSettings.blurBehind,
        swipeY = swipeY,
        screenHeight = screenHeight,
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                translationY = swipeY
                this.alpha = alpha
                clip = true
                shape = RoundedCornerShape(cornerSize)
            }
            .fillMaxSize(),
        color = when (appDrawerSettings.backgroundColor) {
            BackgroundColor.System -> MaterialTheme.colorScheme.surface
            BackgroundColor.Light -> Color.White
            BackgroundColor.Dark -> Color.Black
            BackgroundColor.Custom -> Color(appDrawerSettings.customBackgroundColor)
        },
    ) {
        when (appDrawerSettings.appDrawerType) {
            AppDrawerType.Vertical -> {
                VerticalApplicationScreen(
                    sharedTransitionScope = sharedTransitionScope,
                    appDrawerSettings = appDrawerSettings,
                    drag = drag,
                    eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                    eblanApplicationInfoTags = eblanApplicationInfoTags,
                    eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                    getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    managedProfileResult = managedProfileResult,
                    paddingValues = paddingValues,
                    screenHeight = screenHeight,
                    swipeY = swipeY,
                    isVisibleOverlay = isVisibleOverlay,
                    systemTextColor = systemTextColor,
                    systemCustomTextColor = systemCustomTextColor,
                    animations = animations,
                    onDismiss = onDismiss,
                    onDragEnd = onDragEnd,
                    onEditApplicationInfo = onEditApplicationInfo,
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                    onGetEblanApplicationInfosByTagId = onGetEblanApplicationInfosByTagId,
                    onUpdateGridItemSource = onUpdateGridItemSource,
                    onUpdateImageBitmap = onUpdateImageBitmap,
                    onUpdateIsDragging = onUpdateIsDragging,
                    onUpdateOverlayBounds = onUpdateOverlayBounds,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                    onVerticalDrag = onVerticalDrag,
                    onWidgets = onWidgets,
                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                    onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                )
            }

            AppDrawerType.Horizontal -> {
                HorizontalApplicationScreen(
                    sharedTransitionScope = sharedTransitionScope,
                    appDrawerSettings = appDrawerSettings,
                    drag = drag,
                    eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                    eblanApplicationInfoTags = eblanApplicationInfoTags,
                    eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                    getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    managedProfileResult = managedProfileResult,
                    paddingValues = paddingValues,
                    screenHeight = screenHeight,
                    swipeY = swipeY,
                    isVisibleOverlay = isVisibleOverlay,
                    systemTextColor = systemTextColor,
                    systemCustomTextColor = systemCustomTextColor,
                    animations = animations,
                    onDismiss = onDismiss,
                    onDragEnd = onDragEnd,
                    onEditApplicationInfo = onEditApplicationInfo,
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                    onGetEblanApplicationInfosByTagId = onGetEblanApplicationInfosByTagId,
                    onUpdateGridItemSource = onUpdateGridItemSource,
                    onUpdateImageBitmap = onUpdateImageBitmap,
                    onUpdateIsDragging = onUpdateIsDragging,
                    onUpdateOverlayBounds = onUpdateOverlayBounds,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                    onVerticalDrag = onVerticalDrag,
                    onWidgets = onWidgets,
                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                    onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                )
            }

            AppDrawerType.List -> {
                ListApplicationScreen(
                    sharedTransitionScope = sharedTransitionScope,
                    appDrawerSettings = appDrawerSettings,
                    drag = drag,
                    eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                    eblanApplicationInfoTags = eblanApplicationInfoTags,
                    eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                    getEblanApplicationInfosByLabelAndTag = getEblanApplicationInfosByLabelAndTag,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    managedProfileResult = managedProfileResult,
                    paddingValues = paddingValues,
                    screenHeight = screenHeight,
                    swipeY = swipeY,
                    isVisibleOverlay = isVisibleOverlay,
                    systemTextColor = systemTextColor,
                    systemCustomTextColor = systemCustomTextColor,
                    animations = animations,
                    onDismiss = onDismiss,
                    onDragEnd = onDragEnd,
                    onEditApplicationInfo = onEditApplicationInfo,
                    onGetEblanApplicationInfosByLabel = onGetEblanApplicationInfosByLabel,
                    onGetEblanApplicationInfosByTagId = onGetEblanApplicationInfosByTagId,
                    onUpdateGridItemSource = onUpdateGridItemSource,
                    onUpdateImageBitmap = onUpdateImageBitmap,
                    onUpdateIsDragging = onUpdateIsDragging,
                    onUpdateOverlayBounds = onUpdateOverlayBounds,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                    onVerticalDrag = onVerticalDrag,
                    onWidgets = onWidgets,
                    onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                    onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                )
            }
        }
    }
}

@Composable
internal fun QuiteModeScreen(
    modifier: Modifier = Modifier,
    userHandle: UserHandle?,
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
    onDragEnd: () -> Unit,
    onVerticalDrag: (Float) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val userManager = LocalUserManager.current

    val isDefaultLauncher by rememberIsDefaultLauncher()

    val textColor = getApplicationScreenTextColor(
        backgroundColor = backgroundColor,
        customBackgroundColor = customBackgroundColor,
        systemCustomTextColor = systemCustomTextColor,
        systemTextColor = systemTextColor,
        defaultColor = MaterialTheme.colorScheme.onSurface,
    )

    Column(
        modifier = modifier
            .pointerInput(key1 = Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        onVerticalDrag(dragAmount)
                    },
                    onDragEnd = onDragEnd,
                )
            }
            .fillMaxSize()
            .padding(10.dp),
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
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isDefaultLauncher && userHandle != null) {
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
    modifier: Modifier = Modifier,
    eblanApplicationInfoTag: EblanApplicationInfoTag,
    selectedEblanApplicationInfoTag: Long?,
    onUpdateEblanApplicationInfoTag: (Long?) -> Unit,
) {
    ElevatedFilterChip(
        modifier = modifier.padding(5.dp),
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
@OptIn(ExperimentalMaterial3Api::class)
internal fun EblanApplicationInfoTabRow(
    modifier: Modifier = Modifier,
    currentPage: Int,
    eblanUserPageKeys: List<EblanUserPageKey>,
    eblanApplicationInfos: Map<EblanUserPageKey, List<EblanApplicationInfo>>,
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    onAnimateScrollToPage: suspend (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val currentEblanUserPageKey = eblanApplicationInfos.keys.toList()[currentPage]

    val selectedTabIndex = remember(
        key1 = eblanUserPageKeys,
        key2 = currentEblanUserPageKey,
    ) {
        eblanUserPageKeys.indexOfFirst {
            it.eblanUser.serialNumber == currentEblanUserPageKey.eblanUser.serialNumber
        }
    }

    val containerColor = when (backgroundColor) {
        BackgroundColor.System -> MaterialTheme.colorScheme.surface
        BackgroundColor.Light -> Color.White
        BackgroundColor.Dark -> Color.Black
        BackgroundColor.Custom -> Color(customBackgroundColor)
    }

    val contentColor = getApplicationScreenTextColor(
        backgroundColor = backgroundColor,
        customBackgroundColor = customBackgroundColor,
        systemCustomTextColor = systemCustomTextColor,
        systemTextColor = systemTextColor,
        defaultColor = MaterialTheme.colorScheme.onSurface,
    )

    SecondaryTabRow(
        modifier = modifier,
        selectedTabIndex = selectedTabIndex,
        containerColor = containerColor,
        contentColor = contentColor,
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                color = contentColor,
            )
        },
    ) {
        eblanUserPageKeys.forEach { eblanUserPageKey ->
            Tab(
                selected = currentEblanUserPageKey == eblanUserPageKey,
                onClick = {
                    scope.launch {
                        onAnimateScrollToPage(
                            eblanApplicationInfos.keys.indexOfFirst {
                                it.eblanUser.serialNumber == eblanUserPageKey.eblanUser.serialNumber
                            },
                        )
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

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
internal fun ApplicationScreenEffect(
    horizontalPagerState: PagerState,
    screenHeight: Int,
    selectedEblanApplicationInfoTagId: Long?,
    showPopupApplicationMenu: Boolean,
    swipeY: Float,
    textFieldState: TextFieldState,
    showKeyboard: Boolean,
    focusRequester: FocusRequester,
    onDismiss: () -> Unit,
    onGetEblanApplicationInfosByLabel: (String) -> Unit,
    onGetEblanApplicationInfosByTagId: (Long?) -> Unit,
    onShowPopupApplicationMenu: (Boolean) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = textFieldState) {
        snapshotFlow { textFieldState.text }.debounce(500L.milliseconds).onEach {
            onGetEblanApplicationInfosByLabel(it.toString())

            onShowPopupApplicationMenu(false)
        }.collect()
    }

    LaunchedEffect(key1 = selectedEblanApplicationInfoTagId) {
        onGetEblanApplicationInfosByTagId(selectedEblanApplicationInfoTagId)
    }

    LaunchedEffect(key1 = horizontalPagerState.isScrollInProgress) {
        if (horizontalPagerState.isScrollInProgress && showPopupApplicationMenu) {
            onShowPopupApplicationMenu(false)
        }
    }

    LaunchedEffect(key1 = swipeY) {
        when (swipeY) {
            screenHeight.toFloat() -> {
                textFieldState.clearText()

                horizontalPagerState.scrollToPage(0)

                focusManager.clearFocus()

                keyboardController?.hide()
            }

            0f if showKeyboard -> {
                focusRequester.requestFocus()

                keyboardController?.show()
            }
        }
    }

    BackHandler(enabled = swipeY < screenHeight.toFloat()) {
        onDismiss()
    }

    HomeHandler(enabled = swipeY < screenHeight.toFloat()) {
        onDismiss()
    }
}

@Composable
internal fun rememberIsQuietModeEnabled(
    userHandle: UserHandle?,
    managedProfileResult: ManagedProfileResult?,
    eblanUser: EblanUser?,
): State<Boolean> {
    val userManager = LocalUserManager.current

    return produceState(
        initialValue = false,
        key1 = userHandle,
        key2 = managedProfileResult,
    ) {
        if (userHandle != null) {
            value = userManager.isQuietModeEnabled(userHandle = userHandle)
        }

        if (managedProfileResult != null &&
            managedProfileResult.serialNumber == eblanUser?.serialNumber
        ) {
            value = managedProfileResult.isQuiteModeEnabled
        }
    }
}

@Composable
private fun BlurBehindEffect(
    blurBehind: Boolean,
    swipeY: Float,
    screenHeight: Int,
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

@Composable
private fun EblanUserType.getEblanUserTypeTitle() = when (this) {
    EblanUserType.Personal -> stringResource(R.string.personal)
    EblanUserType.Clone -> stringResource(R.string.clone)
    EblanUserType.Work -> stringResource(R.string.work)
    EblanUserType.Private -> stringResource(R.string.private_space)
}
