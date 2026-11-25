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
package com.eblan.launcher.feature.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.domain.model.userdata.HomeData
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.domain.model.userdata.Theme
import com.eblan.launcher.feature.home.contract.HomeContract
import com.eblan.launcher.feature.home.dialog.TextDialog
import com.eblan.launcher.feature.home.model.HomeUiState
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.screen.editpage.EditDockGridPageScreen
import com.eblan.launcher.feature.home.screen.editpage.EditGridPageScreen
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.feature.home.screen.pager.PagerScreen
import com.eblan.launcher.ui.local.LocalWallpaperManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@Composable
internal fun HomeRoute(
    // State and Modifier
    configureResultCode: Int?,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    // Actions
    onResetConfigureResultCode: () -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onEditGridItem: (String) -> Unit,
    onSettings: () -> Unit,
    onEditFolderApplicationInfo: (String) -> Unit,
) {
    HomeScreen(
        // Contract
        contract = viewModel,
        // General State
        configureResultCode = configureResultCode,
        modifier = modifier,
        // Navigation and Activity Callbacks
        onSettings = onSettings,
        onResetConfigureResultCode = onResetConfigureResultCode,
        onEditApplicationInfo = onEditApplicationInfo,
        onEditGridItem = onEditGridItem,
        onEditFolderApplicationInfo = onEditFolderApplicationInfo,
    )
}

@Composable
internal fun HomeScreen(
    // Contract
    contract: HomeContract,
    // General State
    configureResultCode: Int?,
    modifier: Modifier = Modifier,
    // Navigation and Activity Callbacks
    onSettings: () -> Unit,
    onResetConfigureResultCode: () -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onEditGridItem: (String) -> Unit,
    onEditFolderApplicationInfo: (String) -> Unit,
) {
    val homeUiState = contract.homeUiState.collectAsStateWithLifecycle().value
    val screen by contract.screen.collectAsStateWithLifecycle()

    val paddingValues = WindowInsets.systemBars
        .union(WindowInsets.displayCutout)
        .asPaddingValues()

    var screenIntSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { intSize ->
                screenIntSize = intSize
            },
    ) {
        if (homeUiState is HomeUiState.Success && screenIntSize != IntSize.Zero) {
            Success(
                // Contract
                contract = contract,
                // State
                homeData = homeUiState.homeData,
                screen = screen,
                screenWidth = screenIntSize.width,
                screenHeight = screenIntSize.height,
                paddingValues = paddingValues,
                configureResultCode = configureResultCode,
                // Navigation and Activity Callbacks
                onResetConfigureResultCode = onResetConfigureResultCode,
                onSettings = onSettings,
                onEditApplicationInfo = onEditApplicationInfo,
                onEditGridItem = onEditGridItem,
                onEditFolderApplicationInfo = onEditFolderApplicationInfo,
            )
        }
    }
}

@Composable
private fun Success(
    // Contract
    contract: HomeContract,
    // State
    homeData: HomeData,
    screen: Screen,
    screenWidth: Int,
    screenHeight: Int,
    paddingValues: PaddingValues,
    configureResultCode: Int?,
    // Modifier
    modifier: Modifier = Modifier,
    // Navigation and Activity Callbacks
    onResetConfigureResultCode: () -> Unit,
    onSettings: () -> Unit,
    onEditApplicationInfo: (
        serialNumber: Long,
        componentName: String,
    ) -> Unit,
    onEditGridItem: (String) -> Unit,
    onEditFolderApplicationInfo: (String) -> Unit,
) {
    val activity = LocalActivity.current
    val homeSettings = homeData.userData.homeSettings

    val textColor by produceHomeTextColorState(
        configuredTextColor = homeData.userData.homeSettings.gridItemSettings.textColor,
        theme = homeData.userData.generalSettings.theme,
    )

    val dockTextColor by produceHomeTextColorState(
        configuredTextColor = homeData.userData.homeSettings.dockGridItemSettings.textColor,
        theme = homeData.userData.generalSettings.theme,
    )

    val appDrawerTextColor by produceHomeTextColorState(
        configuredTextColor = homeData.userData.appDrawerSettings.gridItemSettings.textColor,
        theme = homeData.userData.generalSettings.theme,
    )

    DisposableEffect(
        key1 = activity,
        key2 = homeSettings.showStatusBar,
        key3 = homeSettings.showNavigationBar,
    ) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        val systemBars =
            WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()

        val hiddenSystemBars =
            (if (!homeSettings.showStatusBar) WindowInsetsCompat.Type.statusBars() else 0) or
                (if (!homeSettings.showNavigationBar) WindowInsetsCompat.Type.navigationBars() else 0)

        if (hiddenSystemBars != 0) {
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(hiddenSystemBars)
        } else {
            insetsController.show(systemBars)
        }

        onDispose {
            insetsController.show(systemBars)
        }
    }

    AnimatedContent(
        modifier = modifier,
        targetState = screen,
    ) { targetState ->
        when (targetState) {
            Screen.Pager -> {
                PagerScreen(
                    // Contract
                    contract = contract,
                    // Screen Dimensions
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    // Settings
                    homeSettings = homeData.userData.homeSettings,
                    folderSettings = homeData.userData.folderSettings,
                    appDrawerSettings = homeData.userData.appDrawerSettings,
                    gestureSettings = homeData.userData.gestureSettings,
                    experimentalSettings = homeData.userData.experimentalSettings,
                    textColor = textColor,
                    dockTextColor = dockTextColor,
                    appDrawerTextColor = appDrawerTextColor,
                    // System Features
                    hasSystemFeatureAppWidgets = homeData.hasSystemFeatureAppWidgets,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    // Data
                    gridItems = homeData.gridItems,
                    gridItemsByPage = homeData.gridItemsByPage,
                    dockGridItemsByPage = homeData.dockGridItemsByPage,
                    folderPopups = homeData.folderPopups,
                    iconPackFilePaths = homeData.iconPackFilePaths,
                    // State
                    configureResultCode = configureResultCode,
                    // Layout
                    paddingValues = paddingValues,
                    // Callbacks - Navigation and Activity Actions
                    onResetConfigureResultCode = onResetConfigureResultCode,
                    onEditGridItem = onEditGridItem,
                    onEditFolderApplicationInfo = onEditFolderApplicationInfo,
                    onEditApplicationInfo = onEditApplicationInfo,
                    onSettings = onSettings,
                )
            }

            Screen.Loading -> LoadingScreen()

            Screen.EditGridPage -> {
                EditGridPageScreen(
                    // Contract
                    contract = contract,
                    // Content
                    folderPopupGridItems = homeData.folderPopups,
                    iconPackFilePaths = homeData.iconPackFilePaths,
                    // Layout
                    paddingValues = paddingValues,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                    // Configuration
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    initialPage = homeData.userData.homeSettings.initialPage,
                    columns = homeData.userData.homeSettings.columns,
                    rows = homeData.userData.homeSettings.rows,
                    gridItemSettings = homeData.userData.homeSettings.gridItemSettings,
                    folderCornerRadius = homeData.userData.folderSettings.folderCornerRadius,
                    // Appearance
                    textColor = textColor,
                    folderBackgroundColor = homeData.userData.folderSettings.folderBackgroundColor,
                    customFolderBackgroundColor = homeData.userData.folderSettings.customFolderBackgroundColor,
                    folderBorderColor = homeData.userData.folderSettings.folderBorderColor,
                    customFolderBorderColor = homeData.userData.folderSettings.customFolderBorderColor,
                    folderBorderWidth = homeData.userData.folderSettings.borderWidth,
                    systemTextColor = textColor,
                    systemCustomTextColor = homeData.userData.homeSettings.gridItemSettings.customTextColor,
                )
            }

            Screen.EditDockGridPage -> {
                EditDockGridPageScreen(
                    // Contract
                    contract = contract,
                    // Content
                    folderPopupGridItems = homeData.folderPopups,
                    hasShortcutHostPermission = homeData.hasShortcutHostPermission,
                    // Settings
                    dockInitialPage = homeData.userData.homeSettings.dockInitialPage,
                    dockHeight = homeData.userData.homeSettings.dockHeight,
                    dockColumns = homeData.userData.homeSettings.dockColumns,
                    dockRows = homeData.userData.homeSettings.dockRows,
                    dockGridItemSettings = homeData.userData.homeSettings.dockGridItemSettings,
                    folderCornerRadius = homeData.userData.folderSettings.folderCornerRadius,
                    // Appearance
                    textColor = dockTextColor,
                    systemTextColor = dockTextColor,
                    systemCustomTextColor = homeData.userData.homeSettings.dockGridItemSettings.customTextColor,
                    folderBackgroundColor = homeData.userData.folderSettings.folderBackgroundColor,
                    customFolderBackgroundColor = homeData.userData.folderSettings.customFolderBackgroundColor,
                    folderBorderColor = homeData.userData.folderSettings.folderBorderColor,
                    customFolderBorderColor = homeData.userData.folderSettings.customFolderBorderColor,
                    folderBorderWidth = homeData.userData.folderSettings.borderWidth,
                    iconPackFilePaths = homeData.iconPackFilePaths,
                    // Layout
                    paddingValues = paddingValues,
                )
            }
        }
    }

    RequestPermissionsEffect()
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RequestPermissionsEffect(modifier: Modifier = Modifier) {
    val notificationsPermissionState =
        rememberMultiplePermissionsState(
            permissions = buildList {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
                add(Manifest.permission.CALL_PHONE)
            },
        )

    var showTextDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = notificationsPermissionState) {
        if (notificationsPermissionState.shouldShowRationale) {
            showTextDialog = true
        } else {
            notificationsPermissionState.launchMultiplePermissionRequest()
        }
    }

    if (showTextDialog) {
        TextDialog(
            // Content
            title = stringResource(R.string.request_permissions),
            text = stringResource(R.string.allow_permissions_so_we_can_inform_you_about_important_crash_reports_and_make_phone_shortcuts),
            // Layout
            modifier = modifier,
            // Callbacks
            onDismissRequest = {
                showTextDialog = false
            },
            onClick = {
                notificationsPermissionState.launchMultiplePermissionRequest()

                showTextDialog = false
            },
        )
    }
}

@Composable
private fun produceHomeTextColorState(
    configuredTextColor: TextColor?,
    theme: Theme?,
): State<TextColor> {
    val wallpaperManager = LocalWallpaperManager.current
    val isSystemDarkTheme = isSystemInDarkTheme()

    return produceState(
        TextColor.System,
        configuredTextColor,
        theme,
        wallpaperManager,
        isSystemDarkTheme,
    ) {
        if (configuredTextColor == null || theme == null) {
            value = TextColor.System
            return@produceState
        }

        wallpaperManager.getColorsChanged().collect { colorHints ->
            value = if (configuredTextColor != TextColor.System) {
                configuredTextColor
            } else if (colorHints != null) {
                if (colorHints and wallpaperManager.hintSupportsDarkText != 0) {
                    TextColor.Dark
                } else {
                    TextColor.Light
                }
            } else {
                when (theme) {
                    Theme.Light -> TextColor.Light

                    Theme.Dark -> TextColor.Dark

                    Theme.System,
                    Theme.Wallpaper,
                    -> if (isSystemDarkTheme) TextColor.Dark else TextColor.Light
                }
            }
        }
    }
}
