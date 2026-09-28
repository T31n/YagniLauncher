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
package com.eblan.launcher.feature.home.screen.application.list

import android.os.Build
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import coil3.request.crossfade
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
import com.eblan.launcher.feature.home.component.gridItemScaleAnimation
import com.eblan.launcher.feature.home.component.gridItemSharedElement
import com.eblan.launcher.feature.home.component.rememberNestedScrollConnectionEffect
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.ScrollBarItemLayout
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.application.ApplicationScreenEffect
import com.eblan.launcher.feature.home.screen.application.ApplicationSearchBar
import com.eblan.launcher.feature.home.screen.application.EblanApplicationInfoTabRow
import com.eblan.launcher.feature.home.screen.application.QuiteModeScreen
import com.eblan.launcher.feature.home.screen.application.TagElevatedFilterChip
import com.eblan.launcher.feature.home.screen.application.handleDragEblanApplicationInfoItem
import com.eblan.launcher.feature.home.screen.application.handleOnTapEblanApplicationInfoItem
import com.eblan.launcher.feature.home.screen.application.rememberIsPrivateQuietModeEnabled
import com.eblan.launcher.feature.home.screen.application.rememberIsQuietModeEnabled
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor
import com.eblan.launcher.feature.home.util.handleOnPress
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalUserManager
import com.eblan.launcher.ui.settings.rememberIsDefaultLauncher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class, FlowPreview::class)
@Composable
internal fun ListApplicationScreen(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    appDrawerSettings: AppDrawerSettings,
    drag: Drag,
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    getEblanApplicationInfosByLabelAndTag: GetEblanApplicationInfosByLabelAndTag,
    paddingValues: PaddingValues,
    screenHeight: Int,
    swipeY: Float,
    isVisibleOverlay: Boolean,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
    animations: Boolean,
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
                top = paddingValues.calculateTopPadding(),
                start = paddingValues.calculateStartPadding(layoutDirection),
                end = paddingValues.calculateEndPadding(layoutDirection),
                bottom = if (appDrawerSettings.searchBarPosition == SearchBarPosition.Bottom) {
                    paddingValues.calculateBottomPadding()
                } else {
                    0.dp
                },
            ),
    ) {
        if (appDrawerSettings.searchBarPosition == SearchBarPosition.Top) {
            ApplicationSearchBar(
                focusRequester = focusRequester,
                searchBarState = searchBarState,
                textFieldState = textFieldState,
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
                            eblanApplicationInfoTag = it,
                            selectedEblanApplicationInfoTag = selectedEblanApplicationInfoTagId,
                            onUpdateEblanApplicationInfoTag = { id ->
                                selectedEblanApplicationInfoTagId = id
                            },
                        )
                    }
                }
            }

            if (getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos.keys.size > 1) {
                EblanApplicationInfoTabRow(
                    currentPage = horizontalPagerState.currentPage,
                    eblanUserPageKeys = eblanUserPageKeys,
                    eblanApplicationInfos = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos,
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
                    isVisibleOverlay = isVisibleOverlay,
                    swipeY = swipeY,
                    screenHeight = screenHeight,
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
                focusRequester = focusRequester,
                searchBarState = searchBarState,
                textFieldState = textFieldState,
                backgroundColor = appDrawerSettings.backgroundColor,
                customBackgroundColor = appDrawerSettings.customBackgroundColor,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
            )
        }
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
    paddingValues: PaddingValues,
    isVisibleOverlay: Boolean,
    swipeY: Float,
    screenHeight: Int,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
    animations: Boolean,
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
                screenHeight = screenHeight,
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isDefaultLauncher && eblanUserPageKey.eblanUser.serialNumber > 0 && userHandle != null) {
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
    screenHeight: Int,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
    animations: Boolean,
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
    val lazyListState = rememberLazyListState()

    val canScroll by remember(key1 = lazyListState) {
        derivedStateOf {
            lazyListState.canScrollForward || lazyListState.canScrollBackward
        }
    }

    val nestedScrollConnection = rememberNestedScrollConnectionEffect(
        scrollableState = lazyListState,
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

    LaunchedEffect(key1 = swipeY) {
        if (swipeY.toInt() == screenHeight) {
            lazyListState.scrollToItem(0)
        }
    }

    Row(
        modifier = modifier
            .nestedScroll(nestedScrollConnection)
            .fillMaxSize(),
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = bottomPadding),
            userScrollEnabled = !isVisibleOverlay,
        ) {
            when (eblanUserPageKey.eblanUser.eblanUserType) {
                EblanUserType.Personal -> {
                    items(
                        items = getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos[eblanUserPageKey].orEmpty(),
                        key = {
                            it.serialNumber to it.componentName
                        },
                    ) {
                        EblanApplicationInfoItem(
                            sharedTransitionScope = sharedTransitionScope,
                            appDrawerSettings = appDrawerSettings,
                            drag = drag,
                            eblanApplicationInfo = it,
                            paddingValues = paddingValues,
                            isVisibleOverlay = isVisibleOverlay,
                            isSwiping = swipeY > 0f,
                            isScrollInProgress = lazyListState.isScrollInProgress,
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                            iconPackInfoFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackInfoFilePaths,
                            animations = animations,
                            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                            onDragApplicationInfo = onDragApplicationInfo,
                            onLongPressApplicationInfo = onLongPressApplicationInfo,
                        )
                    }

                    privateSpace(
                        appDrawerSettings = appDrawerSettings,
                        isQuietModeEnabled = privateIsQuiteModeEnabled,
                        paddingValues = paddingValues,
                        privateEblanApplicationInfos = getEblanApplicationInfosByLabelAndTag.privateEblanApplicationInfos,
                        privateEblanUser = getEblanApplicationInfosByLabelAndTag.privateEblanUser,
                        isVisibleOverlay = isVisibleOverlay,
                        backgroundColor = appDrawerSettings.backgroundColor,
                        customBackgroundColor = appDrawerSettings.customBackgroundColor,
                        systemTextColor = systemTextColor,
                        systemCustomTextColor = systemCustomTextColor,
                        iconPackInfoFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackInfoFilePaths,
                        animations = animations,
                        onLongPressPrivateSpaceApplicationInfoItem = onLongPressPrivateSpaceApplicationInfoItem,
                    )
                }

                else -> {
                    items(
                        getEblanApplicationInfosByLabelAndTag.eblanApplicationInfos[eblanUserPageKey].orEmpty(),
                        key = {
                            it.serialNumber to it.componentName
                        },
                    ) {
                        EblanApplicationInfoItem(
                            sharedTransitionScope = sharedTransitionScope,
                            appDrawerSettings = appDrawerSettings,
                            drag = drag,
                            eblanApplicationInfo = it,
                            paddingValues = paddingValues,
                            isVisibleOverlay = isVisibleOverlay,
                            isSwiping = swipeY > 0f,
                            isScrollInProgress = lazyListState.isScrollInProgress,
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                            iconPackInfoFilePaths = getEblanApplicationInfosByLabelAndTag.iconPackInfoFilePaths,
                            animations = animations,
                            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                            onDragApplicationInfo = onDragApplicationInfo,
                            onLongPressApplicationInfo = onLongPressApplicationInfo,
                        )
                    }
                }
            }
        }

        ScrollBarType(
            alphabeticalScrollBarItems = alphabeticalScrollBarItems,
            appDrawerSettings = appDrawerSettings,
            canScroll = canScroll,
            lazyListState = lazyListState,
            paddingValues = paddingValues,
            itemLayout = if (
                eblanUserPageKey.eblanUser.eblanUserType == EblanUserType.Personal &&
                getEblanApplicationInfosByLabelAndTag.privateEblanUser
                    ?.isPrivateSpaceEntryPointHidden == false
            ) {
                ScrollBarItemLayout.StickyHeader
            } else {
                ScrollBarItemLayout.Regular
            },
        )
    }
}

@OptIn(
    ExperimentalUuidApi::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalLayoutApi::class,
)
@Composable
private fun EblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    appDrawerSettings: AppDrawerSettings,
    drag: Drag,
    eblanApplicationInfo: EblanApplicationInfo,
    paddingValues: PaddingValues,
    isVisibleOverlay: Boolean,
    isSwiping: Boolean,
    isScrollInProgress: Boolean,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
    iconPackInfoFilePaths: Map<String, String?>,
    animations: Boolean,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onDragApplicationInfo: (GridItem) -> Unit,
    onLongPressApplicationInfo: (
        eblanApplicationInfo: EblanApplicationInfo,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val density = LocalDensity.current

    val launcherApps = LocalLauncherApps.current

    val layoutDirection = LocalLayoutDirection.current

    val keyboardController = LocalSoftwareKeyboardController.current

    val textColor = getTextColorFromBackgroundColor(
        backgroundColor = appDrawerSettings.backgroundColor,
        customBackgroundColor = appDrawerSettings.customBackgroundColor,
        textColor = appDrawerSettings.gridItemSettings.textColor,
        customTextColor = appDrawerSettings.gridItemSettings.customTextColor,
        systemTextColor = systemTextColor,
        systemCustomTextColor = systemCustomTextColor,
    )

    val maxLines = if (appDrawerSettings.gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val icon = iconPackInfoFilePaths[eblanApplicationInfo.componentName]
        ?: eblanApplicationInfo.icon

    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    var isLongPress by remember { mutableStateOf(false) }

    val alpha = if (isLongPress) 0f else 1f

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val iconSizePx = with(density) {
        appDrawerSettings.gridItemSettings.iconSize.dp.roundToPx()
    }

    val sharedElementKey = SharedElementKey(
        id = "${eblanApplicationInfo.serialNumber} ${eblanApplicationInfo.packageName} ${eblanApplicationInfo.componentName}",
        parent = SharedElementKey.Parent.SwipeY,
    )

    val scale = remember { Animatable(1f) }

    val currentOnLongPressApplicationInfo by rememberUpdatedState(onLongPressApplicationInfo)

    LaunchedEffect(
        key1 = drag,
        key2 = isLongPress,
    ) {
        handleDragEblanApplicationInfoItem(
            appDrawerSettings = appDrawerSettings,
            drag = drag,
            eblanApplicationInfo = eblanApplicationInfo,
            isLongPress = isLongPress,
            isSwiping = isSwiping,
            onUpdateIsLongPress = {
                isLongPress = it
            },
            onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
            onDragApplicationInfo = onDragApplicationInfo,
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
            .background(
                color = Color(appDrawerSettings.gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(
                    size = appDrawerSettings.gridItemSettings.cornerRadius.dp,
                ),
            )
            .pointerInput(key1 = isVisibleOverlay) {
                detectTapGestures(
                    onTap = if (!isVisibleOverlay) {
                        {
                            scope.launch {
                                handleOnTapEblanApplicationInfoItem(
                                    componentName = eblanApplicationInfo.componentName,
                                    serialNumber = eblanApplicationInfo.serialNumber,
                                    intOffset = intOffset,
                                    intSize = intSize,
                                    keyboardController = keyboardController,
                                    launcherApps = launcherApps,
                                    leftPadding = leftPadding,
                                    topPadding = topPadding,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onLongPress = if (!isVisibleOverlay) {
                        {
                            scope.launch {
                                isLongPress = true

                                keyboardController?.hide()

                                currentOnLongPressApplicationInfo(
                                    eblanApplicationInfo,
                                    graphicsLayer.toImageBitmap(),
                                    intOffset,
                                    intSize,
                                    sharedElementKey,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onPress = {
                        handleOnPress(
                            animations = animations,
                            scale = scale,
                        )
                    },
                )
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(eblanApplicationInfo.customIcon ?: icon)
                .addLastModifiedToFileCacheKey(true).size(iconSizePx).crossfade(false).build(),
            contentDescription = null,
            modifier = Modifier
                .size(appDrawerSettings.gridItemSettings.iconSize.dp)
                .onGloballyPositioned {
                    intOffset = it.positionInRoot().round()

                    intSize = it.size
                }
                .gridItemScaleAnimation(
                    enabled = animations,
                    isVisibleOverlay = isVisibleOverlay,
                    scale = scale,
                )
                .gridItemSharedElement(
                    enabled = animations,
                    sharedElementKey = sharedElementKey,
                    sharedTransitionScope = sharedTransitionScope,
                    visible = !isScrollInProgress && !isLongPress && !isVisibleOverlay,
                )
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }

                    drawLayer(graphicsLayer)
                }
                .alpha(alpha),
        )

        if (appDrawerSettings.gridItemSettings.showLabel) {
            Spacer(modifier = Modifier.width(10.dp))

            Text(
                modifier = Modifier.alpha(alpha),
                text = eblanApplicationInfo.customLabel
                    ?: eblanApplicationInfo.label,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                fontSize = appDrawerSettings.gridItemSettings.textSize.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
