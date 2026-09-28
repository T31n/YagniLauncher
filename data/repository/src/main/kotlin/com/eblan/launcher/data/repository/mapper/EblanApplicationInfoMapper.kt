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
package com.eblan.launcher.data.repository.mapper

import com.eblan.launcher.data.room.entity.EblanApplicationInfoEntity
import com.eblan.launcher.domain.model.application.EblanApplicationInfo

fun EblanApplicationInfo.asEntity(): EblanApplicationInfoEntity = EblanApplicationInfoEntity(
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
)

fun EblanApplicationInfoEntity.asModel(): EblanApplicationInfo = EblanApplicationInfo(
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
)
