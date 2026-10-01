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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.LayoutType

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ApplicationInfo,
    gridItemSettings: GridItemSettings,
    textColor: Color,
    iconPackInfoFilePaths: Map<String, String?>,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    maxLines: Int,
) {
    val icon = iconPackInfoFilePaths[data.componentName] ?: data.icon

    val itemModifier = modifier
        .fillMaxSize()
        .padding(gridItemSettings.padding.dp)
        .background(
            color = Color(gridItemSettings.customBackgroundColor),
            shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
        )

    when (gridItemSettings.layoutType) {
        LayoutType.TopIconBottomLabel ->
            TopIconBottomLabel(
                modifier = itemModifier,
                data = data,
                icon = icon,
                gridItemSettings = gridItemSettings,
                textColor = textColor,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
            )

        LayoutType.TopLabelBottomIcon ->
            TopLabelBottomIcon(
                modifier = itemModifier,
                data = data,
                icon = icon,
                gridItemSettings = gridItemSettings,
                textColor = textColor,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
            )

        LayoutType.StartIconEndLabel ->
            StartIconEndLabel(
                modifier = itemModifier,
                data = data,
                icon = icon,
                gridItemSettings = gridItemSettings,
                textColor = textColor,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                maxLines = maxLines,
            )

        LayoutType.StartLabelEndIcon ->
            StartLabelEndIcon(
                modifier = itemModifier,
                data = data,
                icon = icon,
                gridItemSettings = gridItemSettings,
                textColor = textColor,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                maxLines = maxLines,
            )
    }
}

@Composable
private fun TopIconBottomLabel(
    modifier: Modifier,
    data: GridItemData.ApplicationInfo,
    icon: String?,
    gridItemSettings: GridItemSettings,
    textColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        ApplicationInfoIcon(
            data = data,
            icon = icon,
            gridItemSettings = gridItemSettings,
        )

        ApplicationInfoLabel(
            data = data,
            gridItemSettings = gridItemSettings,
            textColor = textColor,
            maxLines = maxLines,
        )
    }
}

@Composable
private fun TopLabelBottomIcon(
    modifier: Modifier,
    data: GridItemData.ApplicationInfo,
    icon: String?,
    gridItemSettings: GridItemSettings,
    textColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        ApplicationInfoLabel(
            data = data,
            gridItemSettings = gridItemSettings,
            textColor = textColor,
            maxLines = maxLines,
        )

        ApplicationInfoIcon(
            data = data,
            icon = icon,
            gridItemSettings = gridItemSettings,
        )
    }
}

@Composable
private fun StartIconEndLabel(
    modifier: Modifier,
    data: GridItemData.ApplicationInfo,
    icon: String?,
    gridItemSettings: GridItemSettings,
    textColor: Color,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    maxLines: Int,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        ApplicationInfoIcon(
            data = data,
            icon = icon,
            gridItemSettings = gridItemSettings,
        )

        ApplicationInfoLabel(
            data = data,
            gridItemSettings = gridItemSettings,
            textColor = textColor,
            maxLines = maxLines,
        )
    }
}

@Composable
private fun StartLabelEndIcon(
    modifier: Modifier,
    data: GridItemData.ApplicationInfo,
    icon: String?,
    gridItemSettings: GridItemSettings,
    textColor: Color,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    maxLines: Int,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        ApplicationInfoLabel(
            data = data,
            gridItemSettings = gridItemSettings,
            textColor = textColor,
            maxLines = maxLines,
        )

        ApplicationInfoIcon(
            data = data,
            icon = icon,
            gridItemSettings = gridItemSettings,
        )
    }
}

@Composable
private fun ApplicationInfoIcon(
    modifier: Modifier = Modifier,
    data: GridItemData.ApplicationInfo,
    icon: String?,
    gridItemSettings: GridItemSettings,
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .size(gridItemSettings.iconSize.dp)
            .padding(gridItemSettings.iconPadding.dp),
    ) {
        AsyncImage(
            model = Builder(context)
                .data(data.customIcon ?: icon)
                .addLastModifiedToFileCacheKey(true)
                .size(Size.ORIGINAL)
                .build(),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
        )

        if (data.serialNumber != 0L) {
            ElevatedCard(
                modifier = Modifier
                    .size((gridItemSettings.iconSize * 0.4).dp)
                    .align(Alignment.BottomEnd),
            ) {
                Icon(
                    imageVector = EblanLauncherIcons.Work,
                    contentDescription = null,
                    modifier = Modifier.padding(2.dp),
                )
            }
        }
    }
}

@Composable
private fun ApplicationInfoLabel(
    data: GridItemData.ApplicationInfo,
    gridItemSettings: GridItemSettings,
    textColor: Color,
    maxLines: Int,
) {
    if (gridItemSettings.showLabel) {
        Text(
            modifier = Modifier.padding(gridItemSettings.textPadding.dp),
            text = data.customLabel ?: data.label,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            fontSize = gridItemSettings.textSize.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
