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
internal fun ShortcutConfigGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ShortcutConfig,
    gridItemSettings: GridItemSettings,
    textColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    maxLines: Int,
) {
    val context = LocalContext.current

    val icon = when {
        data.customIcon != null -> data.customIcon
        data.shortcutIntentIcon != null -> data.shortcutIntentIcon
        data.activityIcon != null -> data.activityIcon
        else -> data.applicationIcon
    }

    val label = when {
        data.customLabel != null -> data.customLabel
        data.shortcutIntentName != null -> data.shortcutIntentName
        data.activityLabel != null -> data.activityLabel
        else -> data.applicationLabel
    }

    val itemModifier = modifier
        .fillMaxSize()
        .padding(gridItemSettings.padding.dp)
        .background(
            color = Color(gridItemSettings.customBackgroundColor),
            shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
        )

    val iconContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .size(gridItemSettings.iconSize.dp)
                .padding(gridItemSettings.iconPadding.dp),
        ) {
            AsyncImage(
                model = Builder(context)
                    .data(icon)
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

    val labelContent: @Composable () -> Unit = {
        if (gridItemSettings.showLabel) {
            Text(
                modifier = Modifier.padding(gridItemSettings.textPadding.dp),
                text = label.toString(),
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                fontSize = gridItemSettings.textSize.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
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
    }
}

@Composable
private fun TopIconBottomLabel(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        icon()
        label()
    }
}

@Composable
private fun TopLabelBottomIcon(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        label()
        icon()
    }
}

@Composable
private fun StartIconEndLabel(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        icon()
        label()
    }
}

@Composable
private fun StartLabelEndIcon(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        label()
        icon()
    }
}
