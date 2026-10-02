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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
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
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.folder.PreviewFolder
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.LayoutType
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.IconOnly
import com.eblan.launcher.feature.home.component.LabelOnly
import com.eblan.launcher.feature.home.component.PreviewFolderGridLayout
import com.eblan.launcher.feature.home.component.StartIconEndLabel
import com.eblan.launcher.feature.home.component.StartLabelEndIcon
import com.eblan.launcher.feature.home.component.TopIconBottomLabel
import com.eblan.launcher.feature.home.component.TopLabelBottomIcon
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun FolderGridItem(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    data: GridItemData.Folder,
    gridItemSettings: GridItemSettings,
    textColor: Color,
    previewFolderGridItems: Map<String, PreviewFolder>,
    hasShortcutHostPermission: Boolean,
    iconPackInfoFilePaths: Map<String, String?>,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    maxLines: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderCornerRadius: Int,
) {
    val context = LocalContext.current

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
        if (data.icon != null) {
            AsyncImage(
                model = Builder(context)
                    .data(data.icon)
                    .addLastModifiedToFileCacheKey(true)
                    .size(Size.ORIGINAL)
                    .build(),
                contentDescription = null,
                modifier = iconModifier,
            )
        } else {
            Surface(
                modifier = iconModifier,
                shape = RoundedCornerShape(folderCornerRadius.dp),
                color = when (folderBackgroundColor) {
                    BackgroundColor.System -> MaterialTheme.colorScheme.surface
                    BackgroundColor.Light -> Color.White
                    BackgroundColor.Dark -> Color.Black
                    BackgroundColor.Custom -> Color(customFolderBackgroundColor)
                },
            ) {
                PreviewFolderGridLayout(
                    modifier = Modifier.fillMaxSize(),
                    gridItems = previewFolderGridItems[gridItem.id]?.previewFolderGridItems,
                    slotId = { it.id },
                    content = {
                        PreviewFolderGridItem(
                            gridItem = it,
                            hasShortcutHostPermission = hasShortcutHostPermission,
                            iconPackInfoFilePaths = iconPackInfoFilePaths,
                            gridItemSettings = gridItemSettings,
                            folderBackgroundColor = folderBackgroundColor,
                            customFolderBackgroundColor = customFolderBackgroundColor,
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                        )
                    },
                )
            }
        }
    }

    val labelContent: @Composable (Modifier) -> Unit = {
        Text(
            modifier = Modifier.padding(gridItemSettings.textPadding.dp),
            text = data.label,
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

@Composable
private fun PreviewFolderGridItem(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    hasShortcutHostPermission: Boolean,
    iconPackInfoFilePaths: Map<String, String?>,
    gridItemSettings: GridItemSettings,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
) {
    val context = LocalContext.current

    key(gridItem.id) {
        val currentGridItemSettings = if (gridItem.override) {
            gridItem.gridItemSettings
        } else {
            gridItemSettings
        }

        val alpha = when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo,
            is GridItemData.Folder,
            is GridItemData.ShortcutConfig,
            is GridItemData.Widget,
            -> 1f

            is GridItemData.ShortcutInfo -> {
                if (hasShortcutHostPermission && data.isEnabled) 1f else 0.3f
            }
        }

        val folderIconTint = getTextColorFromBackgroundColor(
            backgroundColor = folderBackgroundColor,
            customBackgroundColor = customFolderBackgroundColor,
            textColor = currentGridItemSettings.textColor,
            customTextColor = currentGridItemSettings.customTextColor,
            systemTextColor = systemTextColor,
            systemCustomTextColor = systemCustomTextColor,
            defaultColor = MaterialTheme.colorScheme.onSurface,
        )

        val commonModifier = modifier
            .padding(1.dp)
            .alpha(alpha)

        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                val icon = iconPackInfoFilePaths[data.componentName] ?: data.icon

                AsyncImage(
                    model = Builder(context)
                        .data(data.customIcon ?: icon)
                        .addLastModifiedToFileCacheKey(true).build(),
                    contentDescription = null,
                    modifier = commonModifier,
                )
            }

            is GridItemData.ShortcutConfig -> {
                val icon = when {
                    data.customIcon != null -> data.customIcon
                    data.shortcutIntentIcon != null -> data.shortcutIntentIcon
                    data.activityIcon != null -> data.activityIcon
                    else -> data.applicationIcon
                }

                AsyncImage(
                    model = Builder(context)
                        .data(icon)
                        .addLastModifiedToFileCacheKey(true).build(),
                    contentDescription = null,
                    modifier = commonModifier,
                )
            }

            is GridItemData.ShortcutInfo -> {
                AsyncImage(
                    model = Builder(context)
                        .data(data.customIcon ?: data.icon)
                        .addLastModifiedToFileCacheKey(true).build(),
                    contentDescription = null,
                    modifier = commonModifier,
                )
            }

            is GridItemData.Folder -> {
                if (data.icon != null) {
                    AsyncImage(
                        model = Builder(context)
                            .data(data.icon)
                            .addLastModifiedToFileCacheKey(true)
                            .size(Size.ORIGINAL)
                            .build(),
                        contentDescription = null,
                        modifier = commonModifier,
                    )
                } else {
                    Icon(
                        imageVector = EblanLauncherIcons.Folder,
                        contentDescription = null,
                        tint = folderIconTint,
                        modifier = commonModifier,
                    )
                }
            }

            else -> Unit
        }
    }
}
