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
package com.eblan.launcher.data.room.transaction

import androidx.room.withTransaction
import com.eblan.launcher.data.room.EblanDatabase
import com.eblan.launcher.data.room.dao.EblanApplicationInfoDao
import com.eblan.launcher.data.room.dao.FolderEblanApplicationInfoDao
import com.eblan.launcher.data.room.entity.EblanApplicationInfoEntity
import com.eblan.launcher.data.room.entity.FolderEblanApplicationInfoEntity
import com.eblan.launcher.data.room.model.FolderGridItemEntities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultFolderGridItemEntityTransaction @Inject constructor(
    private val eblanDatabase: EblanDatabase,
    private val eblanApplicationInfoDao: EblanApplicationInfoDao,
    private val folderEblanApplicationInfoDao: FolderEblanApplicationInfoDao,
) : FolderGridItemEntityTransaction {
    override val folderGridItemEntitiesFlow: Flow<FolderGridItemEntities> =
        eblanDatabase.invalidationTracker.createFlow(
            "EblanApplicationInfoEntity",
            "FolderEblanApplicationInfoEntity",
        ).conflate()
            .map {
                eblanDatabase.withTransaction {
                    FolderGridItemEntities(
                        eblanApplicationInfoEntities = eblanApplicationInfoDao.getEblanApplicationInfoEntities(),
                        folderEblanApplicationInfoEntities = folderEblanApplicationInfoDao.getFolderEblanApplicationInfoEntities(),
                    )
                }
            }
            .distinctUntilChanged()

    override suspend fun getFolderGridItemEntities(): FolderGridItemEntities = FolderGridItemEntities(
        eblanApplicationInfoEntities = eblanApplicationInfoDao.getEblanApplicationInfoEntities(),
        folderEblanApplicationInfoEntities = folderEblanApplicationInfoDao.getFolderEblanApplicationInfoEntities(),
    )

    override suspend fun upsertFolderGridItemEntitiesTransaction(
        eblanApplicationInfoEntities: List<EblanApplicationInfoEntity>,
        folderEblanApplicationInfoEntities: List<FolderEblanApplicationInfoEntity>,
    ) {
        eblanDatabase.withTransaction {
            eblanApplicationInfoDao.upsertEblanApplicationInfoEntities(entities = eblanApplicationInfoEntities)

            folderEblanApplicationInfoDao.upsertFolderEblanApplicationInfoEntities(entities = folderEblanApplicationInfoEntities)
        }
    }

    override suspend fun deleteFolderGridItemEntitiesTransaction(
        eblanApplicationInfoEntities: List<EblanApplicationInfoEntity>,
        folderEblanApplicationInfoEntities: List<FolderEblanApplicationInfoEntity>,
    ) {
        eblanDatabase.withTransaction {
            eblanApplicationInfoDao.deleteEblanApplicationInfoEntities(entities = eblanApplicationInfoEntities)

            folderEblanApplicationInfoDao.deleteFolderEblanApplicationInfoEntities(entities = folderEblanApplicationInfoEntities)
        }
    }
}
