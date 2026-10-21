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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.HorizontalAlignment
import com.eblan.launcher.domain.model.grid.HorizontalArrangement
import com.eblan.launcher.domain.model.grid.LayoutType
import com.eblan.launcher.domain.model.grid.VerticalAlignment
import com.eblan.launcher.domain.model.grid.VerticalArrangement
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.ui.R
import com.eblan.launcher.ui.dialog.ColorPickerDialog
import com.eblan.launcher.ui.dialog.EditCornerRadiusDialog
import com.eblan.launcher.ui.dialog.EditIconSizeDialog
import com.eblan.launcher.ui.dialog.EditPaddingDialog
import com.eblan.launcher.ui.dialog.EditTextSizeDialog
import com.eblan.launcher.ui.dialog.RadioOptionsDialog
import com.eblan.launcher.ui.dialog.TextColorDialog
import com.eblan.launcher.ui.model.SettingsItem
import com.eblan.launcher.common.R as commonR

@Composable
fun GridItemSettings(
    // Grid item
    gridItemSettings: GridItemSettings,
    // Layout
    modifier: Modifier = Modifier,
    // Actions
    onUpdateGridItemSettings: (GridItemSettings) -> Unit,
) {
    var showIconSizeDialog by remember { mutableStateOf(false) }
    var showTextColorDialog by remember { mutableStateOf(false) }
    var showTextSizeDialog by remember { mutableStateOf(false) }
    var showBackgroundColorDialog by remember { mutableStateOf(false) }
    var showPaddingDialog by remember { mutableStateOf(false) }
    var showCornerRadiusDialog by remember { mutableStateOf(false) }
    var showHorizontalAlignment by remember { mutableStateOf(false) }
    var showVerticalArrangement by remember { mutableStateOf(false) }
    var showHorizontalArrangement by remember { mutableStateOf(false) }
    var showVerticalAlignment by remember { mutableStateOf(false) }
    var showLayoutType by remember { mutableStateOf(false) }
    var showIconPaddingDialog by remember { mutableStateOf(false) }
    var showTextPaddingDialog by remember { mutableStateOf(false) }

    val items = buildGridItemSettingsItems(
        // Settings
        gridItemSettings = gridItemSettings,
        // Click Actions
        onIconSizeClick = {
            showIconSizeDialog = true
        },
        onTextColorClick = {
            showTextColorDialog = true
        },
        onTextSizeClick = {
            showTextSizeDialog = true
        },
        onBackgroundColorClick = {
            showBackgroundColorDialog = true
        },
        onPaddingClick = {
            showPaddingDialog = true
        },
        onCornerRadiusClick = {
            showCornerRadiusDialog = true
        },
        onHorizontalAlignmentClick = {
            showHorizontalAlignment = true
        },
        onVerticalArrangementClick = {
            showVerticalArrangement = true
        },
        onHorizontalArrangementClick = {
            showHorizontalArrangement = true
        },
        onVerticalAlignmentClick = {
            showVerticalAlignment = true
        },
        onLayoutTypeClick = {
            showLayoutType = true
        },
        onIconPaddingClick = {
            showIconPaddingDialog = true
        },
        onTextPaddingClick = {
            showTextPaddingDialog = true
        },
        // Update Actions
        onUpdateGridItemSettings = onUpdateGridItemSettings,
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingsCategoryText(text = stringResource(R.string.grid_item))

        SettingsItems(items = items)
    }

    if (showIconSizeDialog) {
        EditIconSizeDialog(
            // Content
            iconSize = gridItemSettings.iconSize,
            // Callbacks
            onDismissRequest = {
                showIconSizeDialog = false
            },
            onUpdateIconSize = {
                onUpdateGridItemSettings(
                    gridItemSettings.copy(
                        iconSize = it,
                    ),
                )
            },
        )
    }

    if (showTextColorDialog) {
        TextColorDialog(
            // Content
            title = stringResource(R.string.text_color),
            // State
            textColor = gridItemSettings.textColor,
            customTextColor = gridItemSettings.customTextColor,
            // Actions
            onDismissRequest = {
                showTextColorDialog = false
            },
            onUpdateClick = { textColor, customTextColor ->
                onUpdateGridItemSettings(
                    gridItemSettings.copy(
                        textColor = textColor,
                        customTextColor = customTextColor,
                    ),
                )
            },
        )
    }

    if (showTextSizeDialog) {
        EditTextSizeDialog(
            // State
            textSize = gridItemSettings.textSize,
            // Actions
            onDismissRequest = {
                showTextSizeDialog = false
            },
            onUpdateTextSize = {
                onUpdateGridItemSettings(
                    gridItemSettings.copy(
                        textSize = it,
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
            // Callbacks
            onDismissRequest = {
                showBackgroundColorDialog = false
            },
            onSelectColor = {
                onUpdateGridItemSettings(gridItemSettings.copy(customBackgroundColor = it))
            },
        )
    }

    if (showPaddingDialog) {
        EditPaddingDialog(
            // Content
            title = stringResource(R.string.padding),
            padding = gridItemSettings.padding,
            // Callbacks
            onDismissRequest = {
                showPaddingDialog = false
            },
            onUpdatePadding = {
                onUpdateGridItemSettings(
                    gridItemSettings.copy(
                        padding = it,
                    ),
                )
            },
        )
    }

    if (showCornerRadiusDialog) {
        EditCornerRadiusDialog(
            // State
            cornerRadius = gridItemSettings.cornerRadius,
            // Callbacks
            onDismissRequest = {
                showCornerRadiusDialog = false
            },
            onUpdateCornerRadius = {
                onUpdateGridItemSettings(
                    gridItemSettings.copy(
                        cornerRadius = it,
                    ),
                )
            },
        )
    }

    if (showHorizontalAlignment) {
        RadioOptionsDialog(
            // Dialog Content
            title = stringResource(R.string.horizontal_alignment),
            options = HorizontalAlignment.entries,
            selected = gridItemSettings.horizontalAlignment,
            // Callbacks
            label = {
                it.getSubtitle()
            },
            onDismissRequest = {
                showHorizontalAlignment = false
            },
            onUpdateClick = {
                onUpdateGridItemSettings(gridItemSettings.copy(horizontalAlignment = it))
            },
        )
    }

    if (showVerticalArrangement) {
        RadioOptionsDialog(
            // Dialog Content
            title = stringResource(R.string.vertical_arrangement),
            options = VerticalArrangement.entries,
            selected = gridItemSettings.verticalArrangement,
            // Callbacks
            label = {
                it.getSubtitle()
            },
            onDismissRequest = {
                showVerticalArrangement = false
            },
            onUpdateClick = {
                onUpdateGridItemSettings(gridItemSettings.copy(verticalArrangement = it))
            },
        )
    }

    if (showHorizontalArrangement) {
        RadioOptionsDialog(
            // Dialog Content
            title = "Horizontal Arrangement",
            options = HorizontalArrangement.entries,
            selected = gridItemSettings.horizontalArrangement,
            // Callbacks
            label = {
                it.getSubtitle()
            },
            onDismissRequest = {
                showHorizontalArrangement = false
            },
            onUpdateClick = {
                onUpdateGridItemSettings(gridItemSettings.copy(horizontalArrangement = it))
            },
        )
    }

    if (showVerticalAlignment) {
        RadioOptionsDialog(
            // Dialog Content
            title = "Vertical Alignment",
            options = VerticalAlignment.entries,
            selected = gridItemSettings.verticalAlignment,
            // Callbacks
            label = {
                it.getSubtitle()
            },
            onDismissRequest = {
                showVerticalAlignment = false
            },
            onUpdateClick = {
                onUpdateGridItemSettings(gridItemSettings.copy(verticalAlignment = it))
            },
        )
    }

    if (showLayoutType) {
        RadioOptionsDialog(
            // Dialog Content
            title = "Layout Type",
            options = LayoutType.entries,
            selected = gridItemSettings.layoutType,
            // Callbacks
            label = {
                it.getSubtitle()
            },
            onDismissRequest = {
                showLayoutType = false
            },
            onUpdateClick = {
                onUpdateGridItemSettings(gridItemSettings.copy(layoutType = it))
            },
        )
    }

    if (showIconPaddingDialog) {
        EditPaddingDialog(
            // Content
            title = "Icon Padding",
            padding = gridItemSettings.iconPadding,
            // Callbacks
            onDismissRequest = {
                showIconPaddingDialog = false
            },
            onUpdatePadding = {
                onUpdateGridItemSettings(
                    gridItemSettings.copy(
                        iconPadding = it,
                    ),
                )
            },
        )
    }

    if (showTextPaddingDialog) {
        EditPaddingDialog(
            // Content
            title = "Text Padding",
            padding = gridItemSettings.textPadding,
            // Callbacks
            onDismissRequest = {
                showTextPaddingDialog = false
            },
            onUpdatePadding = {
                onUpdateGridItemSettings(
                    gridItemSettings.copy(
                        textPadding = it,
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
internal fun CustomBackgroundColor(
    // Item Data
    index: Int,
    size: Int,
    title: String,
    // Appearance
    customBackgroundColor: Int,
    // Layout
    modifier: Modifier = Modifier,
    // Actions
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = settingsItemShape(
            index = index,
            size = size,
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(customBackgroundColor),
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
private fun buildGridItemSettingsItems(
    // Settings
    gridItemSettings: GridItemSettings,
    // Click Actions
    onIconSizeClick: () -> Unit,
    onTextColorClick: () -> Unit,
    onTextSizeClick: () -> Unit,
    onBackgroundColorClick: () -> Unit,
    onPaddingClick: () -> Unit,
    onCornerRadiusClick: () -> Unit,
    onHorizontalAlignmentClick: () -> Unit,
    onVerticalArrangementClick: () -> Unit,
    onHorizontalArrangementClick: () -> Unit,
    onVerticalAlignmentClick: () -> Unit,
    onLayoutTypeClick: () -> Unit,
    onIconPaddingClick: () -> Unit,
    onTextPaddingClick: () -> Unit,
    // Update Actions
    onUpdateGridItemSettings: (GridItemSettings) -> Unit,
): List<SettingsItem> = buildList {
    add(
        SettingsItem.Column(
            title = stringResource(R.string.layout_type),
            subtitle = gridItemSettings.layoutType.getSubtitle(),
            onClick = onLayoutTypeClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.icon_size),
            subtitle = "${gridItemSettings.iconSize}",
            onClick = onIconSizeClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.text_color),
            subtitle = gridItemSettings.textColor.getSubtitle(),
            onClick = onTextColorClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.text_size),
            subtitle = "${gridItemSettings.textSize}",
            onClick = onTextSizeClick,
        ),
    )

    add(
        SettingsItem.CustomBackgroundColor(
            title = stringResource(commonR.string.background_color),
            customBackgroundColor = gridItemSettings.customBackgroundColor,
            onClick = onBackgroundColorClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.padding),
            subtitle = "${gridItemSettings.padding}",
            onClick = onPaddingClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.corner_radius),
            subtitle = "${gridItemSettings.cornerRadius}",
            onClick = onCornerRadiusClick,
        ),
    )

    add(
        SettingsItem.Switch(
            checked = gridItemSettings.singleLineLabel,
            title = stringResource(R.string.single_line_label),
            subtitle = stringResource(R.string.limit_app_names_to_one_line),
            onClick = {
                onUpdateGridItemSettings(
                    gridItemSettings.copy(singleLineLabel = !gridItemSettings.singleLineLabel),
                )
            },
            onCheckedChange = {
                onUpdateGridItemSettings(
                    gridItemSettings.copy(singleLineLabel = it),
                )
            },
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.horizontal_alignment),
            subtitle = gridItemSettings.horizontalAlignment.getSubtitle(),
            onClick = onHorizontalAlignmentClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.vertical_arrangement),
            subtitle = gridItemSettings.verticalArrangement.getSubtitle(),
            onClick = onVerticalArrangementClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.horizontal_arrangement),
            subtitle = gridItemSettings.horizontalArrangement.getSubtitle(),
            onClick = onHorizontalArrangementClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.vertical_alignment),
            subtitle = gridItemSettings.verticalAlignment.getSubtitle(),
            onClick = onVerticalAlignmentClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.icon_padding),
            subtitle = "${gridItemSettings.iconPadding}",
            onClick = onIconPaddingClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.text_padding),
            subtitle = "${gridItemSettings.textPadding}",
            onClick = onTextPaddingClick,
        ),
    )
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
