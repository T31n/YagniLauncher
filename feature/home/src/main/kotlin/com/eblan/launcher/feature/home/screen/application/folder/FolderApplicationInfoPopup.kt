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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfo
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfoByGroup
import com.eblan.launcher.domain.model.widget.EblanAppWidgetProviderInfo
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.component.popup
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.application.ApplicationInfoMenu
import com.eblan.launcher.ui.local.LocalLauncherApps

@Composable
internal fun FolderApplicationInfoPopup(
    modifier: Modifier = Modifier,
    folderEblanApplicationInfo: FolderEblanApplicationInfo?,
    popupIntOffset: IntOffset?,
    popupIntSize: IntSize?,
    paddingValues: PaddingValues,
    isCloseFolderEblanApplicationInfoMenu: Boolean,
    onDismissRequest: () -> Unit,
    onEditFolderApplicationInfo: (String) -> Unit,
    onDeleteFolderEblanApplicationInfoGridItems: (
        icon: String?,
        folderId: String,
    ) -> Unit,
) {
    requireNotNull(popupIntOffset)

    requireNotNull(popupIntSize)

    requireNotNull(folderEblanApplicationInfo)

    val density = LocalDensity.current

    val layoutDirection = LocalLayoutDirection.current

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

    LaunchedEffect(key1 = isCloseFolderEblanApplicationInfoMenu) {
        if (isCloseFolderEblanApplicationInfoMenu) {
            transitionState.targetState = false
        }
    }

    BackHandler(enabled = transitionState.targetState) {
        transitionState.targetState = false
    }

    HomeHandler(enabled = transitionState.targetState) {
        transitionState.targetState = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        awaitRelease()

                        transitionState.targetState = false
                    },
                )
            },
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
            FolderApplicationInfoMenu(
                onDelete = {
                    onDeleteFolderEblanApplicationInfoGridItems(
                        folderEblanApplicationInfo.icon,
                        folderEblanApplicationInfo.id,
                    )

                    transitionState.targetState = false
                },
                onEdit = {
                    onEditFolderApplicationInfo(folderEblanApplicationInfo.id)

                    transitionState.targetState = false
                },
            )
        }
    }
}

@Composable
internal fun FolderApplicationInfoGridItemPopup(
    modifier: Modifier = Modifier,
    folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem?,
    popupIntOffset: IntOffset?,
    popupIntSize: IntSize?,
    paddingValues: PaddingValues,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    isVisibleOverlay: Boolean,
    animations: Boolean,
    isCloseFolderEblanApplicationInfoGridItemMenu: Boolean,
    onDismissRequest: () -> Unit,
    onEditFolderApplicationInfo: (String) -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onWidgets: (EblanApplicationInfoGroup) -> Unit,
    onResetFolderEblanApplicationInfoPopupEntries: () -> Unit,
    onDeleteFolderEblanApplicationInfoGridItems: (
        icon: String?,
        folderId: String,
    ) -> Unit,
    onDragShortcutInfo: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    requireNotNull(popupIntOffset)

    requireNotNull(popupIntSize)

    requireNotNull(folderEblanApplicationInfoGridItem)

    val density = LocalDensity.current

    val layoutDirection = LocalLayoutDirection.current

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

    LaunchedEffect(key1 = isCloseFolderEblanApplicationInfoGridItemMenu) {
        if (isCloseFolderEblanApplicationInfoGridItemMenu) {
            transitionState.targetState = false
        }
    }

    BackHandler(enabled = transitionState.targetState) {
        transitionState.targetState = false
    }

    HomeHandler(enabled = transitionState.targetState) {
        transitionState.targetState = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        awaitRelease()

                        transitionState.targetState = false
                    },
                )
            },
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
            FolderApplicationInfoGridItemPopupContent(
                animations = animations,
                eblanAppWidgetProviderInfosGroup = eblanAppWidgetProviderInfosGroup,
                eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                folderEblanApplicationInfoGridItem = folderEblanApplicationInfoGridItem,
                gridItemSettings = gridItemSettings,
                hasShortcutHostPermission = hasShortcutHostPermission,
                isVisibleOverlay = isVisibleOverlay,
                popupIntOffset = popupIntOffset,
                popupIntSize = popupIntSize,
                onDeleteFolderEblanApplicationInfoGridItems = onDeleteFolderEblanApplicationInfoGridItems,
                onDismiss = {
                    transitionState.targetState = false
                },
                onEditApplicationInfo = onEditApplicationInfo,
                onEditFolderApplicationInfo = onEditFolderApplicationInfo,
                onResetFolderEblanApplicationInfoPopupEntries = onResetFolderEblanApplicationInfoPopupEntries,
                onWidgets = onWidgets,
                onDragShortcutInfo = onDragShortcutInfo,
            )
        }
    }
}

@Composable
private fun FolderApplicationInfoGridItemPopupContent(
    modifier: Modifier = Modifier,
    animations: Boolean,
    eblanAppWidgetProviderInfosGroup: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    folderEblanApplicationInfoGridItem: FolderEblanApplicationInfoGridItem,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    isVisibleOverlay: Boolean,
    popupIntOffset: IntOffset,
    popupIntSize: IntSize,
    onDeleteFolderEblanApplicationInfoGridItems: (
        icon: String?,
        folderId: String,
    ) -> Unit,
    onDismiss: () -> Unit,
    onEditApplicationInfo: (Long, String) -> Unit,
    onEditFolderApplicationInfo: (String) -> Unit,
    onResetFolderEblanApplicationInfoPopupEntries: () -> Unit,
    onWidgets: (EblanApplicationInfoGroup) -> Unit,
    onDragShortcutInfo: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    val launcherApps = LocalLauncherApps.current

    when (val data = folderEblanApplicationInfoGridItem.data) {
        is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> {
            ApplicationInfoMenu(
                modifier = modifier,
                eblanAppWidgetProviderInfosByPackageName = eblanAppWidgetProviderInfosGroup[
                    data.packageName,
                ],
                eblanShortcutInfosGroup = eblanShortcutInfosGroup[
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
                onApplicationInfo = {
                    launcherApps.startAppDetailsActivity(
                        serialNumber = data.serialNumber,
                        componentName = data.componentName,
                        sourceBounds = Rect(
                            popupIntOffset.x,
                            popupIntOffset.y,
                            popupIntOffset.x + popupIntSize.width,
                            popupIntOffset.y + popupIntSize.height,
                        ),
                    )

                    onDismiss()
                },
                onEdit = {
                    onEditApplicationInfo(
                        data.serialNumber,
                        data.componentName,
                    )

                    onResetFolderEblanApplicationInfoPopupEntries()

                    onDismiss()
                },
                onTapShortcutInfo = { serialNumber, packageName, shortcutId ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        launcherApps.startShortcut(
                            serialNumber = serialNumber,
                            packageName = packageName,
                            id = shortcutId,
                            sourceBounds = Rect(
                                popupIntOffset.x,
                                popupIntOffset.y,
                                popupIntOffset.x + popupIntSize.width,
                                popupIntOffset.y + popupIntSize.height,
                            ),
                        )
                    }

                    onDismiss()
                },
                onWidgets = {
                    onWidgets(
                        EblanApplicationInfoGroup(
                            serialNumber = data.serialNumber,
                            packageName = data.packageName,
                            icon = data.icon,
                            label = data.label,
                        ),
                    )

                    onDismiss()
                },
                onDragShortcutInfo = onDragShortcutInfo,
            )
        }

        is FolderEblanApplicationInfoGridItemData.Folder -> {
            FolderApplicationInfoMenu(
                modifier = modifier,
                onDelete = {
                    onDeleteFolderEblanApplicationInfoGridItems(
                        data.icon,
                        folderEblanApplicationInfoGridItem.id,
                    )

                    onDismiss()
                },
                onEdit = {
                    onEditFolderApplicationInfo(folderEblanApplicationInfoGridItem.id)

                    onResetFolderEblanApplicationInfoPopupEntries()

                    onDismiss()
                },
            )
        }
    }
}

@Composable
private fun FolderApplicationInfoMenu(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    Surface(
        modifier = modifier.padding(5.dp),
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            Row {
                IconButton(
                    onClick = onDelete,
                ) {
                    Icon(
                        imageVector = EblanLauncherIcons.Delete,
                        contentDescription = null,
                    )
                }

                IconButton(
                    onClick = onEdit,
                ) {
                    Icon(
                        imageVector = EblanLauncherIcons.Edit,
                        contentDescription = null,
                    )
                }
            }
        },
    )
}
