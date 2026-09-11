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

import android.graphics.RectF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.folder.FolderPopupEntry
import com.eblan.launcher.domain.model.folder.PreviewFolderEblanApplicationInfo
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.domain.usecase.util.FOLDER_PREVIEW_COLUMNS
import com.eblan.launcher.domain.usecase.util.FOLDER_PREVIEW_ROWS
import com.eblan.launcher.feature.home.component.FolderGridLayout
import com.eblan.launcher.feature.home.component.PageIndicator
import com.eblan.launcher.feature.home.util.PAGE_INDICATOR_HEIGHT
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor
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
        folderGridItemPopup = folderEblanApplicationInfoPopup,
        folderPopupIntOffset = folderPopupIntOffset,
        folderPopupIntSize = folderPopupIntSize,
        folderCellWidth = folderCellWidth,
        folderCellHeight = folderCellHeight,
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

    val isFirstFolderGridItem = folderEblanApplicationInfoPopups.size == 1 &&
        folderEblanApplicationInfoPopups.singleOrNull()?.folderEblanApplicationInfo == folderEblanApplicationInfoPopup.folderEblanApplicationInfo

    val isLastFolderGridItem =
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
        key2 = isFirstFolderGridItem,
        key3 = animations,
    ) {
        handleIsCloseFolder(
            folderEblanApplicationInfoPopup = folderEblanApplicationInfoPopup,
            progress = progress,
            isFirstFolderGridItem = isFirstFolderGridItem,
            animations = animations,
            onAnimateToScrollToPage = folderGridHorizontalPagerState::animateScrollToPage,
            onDeleteFolderPopupEntry = onDeleteFolderEblanApplicationInfoPopupEntry,
            onUpdateIsVisibleFolders = onUpdateIsVisibleFolders,
        )
    }

    Box(
        modifier = modifier
            .pointerInput(key1 = isLastFolderGridItem) {
                if (isLastFolderGridItem) {
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
            }
            .fillMaxSize(),
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

@Composable
internal fun FolderTitle(
    modifier: Modifier = Modifier,
    label: String,
    gridItemsByPage: Map<Int, List<FolderEblanApplicationInfoGridItem>>,
    folderGridHorizontalPagerState: PagerState,
    progress: Float,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    textColor: TextColor,
    customTextColor: Int,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
) {
    val color = getTextColorFromBackgroundColor(
        backgroundColor = folderBackgroundColor,
        customBackgroundColor = customFolderBackgroundColor,
        textColor = textColor,
        customTextColor = customTextColor,
        systemTextColor = systemTextColor,
        systemCustomTextColor = systemCustomTextColor,
        defaultColor = MaterialTheme.colorScheme.onSurface,
    )

    Box(
        modifier = modifier
            .alpha(if (progress > 0.5) 1f else 0f)
            .fillMaxWidth()
            .height(PAGE_INDICATOR_HEIGHT)
            .padding(horizontal = 10.dp),
    ) {
        if (gridItemsByPage.size > 1) {
            Row(
                modifier = Modifier.matchParentSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = label,
                    color = color,
                    style = MaterialTheme.typography.bodySmall,
                )

                PageIndicator(
                    color = color,
                    gridHorizontalPagerState = folderGridHorizontalPagerState,
                    infiniteScroll = false,
                    pageCount = gridItemsByPage.size,
                )
            }
        } else {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = label,
                color = color,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private data class FolderPopupLayoutInfo(
    val minCellWidthPx: Int,
    val minCellHeightPx: Int,
    val folderGridWidthPx: Int,
    val folderGridHeightPx: Int,
    val endHeight: Int,
    val startWidth: Float,
    val startHeight: Float,
    val startCenterX: Float,
    val startCenterY: Float,
    val endCenterX: Float,
    val endCenterY: Float,
    val startPreviewWidth: Float,
    val startPreviewHeight: Float,
)

private fun getAnimatedRect(
    progress: Float,
    startWidth: Float,
    startHeight: Float,
    endWidth: Float,
    endHeight: Float,
    startCenterX: Float,
    startCenterY: Float,
    endCenterX: Float,
    endCenterY: Float,
): RectF {
    val width = lerp(
        startWidth,
        endWidth,
        progress,
    )

    val height = lerp(
        startHeight,
        endHeight,
        progress,
    )

    val left = lerp(
        startCenterX,
        endCenterX,
        progress,
    ) - width / 2f

    val top = lerp(
        startCenterY,
        endCenterY,
        progress,
    ) - height / 2f

    return RectF(
        left,
        top,
        left + width,
        top + height,
    )
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

private fun getFolderPopupLayoutInfo(
    density: Density,
    layoutDirection: LayoutDirection,
    paddingValues: PaddingValues,
    safeDrawingWidth: Int,
    safeDrawingHeight: Int,
    folderGridItemPopup: FolderEblanApplicationInfoPopup,
    folderPopupIntOffset: IntOffset,
    folderPopupIntSize: IntSize,
    folderCellWidth: Int,
    folderCellHeight: Int,
): FolderPopupLayoutInfo {
    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val minCellWidthPx = with(density) { folderCellWidth.dp.roundToPx() }
    val minCellHeightPx = with(density) { folderCellHeight.dp.roundToPx() }

    val availableWidth = (safeDrawingWidth - leftPadding * 2).coerceAtLeast(0)
    val availableHeight = (safeDrawingHeight - topPadding * 2).coerceAtLeast(0)

    val folderTitleHeightPx = with(density) {
        PAGE_INDICATOR_HEIGHT.roundToPx()
    }

    val folderGridWidthPx =
        (minCellWidthPx * folderGridItemPopup.columns).coerceAtMost(availableWidth)

    val folderGridHeightPx = (minCellHeightPx * folderGridItemPopup.rows).coerceAtMost(
        (availableHeight - folderTitleHeightPx).coerceAtLeast(0),
    )

    val endHeight = folderGridHeightPx + folderTitleHeightPx

    val maximumX = (safeDrawingWidth - folderGridWidthPx + leftPadding).coerceAtLeast(leftPadding)
    val maximumY = (safeDrawingHeight - endHeight + topPadding).coerceAtLeast(topPadding)

    val endIntOffset = IntOffset(
        x = folderPopupIntOffset.x.coerceIn(leftPadding, maximumX),
        y = folderPopupIntOffset.y.coerceIn(topPadding, maximumY),
    )

    val startWidth = folderPopupIntSize.width.toFloat()
    val startHeight = folderPopupIntSize.height.toFloat()

    val startCenterX = folderPopupIntOffset.x + startWidth / 2f
    val startCenterY = folderPopupIntOffset.y + startHeight / 2f

    val endCenterX = endIntOffset.x + folderGridWidthPx.toFloat() / 2f
    val endCenterY = endIntOffset.y + endHeight.toFloat() / 2f

    val previewCellSize = minOf(
        folderPopupIntSize.width,
        folderPopupIntSize.height,
    ) / maxOf(FOLDER_PREVIEW_COLUMNS, FOLDER_PREVIEW_ROWS)

    val startPreviewWidth = minOf(
        (previewCellSize * folderGridItemPopup.columns).toFloat(),
        availableWidth.toFloat(),
    )

    val startPreviewHeight = minOf(
        (previewCellSize * folderGridItemPopup.rows).toFloat(),
        (availableHeight - folderTitleHeightPx).coerceAtLeast(0).toFloat(),
    )

    return FolderPopupLayoutInfo(
        minCellWidthPx = minCellWidthPx,
        minCellHeightPx = minCellHeightPx,
        folderGridWidthPx = folderGridWidthPx,
        folderGridHeightPx = folderGridHeightPx,
        endHeight = endHeight,
        startWidth = startWidth,
        startHeight = startHeight,
        startCenterX = startCenterX,
        startCenterY = startCenterY,
        endCenterX = endCenterX,
        endCenterY = endCenterY,
        startPreviewWidth = startPreviewWidth,
        startPreviewHeight = startPreviewHeight,
    )
}
