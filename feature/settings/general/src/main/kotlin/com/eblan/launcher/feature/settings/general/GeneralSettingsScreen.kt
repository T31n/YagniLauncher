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
import com.eblan.launcher.domain.model.iconpackinfo.EblanIconPackInfo
import com.eblan.launcher.domain.model.iconpackinfo.PackageManagerIconPack
import com.eblan.launcher.domain.model.userdata.GeneralSettings
import com.eblan.launcher.domain.model.userdata.IconShape
import com.eblan.launcher.domain.model.userdata.IconTint
import com.eblan.launcher.domain.model.userdata.Theme
import com.eblan.launcher.feature.settings.general.dialog.ImportIconPackInfoDialog
import com.eblan.launcher.feature.settings.general.dialog.SelectIconPackInfoDialog
import com.eblan.launcher.feature.settings.general.model.GeneralSettingsUiState
import com.eblan.launcher.service.IconPackService
import com.eblan.launcher.ui.dialog.IconTintDialog
import com.eblan.launcher.ui.dialog.RadioOptionsDialog
import com.eblan.launcher.ui.dialog.getTitle
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

    val eblanIconPackInfos by viewModel.eblanIconPacks.collectAsStateWithLifecycle()

    GeneralSettingsScreen(
        modifier = modifier,
        eblanIconPackInfos = eblanIconPackInfos,
        generalSettingsUiState = generalSettingsUiState,
        packageManagerIconPacks = packageManagerIconPackInfos,
        onDeleteEblanIconPackInfo = viewModel::deleteIconPack,
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
    packageManagerIconPacks: List<PackageManagerIconPack>,
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
                    packageManagerIconPacks = packageManagerIconPacks,
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
    packageManagerIconPacks: List<PackageManagerIconPack>,
    onDeleteEblanIconPackInfo: (String) -> Unit,
    onUpdateGeneralSettings: (GeneralSettings) -> Unit,
) {
    val context = LocalContext.current

    var showThemeDialog by remember { mutableStateOf(false) }
    var showImportIconPackDialog by remember { mutableStateOf(false) }
    var selectIconPackDialog by remember { mutableStateOf(false) }
    var showIconTintDialog by remember { mutableStateOf(false) }
    var showIconShapeDialog by remember { mutableStateOf(false) }

    val items = buildGeneralSettingsItems(
        generalSettings = generalSettings,
        onImportIconPackClick = { showImportIconPackDialog = true },
        onSelectIconPackClick = { selectIconPackDialog = true },
        onThemeClick = { showThemeDialog = true },
        onIconColorClick = { showIconTintDialog = true },
        onDynamicThemeChange = {
            onUpdateGeneralSettings(generalSettings.copy(dynamicTheme = it))
        },
        onEnforceThemedIconsChanged = {
            onUpdateGeneralSettings(generalSettings.copy(fallbackIconTint = it))
        },
        onIconShapeClick = {
            showIconShapeDialog = true
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
                it.getTitle()
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
            packageManagerIconPacks = packageManagerIconPacks,
            onDismissRequest = {
                showImportIconPackDialog = false
            },
            onUpdateIconPackInfo = { packageName, label ->
                val intent = Intent(context, IconPackService::class.java).apply {
                    putExtra(IconPackService.ICON_PACK_PACKAGE_NAME, packageName)
                    putExtra(IconPackService.ICON_PACK_LABEL, label)
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
            iconPackPackageName = generalSettings.iconPackPackageName,
            onDeleteEblanIconPackInfo = onDeleteEblanIconPackInfo,
            onDismissRequest = {
                selectIconPackDialog = false
            },
            onReset = {
                onUpdateGeneralSettings(generalSettings.copy(iconPackPackageName = ""))
            },
            onUpdateIconPackPackageName = {
                onUpdateGeneralSettings(generalSettings.copy(iconPackPackageName = it))
            },
        )
    }

    if (showIconTintDialog) {
        IconTintDialog(
            title = stringResource(R.string.icon_tint),
            iconTint = generalSettings.iconTint,
            customIconTint = generalSettings.customIconTint,
            onDismissRequest = {
                showIconTintDialog = false
            },
            onUpdateClick = { iconTint, customIconTint ->
                onUpdateGeneralSettings(
                    generalSettings.copy(
                        iconTint = iconTint,
                        customIconTint = customIconTint,
                    ),
                )
            },
        )
    }

    if (showIconShapeDialog) {
        RadioOptionsDialog(
            title = stringResource(R.string.icon_shape),
            options = IconShape.entries,
            selected = generalSettings.iconShape,
            label = {
                it.getTitle()
            },
            onDismissRequest = {
                showIconShapeDialog = false
            },
            onUpdateClick = {
                onUpdateGeneralSettings(generalSettings.copy(iconShape = it))
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
    onIconColorClick: () -> Unit,
    onDynamicThemeChange: (Boolean) -> Unit,
    onEnforceThemedIconsChanged: (Boolean) -> Unit,
    onIconShapeClick: () -> Unit,
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
                subtitle = generalSettings.iconPackPackageName.ifEmpty {
                    stringResource(R.string.default_icon_pack)
                },
                onClick = onSelectIconPackClick,
            ),
        )

        add(
            SettingsItem.Column(
                title = stringResource(R.string.theme),
                subtitle = generalSettings.theme.getTitle(),
                onClick = onThemeClick,
            ),
        )

        add(
            SettingsItem.Column(
                title = stringResource(R.string.icon_tint),
                subtitle = generalSettings.iconTint.getTitle(),
                onClick = onIconColorClick,
            ),
        )

        when (generalSettings.iconTint) {
            IconTint.System,
            IconTint.Custom,
            -> {
                add(
                    SettingsItem.Switch(
                        checked = generalSettings.fallbackIconTint,
                        title = stringResource(R.string.fallback_icon_tint),
                        subtitle = stringResource(R.string.use_the_fallback_icon_tint),
                        onClick = {
                            onEnforceThemedIconsChanged(!generalSettings.fallbackIconTint)
                        },
                        onCheckedChange = onEnforceThemedIconsChanged,
                    ),
                )
            }

            else -> Unit
        }

        add(
            SettingsItem.Column(
                title = stringResource(R.string.icon_shape),
                subtitle = generalSettings.iconShape.getTitle(),
                onClick = onIconShapeClick,
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

@Composable
private fun Theme.getTitle() = when (this) {
    Theme.System -> stringResource(commonR.string.system)
    Theme.Light -> stringResource(commonR.string.light)
    Theme.Dark -> stringResource(commonR.string.dark)
}

@Composable
private fun IconShape.getTitle() = when (this) {
    IconShape.None -> stringResource(commonR.string.none)
    IconShape.Circle -> stringResource(R.string.circle)
    IconShape.Square -> stringResource(R.string.square)
    IconShape.RoundedSquare -> stringResource(R.string.rounded_square)
    IconShape.TearDrop -> stringResource(R.string.tear_drop)
    IconShape.Hexagon -> stringResource(R.string.hexagon)
    IconShape.Octagon -> stringResource(R.string.octagon)
    IconShape.Diamond -> stringResource(R.string.diamond)
    IconShape.Pentagon -> stringResource(R.string.pentagon)
    IconShape.Clover -> stringResource(R.string.clover)
    IconShape.Cookie -> stringResource(R.string.cookie)
    IconShape.Flower -> stringResource(R.string.flower)
    IconShape.Shield -> stringResource(R.string.shield)
}
