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

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest.Builder
import coil3.request.addLastModifiedToFileCacheKey
import coil3.size.Size
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.LayoutType
import com.eblan.launcher.feature.home.component.IconOnly
import com.eblan.launcher.feature.home.component.LabelOnly
import com.eblan.launcher.feature.home.component.StartIconEndLabel
import com.eblan.launcher.feature.home.component.StartLabelEndIcon
import com.eblan.launcher.feature.home.component.TopIconBottomLabel
import com.eblan.launcher.feature.home.component.TopLabelBottomIcon

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun ShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ShortcutInfo,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    textColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    maxLines: Int,
) {
    val context = LocalContext.current

    val customIcon = data.customIcon ?: data.icon
    val customShortLabel = data.customShortLabel ?: data.shortLabel

    val alpha = if (hasShortcutHostPermission && data.isEnabled) 1f else 0.3f

    val itemModifier = modifier
        .fillMaxSize()
        .padding(gridItemSettings.padding.dp)
        .background(
            color = Color(gridItemSettings.customBackgroundColor),
            shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
        )
    val iconModifier = Modifier
        .size(gridItemSettings.iconSize.dp)
        .padding(gridItemSettings.iconPadding.dp)

    val iconContent: @Composable () -> Unit = {
        Box(modifier = iconModifier) {
            AsyncImage(
                model = Builder(context)
                    .data(customIcon)
                    .addLastModifiedToFileCacheKey(true)
                    .size(Size.ORIGINAL)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(alpha),
            )

            AsyncImage(
                model = Builder(context)
                    .data(data.eblanApplicationInfoIcon)
                    .addLastModifiedToFileCacheKey(true)
                    .size(Size.ORIGINAL)
                    .build(),
                modifier = Modifier
                    .size((gridItemSettings.iconSize * 0.25).dp)
                    .align(Alignment.BottomEnd)
                    .alpha(alpha),
                contentDescription = null,
            )
        }
    }

    val labelContent: @Composable (Modifier) -> Unit = { modifier ->
        Text(
            modifier = modifier
                .padding(gridItemSettings.textPadding.dp)
                .alpha(alpha),
            text = customShortLabel,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            fontSize = gridItemSettings.textSize.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }

    when (gridItemSettings.layoutType) {
        LayoutType.TopIconBottomLabel ->
            TopIconBottomLabel(
                modifier = itemModifier,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                icon = iconContent,
                label = labelContent,
            )

        LayoutType.TopLabelBottomIcon ->
            TopLabelBottomIcon(
                modifier = itemModifier,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                icon = iconContent,
                label = labelContent,
            )

        LayoutType.StartIconEndLabel ->
            StartIconEndLabel(
                modifier = itemModifier,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                icon = iconContent,
                label = labelContent,
            )

        LayoutType.StartLabelEndIcon ->
            StartLabelEndIcon(
                modifier = itemModifier,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                icon = iconContent,
                label = labelContent,
            )

        LayoutType.IconOnly -> {
            IconOnly(
                modifier = itemModifier,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                icon = iconContent,
            )
        }

        LayoutType.LabelOnly -> {
            LabelOnly(
                modifier = itemModifier,
                iconSize = gridItemSettings.iconSize.dp,
                iconPadding = gridItemSettings.iconPadding.dp,
                iconModifier = iconModifier,
                label = labelContent,
            )
        }
    }
}
