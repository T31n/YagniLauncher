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
package com.eblan.launcher.feature.settings.appdrawer.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.feature.settings.appdrawer.R
import com.eblan.launcher.ui.lazylist.DraggableColumnItem
import com.eblan.launcher.ui.lazylist.dragColumnContainer
import com.eblan.launcher.ui.lazylist.rememberLazyColumnDragDropState
import com.eblan.launcher.common.R as commonR

@Composable
internal fun ManageFoldersDialog(
    modifier: Modifier = Modifier,
    folderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
    onDismissRequest: () -> Unit,
    onUpdateFolderEblanApplicationInfos: (List<FolderEblanApplicationInfo>) -> Unit,
) {
    var currentFolderEblanApplicationInfos by remember { mutableStateOf(folderEblanApplicationInfos) }

    val lazyListState = rememberLazyListState()

    val lazyColumnDragDropState = rememberLazyColumnDragDropState(
        lazyListState = lazyListState,
    ) { from, to ->
        currentFolderEblanApplicationInfos =
            currentFolderEblanApplicationInfos.toMutableList().apply {
                add(
                    index = to,
                    element = removeAt(from),
                )
            }
    }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(R.string.manage_folders),
            style = MaterialTheme.typography.titleLarge,
        )

        when {
            currentFolderEblanApplicationInfos.isEmpty() -> {
                Text(
                    text = stringResource(R.string.no_folders),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .dragColumnContainer(lazyColumnDragDropState = lazyColumnDragDropState)
                        .fillMaxWidth(),
                    state = lazyListState,
                ) {
                    itemsIndexed(
                        items = currentFolderEblanApplicationInfos,
                        key = { _, folderEblanApplicationInfo -> folderEblanApplicationInfo.id },
                    ) { index, folderEblanApplicationInfo ->
                        DraggableColumnItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            lazyColumnDragDropState = lazyColumnDragDropState,
                            index = index,
                        ) {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(5.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                ) {
                                    Text(
                                        text = folderEblanApplicationInfo.label,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            }
                        }
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

            TextButton(
                onClick = {
                    val indexedFolderEblanApplicationInfos =
                        currentFolderEblanApplicationInfos.mapIndexed { index, folderEblanApplicationInfo ->
                            folderEblanApplicationInfo.copy(index = index)
                        }

                    if (folderEblanApplicationInfos != indexedFolderEblanApplicationInfos) {
                        onUpdateFolderEblanApplicationInfos(indexedFolderEblanApplicationInfos)
                    }

                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(commonR.string.update))
            }
        }
    }
}
