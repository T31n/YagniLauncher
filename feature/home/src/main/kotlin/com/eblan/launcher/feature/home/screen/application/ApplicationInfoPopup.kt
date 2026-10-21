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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfo
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfoByGroup
import com.eblan.launcher.domain.model.widget.EblanAppWidgetProviderInfo
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.component.popup
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.screen.shortcutinfo.PrivateShortcutInfoMenu
import com.eblan.launcher.feature.home.screen.shortcutinfo.ShortcutInfoScreen
import com.eblan.launcher.feature.home.util.handleOnUninstall
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalPackageManager
import com.eblan.launcher.ui.local.LocalUserManager

@Composable
internal fun ApplicationInfoPopup(
    // Application Data
    eblanApplicationInfo: EblanApplicationInfo?,
    eblanAppWidgetProviderInfos: Map<String, List<EblanAppWidgetProviderInfo>>,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    // Settings and State
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    isVisibleOverlay: Boolean,
    isCloseEblanApplicationInfoMenu: Boolean,
    // Layout and Positioning
    popupIntOffset: IntOffset?,
    popupIntSize: IntSize?,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    // Callbacks
    onDismissRequest: () -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onWidgets: (EblanApplicationInfoGroup) -> Unit,
    onDragShortcutInfo: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    requireNotNull(eblanApplicationInfo)
    requireNotNull(popupIntOffset)
    requireNotNull(popupIntSize)

    val launcherApps = LocalLauncherApps.current
    val packageManager = LocalPackageManager.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val userManager = LocalUserManager.current
    val context = LocalContext.current

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

    LaunchedEffect(key1 = isCloseEblanApplicationInfoMenu) {
        if (isCloseEblanApplicationInfoMenu) {
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
                // Dimensions
                width = popupIntSize.width,
                height = popupIntSize.height,
                // Position
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
            ApplicationInfoMenu(
                // Item Data
                icon = eblanApplicationInfo.icon,
                isUserInstalled = packageManager.isUserInstalled(eblanApplicationInfo.flags),
                isVisibleOverlay = isVisibleOverlay,
                hasShortcutHostPermission = hasShortcutHostPermission,
                gridItemSettings = gridItemSettings,
                eblanShortcutInfosGroup = eblanShortcutInfosGroup[
                    EblanShortcutInfoByGroup(
                        serialNumber = eblanApplicationInfo.serialNumber,
                        packageName = eblanApplicationInfo.packageName,
                    ),
                ],
                eblanAppWidgetProviderInfosByPackageName = eblanAppWidgetProviderInfos[
                    eblanApplicationInfo.packageName,
                ],
                // Callbacks
                onApplicationInfo = {
                    launcherApps.startAppDetailsActivity(
                        serialNumber = eblanApplicationInfo.serialNumber,
                        componentName = eblanApplicationInfo.componentName,
                        sourceBounds = Rect(
                            popupIntOffset.x,
                            popupIntOffset.y,
                            popupIntOffset.x + popupIntSize.width,
                            popupIntOffset.y + popupIntSize.height,
                        ),
                    )

                    transitionState.targetState = false
                },
                onEdit = {
                    onEditApplicationInfo(
                        eblanApplicationInfo.serialNumber,
                        eblanApplicationInfo.componentName,
                    )

                    transitionState.targetState = false
                },
                onWidgets = {
                    onWidgets(
                        EblanApplicationInfoGroup(
                            serialNumber = eblanApplicationInfo.serialNumber,
                            packageName = eblanApplicationInfo.packageName,
                            icon = eblanApplicationInfo.icon,
                            label = eblanApplicationInfo.label,
                        ),
                    )

                    transitionState.targetState = false
                },
                onUninstall = {
                    handleOnUninstall(
                        // Android Context and Wrappers
                        context = context,
                        launcherApps = launcherApps,
                        userManager = userManager,
                        // App Target Identification
                        serialNumber = eblanApplicationInfo.serialNumber,
                        componentName = eblanApplicationInfo.componentName,
                        packageName = eblanApplicationInfo.packageName,
                        // Popup Layout
                        popupIntOffset = popupIntOffset,
                        popupIntSize = popupIntSize,
                    )

                    transitionState.targetState = false
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

                    transitionState.targetState = false
                },
                onDragShortcutInfo = onDragShortcutInfo,
            )
        }
    }
}

@Composable
internal fun PrivateApplicationInfoPopup(
    // Application Info
    eblanApplicationInfo: EblanApplicationInfo?,
    eblanShortcutInfosGroup: Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>,
    hasShortcutHostPermission: Boolean,
    // Popup Layout
    popupIntOffset: IntOffset?,
    popupIntSize: IntSize?,
    paddingValues: PaddingValues,
    isClosePrivateEblanApplicationInfoMenu: Boolean,
    modifier: Modifier = Modifier,
    // Callbacks
    onDismissRequest: () -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
) {
    requireNotNull(eblanApplicationInfo)
    requireNotNull(popupIntOffset)
    requireNotNull(popupIntSize)

    val launcherApps = LocalLauncherApps.current
    val packageManager = LocalPackageManager.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val userManager = LocalUserManager.current
    val context = LocalContext.current

    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val x = popupIntOffset.x - leftPadding
    val y = popupIntOffset.y - topPadding

    val transitionState = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }

    LaunchedEffect(
        key1 = transitionState.targetState,
        key2 = transitionState.isIdle,
    ) {
        if (!transitionState.targetState && transitionState.isIdle) {
            onDismissRequest()
        }
    }

    LaunchedEffect(key1 = isClosePrivateEblanApplicationInfoMenu) {
        if (isClosePrivateEblanApplicationInfoMenu) {
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
                // Dimensions
                width = popupIntSize.width,
                height = popupIntSize.height,
                // Position
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
            PrivateApplicationInfoMenu(
                // Application State
                hasShortcutHostPermission = hasShortcutHostPermission,
                isUserInstalled = packageManager.isUserInstalled(flags = eblanApplicationInfo.flags),
                eblanShortcutInfosGroup = eblanShortcutInfosGroup[
                    EblanShortcutInfoByGroup(
                        serialNumber = eblanApplicationInfo.serialNumber,
                        packageName = eblanApplicationInfo.packageName,
                    ),
                ],
                // Callbacks
                onUninstall = {
                    handleOnUninstall(
                        // Android Context and Wrappers
                        context = context,
                        launcherApps = launcherApps,
                        userManager = userManager,
                        // App Target Identification
                        serialNumber = eblanApplicationInfo.serialNumber,
                        componentName = eblanApplicationInfo.componentName,
                        packageName = eblanApplicationInfo.packageName,
                        // Popup Layout
                        popupIntOffset = popupIntOffset,
                        popupIntSize = popupIntSize,
                    )

                    transitionState.targetState = false
                },
                onApplicationInfo = {
                    launcherApps.startAppDetailsActivity(
                        serialNumber = eblanApplicationInfo.serialNumber,
                        componentName = eblanApplicationInfo.componentName,
                        sourceBounds = Rect(
                            popupIntOffset.x,
                            popupIntOffset.y,
                            popupIntOffset.x + popupIntSize.width,
                            popupIntOffset.y + popupIntSize.height,
                        ),
                    )

                    transitionState.targetState = false
                },
                onEdit = {
                    onEditApplicationInfo(
                        eblanApplicationInfo.serialNumber,
                        eblanApplicationInfo.componentName,
                    )

                    transitionState.targetState = false
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

                    transitionState.targetState = false
                },
            )
        }
    }
}

@Composable
internal fun ApplicationInfoMenu(
    // Item Data
    icon: String?,
    isUserInstalled: Boolean,
    isVisibleOverlay: Boolean,
    hasShortcutHostPermission: Boolean,
    gridItemSettings: GridItemSettings,
    eblanShortcutInfosGroup: List<EblanShortcutInfo>?,
    eblanAppWidgetProviderInfosByPackageName: List<EblanAppWidgetProviderInfo>?,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onApplicationInfo: () -> Unit,
    onEdit: () -> Unit,
    onWidgets: () -> Unit,
    onUninstall: () -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
    onDragShortcutInfo: (
        gridItem: GridItem,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        intSize: IntSize,
        sharedElementKey: SharedElementKey,
    ) -> Unit,
) {
    Surface(
        modifier = modifier.padding(5.dp),
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (hasShortcutHostPermission && !eblanShortcutInfosGroup.isNullOrEmpty()) {
                    ShortcutInfoScreen(
                        // Data
                        eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                        gridItemSettings = gridItemSettings,
                        // Appearance
                        icon = icon,
                        isVisibleOverlay = isVisibleOverlay,
                        // Actions
                        onTapShortcutInfo = onTapShortcutInfo,
                        onDragShortcutInfo = onDragShortcutInfo,
                    )

                    Spacer(modifier = Modifier.height(5.dp))
                }

                Row {
                    IconButton(
                        onClick = onApplicationInfo,
                    ) {
                        Icon(
                            imageVector = EblanLauncherIcons.Info,
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

                    if (isUserInstalled) {
                        IconButton(
                            onClick = onUninstall,
                        ) {
                            Icon(
                                imageVector = EblanLauncherIcons.Delete,
                                contentDescription = null,
                            )
                        }
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
private fun PrivateApplicationInfoMenu(
    // Application State
    hasShortcutHostPermission: Boolean,
    isUserInstalled: Boolean,
    eblanShortcutInfosGroup: List<EblanShortcutInfo>?,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onUninstall: () -> Unit,
    onApplicationInfo: () -> Unit,
    onEdit: () -> Unit,
    onTapShortcutInfo: (
        serialNumber: Long,
        packageName: String,
        shortcutId: String,
    ) -> Unit,
) {
    Surface(
        modifier = modifier.padding(5.dp),
        shape = RoundedCornerShape(30.dp),
        shadowElevation = 2.dp,
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (hasShortcutHostPermission && !eblanShortcutInfosGroup.isNullOrEmpty()) {
                    PrivateShortcutInfoMenu(
                        // Shortcut Data
                        eblanShortcutInfosGroup = eblanShortcutInfosGroup,
                        // Actions
                        onTapShortcutInfo = onTapShortcutInfo,
                    )

                    Spacer(modifier = Modifier.height(5.dp))
                }

                Row {
                    IconButton(
                        onClick = onApplicationInfo,
                    ) {
                        Icon(
                            imageVector = EblanLauncherIcons.Info,
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

                    if (isUserInstalled) {
                        IconButton(
                            onClick = onUninstall,
                        ) {
                            Icon(
                                imageVector = EblanLauncherIcons.Delete,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        },
    )
}
