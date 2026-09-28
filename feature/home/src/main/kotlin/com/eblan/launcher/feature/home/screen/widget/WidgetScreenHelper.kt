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
package com.eblan.launcher.feature.home.screen.widget

import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType

internal fun getWidgetGridItem(
    componentName: String,
    configure: String?,
    gridItemSettings: GridItemSettings,
    icon: String?,
    id: String,
    label: String?,
    maxResizeHeight: Int,
    maxResizeWidth: Int,
    minHeight: Int,
    minResizeHeight: Int,
    minResizeWidth: Int,
    minWidth: Int,
    packageName: String,
    page: Int,
    preview: String?,
    resizeMode: Int,
    serialNumber: Long,
    targetCellHeight: Int,
    targetCellWidth: Int,
): GridItem {
    val data = GridItemData.Widget(
        appWidgetId = 0,
        componentName = componentName,
        packageName = packageName,
        serialNumber = serialNumber,
        configure = configure,
        minWidth = minWidth,
        minHeight = minHeight,
        resizeMode = resizeMode,
        minResizeWidth = minResizeWidth,
        minResizeHeight = minResizeHeight,
        maxResizeWidth = maxResizeWidth,
        maxResizeHeight = maxResizeHeight,
        targetCellHeight = targetCellHeight,
        targetCellWidth = targetCellWidth,
        preview = preview,
        label = label,
        icon = icon,
    )

    val eblanAction = EblanAction(
        eblanActionType = EblanActionType.None,
        serialNumber = 0L,
        componentName = "",
    )

    return GridItem(
        id = id,
        page = page,
        startColumn = -1,
        startRow = -1,
        columnSpan = 1,
        rowSpan = 1,
        data = data,
        associate = Associate.Grid,
        override = false,
        gridItemSettings = gridItemSettings,
        doubleTap = eblanAction,
        swipeUp = eblanAction,
        swipeDown = eblanAction,
    )
}
