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
package com.eblan.launcher.feature.settings.general

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.EblanIconPackInfo
import com.eblan.launcher.domain.model.GeneralSettings
import com.eblan.launcher.domain.model.PackageManagerIconPackInfo
import com.eblan.launcher.domain.model.Theme
import com.eblan.launcher.feature.settings.general.dialog.ImportIconPackInfoDialog
import com.eblan.launcher.feature.settings.general.dialog.SelectIconPackInfoDialog
import com.eblan.launcher.feature.settings.general.model.GeneralSettingsUiState
import com.eblan.launcher.service.IconPackInfoService
import com.eblan.launcher.ui.dialog.RadioOptionsDialog
import com.eblan.launcher.ui.model.SettingsItem
import com.eblan.launcher.ui.settings.SettingsItems
import com.eblan.launcher.ui.settings.rememberIsNotificationAccessGranted
import com.eblan.launcher.common.R as commonR

@Composable
internal fun GeneralSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: GeneralSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val generalSettingsUiState by viewModel.generalSettingsUiState.collectAsStateWithLifecycle()

    val packageManagerIconPackInfos by viewModel.packageManagerIconPackInfos.collectAsStateWithLifecycle()

    val eblanIconPackInfos by viewModel.eblanIconPackInfos.collectAsStateWithLifecycle()

    GeneralSettingsScreen(
        modifier = modifier,
        eblanIconPackInfos = eblanIconPackInfos,
        generalSettingsUiState = generalSettingsUiState,
        packageManagerIconPackInfos = packageManagerIconPackInfos,
        onDeleteEblanIconPackInfo = viewModel::deleteIconPackInfo,
        onNavigateUp = onNavigateUp,
        onUpdateGeneralSettings = viewModel::updateGeneralSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GeneralSettingsScreen(
    modifier: Modifier = Modifier,
    eblanIconPackInfos: List<EblanIconPackInfo>,
    generalSettingsUiState: GeneralSettingsUiState,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    onDeleteEblanIconPackInfo: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onUpdateGeneralSettings: (GeneralSettings) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(commonR.string.general))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = EblanLauncherIcons.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (generalSettingsUiState is GeneralSettingsUiState.Success) {
                Success(
                    eblanIconPackInfos = eblanIconPackInfos,
                    generalSettings = generalSettingsUiState.generalSettings,
                    packageManagerIconPackInfos = packageManagerIconPackInfos,
                    onDeleteEblanIconPackInfo = onDeleteEblanIconPackInfo,
                    onUpdateGeneralSettings = onUpdateGeneralSettings,
                )
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    eblanIconPackInfos: List<EblanIconPackInfo>,
    generalSettings: GeneralSettings,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    onDeleteEblanIconPackInfo: (String) -> Unit,
    onUpdateGeneralSettings: (GeneralSettings) -> Unit,
) {
    val context = LocalContext.current

    var showThemeDialog by remember { mutableStateOf(false) }

    var showImportIconPackDialog by remember { mutableStateOf(false) }

    var selectIconPackDialog by remember { mutableStateOf(false) }

    val items = buildGeneralSettingsItems(
        generalSettings = generalSettings,
        onImportIconPackClick = { showImportIconPackDialog = true },
        onSelectIconPackClick = { selectIconPackDialog = true },
        onThemeClick = { showThemeDialog = true },
        onDynamicThemeChange = {
            onUpdateGeneralSettings(generalSettings.copy(dynamicTheme = it))
        },
    )

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingsItems(items = items)
    }

    if (showThemeDialog) {
        RadioOptionsDialog(
            title = stringResource(R.string.theme),
            options = Theme.entries,
            selected = generalSettings.theme,
            label = {
                it.getTitle(context = context)
            },
            onDismissRequest = {
                showThemeDialog = false
            },
            onUpdateClick = {
                onUpdateGeneralSettings(generalSettings.copy(theme = it))
            },
        )
    }

    if (showImportIconPackDialog) {
        ImportIconPackInfoDialog(
            packageManagerIconPackInfos = packageManagerIconPackInfos,
            onDismissRequest = {
                showImportIconPackDialog = false
            },
            onUpdateIconPackInfo = { packageName, label ->
                val intent = Intent(context, IconPackInfoService::class.java).apply {
                    putExtra(IconPackInfoService.ICON_PACK_INFO_PACKAGE_NAME, packageName)
                    putExtra(IconPackInfoService.ICON_PACK_INFO_LABEL, label)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            },
        )
    }

    if (selectIconPackDialog) {
        SelectIconPackInfoDialog(
            eblanIconPackInfos = eblanIconPackInfos,
            iconPackInfoPackageName = generalSettings.iconPackInfoPackageName,
            onDeleteEblanIconPackInfo = onDeleteEblanIconPackInfo,
            onDismissRequest = {
                selectIconPackDialog = false
            },
            onReset = {
                onUpdateGeneralSettings(generalSettings.copy(iconPackInfoPackageName = ""))
            },
            onUpdateIconPackInfoPackageName = {
                onUpdateGeneralSettings(generalSettings.copy(iconPackInfoPackageName = it))
            },
        )
    }
}

@Composable
private fun buildGeneralSettingsItems(
    generalSettings: GeneralSettings,
    onImportIconPackClick: () -> Unit,
    onSelectIconPackClick: () -> Unit,
    onThemeClick: () -> Unit,
    onDynamicThemeChange: (Boolean) -> Unit,
): List<SettingsItem> {
    val context = LocalContext.current

    val isNotificationAccessGranted by rememberIsNotificationAccessGranted()

    return buildList {
        add(
            SettingsItem.Column(
                title = stringResource(R.string.import_icon_pack),
                subtitle = stringResource(R.string.apply_icons_from_supported_icon_packs),
                onClick = onImportIconPackClick,
            ),
        )

        add(
            SettingsItem.Column(
                title = stringResource(R.string.select_icon_pack),
                subtitle = generalSettings.iconPackInfoPackageName.ifEmpty {
                    stringResource(R.string.default_icon_pack)
                },
                onClick = onSelectIconPackClick,
            ),
        )

        add(
            SettingsItem.Column(
                title = stringResource(R.string.theme),
                subtitle = generalSettings.theme.getTitle(context = context),
                onClick = onThemeClick,
            ),
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(
                SettingsItem.Switch(
                    checked = generalSettings.dynamicTheme,
                    title = stringResource(R.string.dynamic_theme),
                    subtitle = stringResource(R.string.adapt_colors_to_your_wallpaper_automatically),
                    onClick = {
                        onDynamicThemeChange(!generalSettings.dynamicTheme)
                    },
                    onCheckedChange = onDynamicThemeChange,
                ),
            )
        }

        if (!isNotificationAccessGranted) {
            add(
                SettingsItem.Column(
                    title = stringResource(R.string.notification_dots),
                    subtitle = stringResource(R.string.show_notification_dots),
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    },
                ),
            )
        }
    }
}

private fun Theme.getTitle(context: Context) = when (this) {
    Theme.System -> context.getString(commonR.string.system)
    Theme.Light -> context.getString(commonR.string.light)
    Theme.Dark -> context.getString(commonR.string.dark)
}
