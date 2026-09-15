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

import com.eblan.launcher.data.room.dao.EblanApplicationInfoTagCrossRefDao
import com.eblan.launcher.data.room.entity.EblanApplicationInfoTagCrossRefEntity
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTagCrossRef
import com.eblan.launcher.domain.repository.EblanApplicationInfoTagCrossRefRepository
import javax.inject.Inject

internal class DefaultEblanApplicationInfoTagCrossRefRepository @Inject constructor(private val eblanApplicationInfoTagCrossRefDao: EblanApplicationInfoTagCrossRefDao) : EblanApplicationInfoTagCrossRefRepository {
    override suspend fun insertEblanApplicationInfoTagCrossRef(eblanApplicationInfoTagCrossRef: EblanApplicationInfoTagCrossRef) {
        eblanApplicationInfoTagCrossRefDao.insertEblanApplicationInfoTagCrossRefEntity(
            entity = eblanApplicationInfoTagCrossRef.asEntity(),
        )
    }

    override suspend fun deleteEblanApplicationInfoTagCrossRef(
        componentName: String,
        serialNumber: Long,
        tagId: Long,
    ) {
        eblanApplicationInfoTagCrossRefDao.deleteEblanApplicationInfoTagCrossRefEntity(
            componentName = componentName,
            serialNumber = serialNumber,
            tagId = tagId,
        )
    }

    private fun EblanApplicationInfoTagCrossRef.asEntity(): EblanApplicationInfoTagCrossRefEntity = EblanApplicationInfoTagCrossRefEntity(
        componentName = componentName,
        serialNumber = serialNumber,
        id = id,
    )
}
