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

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.component.GridItemLayoutType
import com.eblan.launcher.feature.home.component.LauncherIconImage
import com.eblan.launcher.feature.home.component.PreviewFolderGridLayout
import com.eblan.launcher.feature.home.util.getBackgroundColor
import com.eblan.launcher.feature.home.util.getFolderBorderColor
import com.eblan.launcher.feature.home.util.getGridItemBackgroundColor
import com.eblan.launcher.feature.home.util.getGridItemIconPath
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor

@Composable
internal fun FolderGridItem(
    // Item Data
    data: GridItemData.Folder,
    // Settings and Configuration
    gridItemSettings: GridItemSettings,
    iconPackFilePaths: Map<String, String?>,
    hasShortcutHostPermission: Boolean,
    previewFolderGridItems: List<GridItem>?,
    maxLines: Int,
    // Appearance and Colors
    textColor: Color,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    folderBorderColor: BackgroundColor,
    customFolderBorderColor: Int,
    folderBorderWidth: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderCornerRadius: Int,
    // Layout and Arrangement
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    // Modifier
    modifier: Modifier = Modifier,
) {
    val icon = data.icon

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

    val iconContent: @Composable () -> Unit = {
        if (icon != null) {
            LauncherIconImage(
                iconPath = icon,
                modifier = commonModifier,
            )
        } else {
            Surface(
                modifier = commonModifier,
                shape = RoundedCornerShape(folderCornerRadius.dp),
                border = BorderStroke(
                    width = folderBorderWidth.dp,
                    color = getFolderBorderColor(
                        folderBorderColor = folderBorderColor,
                        customFolderBorderColor = customFolderBorderColor,
                    ),
                ),
                color = getBackgroundColor(
                    backgroundColor = folderBackgroundColor,
                    customBackgroundColor = customFolderBackgroundColor,
                    defaultColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                PreviewFolderGridLayout(
                    // Data
                    gridItems = previewFolderGridItems,
                    // Layout
                    modifier = Modifier.fillMaxSize(),
                    // Callbacks
                    slotId = { currentFolderGridItem -> currentFolderGridItem.id },
                    content = { currentFolderGridItem ->
                        PreviewFolderGridItem(
                            // Item and Settings
                            gridItem = currentFolderGridItem,
                            gridItemTextColor = gridItemSettings.textColor,
                            gridItemCustomTextColor = gridItemSettings.customTextColor,
                            // Appearance and Colors
                            iconPackFilePaths = iconPackFilePaths,
                            folderBackgroundColor = folderBackgroundColor,
                            customFolderBackgroundColor = customFolderBackgroundColor,
                            systemTextColor = systemTextColor,
                            systemCustomTextColor = systemCustomTextColor,
                            // State and Layout
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

@Composable
private fun PreviewFolderGridItem(
    // Item and Settings
    gridItem: GridItem,
    gridItemTextColor: TextColor,
    gridItemCustomTextColor: Int,
    // Appearance and Colors
    iconPackFilePaths: Map<String, String?>,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // State and Layout
    hasShortcutHostPermission: Boolean,
    modifier: Modifier = Modifier,
) {
    key(gridItem.id) {
        val (currentGridItemTextColor, currentGridItemCustomTextColor) = if (gridItem.override) {
            gridItem.gridItemSettings.textColor to gridItem.gridItemSettings.customTextColor
        } else {
            gridItemTextColor to gridItemCustomTextColor
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
            // Background Color
            backgroundColor = folderBackgroundColor,
            customBackgroundColor = customFolderBackgroundColor,
            // Text Color
            textColor = currentGridItemTextColor,
            customTextColor = currentGridItemCustomTextColor,
            systemCustomTextColor = systemCustomTextColor,
            systemTextColor = systemTextColor,
            // Default Color
            defaultColor = MaterialTheme.colorScheme.onSurface,
        )

        val commonModifier = modifier
            .padding(1.dp)
            .alpha(alpha)

        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                val iconPath = getGridItemIconPath(
                    data = data,
                    iconPackFilePath = iconPackFilePaths[data.componentName],
                )

                LauncherIconImage(
                    iconPath = iconPath,
                    modifier = commonModifier,
                )
            }

            is GridItemData.ShortcutConfig -> {
                val iconPath = getGridItemIconPath(data = data)

                LauncherIconImage(
                    iconPath = iconPath,
                    modifier = commonModifier,
                )
            }

            is GridItemData.ShortcutInfo -> {
                LauncherIconImage(
                    iconPath = getGridItemIconPath(data = data),
                    modifier = commonModifier,
                )
            }

            is GridItemData.Folder -> {
                val icon = data.icon

                if (icon != null) {
                    LauncherIconImage(
                        iconPath = icon,
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
