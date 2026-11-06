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

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.component.VerticalSlideReveal
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.iconpackinfo.PackageManagerIconPack
import com.eblan.launcher.ui.R
import com.eblan.launcher.ui.local.LocalAccessibilityManager
import com.eblan.launcher.ui.local.LocalPackageManager
import com.eblan.launcher.ui.local.LocalSettings
import com.eblan.launcher.common.R as commonR

@Composable
fun SettingsSwitch(
    // Content
    title: String,
    subtitle: String,
    // State
    checked: Boolean,
    // Layout
    modifier: Modifier = Modifier,
    // Callbacks
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(
                onClick = {
                    onCheckedChange(!checked)
                },
            )
            .fillMaxWidth()
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SettingsItemText(
                title = title,
                subtitle = subtitle,
                spacing = 4.dp,
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
internal fun SettingsItemCard(
    // Modifier and Layout
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    shape: Shape,
    // Actions and Content
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
fun SettingsColumnItem(
    // Content
    title: String,
    subtitle: String,
    // Layout
    shape: Shape,
    modifier: Modifier = Modifier,
    // Actions
    onClick: () -> Unit,
) {
    SettingsItemCard(
        // Modifier and Layout
        modifier = modifier,
        shape = shape,
        // Actions and Content
        onClick = onClick,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SettingsItemText(
                title = title,
                subtitle = subtitle,
            )
        }
    }
}

@Composable
fun SettingsRowItem(
    // Content
    title: String,
    subtitle: String,
    imageVector: ImageVector,
    // Appearance
    shape: Shape,
    modifier: Modifier = Modifier,
    // Action
    onClick: () -> Unit,
) {
    SettingsItemCard(
        // Modifier and Layout
        modifier = modifier,
        shape = shape,
        // Actions and Content
        onClick = onClick,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.width(20.dp))

        Column {
            SettingsItemText(
                title = title,
                subtitle = subtitle,
            )
        }
    }
}

@Composable
fun <T> SettingsDropdownItem(
    // Content
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    // Layout
    shape: Shape,
    modifier: Modifier = Modifier,
    // Actions
    onSelectedChange: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    SettingsItemCard(
        // Modifier and Layout
        modifier = modifier,
        shape = shape,
        // Actions and Content
        onClick = {
            expanded = true
        },
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = label(selected))

                val iconRotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    label = "dropdownIconRotation",
                )

                Icon(
                    modifier = Modifier.graphicsLayer {
                        rotationZ = iconRotation
                    },
                    imageVector = EblanLauncherIcons.ArrowDropDown,
                    contentDescription = null,
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(text = label(option))
                        },
                        onClick = {
                            expanded = false
                            onSelectedChange(option)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    // Content
    title: String,
    subtitle: String,
    // State
    checked: Boolean,
    // Appearance
    shape: Shape,
    modifier: Modifier = Modifier,
    // Callbacks
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsItemCard(
        // Modifier and Layout
        modifier = modifier,
        shape = shape,
        // Actions and Content
        onClick = { onCheckedChange(!checked) },
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SettingsItemText(
                title = title,
                subtitle = subtitle,
                spacing = 4.dp,
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SettingsItemText(
    title: String,
    subtitle: String,
    spacing: Dp = 5.dp,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMediumEmphasized,
    )

    Spacer(modifier = Modifier.height(spacing))

    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
fun SettingsCategoryText(
    modifier: Modifier = Modifier,
    text: String,
) {
    Text(
        modifier = modifier.padding(15.dp),
        text = text,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
fun rememberIsDefaultLauncher(): State<Boolean> {
    val packageManager = LocalPackageManager.current

    val lifecycleOwner = LocalLifecycleOwner.current

    return produceState(
        initialValue = false,
        key1 = lifecycleOwner,
        key2 = packageManager,
    ) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            value = packageManager.isDefaultLauncher()
        }
    }
}

@Composable
fun rememberIsNotificationAccessGranted(): State<Boolean> {
    val settingsWrapper = LocalSettings.current

    val lifecycleOwner = LocalLifecycleOwner.current

    return produceState(
        initialValue = false,
        key1 = lifecycleOwner,
        key2 = settingsWrapper,
    ) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            value = settingsWrapper.isNotificationAccessGranted()
        }
    }
}

@Composable
fun rememberIsAccessibilityServiceEnabled(): State<Boolean> {
    val accessibilityManager = LocalAccessibilityManager.current

    val lifecycleOwner = LocalLifecycleOwner.current

    return produceState(
        initialValue = false,
        key1 = lifecycleOwner,
        key2 = accessibilityManager,
    ) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            value = accessibilityManager.isAccessibilityServiceEnabled()
        }
    }
}

val settingsFirstItemShape: Shape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = 8.dp,
    bottomEnd = 8.dp,
)

val settingsMiddleItemShape: Shape = RoundedCornerShape(size = 8.dp)

val settingsLastItemShape: Shape = RoundedCornerShape(
    topStart = 8.dp,
    topEnd = 8.dp,
    bottomStart = 16.dp,
    bottomEnd = 16.dp,
)

@Composable
fun CustomIcon(
    // Icon Data
    customIcon: String?,
    shape: Shape,
    packageManagerIconPacks: List<PackageManagerIconPack>,
    // Layout
    modifier: Modifier = Modifier,
    // Callbacks
    onUpdateIconPackPackageName: (
        packageName: String,
        label: String?,
    ) -> Unit,
    onUpdateUri: (String) -> Unit,
    onResetCustomIcon: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION

                context.contentResolver.takePersistableUriPermission(uri, flag)

                onUpdateUri(uri.toString())
            }
        }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (customIcon != null) {
                    AsyncImage(
                        modifier = Modifier.size(40.dp),
                        model = customIcon,
                        contentDescription = null,
                    )
                } else {
                    Icon(
                        modifier = Modifier.size(40.dp),
                        imageVector = EblanLauncherIcons.BrokenImage,
                        contentDescription = null,
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.custom_icon))

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = customIcon ?: stringResource(commonR.string.none))
                }

                IconButton(
                    onClick = {
                        expanded = !expanded
                    },
                ) {
                    Icon(
                        imageVector = if (expanded) {
                            EblanLauncherIcons.ArrowDropUp
                        } else {
                            EblanLauncherIcons.ArrowDropDown
                        },
                        contentDescription = null,
                    )
                }
            }

            VerticalSlideReveal(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsColumn(
                        // Content
                        title = stringResource(R.string.gallery),
                        subtitle = stringResource(R.string.pick_icons_from_your_gallery),
                        // Actions
                        onClick = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    )

                    packageManagerIconPacks.forEach { packageManagerIconPackInfo ->
                        IconPackItem(
                            // Item Data
                            icon = packageManagerIconPackInfo.icon,
                            label = packageManagerIconPackInfo.label,
                            packageName = packageManagerIconPackInfo.packageName,
                            // Action
                            onClick = {
                                onUpdateIconPackPackageName(
                                    packageManagerIconPackInfo.packageName,
                                    packageManagerIconPackInfo.label,
                                )
                            },
                        )
                    }

                    if (customIcon != null) {
                        SettingsColumn(
                            // Content
                            title = stringResource(R.string.reset_custom_icon),
                            subtitle = stringResource(R.string.reset_custom_icon),
                            // Actions
                            onClick = onResetCustomIcon,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsColumn(
    // Content
    title: String,
    subtitle: String,
    // Layout
    modifier: Modifier = Modifier,
    // Actions
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(15.dp),
    ) {
        SettingsItemText(
            title = title,
            subtitle = subtitle,
        )
    }
}

@Composable
private fun IconPackItem(
    // Item Data
    icon: ByteArray?,
    label: String?,
    packageName: String,
    // Layout
    modifier: Modifier = Modifier,
    // Action
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            modifier = Modifier.size(40.dp),
            model = icon,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.width(20.dp))

        Column {
            SettingsItemText(
                title = label.toString(),
                subtitle = packageName,
            )
        }
    }
}
