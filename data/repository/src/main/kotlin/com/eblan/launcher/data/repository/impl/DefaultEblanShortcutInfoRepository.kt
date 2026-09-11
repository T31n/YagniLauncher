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

import com.eblan.launcher.data.room.dao.EblanShortcutInfoDao
import com.eblan.launcher.data.room.entity.EblanShortcutInfoEntity
import com.eblan.launcher.domain.model.DeleteEblanShortcutInfo
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultEblanShortcutInfoRepository @Inject constructor(private val eblanShortcutInfoDao: EblanShortcutInfoDao) : EblanShortcutInfoRepository {
    override val eblanShortcutInfosFlow =
        eblanShortcutInfoDao.getEblanShortcutInfoEntitiesFlow().map { entities ->
            entities.map {
                it.asModel()
            }
        }

    override suspend fun getEblanShortcutInfos(): List<EblanShortcutInfo> = eblanShortcutInfoDao.getEblanShortcutInfoEntities()
        .map {
            it.asModel()
        }

    override suspend fun upsertEblanShortcutInfos(eblanShortcutInfos: List<EblanShortcutInfo>) {
        val entities = eblanShortcutInfos.map {
            it.asEntity()
        }

        eblanShortcutInfoDao.upsertEblanShortcutInfoEntities(entities = entities)
    }

    override suspend fun deleteEblanShortcutInfos(deleteEblanShortcutInfos: List<DeleteEblanShortcutInfo>) {
        eblanShortcutInfoDao.deleteEblanShortcutInfoEntities(deleteEblanShortcutInfos = deleteEblanShortcutInfos)
    }

    override suspend fun getEblanShortcutInfos(
        serialNumber: Long,
        packageName: String,
    ): List<EblanShortcutInfo> = eblanShortcutInfoDao.getEblanShortcutInfoEntities(
        serialNumber = serialNumber,
        packageName = packageName,
    ).map {
        it.asModel()
    }

    override suspend fun deleteEblanShortcutInfos(
        serialNumber: Long,
        packageName: String,
    ) {
        eblanShortcutInfoDao.deleteEblanShortcutInfoEntities(
            serialNumber = serialNumber,
            packageName = packageName,
        )
    }

    private fun EblanShortcutInfo.asEntity(): EblanShortcutInfoEntity = EblanShortcutInfoEntity(
        shortcutId = shortcutId,
        serialNumber = serialNumber,
        packageName = packageName,
        shortLabel = shortLabel,
        longLabel = longLabel,
        icon = icon,
        shortcutQueryFlag = shortcutQueryFlag,
        isEnabled = isEnabled,
        lastChangedTimestamp = lastChangedTimestamp,
    )

    private fun EblanShortcutInfoEntity.asModel(): EblanShortcutInfo = EblanShortcutInfo(
        shortcutId = shortcutId,
        serialNumber = serialNumber,
        packageName = packageName,
        shortLabel = shortLabel,
        longLabel = longLabel,
        icon = icon,
        shortcutQueryFlag = shortcutQueryFlag,
        isEnabled = isEnabled,
        lastChangedTimestamp = lastChangedTimestamp,
    )
}
