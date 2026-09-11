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
package com.eblan.launcher.feature.home.screen.folder

import android.graphics.Rect
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.application.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.MoveGridItemResult
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfo
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfoByGroup
import com.eblan.launcher.domain.model.widget.EblanAppWidgetProviderInfo
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.component.popup
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.shortcutinfo.ShortcutInfoScreen
import com.eblan.launcher.ui.local.LocalLauncherApps

@Composable
internal fun FolderGridItemPopup(
    modifier: Modifier = Modifier,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    popupIntOffset: IntOffset?,
    popupIntSize: IntSize?,
    movingGridItem: GridItem,
    isVisibleOverlay: Boolean,
    paddingValues: PaddingValues,
    isCloseFolderGridItemPopup: Boolean,
    animations: Boolean,
    onDeleteGridItem: (GridItem) -> Unit,
    onDismissRequest: () -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onEdit: (String) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onWidgets: (EblanApplicationInfoGroup) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
    onResetFolderPopupEntries: () -> Unit,
) {
    requireNotNull(popupIntOffset)

    requireNotNull(popupIntSize)

    val density = LocalDensity.current

    val layoutDirection = LocalLayoutDirection.current

    val launcherApps = LocalLauncherApps.current

    val transitionState = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }

    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }
    val x = popupIntOffset.x - leftPadding

    val y = popupIntOffset.y - topPadding

    LaunchedEffect(
        key1 = transitionState.targetState,
        key2 = transitionState.isIdle,
    ) {
        if (!transitionState.targetState && transitionState.isIdle) {
            onDismissRequest()
        }
    }

    LaunchedEffect(key1 = isCloseFolderGridItemPopup) {
        if (isCloseFolderGridItemPopup) {
            transitionState.targetState = false
        }
    }

    BackHandler(enabled = transitionState.targetState) {
        transitionState.targetState = false
    }

    HomeHandler(enabled = transitionState.targetState) {
        transitionState.targetState = false
    }

    fun getSourceBounds(): Rect {
        val left = popupIntOffset.x + leftPadding
        val top = popupIntOffset.y + topPadding

        return Rect(
            left,
            top,
            left + popupIntSize.width,
            top + popupIntSize.height,
        )
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        awaitRelease()

                        transitionState.targetState = false
                    },
                )
            }
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        AnimatedVisibility(
            modifier = Modifier.popup(
                width = popupIntSize.width,
                height = popupIntSize.height,
                x = x,
                y = y,
            ),
            visibleState = transitionState,
            enter = fadeIn(tween()) + scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(),
            ),
            exit = fadeOut(tween()) + scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(),
            ),
        ) {
            FolderGridItemPopupContent(
                eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                gridItemSettings = gridItemSettings,
                movingFolderGridItem = movingGridItem,
                hasShortcutHostPermission = hasShortcutHostPermission,
                isVisibleOverlay = isVisibleOverlay,
                animations = animations,
                onDeleteGridItem = onDeleteGridItem,
                onUpdateTransitionState = {
                    transitionState.targetState = it
                },
                onUpdateIsDragging = onUpdateIsDragging,
                onEdit = onEdit,
                onInfo = { serialNumber, componentName ->
                    launcherApps.startAppDetailsActivity(
                        serialNumber = serialNumber,
                        componentName = componentName,
                        sourceBounds = getSourceBounds(),
                    )
                },
                onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        launcherApps.startShortcut(
                            serialNumber = serialNumber,
                            packageName = packageName,
                            id = shortcutId,
                            sourceBounds = getSourceBounds(),
                        )
                    }
                },
                onUpdateGridItemSource = onUpdateGridItemSource,
                onUpdateImageBitmap = onUpdateImageBitmap,
                onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                onUpdateOverlayBounds = onUpdateOverlayBounds,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onWidgets = onWidgets,
                onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                onDismissRequest = onDismissRequest,
                onResetFolderPopupEntries = onResetFolderPopupEntries,
            )
        }
    }
}

@Composable
private fun FolderGridItemPopupContent(
    modifier: Modifier = Modifier,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    gridItemSettings: GridItemSettings,
    movingFolderGridItem: GridItem,
    hasShortcutHostPermission: Boolean,
    isVisibleOverlay: Boolean,
    animations: Boolean,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateTransitionState: (Boolean) -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onEdit: (String) -> Unit,
    onInfo: (Long, String) -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onWidgets: (EblanApplicationInfoGroup) -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
    onDismissRequest: () -> Unit,
    onResetFolderPopupEntries: () -> Unit,
) {
    Surface(
        modifier = modifier.width(IntrinsicSize.Max),
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            when (val data = movingFolderGridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    ApplicationInfoFolderGridItemPopupContent(
                        modifier = modifier,
                        eblanAppWidgetProviderInfosByPackageName = eblanAppWidgetProviderInfosGroup[data.packageName],
                        eblanShortcutInfosByPackageName = eblanShortcutInfosGroup[
                            EblanShortcutInfoByGroup(
                                serialNumber = data.serialNumber,
                                packageName = data.packageName,
                            ),
                        ],
                        gridItemSettings = gridItemSettings,
                        hasShortcutHostPermission = hasShortcutHostPermission,
                        icon = data.icon,
                        isVisibleOverlay = isVisibleOverlay,
                        animations = animations,
                        onUpdateIsDragging = {
                            onUpdateIsDragging(it)

                            onUpdateTransitionState(false)
                        },
                        onDelete = {
                            onDeleteGridItem(movingFolderGridItem)

                            onUpdateTransitionState(false)
                        },
                        onEdit = {
                            onEdit(movingFolderGridItem.id)

                            onResetFolderPopupEntries()

                            onUpdateTransitionState(false)
                        },
                        onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                            onTapShortcutInfo(
                                serialNumber,
                                packageName,
                                shortcutId,
                            )

                            onUpdateTransitionState(false)
                        },
                        onUpdateGridItemSource = onUpdateGridItemSource,
                        onUpdateImageBitmap = onUpdateImageBitmap,
                        onUpdateOverlayBounds = onUpdateOverlayBounds,
                        onUpdateSharedElementKey = onUpdateSharedElementKey,
                        onWidgets = {
                            onWidgets(
                                EblanApplicationInfoGroup(
                                    serialNumber = data.serialNumber,
                                    packageName = data.packageName,
                                    icon = data.icon,
                                    label = data.label,
                                ),
                            )

                            onResetFolderPopupEntries()

                            onDismissRequest()
                        },
                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                        onInfo = {
                            onInfo(
                                data.serialNumber,
                                data.componentName,
                            )

                            onUpdateTransitionState(false)
                        },
                        onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                        onUpdateTransitionState = onUpdateTransitionState,
                    )
                }

                is GridItemData.ShortcutInfo,
                is GridItemData.ShortcutConfig,
                is GridItemData.Folder,
                -> {
                    FolderGridItemMenu(
                        onDelete = {
                            onDeleteGridItem(movingFolderGridItem)

                            onUpdateTransitionState(false)
                        },
                        onEdit = {
                            onEdit(movingFolderGridItem.id)

                            onResetFolderPopupEntries()

                            onUpdateTransitionState(false)
                        },
                    )
                }

                else -> Unit
            }
        },
    )
}

@Composable
private fun ApplicationInfoFolderGridItemPopupContent(
    modifier: Modifier = Modifier,
    eblanAppWidgetProviderInfosByPackageName: List<EblanAppWidgetProviderInfo>?,
    eblanShortcutInfosByPackageName: List<EblanShortcutInfo>?,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    icon: String?,
    isVisibleOverlay: Boolean,
    animations: Boolean,
    onDelete: () -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
    onUpdateImageBitmap: (ImageBitmap) -> Unit,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onWidgets: () -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
    onInfo: () -> Unit,
    onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
    onUpdateTransitionState: (Boolean) -> Unit,
) {
    Surface(
        modifier = modifier.width(IntrinsicSize.Max),
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (hasShortcutHostPermission && !eblanShortcutInfosByPackageName.isNullOrEmpty()) {
                    ShortcutInfoScreen(
                        modifier = modifier,
                        eblanShortcutInfosGroup = eblanShortcutInfosByPackageName,
                        gridItemSettings = gridItemSettings,
                        icon = icon,
                        isVisibleOverlay = isVisibleOverlay,
                        animations = animations,
                        onUpdateIsDragging = onUpdateIsDragging,
                        onTapShortcutInfo = onTapShortcutInfo,
                        onUpdateGridItemSource = onUpdateGridItemSource,
                        onUpdateImageBitmap = onUpdateImageBitmap,
                        onUpdateOverlayBounds = onUpdateOverlayBounds,
                        onUpdateSharedElementKey = onUpdateSharedElementKey,
                        onUpdateIsVisibleOverlay = onUpdateIsVisibleOverlay,
                        onUpdateMoveGridItemResult = onUpdateMoveGridItemResult,
                        onUpdateTransitionState = onUpdateTransitionState,
                    )

                    Spacer(modifier = Modifier.height(5.dp))
                }

                Row(modifier = modifier) {
                    IconButton(
                        onClick = onEdit,
                    ) {
                        Icon(imageVector = EblanLauncherIcons.Edit, contentDescription = null)
                    }

                    IconButton(
                        onClick = onInfo,
                    ) {
                        Icon(imageVector = EblanLauncherIcons.Info, contentDescription = null)
                    }

                    IconButton(
                        onClick = onDelete,
                    ) {
                        Icon(imageVector = EblanLauncherIcons.Delete, contentDescription = null)
                    }

                    if (!eblanAppWidgetProviderInfosByPackageName.isNullOrEmpty()) {
                        IconButton(
                            onClick = onWidgets,
                        ) {
                            Icon(
                                imageVector = EblanLauncherIcons.Widgets,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun FolderGridItemMenu(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    Row(modifier = modifier) {
        IconButton(
            onClick = onEdit,
        ) {
            Icon(imageVector = EblanLauncherIcons.Edit, contentDescription = null)
        }

        IconButton(
            onClick = onDelete,
        ) {
            Icon(imageVector = EblanLauncherIcons.Delete, contentDescription = null)
        }
    }
}
