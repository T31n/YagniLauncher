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
package com.eblan.launcher.feature.home.screen.application.folder

import com.eblan.launcher.feature.home.model.Drag

internal fun handleDropFolderEblanApplicationInfoGridItem(
    drag: Drag,
    isDragging: Boolean,
    lockMovement: Boolean,
    isVisibleOverlay: Boolean,
    isLastFolderEblanApplicationInfo: Boolean,
    onResetGrid: () -> Unit,
    onDragEndAfterMoveFolderEblanApplicationInfo: () -> Unit,
    onUpdateIsDragging: (Boolean) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
) {
    if (drag == Drag.None ||
        drag == Drag.Start ||
        drag == Drag.Dragging ||
        !isLastFolderEblanApplicationInfo
    ) {
        return
    }

    if (isVisibleOverlay && !isDragging) {
        onUpdateIsVisibleOverlay(false)

        return
    }

    if (lockMovement) {
        onUpdateIsVisibleOverlay(false)

        onUpdateIsDragging(false)

        onResetGrid()

        return
    }

    if (isVisibleOverlay) {
        onDragEndAfterMoveFolderEblanApplicationInfo()

        onUpdateIsDragging(false)
    }
}
