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
package com.eblan.launcher.feature.settings.settings

import android.content.Intent
import android.provider.Settings.ACTION_HOME_SETTINGS
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.ui.model.SettingsItem
import com.eblan.launcher.ui.settings.SettingsItems
import com.eblan.launcher.ui.settings.rememberIsDefaultLauncher
import com.eblan.launcher.common.R as commonR

@Composable
internal fun SettingsRoute(
    modifier: Modifier = Modifier,
    onAppDrawer: () -> Unit,
    onExperimental: () -> Unit,
    onFinish: () -> Unit,
    onGeneral: () -> Unit,
    onGestures: () -> Unit,
    onHome: () -> Unit,
) {
    SettingsScreen(
        modifier = modifier,
        onAppDrawer = onAppDrawer,
        onExperimental = onExperimental,
        onFinish = onFinish,
        onGeneral = onGeneral,
        onGestures = onGestures,
        onHome = onHome,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    modifier: Modifier = Modifier,
    onAppDrawer: () -> Unit,
    onExperimental: () -> Unit,
    onFinish: () -> Unit,
    onGeneral: () -> Unit,
    onGestures: () -> Unit,
    onHome: () -> Unit,
) {
    val items = buildSettingsItems(
        onGeneralClick = onGeneral,
        onHomeClick = onHome,
        onAppDrawerClick = onAppDrawer,
        onGesturesClick = onGestures,
        onExperimentalClick = onExperimental,
    )

    BackHandler {
        onFinish()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(commonR.string.settings))
                },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
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
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .matchParentSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                AlphaWarningCard()

                SettingsItems(items = items)
            }
        }
    }
}

@Composable
private fun AlphaWarningCard(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    val repoUrl = "https://github.com/JackEblan/YagniLauncher"

    val kofiUrl = "https://ko-fi.com/I3I01OJG21"

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.thank_you_for_using_yagni_launcher_alpha),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(R.string.about_development_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(R.string.support_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { uriHandler.openUri(kofiUrl) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(R.string.support_on_ko_fi),
                        textAlign = TextAlign.Center,
                    )
                }

                OutlinedButton(
                    onClick = { uriHandler.openUri(repoUrl) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(R.string.star_on_github),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Text(
                text = stringResource(R.string.note_this_informational_card_will_be_removed_in_future_stable_releases),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun buildSettingsItems(
    onGeneralClick: () -> Unit,
    onHomeClick: () -> Unit,
    onAppDrawerClick: () -> Unit,
    onGesturesClick: () -> Unit,
    onExperimentalClick: () -> Unit,
): List<SettingsItem> {
    val context = LocalContext.current

    val isDefaultLauncher by rememberIsDefaultLauncher()

    return buildList {
        if (!isDefaultLauncher) {
            add(
                SettingsItem.Row(
                    imageVector = EblanLauncherIcons.Info,
                    title = stringResource(R.string.default_launcher),
                    subtitle = stringResource(R.string.choose_yagni_launcher),
                    onClick = {
                        context.startActivity(Intent(ACTION_HOME_SETTINGS))
                    },
                ),
            )
        }

        add(
            SettingsItem.Row(
                imageVector = EblanLauncherIcons.Settings,
                title = stringResource(commonR.string.general),
                subtitle = stringResource(R.string.themes_icon_packs),
                onClick = onGeneralClick,
            ),
        )

        add(
            SettingsItem.Row(
                imageVector = EblanLauncherIcons.Home,
                title = stringResource(commonR.string.home),
                subtitle = stringResource(R.string.grid_icon_dock_and_more),
                onClick = onHomeClick,
            ),
        )

        add(
            SettingsItem.Row(
                imageVector = EblanLauncherIcons.Apps,
                title = stringResource(commonR.string.app_drawer),
                subtitle = stringResource(R.string.columns_and_rows_count),
                onClick = onAppDrawerClick,
            ),
        )

        add(
            SettingsItem.Row(
                imageVector = EblanLauncherIcons.Gesture,
                title = stringResource(commonR.string.gestures),
                subtitle = stringResource(R.string.swipe_gesture_actions),
                onClick = onGesturesClick,
            ),
        )

        add(
            SettingsItem.Row(
                imageVector = EblanLauncherIcons.DeveloperMode,
                title = stringResource(commonR.string.experimental),
                subtitle = stringResource(R.string.advanced_options_for_power_users),
                onClick = onExperimentalClick,
            ),
        )
    }
}
