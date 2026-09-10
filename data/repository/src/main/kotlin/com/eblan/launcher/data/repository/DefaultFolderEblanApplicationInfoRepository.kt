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
package com.eblan.launcher.data.repository

import com.eblan.launcher.data.repository.mapper.asModel
import com.eblan.launcher.data.room.dao.FolderEblanApplicationInfoDao
import com.eblan.launcher.data.room.entity.FolderEblanApplicationInfoEntity
import com.eblan.launcher.data.room.entity.FolderEblanApplicationInfoWrapperEntity
import com.eblan.launcher.domain.model.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.FolderEblanApplicationInfoWrapper
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultFolderEblanApplicationInfoRepository @Inject constructor(private val folderEblanApplicationInfoDao: FolderEblanApplicationInfoDao) :
    FolderEblanApplicationInfoRepository {
    override val folderEblanApplicationInfoWrappersFlow: Flow<List<FolderEblanApplicationInfoWrapper>> =
        folderEblanApplicationInfoDao.getFolderEblanApplicationInfoWrapperEntitiesFlow()
            .map { entities ->
                entities.map { entity ->
                    entity.asModel()
                }
            }


    override fun getFolderEblanApplicationInfos(): List<FolderEblanApplicationInfo> =
        folderEblanApplicationInfoDao.getFolderEblanApplicationInfoEntities().map {
            it.asModel()
        }

    override suspend fun getFolderEblanApplicationInfoWrapper(id: String): FolderEblanApplicationInfoWrapper? =
        folderEblanApplicationInfoDao.getFolderEblanApplicationInfoWrapperEntity(id = id)?.asModel()

    private fun FolderEblanApplicationInfoWrapperEntity.asModel(): FolderEblanApplicationInfoWrapper {
        return FolderEblanApplicationInfoWrapper(
            folderEblanApplicationInfo = folderEblanApplicationInfoEntity.asModel(),
            eblanApplicationInfoEntities = eblanApplicationInfoEntities.map { it.asModel() },
        )
    }

    private fun FolderEblanApplicationInfoEntity.asModel(): FolderEblanApplicationInfo {
        return FolderEblanApplicationInfo(
            id = id,
            folderIndex = folderIndex,
            folderId = folderId,
        )
    }
}