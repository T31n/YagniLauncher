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
package com.eblan.launcher.ui.dialog

import android.graphics.drawable.Drawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.model.iconpackinfo.IconPackInfoComponent
import com.eblan.launcher.ui.local.LocalFileManager
import com.eblan.launcher.ui.local.LocalIconKeyGenerator
import com.eblan.launcher.ui.local.LocalIconPackManager
import com.eblan.launcher.ui.local.LocalImageSerializer
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import com.eblan.launcher.common.R as commonR

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun IconPackInfoFilesDialog(
    modifier: Modifier = Modifier,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    iconPackInfoPackageName: String?,
    iconPackInfoLabel: String?,
    iconName: String,
    onDismissRequest: () -> Unit,
    onUpdateIcon: (String?) -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
) {
    requireNotNull(iconPackInfoPackageName)

    requireNotNull(iconPackInfoLabel)
    val scope = rememberCoroutineScope()

    val byteArray = LocalImageSerializer.current

    val fileManager = LocalFileManager.current

    val iconPackManager = LocalIconPackManager.current

    val iconKeyGenerator = LocalIconKeyGenerator.current

    val searchBarState = rememberSearchBarState()

    val textFieldState = rememberTextFieldState()

    LaunchedEffect(key1 = textFieldState) {
        snapshotFlow { textFieldState.text }.debounce(500L.milliseconds).onEach { text ->
            onSearchIconPackInfoComponent(text.toString())
        }.collect()
    }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = iconPackInfoLabel,
            style = MaterialTheme.typography.titleLarge,
        )

        SearchBar(
            state = searchBarState,
            modifier = Modifier.fillMaxWidth(),
            inputField = {
                SearchBarDefaults.InputField(
                    textFieldState = textFieldState,
                    searchBarState = searchBarState,
                    leadingIcon = {
                        Icon(
                            imageVector = EblanLauncherIcons.Search,
                            contentDescription = null,
                        )
                    },
                    onSearch = {
                        scope.launch { searchBarState.animateToCollapsed() }
                    },
                    placeholder = {
                        Text(text = stringResource(commonR.string.search_applications))
                    },
                )
            },
        )

        when {
            iconPackInfoComponents.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                )
            }

            else -> {
                LazyVerticalGrid(
                    modifier = Modifier.weight(
                        weight = 1f,
                        fill = false,
                    ),
                    columns = GridCells.Fixed(5),
                ) {
                    items(iconPackInfoComponents) { iconPackInfoComponent ->

                        var drawable by remember { mutableStateOf<Drawable?>(null) }

                        LaunchedEffect(iconPackInfoComponent) {
                            drawable = iconPackManager.loadDrawableFromIconPack(
                                packageName = iconPackInfoPackageName,
                                drawableName = iconPackInfoComponent.drawableName,
                            )
                        }

                        AsyncImage(
                            model = drawable,
                            contentDescription = null,
                            modifier = Modifier
                                .clickable {
                                    scope.launch {
                                        val icon = drawable?.let { currentDrawable ->
                                            val directory = fileManager.getFilesDirectory(
                                                FileManager.CUSTOM_ICONS_DIR,
                                            )

                                            val file = File(
                                                directory,
                                                iconKeyGenerator.getHashedName(name = iconName),
                                            )

                                            byteArray.createDrawablePath(
                                                drawable = currentDrawable,
                                                file = file,
                                            )

                                            file.absolutePath
                                        }

                                        if (icon != null) {
                                            onUpdateIcon(icon)
                                        }

                                        onDismissRequest()
                                    }
                                }
                                .size(40.dp)
                                .padding(2.dp),
                        )
                    }
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
                Text(text = stringResource(commonR.string.cancel))
            }
        }
    }
}
