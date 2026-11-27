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

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.designsystem.theme.EblanSpacing
import com.eblan.launcher.domain.model.cache.CacheInfo
import com.eblan.launcher.feature.settings.experimental.R
import com.eblan.launcher.ui.settings.SettingsSwitch
import com.eblan.launcher.common.R as commonR

@Composable
internal fun CleanCacheDialog(
    // State
    cleanCache: Boolean,
    cacheInfo: CacheInfo,
    isCleaningUp: Boolean,
    // Layout
    modifier: Modifier = Modifier,
    // Actions
    onDismissRequest: () -> Unit,
    onUpdateCleanCache: (Boolean) -> Unit,
    onCleanUpCache: () -> Unit,
) {
    val context = LocalContext.current

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(commonR.string.cache),
            style = MaterialTheme.typography.titleLarge,
        )

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(EblanSpacing.large),
        ) {
            SettingsSwitch(
                // Content
                title = stringResource(R.string.clean_cache),
                subtitle = stringResource(R.string.clean_cache_on_launch),
                // State
                checked = cleanCache,
                // Callbacks
                onCheckedChange = onUpdateCleanCache,
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(EblanSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(EblanSpacing.medium),
                ) {
                    CacheSizeRow(
                        label = stringResource(R.string.total_cache),
                        size = Formatter.formatShortFileSize(context, cacheInfo.totalSize),
                    )

                    CacheSizeRow(
                        label = stringResource(R.string.can_be_freed),
                        size = Formatter.formatShortFileSize(context, cacheInfo.reclaimableSize),
                    )
                }
            }

            Text(
                text = stringResource(R.string.cache_description),
                style = MaterialTheme.typography.bodyMedium,
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCleaningUp && cacheInfo.reclaimableSize > 0L,
                onClick = onCleanUpCache,
            ) {
                if (isCleaningUp) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        strokeWidth = 2.dp,
                    )

                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))

                    Text(text = stringResource(R.string.cleaning_up))
                } else if (cacheInfo.reclaimableSize > 0L) {
                    Text(text = stringResource(R.string.clean_up))
                } else {
                    Text(text = stringResource(R.string.nothing_to_clean_up))
                }
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

@Composable
private fun CacheSizeRow(
    modifier: Modifier = Modifier,
    label: String,
    size: String,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )

        Text(
            text = size,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
