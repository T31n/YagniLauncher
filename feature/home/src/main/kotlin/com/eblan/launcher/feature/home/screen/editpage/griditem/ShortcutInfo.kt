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
package com.eblan.launcher.feature.home.screen.editpage.griditem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.feature.home.component.GridItemLayoutType
import com.eblan.launcher.feature.home.component.LauncherIconImage
import com.eblan.launcher.feature.home.util.getGridItemBackgroundColor
import com.eblan.launcher.feature.home.util.getGridItemIconPath
import com.eblan.launcher.feature.home.util.getGridItemLabel

@Composable
internal fun ShortcutInfoGridItem(
    // Data and Settings
    data: GridItemData.ShortcutInfo,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    // Appearance
    textColor: Color,
    maxLines: Int,
    // Layout
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    // Modifier
    modifier: Modifier = Modifier,
) {
    val alpha = if (hasShortcutHostPermission && data.isEnabled) 1f else 0.3f

    val iconPath = getGridItemIconPath(data = data)
    val label = getGridItemLabel(data).toString()

    val itemModifier = modifier
        .fillMaxSize()
        .padding(gridItemSettings.padding.dp)
        .background(
            color = getGridItemBackgroundColor(
                backgroundColor = gridItemSettings.backgroundColor,
                customBackgroundColor = gridItemSettings.customBackgroundColor,
            ),
            shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
        )

    val commonModifier = Modifier
        .size(gridItemSettings.iconSize.dp)
        .padding(gridItemSettings.iconPadding.dp)
        .alpha(alpha)

    val iconContent: @Composable () -> Unit = {
        Box(modifier = commonModifier) {
            LauncherIconImage(
                iconPath = iconPath,
                modifier = Modifier.matchParentSize(),
            )

            LauncherIconImage(
                iconPath = data.eblanApplicationInfoIcon,
                modifier = Modifier
                    .size((gridItemSettings.iconSize * 0.25).dp)
                    .align(Alignment.BottomEnd),
            )
        }
    }

    val labelContent: @Composable () -> Unit = {
        Text(
            modifier = commonModifier,
            text = label,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            fontSize = gridItemSettings.textSize.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }

    GridItemLayoutType(
        // Layout and Modifier
        layoutType = gridItemSettings.layoutType,
        modifier = itemModifier,
        // Alignment and Arrangement
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        // Content
        iconContent = iconContent,
        labelContent = labelContent,
        labelOnlyContent = labelContent,
    )
}
