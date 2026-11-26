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
package com.eblan.launcher.feature.settings.general.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.feature.settings.general.R
import com.eblan.launcher.feature.settings.general.model.ImportIconPackProgress
import com.eblan.launcher.common.R as commonR

@Composable
internal fun ImportIconPackProgressDialog(
    // State
    importIconPackProgress: ImportIconPackProgress,
    // Layout and Appearance
    modifier: Modifier = Modifier,
    // Actions
    onCancelImport: () -> Unit,
) {
    EblanDialog(
        modifier = modifier,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        onDismissRequest = onCancelImport,
    ) {
        Text(
            text = stringResource(
                R.string.caching,
                importIconPackProgress.label,
            ),
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = stringResource(R.string.loading_icons_from_cache_is_faster),
            style = MaterialTheme.typography.bodyMedium,
        )

        if (importIconPackProgress.total > 0) {
            LinearProgressIndicator(
                progress = {
                    importIconPackProgress.current.toFloat() / importIconPackProgress.total
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = "${importIconPackProgress.current} / ${importIconPackProgress.total}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End),
            )
        } else {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onCancelImport,
            ) {
                Text(text = stringResource(commonR.string.cancel))
            }
        }
    }
}
