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

import com.eblan.launcher.data.room.dao.EblanShortcutConfigDao
import com.eblan.launcher.data.room.entity.EblanShortcutConfigEntity
import com.eblan.launcher.domain.model.DeleteEblanShortcutConfig
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultEblanShortcutConfigRepository @Inject constructor(private val eblanShortcutConfigDao: EblanShortcutConfigDao) : EblanShortcutConfigRepository {
    override val eblanShortcutConfigsFlow =
        eblanShortcutConfigDao.getEblanShortcutConfigEntitiesFlow().map { entities ->
            entities.map {
                it.asModel()
            }
        }

    override suspend fun getEblanShortcutConfigs(): List<EblanShortcutConfig> = eblanShortcutConfigDao.getEblanShortcutConfigEntities()
        .map {
            it.asModel()
        }

    override suspend fun upsertEblanShortcutConfigs(eblanShortcutConfigs: List<EblanShortcutConfig>) {
        val entities = eblanShortcutConfigs.map {
            it.asEntity()
        }

        eblanShortcutConfigDao.upsertEblanShortcutConfigEntities(entities = entities)
    }

    override suspend fun deleteEblanShortcutConfig(
        serialNumber: Long,
        packageName: String,
    ) {
        eblanShortcutConfigDao.deleteEblanShortcutConfigEntity(
            serialNumber = serialNumber,
            packageName = packageName,
        )
    }

    override suspend fun deleteEblanShortcutConfigs(deleteEblanShortcutConfigs: List<DeleteEblanShortcutConfig>) {
        eblanShortcutConfigDao.deleteEblanShortcutConfigEntities(deleteEblanShortcutConfigs = deleteEblanShortcutConfigs)
    }

    override suspend fun getEblanShortcutConfigsByPackageName(
        serialNumber: Long,
        packageName: String,
    ): List<EblanShortcutConfig> = eblanShortcutConfigDao.getEblanShortcutConfigEntitiesByPackageName(
        serialNumber = serialNumber,
        packageName = packageName,
    ).map {
        it.asModel()
    }

    private fun EblanShortcutConfig.asEntity(): EblanShortcutConfigEntity = EblanShortcutConfigEntity(
        componentName = componentName,
        serialNumber = serialNumber,
        packageName = packageName,
        activityIcon = activityIcon,
        activityLabel = activityLabel,
        applicationIcon = applicationIcon,
        applicationLabel = applicationLabel,
    )

    private fun EblanShortcutConfigEntity.asModel(): EblanShortcutConfig = EblanShortcutConfig(
        componentName = componentName,
        packageName = packageName,
        serialNumber = serialNumber,
        activityIcon = activityIcon,
        activityLabel = activityLabel,
        applicationIcon = applicationIcon,
        applicationLabel = applicationLabel,
    )
}
