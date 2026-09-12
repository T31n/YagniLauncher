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
package com.eblan.launcher.feature.home.util

import android.graphics.RectF
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.eblan.launcher.domain.usecase.util.FOLDER_PREVIEW_COLUMNS
import com.eblan.launcher.domain.usecase.util.FOLDER_PREVIEW_ROWS
import com.eblan.launcher.feature.home.model.FolderPopupLayoutInfo

internal fun getAnimatedRect(
    progress: Float,
    startWidth: Float,
    startHeight: Float,
    endWidth: Float,
    endHeight: Float,
    startCenterX: Float,
    startCenterY: Float,
    endCenterX: Float,
    endCenterY: Float,
): RectF {
    val width = lerp(
        startWidth,
        endWidth,
        progress,
    )

    val height = lerp(
        startHeight,
        endHeight,
        progress,
    )

    val left = lerp(
        startCenterX,
        endCenterX,
        progress,
    ) - width / 2f

    val top = lerp(
        startCenterY,
        endCenterY,
        progress,
    ) - height / 2f

    return RectF(
        left,
        top,
        left + width,
        top + height,
    )
}

internal fun getFolderPopupLayoutInfo(
    density: Density,
    layoutDirection: LayoutDirection,
    paddingValues: PaddingValues,
    safeDrawingWidth: Int,
    safeDrawingHeight: Int,
    folderPopupIntOffset: IntOffset,
    folderPopupIntSize: IntSize,
    folderCellWidth: Int,
    folderCellHeight: Int,
    columns: Int,
    rows: Int,
): FolderPopupLayoutInfo {
    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val minCellWidthPx = with(density) { folderCellWidth.dp.roundToPx() }
    val minCellHeightPx = with(density) { folderCellHeight.dp.roundToPx() }

    val availableWidth = (safeDrawingWidth - leftPadding * 2).coerceAtLeast(0)
    val availableHeight = (safeDrawingHeight - topPadding * 2).coerceAtLeast(0)

    val folderTitleHeightPx = with(density) {
        PAGE_INDICATOR_HEIGHT.roundToPx()
    }

    val folderGridWidthPx =
        (minCellWidthPx * columns).coerceAtMost(availableWidth)

    val folderGridHeightPx = (minCellHeightPx * rows).coerceAtMost(
        (availableHeight - folderTitleHeightPx).coerceAtLeast(0),
    )

    val endHeight = folderGridHeightPx + folderTitleHeightPx

    val maximumX = (safeDrawingWidth - folderGridWidthPx + leftPadding).coerceAtLeast(leftPadding)
    val maximumY = (safeDrawingHeight - endHeight + topPadding).coerceAtLeast(topPadding)

    val endIntOffset = IntOffset(
        x = folderPopupIntOffset.x.coerceIn(leftPadding, maximumX),
        y = folderPopupIntOffset.y.coerceIn(topPadding, maximumY),
    )

    val startWidth = folderPopupIntSize.width.toFloat()
    val startHeight = folderPopupIntSize.height.toFloat()

    val startCenterX = folderPopupIntOffset.x + startWidth / 2f
    val startCenterY = folderPopupIntOffset.y + startHeight / 2f

    val endCenterX = endIntOffset.x + folderGridWidthPx.toFloat() / 2f
    val endCenterY = endIntOffset.y + endHeight.toFloat() / 2f

    val previewCellSize = minOf(
        folderPopupIntSize.width,
        folderPopupIntSize.height,
    ) / maxOf(FOLDER_PREVIEW_COLUMNS, FOLDER_PREVIEW_ROWS)

    val startPreviewWidth = minOf(
        (previewCellSize * columns).toFloat(),
        availableWidth.toFloat(),
    )

    val startPreviewHeight = minOf(
        (previewCellSize * rows).toFloat(),
        (availableHeight - folderTitleHeightPx).coerceAtLeast(0).toFloat(),
    )

    return FolderPopupLayoutInfo(
        minCellWidthPx = minCellWidthPx,
        minCellHeightPx = minCellHeightPx,
        folderGridWidthPx = folderGridWidthPx,
        folderGridHeightPx = folderGridHeightPx,
        endHeight = endHeight,
        startWidth = startWidth,
        startHeight = startHeight,
        startCenterX = startCenterX,
        startCenterY = startCenterY,
        endCenterX = endCenterX,
        endCenterY = endCenterY,
        startPreviewWidth = startPreviewWidth,
        startPreviewHeight = startPreviewHeight,
    )
}
