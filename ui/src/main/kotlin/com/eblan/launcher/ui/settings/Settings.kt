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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
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
import com.eblan.launcher.domain.model.iconpackinfo.PackageManagerIconPackInfo
import com.eblan.launcher.ui.R
import com.eblan.launcher.ui.local.LocalAccessibilityManager
import com.eblan.launcher.ui.local.LocalPackageManager
import com.eblan.launcher.ui.local.LocalSettings
import com.eblan.launcher.ui.model.SettingsItem
import com.eblan.launcher.common.R as commonR

@Composable
fun SettingsSwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    title: String,
    subtitle: String,
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
fun SettingsSwitch(
    modifier: Modifier = Modifier,
    index: Int,
    size: Int,
    checked: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@Composable
fun SettingsItems(
    modifier: Modifier = Modifier,
    items: List<SettingsItem>,
) {
    items.forEachIndexed { index, settingsItem ->
        when (settingsItem) {
            is SettingsItem.Column -> {
                SettingsColumn(
                    modifier = modifier,
                    index = index,
                    size = items.size,
                    title = settingsItem.title,
                    subtitle = settingsItem.subtitle,
                    onClick = settingsItem.onClick,
                )
            }

            is SettingsItem.Switch -> {
                SettingsSwitch(
                    modifier = modifier,
                    index = index,
                    size = items.size,
                    checked = settingsItem.checked,
                    title = settingsItem.title,
                    subtitle = settingsItem.subtitle,
                    onClick = settingsItem.onClick,
                    onCheckedChange = settingsItem.onCheckedChange,
                )
            }

            is SettingsItem.CustomBackgroundColor,
            -> {
                CustomBackgroundColor(
                    modifier = modifier,
                    index = index,
                    size = items.size,
                    title = settingsItem.title,
                    customBackgroundColor = settingsItem.customBackgroundColor,
                    onClick = settingsItem.onClick,
                )
            }

            is SettingsItem.Row -> {
                SettingsRow(
                    modifier = modifier,
                    index = index,
                    size = items.size,
                    imageVector = settingsItem.imageVector,
                    title = settingsItem.title,
                    subtitle = settingsItem.subtitle,
                    onClick = settingsItem.onClick,
                )
            }

            is SettingsItem.CustomIcon -> {
                CustomIcon(
                    modifier = modifier,
                    index = index,
                    size = items.size,
                    customIcon = settingsItem.customIcon,
                    packageManagerIconPackInfos = settingsItem.packageManagerIconPackInfos,
                    onUpdateIconPackInfoPackageName = settingsItem.onUpdateIconPackInfoPackageName,
                    onUpdateUri = settingsItem.onUpdateUri,
                    onResetCustomIcon = settingsItem.onResetCustomIcon,
                )
            }
        }
    }
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

fun settingsItemShape(
    index: Int,
    size: Int,
    radius: Dp = 16.dp,
): Shape = when {
    size == 1 -> RoundedCornerShape(size = radius)

    index == 0 -> RoundedCornerShape(
        topStart = radius,
        topEnd = radius,
        bottomStart = radius / 2,
        bottomEnd = radius / 2,
    )

    index == size - 1 -> RoundedCornerShape(
        topStart = radius / 2,
        topEnd = radius / 2,
        bottomStart = radius,
        bottomEnd = radius,
    )

    else -> RoundedCornerShape(size = radius / 2)
}

@Composable
private fun SettingsColumn(
    modifier: Modifier = Modifier,
    index: Int,
    size: Int,
    title: String,
    subtitle: String,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun CustomIcon(
    modifier: Modifier = Modifier,
    index: Int,
    size: Int,
    customIcon: String?,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    onUpdateIconPackInfoPackageName: (
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
        shape = settingsItemShape(
            index = index,
            size = size,
        ),
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
                        title = stringResource(R.string.gallery),
                        subtitle = stringResource(R.string.pick_icons_from_your_gallery),
                        onClick = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    )

                    packageManagerIconPackInfos.forEach { packageManagerIconPackInfo ->
                        IconPackItem(
                            icon = packageManagerIconPackInfo.icon,
                            label = packageManagerIconPackInfo.label,
                            packageName = packageManagerIconPackInfo.packageName,
                            onClick = {
                                onUpdateIconPackInfoPackageName(
                                    packageManagerIconPackInfo.packageName,
                                    packageManagerIconPackInfo.label,
                                )
                            },
                        )
                    }

                    if (customIcon != null) {
                        SettingsColumn(
                            title = stringResource(R.string.reset_custom_icon),
                            subtitle = stringResource(R.string.reset_custom_icon),
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
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(15.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun IconPackItem(
    modifier: Modifier = Modifier,
    icon: ByteArray?,
    label: String?,
    packageName: String,
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
            Text(
                text = label.toString(),
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = packageName,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SettingsRow(
    modifier: Modifier = Modifier,
    index: Int,
    size: Int,
    imageVector: ImageVector,
    subtitle: String,
    title: String,
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
