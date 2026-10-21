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
package com.eblan.launcher.feature.settings.experimental.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.feature.settings.experimental.R
import com.eblan.launcher.ui.settings.SettingsSwitch
import com.eblan.launcher.common.R as commonR

@Composable
internal fun SyncDataDialog(
    // State
    syncData: Boolean,
    // Layout
    modifier: Modifier = Modifier,
    // Actions
    onDismissRequest: () -> Unit,
    onUpdateSyncData: (Boolean) -> Unit,
) {
    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(R.string.warning),
            style = MaterialTheme.typography.titleLarge,
        )

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.disable_background_sync_description),
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                SettingsSwitch(
                    // Content
                    title = stringResource(R.string.sync_data),
                    subtitle = stringResource(R.string.keep_data_up_to_date),
                    // State
                    checked = syncData,
                    // Callbacks
                    onCheckedChange = onUpdateSyncData,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onDismissRequest,
            ) {
                Text(text = stringResource(commonR.string.okay))
            }
        }
    }
}
