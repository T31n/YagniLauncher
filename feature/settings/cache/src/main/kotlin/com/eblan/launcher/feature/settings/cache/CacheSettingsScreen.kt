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
package com.eblan.launcher.feature.settings.cache

import android.content.Intent
import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.designsystem.theme.EblanSpacing
import com.eblan.launcher.domain.model.cache.CacheInfo
import com.eblan.launcher.feature.settings.cache.model.CacheSettingsUiState
import com.eblan.launcher.platform.service.IconPackService
import com.eblan.launcher.common.R as commonR

@Composable
internal fun CacheSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: CacheSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val cacheSettingsUiState by viewModel.cacheSettingsUiState.collectAsStateWithLifecycle()

    CacheSettingsScreen(
        modifier = modifier,
        cacheSettingsUiState = cacheSettingsUiState,
        onNavigateUp = onNavigateUp,
        onCleanUpCache = viewModel::cleanUpCache,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CacheSettingsScreen(
    modifier: Modifier = Modifier,
    cacheSettingsUiState: CacheSettingsUiState,
    onNavigateUp: () -> Unit,
    onCleanUpCache: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(commonR.string.cache))
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
            when (cacheSettingsUiState) {
                CacheSettingsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is CacheSettingsUiState.Success -> {
                    Success(
                        cacheInfo = cacheSettingsUiState.cacheInfo,
                        isCleaningUp = cacheSettingsUiState.isCleaningUp,
                        onCleanUpCache = onCleanUpCache,
                    )
                }
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    cacheInfo: CacheInfo,
    isCleaningUp: Boolean,
    onCleanUpCache: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                context.stopService(Intent(context, IconPackService::class.java))
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(EblanSpacing.large),
        verticalArrangement = Arrangement.spacedBy(EblanSpacing.large),
    ) {
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
