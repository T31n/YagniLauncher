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
import com.eblan.launcher.data.room.dao.EblanApplicationInfoTagCrossRefDao
import com.eblan.launcher.data.room.dao.EblanApplicationInfoTagDao
import com.eblan.launcher.data.room.entity.EblanApplicationInfoTagCrossRefEntity
import com.eblan.launcher.data.room.entity.EblanApplicationInfoTagEntity

internal class DefaultEblanApplicationInfoTagEntityTransaction(
    private val eblanDatabase: EblanDatabase,
    private val eblanApplicationInfoTagDao: EblanApplicationInfoTagDao,
    private val eblanApplicationInfoTagCrossRefDao: EblanApplicationInfoTagCrossRefDao,
) : EblanApplicationInfoTagEntityTransaction {
    override suspend fun replaceEblanApplicationInfoTagEntitiesTransaction(
        eblanApplicationInfoTagEntities: List<EblanApplicationInfoTagEntity>,
        eblanApplicationInfoTagCrossRefEntities: List<EblanApplicationInfoTagCrossRefEntity>,
    ) {
        eblanDatabase.withTransaction {
            eblanApplicationInfoTagDao.deleteAllEblanApplicationInfoTagEntities()

            eblanApplicationInfoTagEntities.forEach {
                eblanApplicationInfoTagDao.insertEblanApplicationInfoTagEntity(entity = it)
            }

            eblanApplicationInfoTagCrossRefEntities.forEach {
                eblanApplicationInfoTagCrossRefDao.insertEblanApplicationInfoTagCrossRefEntity(entity = it)
            }
        }
    }
}
