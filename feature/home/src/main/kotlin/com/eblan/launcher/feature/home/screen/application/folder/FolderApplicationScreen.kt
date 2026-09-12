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
package com.eblan.launcher.feature.home.screen.application.folder

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.folder.FolderPopupEntry
import com.eblan.launcher.domain.model.folder.PreviewFolderEblanApplicationInfo
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.FolderGridLayout
import com.eblan.launcher.feature.home.component.FolderTitle
import com.eblan.launcher.feature.home.util.getAnimatedRect
import com.eblan.launcher.feature.home.util.getFolderPopupLayoutInfo
import kotlin.math.roundToInt

@Composable
internal fun FolderApplicationScreen(
    modifier: Modifier = Modifier,
    folderEblanApplicationInfoPopup: FolderEblanApplicationInfoPopup,
    paddingValues: PaddingValues,
    safeDrawingHeight: Int,
    safeDrawingWidth: Int,
    isVisibleOverlay: Boolean,
    screenWidth: Int,
    folderEblanApplicationInfoPopups: List<FolderEblanApplicationInfoPopup>,
    animations: Boolean,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    previewFolderEblanApplicationInfos: Map<String, PreviewFolderEblanApplicationInfo>,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    appDrawerSettings: AppDrawerSettings,
    folderCornerRadius: Int,
    folderCellWidth: Int,
    folderCellHeight: Int,
    onDeleteFolderEblanApplicationInfoPopupEntry: (FolderPopupEntry) -> Unit,
    onUpsertFolderEblanApplicationInfoPopupEntry: (FolderPopupEntry) -> Unit,
    onUpdateIsVisibleFolders: (Boolean) -> Unit,
) {
    val folderPopupIntOffset = IntOffset(
        x = folderEblanApplicationInfoPopup.folderPopupEntry.x,
        y = folderEblanApplicationInfoPopup.folderPopupEntry.y,
    )

    val folderPopupIntSize = IntSize(
        width = folderEblanApplicationInfoPopup.folderPopupEntry.width,
        height = folderEblanApplicationInfoPopup.folderPopupEntry.height,
    )

    val density = LocalDensity.current

    val layoutDirection = LocalLayoutDirection.current

    val folderPopupLayoutInfo = getFolderPopupLayoutInfo(
        density = density,
        layoutDirection = layoutDirection,
        paddingValues = paddingValues,
        safeDrawingWidth = safeDrawingWidth,
        safeDrawingHeight = safeDrawingHeight,
        folderPopupIntOffset = folderPopupIntOffset,
        folderPopupIntSize = folderPopupIntSize,
        folderCellWidth = folderCellWidth,
        folderCellHeight = folderCellHeight,
        columns = folderEblanApplicationInfoPopup.columns,
        rows = folderEblanApplicationInfoPopup.rows,
    )

    val progress = remember { Animatable(0f) }

    val animatedFolderRect by remember(key1 = folderPopupLayoutInfo) {
        derivedStateOf {
            getAnimatedRect(
                progress = progress.value,
                startWidth = folderPopupLayoutInfo.startWidth,
                startHeight = folderPopupLayoutInfo.startHeight,
                endWidth = folderPopupLayoutInfo.folderGridWidthPx.toFloat(),
                endHeight = folderPopupLayoutInfo.endHeight.toFloat(),
                startCenterX = folderPopupLayoutInfo.startCenterX,
                startCenterY = folderPopupLayoutInfo.startCenterY,
                endCenterX = folderPopupLayoutInfo.endCenterX,
                endCenterY = folderPopupLayoutInfo.endCenterY,
            )
        }
    }

    val animatedPreviewRect by remember(key1 = folderPopupLayoutInfo) {
        derivedStateOf {
            getAnimatedRect(
                progress = progress.value,
                startWidth = folderPopupLayoutInfo.startPreviewWidth,
                startHeight = folderPopupLayoutInfo.startPreviewHeight,
                endWidth = folderPopupLayoutInfo.folderGridWidthPx.toFloat(),
                endHeight = folderPopupLayoutInfo.folderGridHeightPx.toFloat(),
                startCenterX = folderPopupLayoutInfo.startCenterX,
                startCenterY = folderPopupLayoutInfo.startCenterY,
                endCenterX = folderPopupLayoutInfo.endCenterX,
                endCenterY = folderPopupLayoutInfo.endCenterY,
            )
        }
    }

    val folderGridHorizontalPagerState = rememberPagerState(
        pageCount = {
            folderEblanApplicationInfoPopup.folderEblanApplicationInfosByPage.size
        },
    )

    val isFirstFolderEblanApplicationInfo = folderEblanApplicationInfoPopups.size == 1 &&
        folderEblanApplicationInfoPopups.singleOrNull()?.folderEblanApplicationInfo == folderEblanApplicationInfoPopup.folderEblanApplicationInfo

    val isLastFolderEblanApplicationInfo =
        folderEblanApplicationInfoPopups.lastOrNull()?.folderEblanApplicationInfo == folderEblanApplicationInfoPopup.folderEblanApplicationInfo

    val isInProgress by remember {
        derivedStateOf { progress.value < 1f }
    }

    LaunchedEffect(key1 = animations) {
        if (animations) {
            progress.animateTo(targetValue = 1f)
        } else {
            progress.snapTo(targetValue = 1f)
        }
    }

    LaunchedEffect(
        key1 = folderEblanApplicationInfoPopup,
        key2 = isFirstFolderEblanApplicationInfo,
        key3 = animations,
    ) {
        handleIsCloseFolder(
            folderEblanApplicationInfoPopup = folderEblanApplicationInfoPopup,
            progress = progress,
            isFirstFolderGridItem = isFirstFolderEblanApplicationInfo,
            animations = animations,
            onAnimateToScrollToPage = folderGridHorizontalPagerState::animateScrollToPage,
            onDeleteFolderPopupEntry = onDeleteFolderEblanApplicationInfoPopupEntry,
            onUpdateIsVisibleFolders = onUpdateIsVisibleFolders,
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(key1 = isLastFolderEblanApplicationInfo) {
                if (isLastFolderEblanApplicationInfo) {
                    detectTapGestures(
                        onPress = {
                            awaitRelease()

                            onUpsertFolderEblanApplicationInfoPopupEntry(
                                folderEblanApplicationInfoPopup.folderPopupEntry.copy(
                                    isCloseFolder = true,
                                ),
                            )
                        },
                    )
                }
            },
    ) {
        Surface(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = when (layoutDirection) {
                            LayoutDirection.Ltr -> animatedFolderRect.left.roundToInt()

                            LayoutDirection.Rtl -> screenWidth - animatedFolderRect.width()
                                .roundToInt() - animatedFolderRect.left.roundToInt()
                        },
                        y = animatedFolderRect.top.roundToInt(),
                    )
                }
                .size(
                    width = with(density) { animatedFolderRect.width().toDp() },
                    height = with(density) { animatedFolderRect.height().toDp() },
                )
                .clipToBounds(),
            shape = RoundedCornerShape(folderCornerRadius.dp),
            color = when (folderBackgroundColor) {
                BackgroundColor.System -> MaterialTheme.colorScheme.surface
                BackgroundColor.Light -> Color.White
                BackgroundColor.Dark -> Color.Black
                BackgroundColor.Custom -> Color(customFolderBackgroundColor)
            },
            shadowElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.size(
                    width = with(density) { folderPopupLayoutInfo.folderGridWidthPx.toDp() },
                    height = with(density) { folderPopupLayoutInfo.endHeight.toDp() },
                ),
            ) {
                HorizontalPager(
                    modifier = Modifier.weight(1f),
                    state = folderGridHorizontalPagerState,
                    userScrollEnabled = !isVisibleOverlay && !isInProgress,
                ) { index ->
                    FolderGridLayout(
                        modifier = Modifier.fillMaxSize(),
                        columns = folderEblanApplicationInfoPopup.columns,
                        gridItems = folderEblanApplicationInfoPopup.folderEblanApplicationInfosByPage[index],
                        rows = folderEblanApplicationInfoPopup.rows,
                        width = animatedPreviewRect.width().roundToInt(),
                        height = animatedPreviewRect.height().roundToInt(),
                        animate = isVisibleOverlay && !isInProgress && animations,
                        slotId = { it.id },
                        content = {
                            InteractiveFolderEblanApplicationInfoItem(
                                folderEblanApplicationInfoGridItem = it,
                                folderBackgroundColor = folderBackgroundColor,
                                customFolderBackgroundColor = customFolderBackgroundColor,
                                systemTextColor = systemTextColor,
                                systemCustomTextColor = systemCustomTextColor,
                                previewFolderEblanApplicationInfos = previewFolderEblanApplicationInfos,
                                appDrawerSettings = appDrawerSettings,
                                folderCornerRadius = folderCornerRadius,
                                isVisibleOverlay = isVisibleOverlay,
                            )
                        },
                    )
                }

                FolderTitle(
                    label = folderEblanApplicationInfoPopup.label,
                    gridItemsByPage = folderEblanApplicationInfoPopup.folderEblanApplicationInfosByPage,
                    folderGridHorizontalPagerState = folderGridHorizontalPagerState,
                    progress = progress.value,
                    folderBackgroundColor = folderBackgroundColor,
                    customFolderBackgroundColor = customFolderBackgroundColor,
                    textColor = appDrawerSettings.gridItemSettings.textColor,
                    customTextColor = appDrawerSettings.gridItemSettings.customTextColor,
                    systemCustomTextColor = systemCustomTextColor,
                    systemTextColor = systemTextColor,
                )
            }
        }
    }
}

private suspend fun handleIsCloseFolder(
    folderEblanApplicationInfoPopup: FolderEblanApplicationInfoPopup,
    progress: Animatable<Float, AnimationVector1D>,
    isFirstFolderGridItem: Boolean,
    animations: Boolean,
    onAnimateToScrollToPage: suspend (Int) -> Unit,
    onDeleteFolderPopupEntry: (FolderPopupEntry) -> Unit,
    onUpdateIsVisibleFolders: (Boolean) -> Unit,
) {
    if (!folderEblanApplicationInfoPopup.folderPopupEntry.isCloseFolder) return

    onAnimateToScrollToPage(0)

    if (animations) {
        progress.animateTo(targetValue = 0f)
    } else {
        progress.snapTo(targetValue = 0f)
    }

    if (isFirstFolderGridItem) {
        onUpdateIsVisibleFolders(false)
    }

    onDeleteFolderPopupEntry(folderEblanApplicationInfoPopup.folderPopupEntry)
}
