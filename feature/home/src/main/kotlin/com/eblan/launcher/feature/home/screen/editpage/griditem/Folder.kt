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
import com.eblan.launcher.domain.model.folder.FolderPopup
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.GridItemLayoutType
import com.eblan.launcher.feature.home.component.PreviewFolderGridLayout
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor
import java.io.File

@Composable
internal fun FolderGridItem(
    // Item and Data
    gridItem: GridItem,
    data: GridItemData.Folder,
    gridItemSettings: GridItemSettings,
    // Appearance and Styling
    textColor: Color,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderCornerRadius: Int,
    iconPackFilePaths: Map<String, String?>,
    hasShortcutHostPermission: Boolean,
    folderPopupGridItems: Map<String, FolderPopup>,
    maxLines: Int,
    // Layout
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val icon = data.icon

    val itemModifier = modifier
        .fillMaxSize()
        .padding(gridItemSettings.padding.dp)
        .background(
            color = Color(gridItemSettings.customBackgroundColor),
            shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
        )

    val commonModifier = Modifier
        .size(gridItemSettings.iconSize.dp)
        .padding(gridItemSettings.iconPadding.dp)

    val iconContent: @Composable () -> Unit = {
        if (icon != null) {
            AsyncImage(
                model = Builder(context)
                    .data(icon)
                    .addLastModifiedToFileCacheKey(true)
                    .size(Size.ORIGINAL)
                    .build(),
                contentDescription = null,
                modifier = commonModifier,
            )
        } else {
            Surface(
                modifier = commonModifier,
                shape = RoundedCornerShape(folderCornerRadius.dp),
                color = when (folderBackgroundColor) {
                    BackgroundColor.System -> MaterialTheme.colorScheme.surface
                    BackgroundColor.Light -> Color.White
                    BackgroundColor.Dark -> Color.Black
                    BackgroundColor.Custom -> Color(customFolderBackgroundColor)
                },
            ) {
                PreviewFolderGridLayout(
                    gridItems = folderPopupGridItems[gridItem.id]?.previewFolderGridItems,
                    modifier = Modifier.fillMaxSize(),
                    slotId = { it.id },
                    content = {
                        PreviewFolderGridItem(
                            gridItem = it,
                            iconPackFilePaths = iconPackFilePaths,
                            gridItemSettings = gridItemSettings,
                            folderBackgroundColor = folderBackgroundColor,
                            customFolderBackgroundColor = customFolderBackgroundColor,
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                            hasShortcutHostPermission = hasShortcutHostPermission,
                        )
                    },
                )
            }
        }
    }

    val labelContent: @Composable () -> Unit = {
        Text(
            modifier = commonModifier,
            text = data.label,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            fontSize = gridItemSettings.textSize.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }

    GridItemLayoutType(
        gridItemSettings = gridItemSettings,
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        modifier = itemModifier,
        iconContent = iconContent,
        labelContent = labelContent,
        labelOnlyContent = labelContent,
    )
}

@Composable
private fun PreviewFolderGridItem(
    // Content
    gridItem: GridItem,
    iconPackFilePaths: Map<String, String?>,
    // Appearance
    gridItemSettings: GridItemSettings,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // State
    hasShortcutHostPermission: Boolean,
    // Layout
    modifier: Modifier = Modifier,
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
            systemCustomTextColor = systemCustomTextColor,
            systemTextColor = systemTextColor,
            defaultColor = MaterialTheme.colorScheme.onSurface,
        )

        val commonModifier = modifier
            .padding(1.dp)
            .alpha(alpha)

        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                val iconPackFilePath = iconPackFilePaths[data.componentName] ?: data.icon
                val icon = data.icon
                val customIcon = data.customIcon

                val iconPath = when {
                    iconPackFilePath != null -> iconPackFilePath
                    customIcon != null -> customIcon
                    icon != null -> icon
                    else -> null
                }

                AsyncImage(
                    model = Builder(context)
                        .data(iconPath)
                        .addLastModifiedToFileCacheKey(true).build(),
                    contentDescription = null,
                    modifier = commonModifier,
                )
            }

            is GridItemData.ShortcutConfig -> {
                val customIcon = data.customIcon
                val shortcutIntentIcon = data.shortcutIntentIcon
                val activityIcon = data.activityIcon
                val applicationIcon = data.applicationIcon

                val iconPath = when {
                    customIcon != null -> customIcon
                    shortcutIntentIcon != null -> shortcutIntentIcon
                    activityIcon != null -> activityIcon
                    applicationIcon != null -> File(applicationIcon)
                    else -> null
                }

                AsyncImage(
                    model = Builder(context)
                        .data(iconPath)
                        .addLastModifiedToFileCacheKey(true).build(),
                    contentDescription = null,
                    modifier = commonModifier,
                )
            }

            is GridItemData.ShortcutInfo -> {
                val icon = data.icon
                val customIcon = data.customIcon

                AsyncImage(
                    model = Builder(context)
                        .data(customIcon ?: icon)
                        .addLastModifiedToFileCacheKey(true).build(),
                    contentDescription = null,
                    modifier = commonModifier,
                )
            }

            is GridItemData.Folder -> {
                val icon = data.icon

                if (icon != null) {
                    AsyncImage(
                        model = Builder(context)
                            .data(icon)
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
