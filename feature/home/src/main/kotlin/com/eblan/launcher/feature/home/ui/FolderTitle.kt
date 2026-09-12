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
package com.eblan.launcher.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.PageIndicator
import com.eblan.launcher.feature.home.util.PAGE_INDICATOR_HEIGHT
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor

@Composable
internal fun <T> FolderTitle(
    modifier: Modifier = Modifier,
    label: String,
    gridItemsByPage: Map<Int, List<T>>,
    folderGridHorizontalPagerState: PagerState,
    progress: Float,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    textColor: TextColor,
    customTextColor: Int,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
) {
    val color = getTextColorFromBackgroundColor(
        backgroundColor = folderBackgroundColor,
        customBackgroundColor = customFolderBackgroundColor,
        textColor = textColor,
        customTextColor = customTextColor,
        systemTextColor = systemTextColor,
        systemCustomTextColor = systemCustomTextColor,
        defaultColor = MaterialTheme.colorScheme.onSurface,
    )

    Box(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
            .height(PAGE_INDICATOR_HEIGHT)
            .alpha(if (progress > 0.5) 1f else 0f),
    ) {
        if (gridItemsByPage.size > 1) {
            Row(
                modifier = Modifier.matchParentSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = label,
                    color = color,
                    style = MaterialTheme.typography.bodySmall,
                )

                PageIndicator(
                    color = color,
                    gridHorizontalPagerState = folderGridHorizontalPagerState,
                    infiniteScroll = false,
                    pageCount = gridItemsByPage.size,
                )
            }
        } else {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = label,
                color = color,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
