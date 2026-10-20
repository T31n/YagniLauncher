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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import com.eblan.launcher.domain.model.folder.FolderPopup
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getHorizontalArrangement
import com.eblan.launcher.feature.home.util.getVerticalAlignment
import com.eblan.launcher.feature.home.util.getVerticalArrangement

@Composable
internal fun PageGridItem(
    // Grid Item
    gridItem: GridItem,
    gridItemSettings: GridItemSettings,
    folderPopupGridItems: Map<String, FolderPopup>,
    // Permissions
    hasShortcutHostPermission: Boolean,
    // Styling
    textColor: TextColor,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    folderBackgroundColor: BackgroundColor,
    customFolderBackgroundColor: Int,
    folderCornerRadius: Int,
    // Assets
    iconPackFilePaths: Map<String, String?>,
    // Modifier
    modifier: Modifier = Modifier
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
        systemTextColor = textColor
    )

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = currentGridItemSettings.horizontalAlignment)
    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = currentGridItemSettings.verticalArrangement)
    val horizontalArrangement =
        getHorizontalArrangement(horizontalArrangement = currentGridItemSettings.horizontalArrangement)
    val verticalAlignment =
        getVerticalAlignment(verticalAlignment = currentGridItemSettings.verticalAlignment)

    val maxLines = if (currentGridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo ->
            ApplicationInfoGridItem(
                data = data,
                gridItemSettings = currentGridItemSettings,
                textColor = currentTextColor,
                iconPackFilePaths = iconPackFilePaths,
                maxLines = maxLines,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                modifier = modifier
            )

        is GridItemData.Widget -> WidgetGridItem(
            modifier = modifier,
            data = data,
        )

        is GridItemData.ShortcutInfo ->
            ShortcutInfoGridItem(
                data = data,
                gridItemSettings = currentGridItemSettings,
                hasShortcutHostPermission = hasShortcutHostPermission,
                textColor = currentTextColor,
                maxLines = maxLines,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                modifier = modifier
            )

        is GridItemData.Folder ->
            FolderGridItem(
                gridItem = gridItem,
                data = data,
                gridItemSettings = currentGridItemSettings,
                textColor = currentTextColor,
                folderBackgroundColor = folderBackgroundColor,
                customFolderBackgroundColor = customFolderBackgroundColor,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                folderCornerRadius = folderCornerRadius,
                iconPackFilePaths = iconPackFilePaths,
                hasShortcutHostPermission = hasShortcutHostPermission,
                folderPopupGridItems = folderPopupGridItems,
                maxLines = maxLines,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                modifier = modifier
            )

        is GridItemData.ShortcutConfig ->
            ShortcutConfigGridItem(
                data = data,
                gridItemSettings = currentGridItemSettings,
                textColor = currentTextColor,
                maxLines = maxLines,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                modifier = modifier
            )
    }
}

@Composable
private fun WidgetGridItem(modifier: Modifier = Modifier, data: GridItemData.Widget) {
    AsyncImage(
        model = data.preview ?: data.icon,
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
    )
}
