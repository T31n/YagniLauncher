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
package com.eblan.launcher.data.repository.impl

import com.eblan.launcher.data.repository.mapper.asEntity
import com.eblan.launcher.data.repository.mapper.asModel
import com.eblan.launcher.data.room.dao.FolderEblanApplicationInfoDao
import com.eblan.launcher.data.room.entity.FolderEblanApplicationInfoWrapperEntity
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoWrapper
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultFolderEblanApplicationInfoRepository @Inject constructor(private val folderEblanApplicationInfoDao: FolderEblanApplicationInfoDao) : FolderEblanApplicationInfoRepository {
    override val folderEblanApplicationInfosFlow: Flow<List<FolderEblanApplicationInfo>> =
        folderEblanApplicationInfoDao.getFolderEblanApplicationInfoEntitiesFlow()
            .map { entities ->
                entities.map { entity ->
                    entity.asModel()
                }
            }

    override val folderEblanApplicationInfoWrappersFlow: Flow<List<FolderEblanApplicationInfoWrapper>> =
        folderEblanApplicationInfoDao.getFolderEblanApplicationInfoWrapperEntitiesFlow()
            .map { entities ->
                entities.map { entity ->
                    entity.asModel()
                }
            }

    override suspend fun getFolderEblanApplicationInfos(): List<FolderEblanApplicationInfo> = folderEblanApplicationInfoDao.getFolderEblanApplicationInfoEntities().map {
        it.asModel()
    }

    override suspend fun upsertFolderEblanApplicationInfo(folderEblanApplicationInfo: FolderEblanApplicationInfo) {
        folderEblanApplicationInfoDao.upsertFolderEblanApplicationInfoEntity(entity = folderEblanApplicationInfo.asEntity())
    }

    override suspend fun getFolderEblanApplicationInfoById(id: String): FolderEblanApplicationInfo? = folderEblanApplicationInfoDao.getFolderEblanApplicationInfoEntityById(id = id)?.asModel()

    override suspend fun insertFolderEblanApplicationInfo(folderEblanApplicationInfo: FolderEblanApplicationInfo) {
        folderEblanApplicationInfoDao.insertFolderEblanApplicationInfoEntity(entity = folderEblanApplicationInfo.asEntity())
    }

    override suspend fun updateFolderEblanApplicationInfo(folderEblanApplicationInfo: FolderEblanApplicationInfo) {
        folderEblanApplicationInfoDao.updateFolderEblanApplicationInfoEntity(entity = folderEblanApplicationInfo.asEntity())
    }

    override suspend fun updateFolderEblanApplicationInfos(folderEblanApplicationInfos: List<FolderEblanApplicationInfo>) {
        folderEblanApplicationInfoDao.updateFolderEblanApplicationInfoEntities(entities = folderEblanApplicationInfos.map { it.asEntity() })
    }

    override suspend fun getFolderEblanApplicationInfoWrappers(): List<FolderEblanApplicationInfoWrapper> = folderEblanApplicationInfoDao.getFolderEblanApplicationInfoWrapperEntities().map {
        it.asModel()
    }

    override suspend fun deleteFolderEblanApplicationInfoById(id: String) {
        folderEblanApplicationInfoDao.deleteFolderEblanApplicationInfoEntityById(id = id)
    }

    private fun FolderEblanApplicationInfoWrapperEntity.asModel(): FolderEblanApplicationInfoWrapper = FolderEblanApplicationInfoWrapper(
        folderEblanApplicationInfo = folderEblanApplicationInfoEntity.asModel(),
        eblanApplicationInfos = eblanApplicationInfoEntities.map { it.asModel() },
        folderEblanApplicationInfos = folderEblanApplicationInfoEntities.map { it.asModel() },
    )
}
