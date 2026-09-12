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
import com.eblan.launcher.data.room.dao.EblanIconPackInfoDao
import com.eblan.launcher.domain.model.iconpackinfo.EblanIconPackInfo
import com.eblan.launcher.domain.repository.EblanIconPackInfoRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultEblanIconPackInfoRepository @Inject constructor(private val eblanIconPackInfoDao: EblanIconPackInfoDao) : EblanIconPackInfoRepository {
    override val eblanIconPackInfosFlow =
        eblanIconPackInfoDao.getEblanIconPackInfoEntitiesFlow().map { entities ->
            entities.map {
                it.asModel()
            }
        }

    override suspend fun upsertEblanIconPackInfo(eblanIconPackInfo: EblanIconPackInfo): Long = eblanIconPackInfoDao.upsertEblanIconPackInfoEntity(entity = eblanIconPackInfo.asEntity())

    override suspend fun deleteEblanIconPackInfo(eblanIconPackInfo: EblanIconPackInfo) {
        eblanIconPackInfoDao.deleteEblanIconPackInfoEntity(entity = eblanIconPackInfo.asEntity())
    }

    override suspend fun getEblanIconPackInfo(packageName: String): EblanIconPackInfo? = eblanIconPackInfoDao.getEblanIconPackInfoEntity(packageName = packageName)?.asModel()
}
