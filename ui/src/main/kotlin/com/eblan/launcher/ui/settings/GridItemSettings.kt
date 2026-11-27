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
package com.eblan.launcher.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.theme.EblanShapes
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.HorizontalAlignment
import com.eblan.launcher.domain.model.grid.HorizontalArrangement
import com.eblan.launcher.domain.model.grid.LayoutType
import com.eblan.launcher.domain.model.grid.VerticalAlignment
import com.eblan.launcher.domain.model.grid.VerticalArrangement
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.ui.R
import com.eblan.launcher.ui.dialog.ColorPickerDialog
import com.eblan.launcher.common.R as commonR

@Composable
fun GridItemSettings(
    // Grid item
    gridItemSettings: GridItemSettings,
    // Content
    title: String,
    // Layout
    modifier: Modifier = Modifier,
    // Actions
    onUpdateGridItemSettings: (GridItemSettings) -> Unit,
) {
    var showTextColorPickerDialog by remember { mutableStateOf(false) }
    var showBackgroundColorDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingsCategoryText(text = title)

        SettingsDropdownItem(
            // Content
            title = stringResource(R.string.layout_type),
            options = LayoutType.entries,
            selected = gridItemSettings.layoutType,
            label = { currentLayoutType ->
                currentLayoutType.getSubtitle()
            },
            // Layout
            shape = EblanShapes.first,
            // Actions
            onSelectedChange = { currentLayoutType ->
                onUpdateGridItemSettings(gridItemSettings.copy(layoutType = currentLayoutType))
            },
        )

        SettingsSliderItem(
            // Content
            title = stringResource(R.string.icon_size),
            value = gridItemSettings.iconSize,
            valueRange = 24..120,
            // Layout
            shape = EblanShapes.middle,
            step = 2,
            // Actions
            onValueChange = { currentIconSize ->
                onUpdateGridItemSettings(
                    gridItemSettings.copy(iconSize = currentIconSize),
                )
            },
        )

        SettingsDropdownItem(
            // Content
            title = stringResource(R.string.text_color),
            options = TextColor.entries,
            selected = gridItemSettings.textColor,
            label = { currentTextColor ->
                currentTextColor.getSubtitle()
            },
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onSelectedChange = { currentTextColor ->
                if (currentTextColor == TextColor.Custom) {
                    showTextColorPickerDialog = true
                } else {
                    onUpdateGridItemSettings(gridItemSettings.copy(textColor = currentTextColor))
                }
            },
        )

        SettingsSliderItem(
            // Content
            title = stringResource(R.string.text_size),
            value = gridItemSettings.textSize,
            valueRange = 6..32,
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onValueChange = { currentTextSize ->
                onUpdateGridItemSettings(
                    gridItemSettings.copy(textSize = currentTextSize),
                )
            },
        )

        SettingsDropdownItem(
            // Content
            title = stringResource(commonR.string.background_color),
            options = BackgroundColor.entries,
            selected = gridItemSettings.backgroundColor,
            label = { currentBackgroundColor ->
                currentBackgroundColor.getTitle()
            },
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onSelectedChange = { currentBackgroundColor ->
                if (currentBackgroundColor == BackgroundColor.Custom) {
                    showBackgroundColorDialog = true
                } else {
                    onUpdateGridItemSettings(
                        gridItemSettings.copy(backgroundColor = currentBackgroundColor),
                    )
                }
            },
        )

        SettingsSliderItem(
            // Content
            title = stringResource(R.string.padding),
            value = gridItemSettings.padding,
            valueRange = 0..32,
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onValueChange = { currentPadding ->
                onUpdateGridItemSettings(
                    gridItemSettings.copy(padding = currentPadding),
                )
            },
        )

        SettingsSliderItem(
            // Content
            title = stringResource(R.string.corner_radius),
            value = gridItemSettings.cornerRadius,
            valueRange = 0..50,
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onValueChange = { currentCornerRadius ->
                onUpdateGridItemSettings(
                    gridItemSettings.copy(cornerRadius = currentCornerRadius),
                )
            },
        )

        SettingsSwitchItem(
            // Content
            title = stringResource(R.string.single_line_label),
            subtitle = stringResource(R.string.limit_app_names_to_one_line),
            // State
            checked = gridItemSettings.singleLineLabel,
            // Appearance
            shape = EblanShapes.middle,
            // Callbacks
            onCheckedChange = { currentSingleLineLabel ->
                onUpdateGridItemSettings(
                    gridItemSettings.copy(singleLineLabel = currentSingleLineLabel),
                )
            },
        )

        SettingsDropdownItem(
            // Content
            title = stringResource(R.string.horizontal_alignment),
            options = HorizontalAlignment.entries,
            selected = gridItemSettings.horizontalAlignment,
            label = { currentHorizontalAlignment ->
                currentHorizontalAlignment.getSubtitle()
            },
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onSelectedChange = { currentHorizontalAlignment ->
                onUpdateGridItemSettings(
                    gridItemSettings.copy(horizontalAlignment = currentHorizontalAlignment),
                )
            },
        )

        SettingsDropdownItem(
            // Content
            title = stringResource(R.string.vertical_arrangement),
            options = VerticalArrangement.entries,
            selected = gridItemSettings.verticalArrangement,
            label = { currentVerticalArrangement ->
                currentVerticalArrangement.getSubtitle()
            },
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onSelectedChange = { currentVerticalArrangement ->
                onUpdateGridItemSettings(
                    gridItemSettings.copy(verticalArrangement = currentVerticalArrangement),
                )
            },
        )

        SettingsDropdownItem(
            // Content
            title = stringResource(R.string.horizontal_arrangement),
            options = HorizontalArrangement.entries,
            selected = gridItemSettings.horizontalArrangement,
            label = { currentHorizontalArrangement ->
                currentHorizontalArrangement.getSubtitle()
            },
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onSelectedChange = { currentHorizontalArrangement ->
                onUpdateGridItemSettings(
                    gridItemSettings.copy(horizontalArrangement = currentHorizontalArrangement),
                )
            },
        )

        SettingsDropdownItem(
            // Content
            title = stringResource(R.string.vertical_alignment),
            options = VerticalAlignment.entries,
            selected = gridItemSettings.verticalAlignment,
            label = { currentVerticalAlignment ->
                currentVerticalAlignment.getSubtitle()
            },
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onSelectedChange = { currentVerticalAlignment ->
                onUpdateGridItemSettings(
                    gridItemSettings.copy(verticalAlignment = currentVerticalAlignment),
                )
            },
        )

        SettingsSliderItem(
            // Content
            title = stringResource(R.string.icon_padding),
            value = gridItemSettings.iconPadding,
            valueRange = 0..32,
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onValueChange = { currentIconPadding ->
                onUpdateGridItemSettings(
                    gridItemSettings.copy(iconPadding = currentIconPadding),
                )
            },
        )

        SettingsSliderItem(
            // Content
            title = stringResource(R.string.text_padding),
            value = gridItemSettings.textPadding,
            valueRange = 0..32,
            // Layout
            shape = EblanShapes.last,
            // Actions
            onValueChange = { currentTextPadding ->
                onUpdateGridItemSettings(
                    gridItemSettings.copy(textPadding = currentTextPadding),
                )
            },
        )
    }

    if (showTextColorPickerDialog) {
        ColorPickerDialog(
            // Content
            title = stringResource(R.string.text_color),
            customColor = gridItemSettings.customTextColor,
            showAlphaSlider = true,
            // Callbacks
            onDismissRequest = {
                showTextColorPickerDialog = false
            },
            onSelectColor = { currentCustomTextColor ->
                onUpdateGridItemSettings(
                    gridItemSettings.copy(
                        textColor = TextColor.Custom,
                        customTextColor = currentCustomTextColor,
                    ),
                )
            },
        )
    }

    if (showBackgroundColorDialog) {
        ColorPickerDialog(
            // Content
            title = stringResource(commonR.string.background_color),
            customColor = gridItemSettings.customBackgroundColor,
            showAlphaSlider = true,
            // Callbacks
            onDismissRequest = {
                showBackgroundColorDialog = false
            },
            onSelectColor = { currentCustomBackgroundColor ->
                onUpdateGridItemSettings(
                    gridItemSettings.copy(
                        backgroundColor = BackgroundColor.Custom,
                        customBackgroundColor = currentCustomBackgroundColor,
                    ),
                )
            },
        )
    }
}

@Composable
fun TextColor.getSubtitle(): String = when (this) {
    TextColor.System -> stringResource(commonR.string.system)
    TextColor.Light -> stringResource(commonR.string.light)
    TextColor.Dark -> stringResource(commonR.string.dark)
    TextColor.Custom -> stringResource(commonR.string.custom)
}

@Composable
private fun HorizontalAlignment.getSubtitle(): String = when (this) {
    HorizontalAlignment.Start -> stringResource(R.string.start)
    HorizontalAlignment.CenterHorizontally -> stringResource(R.string.center_horizontally)
    HorizontalAlignment.End -> stringResource(R.string.end)
}

@Composable
private fun VerticalArrangement.getSubtitle(): String = when (this) {
    VerticalArrangement.Top -> stringResource(R.string.top)
    VerticalArrangement.Center -> stringResource(R.string.center)
    VerticalArrangement.Bottom -> stringResource(R.string.bottom)
}

@Composable
private fun HorizontalArrangement.getSubtitle(): String = when (this) {
    HorizontalArrangement.Start -> stringResource(R.string.start)
    HorizontalArrangement.Center -> stringResource(R.string.center)
    HorizontalArrangement.End -> stringResource(R.string.end)
}

@Composable
private fun VerticalAlignment.getSubtitle(): String = when (this) {
    VerticalAlignment.Top -> stringResource(R.string.top)
    VerticalAlignment.CenterVertically -> stringResource(R.string.center_vertically)
    VerticalAlignment.Bottom -> stringResource(R.string.bottom)
}

@Composable
private fun LayoutType.getSubtitle(): String = when (this) {
    LayoutType.TopIconBottomLabel -> stringResource(R.string.top_icon_bottom_label)
    LayoutType.TopLabelBottomIcon -> stringResource(R.string.top_label_bottom_icon)
    LayoutType.StartIconEndLabel -> stringResource(R.string.start_icon_end_label)
    LayoutType.StartLabelEndIcon -> stringResource(R.string.start_label_end_icon)
    LayoutType.IconOnly -> stringResource(R.string.icon_only)
    LayoutType.LabelOnly -> stringResource(R.string.label_only)
}
