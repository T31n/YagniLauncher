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
import android.content.res.Configuration
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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.designsystem.theme.EblanShapes
import com.eblan.launcher.designsystem.theme.EblanSpacing
import com.eblan.launcher.domain.model.iconpackinfo.EblanIconPackInfo
import com.eblan.launcher.domain.model.userdata.ColorSchemeContrastPreset
import com.eblan.launcher.domain.model.userdata.ColorSchemePaletteStyle
import com.eblan.launcher.domain.model.userdata.ColorSource
import com.eblan.launcher.domain.model.userdata.GeneralSettings
import com.eblan.launcher.domain.model.userdata.IconShape
import com.eblan.launcher.domain.model.userdata.Theme
import com.eblan.launcher.feature.settings.general.dialog.ImportIconPackInfoDialog
import com.eblan.launcher.feature.settings.general.dialog.SelectIconPackInfoDialog
import com.eblan.launcher.feature.settings.general.model.GeneralSettingsUiState
import com.eblan.launcher.platform.service.IconPackService
import com.eblan.launcher.ui.dialog.ColorPickerDialog
import com.eblan.launcher.ui.local.LocalLocaleWrapper
import com.eblan.launcher.ui.settings.SettingsColumnItem
import com.eblan.launcher.ui.settings.SettingsDropdownItem
import com.eblan.launcher.ui.settings.SettingsSwitchItem
import com.eblan.launcher.ui.settings.produceIsNotificationAccessGrantedState
import java.util.Locale
import com.eblan.launcher.common.R as commonR

@Composable
internal fun GeneralSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: GeneralSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val generalSettingsUiState by viewModel.generalSettingsUiState.collectAsStateWithLifecycle()
    val eblanIconPackInfos by viewModel.eblanIconPacks.collectAsStateWithLifecycle()

    GeneralSettingsScreen(
        // State
        generalSettingsUiState = generalSettingsUiState,
        // Content
        eblanIconPackInfos = eblanIconPackInfos,
        modifier = modifier,
        // Actions
        onDeleteEblanIconPackInfo = viewModel::deleteIconPackInfo,
        onNavigateUp = onNavigateUp,
        onUpdateGeneralSettings = viewModel::updateGeneralSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GeneralSettingsScreen(
    // State
    generalSettingsUiState: GeneralSettingsUiState,
    // Content
    eblanIconPackInfos: List<EblanIconPackInfo>,
    modifier: Modifier = Modifier,
    // Actions
    onDeleteEblanIconPackInfo: (EblanIconPackInfo) -> Unit,
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
                    // Icon Packs
                    eblanIconPackInfos = eblanIconPackInfos,
                    // Settings
                    generalSettings = generalSettingsUiState.generalSettings,
                    // Callbacks
                    onDeleteEblanIconPackInfo = onDeleteEblanIconPackInfo,
                    onUpdateGeneralSettings = onUpdateGeneralSettings,
                )
            }
        }
    }
}

@Composable
private fun Success(
    // Icon Packs
    eblanIconPackInfos: List<EblanIconPackInfo>,
    // Settings
    generalSettings: GeneralSettings,
    // UI
    modifier: Modifier = Modifier,
    // Callbacks
    onDeleteEblanIconPackInfo: (EblanIconPackInfo) -> Unit,
    onUpdateGeneralSettings: (GeneralSettings) -> Unit,
) {
    val context = LocalContext.current
    val localeWrapper = LocalLocaleWrapper.current
    val supportedLanguageTags by produceSupportedLanguageTagsState()
    val applicationLanguageTags = remember(supportedLanguageTags) {
        listOf("") + supportedLanguageTags
    }

    val isNotificationAccessGranted by produceIsNotificationAccessGrantedState()

    var showImportIconPackDialog by remember { mutableStateOf(false) }
    var selectIconPackDialog by remember { mutableStateOf(false) }
    var showCustomSeedColorPickerDialog by remember { mutableStateOf(false) }
    val applicationLanguageTag = localeWrapper.getApplicationLanguageTag()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(EblanSpacing.large),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingsColumnItem(
            // Content
            title = stringResource(R.string.import_icon_pack),
            subtitle = stringResource(R.string.apply_icons_from_supported_icon_packs),
            // Layout
            shape = EblanShapes.first,
            // Actions
            onClick = {
                showImportIconPackDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.select_icon_pack),
            subtitle = generalSettings.iconPackPackageName.ifEmpty {
                stringResource(R.string.default_icon_pack)
            },
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onClick = {
                selectIconPackDialog = true
            },
        )

        SettingsDropdownItem(
            // Content
            title = stringResource(R.string.language),
            options = applicationLanguageTags,
            selected = applicationLanguageTag,
            label = { currentLanguageTag ->
                getLanguageLabel(currentLanguageTag)
            },
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onSelectedChange = { currentLanguageTag ->
                localeWrapper.setApplicationLanguageTag(languageTag = currentLanguageTag)
            },
        )

        SettingsDropdownItem(
            // Content
            title = stringResource(R.string.theme),
            options = Theme.entries,
            selected = generalSettings.theme,
            label = { currentTheme ->
                currentTheme.getTitle()
            },
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onSelectedChange = { currentTheme ->
                onUpdateGeneralSettings(generalSettings.copy(theme = currentTheme))
            },
        )

        SettingsDropdownItem(
            // Content
            title = stringResource(R.string.system_bars_theme),
            options = Theme.entries,
            selected = generalSettings.systemBarsTheme,
            label = { currentSystemBarsTheme ->
                currentSystemBarsTheme.getTitle()
            },
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onSelectedChange = { currentSystemBarsTheme ->
                onUpdateGeneralSettings(generalSettings.copy(systemBarsTheme = currentSystemBarsTheme))
            },
        )

        SettingsDropdownItem(
            // Content
            title = stringResource(R.string.color_source),
            options = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ColorSource.entries
            } else {
                ColorSource.entries.filterNot { currentColorSource ->
                    currentColorSource == ColorSource.Wallpaper
                }
            },
            selected = generalSettings.colorSource,
            label = { currentColorSource ->
                currentColorSource.getTitle()
            },
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onSelectedChange = { currentColorSource ->
                if (currentColorSource == ColorSource.Custom) {
                    showCustomSeedColorPickerDialog = true
                } else {
                    onUpdateGeneralSettings(generalSettings.copy(colorSource = currentColorSource))
                }
            },
        )

        if (generalSettings.colorSource == ColorSource.Custom) {
            SettingsDropdownItem(
                // Content
                title = stringResource(R.string.palette_style),
                options = ColorSchemePaletteStyle.entries,
                selected = generalSettings.colorSchemeSettings.paletteStyle,
                label = { currentColorSchemePaletteStyle ->
                    currentColorSchemePaletteStyle.getTitle()
                },
                // Layout
                shape = EblanShapes.middle,
                // Actions
                onSelectedChange = { currentColorSchemePaletteStyle ->
                    onUpdateGeneralSettings(
                        generalSettings.copy(
                            colorSchemeSettings = generalSettings.colorSchemeSettings.copy(
                                paletteStyle = currentColorSchemePaletteStyle,
                            ),
                        ),
                    )
                },
            )

            SettingsDropdownItem(
                // Content
                title = stringResource(R.string.contrast_preset),
                options = ColorSchemeContrastPreset.entries,
                selected = generalSettings.colorSchemeSettings.contrastPreset,
                label = { currentColorSchemeContrastPreset ->
                    currentColorSchemeContrastPreset.getTitle()
                },
                // Layout
                shape = EblanShapes.middle,
                // Actions
                onSelectedChange = { currentColorSchemeContrastPreset ->
                    onUpdateGeneralSettings(
                        generalSettings.copy(
                            colorSchemeSettings = generalSettings.colorSchemeSettings.copy(
                                contrastPreset = currentColorSchemeContrastPreset,
                            ),
                        ),
                    )
                },
            )

            SettingsSwitchItem(
                // Content
                title = stringResource(R.string.amoled_black),
                subtitle = stringResource(R.string.use_pure_black_surfaces_in_dark_mode),
                // State
                checked = generalSettings.colorSchemeSettings.amoledBlack,
                // Appearance
                shape = EblanShapes.middle,
                // Callbacks
                onCheckedChange = { currentAmoledBlack ->
                    onUpdateGeneralSettings(
                        generalSettings.copy(
                            colorSchemeSettings = generalSettings.colorSchemeSettings.copy(
                                amoledBlack = currentAmoledBlack,
                            ),
                        ),
                    )
                },
            )
        }

        SettingsSwitchItem(
            // Content
            title = stringResource(R.string.icon_tint),
            subtitle = stringResource(R.string.tint_icons_using_the_selected_color_source),
            // State
            checked = generalSettings.iconTint,
            // Appearance
            shape = EblanShapes.middle,
            // Callbacks
            onCheckedChange = { currentIconTint ->
                onUpdateGeneralSettings(generalSettings.copy(iconTint = currentIconTint))
            },
        )

        SettingsDropdownItem(
            // Content
            title = stringResource(R.string.icon_shape),
            options = IconShape.entries,
            selected = generalSettings.iconShape,
            label = { currentIconShape ->
                currentIconShape.getTitle()
            },
            // Layout
            shape = if (isNotificationAccessGranted) {
                EblanShapes.last
            } else {
                EblanShapes.middle
            },
            // Actions
            onSelectedChange = { currentIconShape ->
                onUpdateGeneralSettings(generalSettings.copy(iconShape = currentIconShape))
            },
        )

        if (!isNotificationAccessGranted) {
            SettingsColumnItem(
                // Content
                title = stringResource(R.string.notification_dots),
                subtitle = stringResource(R.string.show_notification_dots),
                // Layout
                shape = EblanShapes.last,
                // Actions
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                },
            )
        }
    }

    if (showImportIconPackDialog) {
        ImportIconPackInfoDialog(
            // Actions
            onDismissRequest = {
                showImportIconPackDialog = false
            },
            onUpdateIconPackInfo = { packageName, label ->
                val intent = Intent(context, IconPackService::class.java).apply {
                    putExtra(IconPackService.ICON_PACK_PACKAGE_NAME, packageName)
                    putExtra(IconPackService.ICON_PACK_LABEL, label)
                }

                context.startForegroundService(intent)
            },
        )
    }

    if (selectIconPackDialog) {
        SelectIconPackInfoDialog(
            // Icon Pack Data
            eblanIconPackInfos = eblanIconPackInfos,
            iconPackPackageName = generalSettings.iconPackPackageName,
            // Actions
            onDeleteEblanIconPackInfo = onDeleteEblanIconPackInfo,
            onDismissRequest = {
                selectIconPackDialog = false
            },
            onReset = {
                onUpdateGeneralSettings(generalSettings.copy(iconPackPackageName = ""))
            },
            onUpdateIconPackPackageName = { currentIconPackPackageName ->
                onUpdateGeneralSettings(
                    generalSettings.copy(iconPackPackageName = currentIconPackPackageName),
                )
            },
        )
    }

    if (showCustomSeedColorPickerDialog) {
        ColorPickerDialog(
            // Content
            title = stringResource(R.string.color_source),
            customColor = generalSettings.customSeedColor,
            showAlphaSlider = false,
            // Callbacks
            onDismissRequest = {
                showCustomSeedColorPickerDialog = false
            },
            onSelectColor = { currentCustomSeedColor ->
                onUpdateGeneralSettings(
                    generalSettings.copy(
                        colorSource = ColorSource.Custom,
                        customSeedColor = currentCustomSeedColor,
                    ),
                )
            },
        )
    }
}

@Composable
private fun Theme.getTitle() = when (this) {
    Theme.System -> stringResource(commonR.string.system)
    Theme.Light -> stringResource(commonR.string.light)
    Theme.Dark -> stringResource(commonR.string.dark)
    Theme.Wallpaper -> stringResource(R.string.wallpaper)
}

@Composable
private fun ColorSource.getTitle() = when (this) {
    ColorSource.None -> stringResource(commonR.string.none)
    ColorSource.Wallpaper -> stringResource(R.string.wallpaper)
    ColorSource.Custom -> stringResource(commonR.string.custom)
}

@Composable
private fun ColorSchemePaletteStyle.getTitle() = when (this) {
    ColorSchemePaletteStyle.TonalSpot -> stringResource(R.string.tonal_spot)
    ColorSchemePaletteStyle.Neutral -> stringResource(R.string.neutral)
    ColorSchemePaletteStyle.Vibrant -> stringResource(R.string.vibrant)
    ColorSchemePaletteStyle.Expressive -> stringResource(R.string.expressive)
    ColorSchemePaletteStyle.Rainbow -> stringResource(R.string.rainbow)
    ColorSchemePaletteStyle.FruitSalad -> stringResource(R.string.fruit_salad)
    ColorSchemePaletteStyle.Monochrome -> stringResource(R.string.monochrome)
    ColorSchemePaletteStyle.Fidelity -> stringResource(R.string.fidelity)
    ColorSchemePaletteStyle.Content -> stringResource(R.string.content)
}

@Composable
private fun ColorSchemeContrastPreset.getTitle() = when (this) {
    ColorSchemeContrastPreset.Standard -> stringResource(R.string.standard)
    ColorSchemeContrastPreset.Reduced -> stringResource(R.string.reduced)
    ColorSchemeContrastPreset.Medium -> stringResource(R.string.medium)
    ColorSchemeContrastPreset.High -> stringResource(R.string.high)
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

@Composable
private fun getLanguageLabel(languageTag: String): String {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    return if (languageTag.isEmpty()) {
        stringResource(R.string.system_default)
    } else {
        val languageConfiguration = Configuration(configuration).apply {
            setLocale(Locale.forLanguageTag(languageTag))
        }

        context.createConfigurationContext(languageConfiguration).getString(R.string.language_name)
    }
}

@Composable
private fun produceSupportedLanguageTagsState(): State<List<String>> {
    val localeWrapper = LocalLocaleWrapper.current

    return produceState(
        initialValue = emptyList(),
        key1 = localeWrapper,
    ) {
        value = localeWrapper.getSupportedLanguageTags()
    }
}
