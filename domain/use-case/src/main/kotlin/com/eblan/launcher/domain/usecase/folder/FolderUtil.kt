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
package com.eblan.launcher.domain.usecase.folder

import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoWrapper

internal fun FolderEblanApplicationInfoWrapper.asFolderEblanApplicationInfoGridItem(): FolderEblanApplicationInfoGridItem = FolderEblanApplicationInfoGridItem(
    id = folderEblanApplicationInfo.id,
    data = FolderEblanApplicationInfoGridItemData.Folder(
        icon = folderEblanApplicationInfo.icon,
        label = folderEblanApplicationInfo.label,
        folderIndex = folderEblanApplicationInfo.folderIndex,
        folderId = folderEblanApplicationInfo.folderId,
    ),
)

internal fun FolderEblanApplicationInfo.asFolderEblanApplicationInfoGridItem(): FolderEblanApplicationInfoGridItem = FolderEblanApplicationInfoGridItem(
    id = id,
    data = FolderEblanApplicationInfoGridItemData.Folder(
        icon = icon,
        label = label,
        folderIndex = folderIndex,
        folderId = folderId,
    ),
)

internal fun EblanApplicationInfo.asFolderEblanApplicationInfoGridItem(): FolderEblanApplicationInfoGridItem = FolderEblanApplicationInfoGridItem(
    id = "$componentName $serialNumber",
    data = FolderEblanApplicationInfoGridItemData.ApplicationInfo(
        componentName = componentName,
        serialNumber = serialNumber,
        packageName = packageName,
        icon = icon,
        label = label,
        customIcon = customIcon,
        customLabel = customLabel,
        isHidden = isHidden,
        lastUpdateTime = lastUpdateTime,
        flags = flags,
        folderIndex = folderIndex,
        folderId = folderId,
    ),
)
