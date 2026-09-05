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
package com.eblan.launcher.feature.home.screen.editpage

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
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
import com.eblan.launcher.domain.model.BackgroundColor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.PreviewFolder
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.PreviewFolderGridLayout
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun GridItemContent(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    textColor: TextColor,
    previewFolderGridItems: Map<String, PreviewFolder>,
    iconPackInfoFilePaths: Map<String, String?>,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderCornerRadius: Int,
) {
    val currentGridItemSettings = if (gridItem.override) {
        gridItem.gridItemSettings
    } else {
        gridItemSettings
    }

    val currentTextColor = getGridItemTextColor(
        gridItemCustomTextColor = currentGridItemSettings.customTextColor,
        gridItemTextColor = currentGridItemSettings.textColor,
        systemCustomTextColor = gridItemSettings.customTextColor,
        systemTextColor = textColor,
    )

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = currentGridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = currentGridItemSettings.verticalArrangement)

    val maxLines = if (currentGridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo ->
            ApplicationInfoGridItem(
                modifier = modifier,
                data = data,
                gridItemSettings = currentGridItemSettings,
                textColor = currentTextColor,
                iconPackInfoFilePaths = iconPackInfoFilePaths,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
            )

        is GridItemData.Widget -> WidgetGridItem(
            modifier = modifier,
            data = data,
        )

        is GridItemData.ShortcutInfo ->
            ShortcutInfoGridItem(
                modifier = modifier,
                data = data,
                gridItemSettings = currentGridItemSettings,
                hasShortcutHostPermission = hasShortcutHostPermission,
                textColor = currentTextColor,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
            )

        is GridItemData.Folder ->
            FolderGridItem(
                modifier = modifier,
                gridItem = gridItem,
                data = data,
                gridItemSettings = currentGridItemSettings,
                textColor = currentTextColor,
                previewFolderGridItems = previewFolderGridItems,
                hasShortcutHostPermission = hasShortcutHostPermission,
                iconPackInfoFilePaths = iconPackInfoFilePaths,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
                folderBackgroundColor = folderBackgroundColor,
                customFolderBackgroundColor = customFolderBackgroundColor,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                folderCornerRadius = folderCornerRadius,
            )

        is GridItemData.ShortcutConfig ->
            ShortcutConfigGridItem(
                modifier = modifier,
                data = data,
                gridItemSettings = currentGridItemSettings,
                textColor = currentTextColor,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                maxLines = maxLines,
            )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ApplicationInfo,
    gridItemSettings: GridItemSettings,
    textColor: Color,
    iconPackInfoFilePaths: Map<String, String?>,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
) {
    val icon = iconPackInfoFilePaths[data.componentName] ?: data.icon

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(gridItemSettings.padding.dp)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(modifier = Modifier.size(gridItemSettings.iconSize.dp)) {
            AsyncImage(
                model = Builder(LocalContext.current).data(data.customIcon ?: icon)
                    .addLastModifiedToFileCacheKey(true).build(),
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

        if (gridItemSettings.showLabel) {
            Text(
                text = data.customLabel ?: data.label,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                fontSize = gridItemSettings.textSize.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ShortcutInfo,
    gridItemSettings: GridItemSettings,
    hasShortcutHostPermission: Boolean,
    textColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
) {
    val customIcon = data.customIcon ?: data.icon

    val customShortLabel = data.customShortLabel ?: data.shortLabel

    val alpha = if (hasShortcutHostPermission && data.isEnabled) 1f else 0.3f

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(gridItemSettings.padding.dp)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(
            modifier = Modifier
                .size(gridItemSettings.iconSize.dp)
                .alpha(alpha),
        ) {
            AsyncImage(
                model = customIcon,
                modifier = Modifier
                    .matchParentSize(),
                contentDescription = null,
            )

            AsyncImage(
                model = data.eblanApplicationInfoIcon,
                modifier = Modifier
                    .size((gridItemSettings.iconSize * 0.25).dp)
                    .align(Alignment.BottomEnd),
                contentDescription = null,
            )
        }

        if (gridItemSettings.showLabel) {
            Text(
                modifier = Modifier.alpha(alpha),
                text = customShortLabel,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                fontSize = gridItemSettings.textSize.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun FolderGridItem(
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
    maxLines: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderCornerRadius: Int,
) {
    val commonModifier = Modifier.size(gridItemSettings.iconSize.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(gridItemSettings.padding.dp)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        if (data.icon != null) {
            AsyncImage(
                model = data.icon,
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
                    modifier = Modifier.fillMaxSize(),
                    gridItems = previewFolderGridItems[gridItem.id]?.previewFolderGridItems,
                    content = {
                        PreviewFolderGridItemContent(
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

        if (gridItemSettings.showLabel) {
            Text(
                text = data.label,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                fontSize = gridItemSettings.textSize.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun WidgetGridItem(modifier: Modifier = Modifier, data: GridItemData.Widget) {
    AsyncImage(
        model = data.preview ?: data.icon,
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ShortcutConfigGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ShortcutConfig,
    gridItemSettings: GridItemSettings,
    textColor: Color,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    maxLines: Int,
) {
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(gridItemSettings.padding.dp)
            .background(
                color = Color(gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
            ),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        Box(modifier = Modifier.size(gridItemSettings.iconSize.dp)) {
            AsyncImage(
                model = Builder(LocalContext.current).data(icon)
                    .addLastModifiedToFileCacheKey(true).build(),
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

        if (gridItemSettings.showLabel) {
            Text(
                text = label.toString(),
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                fontSize = gridItemSettings.textSize.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PreviewFolderGridItemContent(
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
