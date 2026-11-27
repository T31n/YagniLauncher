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

import com.eblan.launcher.data.room.dao.EblanApplicationInfoTagDao
import com.eblan.launcher.data.room.entity.EblanApplicationInfoTagEntity
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.repository.EblanApplicationInfoTagRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultEblanApplicationInfoTagRepository @Inject constructor(private val eblanApplicationInfoTagDao: EblanApplicationInfoTagDao) : EblanApplicationInfoTagRepository {
    override val eblanApplicationInfoTagsFlow =
        eblanApplicationInfoTagDao.getEblanApplicationInfoTagEntitiesFlow().map { entities ->
            entities.map { currentEblanApplicationInfoTagEntity ->
                currentEblanApplicationInfoTagEntity.asModel()
            }
        }

    override suspend fun insertEblanApplicationInfoTag(eblanApplicationInfoTag: EblanApplicationInfoTag) {
        eblanApplicationInfoTagDao.insertEblanApplicationInfoTagEntity(eblanApplicationInfoTag.asEntity())
    }

    override suspend fun updateEblanApplicationInfoTag(eblanApplicationInfoTag: EblanApplicationInfoTag) {
        eblanApplicationInfoTagDao.updateEblanApplicationInfoTagEntity(eblanApplicationInfoTag.asEntity())
    }

    override suspend fun deleteEblanApplicationInfoTag(eblanApplicationInfoTag: EblanApplicationInfoTag) {
        eblanApplicationInfoTagDao.deleteEblanApplicationInfoTagEntity(eblanApplicationInfoTag.asEntity())
    }

    override suspend fun updateEblanApplicationInfoTags(eblanApplicationInfoTags: List<EblanApplicationInfoTag>) {
        val entities = eblanApplicationInfoTags.map { currentEblanApplicationInfoTag ->
            currentEblanApplicationInfoTag.asEntity()
        }

        eblanApplicationInfoTagDao.updateEblanApplicationInfoTagEntities(entities)
    }

    private fun EblanApplicationInfoTagEntity.asModel(): EblanApplicationInfoTag = EblanApplicationInfoTag(
        id = id,
        name = name,
        index = index,
    )

    private fun EblanApplicationInfoTag.asEntity(): EblanApplicationInfoTagEntity = EblanApplicationInfoTagEntity(
        id = id,
        name = name,
        index = index,
    )
}
