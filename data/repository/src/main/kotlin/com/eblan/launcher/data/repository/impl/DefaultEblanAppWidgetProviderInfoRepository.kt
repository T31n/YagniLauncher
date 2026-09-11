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

import com.eblan.launcher.data.room.dao.EblanAppWidgetProviderInfoDao
import com.eblan.launcher.data.room.entity.EblanAppWidgetProviderInfoEntity
import com.eblan.launcher.domain.model.DeleteEblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultEblanAppWidgetProviderInfoRepository @Inject constructor(private val eblanAppWidgetProviderInfoDao: EblanAppWidgetProviderInfoDao) : EblanAppWidgetProviderInfoRepository {
    override val eblanAppWidgetProviderInfosFlow =
        eblanAppWidgetProviderInfoDao.getEblanAppWidgetProviderInfoEntitiesFlow().map { entities ->
            entities.map {
                it.asModel()
            }
        }

    override suspend fun getEblanAppWidgetProviderInfos(): List<EblanAppWidgetProviderInfo> = eblanAppWidgetProviderInfoDao.getEblanAppWidgetProviderInfoEntityList()
        .map {
            it.asModel()
        }

    override suspend fun upsertEblanAppWidgetProviderInfos(eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>) {
        val entities = eblanAppWidgetProviderInfos.map {
            it.asEntity()
        }

        eblanAppWidgetProviderInfoDao.upsertEblanAppWidgetProviderInfoEntities(entities = entities)
    }

    override suspend fun deleteEblanAppWidgetProviderInfos(deleteEblanAppWidgetProviderInfos: List<DeleteEblanAppWidgetProviderInfo>) {
        eblanAppWidgetProviderInfoDao.deleteEblanAppWidgetProviderInfoEntities(
            deleteEblanAppWidgetProviderInfos = deleteEblanAppWidgetProviderInfos,
        )
    }

    override suspend fun getEblanAppWidgetProviderInfosByPackageName(packageName: String): List<EblanAppWidgetProviderInfo> = eblanAppWidgetProviderInfoDao.getEblanAppWidgetProviderInfoEntitiesByPackageName(
        packageName = packageName,
    ).map {
        it.asModel()
    }

    override suspend fun deleteEblanAppWidgetProviderInfoByPackageName(packageName: String) {
        eblanAppWidgetProviderInfoDao.deleteEblanAppWidgetProviderInfoEntityByPackageName(
            packageName = packageName,
        )
    }

    private fun EblanAppWidgetProviderInfo.asEntity(): EblanAppWidgetProviderInfoEntity = EblanAppWidgetProviderInfoEntity(
        componentName = componentName,
        serialNumber = serialNumber,
        configure = configure,
        packageName = packageName,
        targetCellWidth = targetCellWidth,
        targetCellHeight = targetCellHeight,
        minWidth = minWidth,
        minHeight = minHeight,
        resizeMode = resizeMode,
        minResizeWidth = minResizeWidth,
        minResizeHeight = minResizeHeight,
        maxResizeWidth = maxResizeWidth,
        maxResizeHeight = maxResizeHeight,
        preview = preview,
        applicationLabel = applicationLabel,
        applicationIcon = applicationIcon,
        lastUpdateTime = lastUpdateTime,
        label = label,
        description = description,
    )

    private fun EblanAppWidgetProviderInfoEntity.asModel(): EblanAppWidgetProviderInfo = EblanAppWidgetProviderInfo(
        componentName = componentName,
        serialNumber = serialNumber,
        configure = configure,
        packageName = packageName,
        targetCellWidth = targetCellWidth,
        targetCellHeight = targetCellHeight,
        minWidth = minWidth,
        minHeight = minHeight,
        resizeMode = resizeMode,
        minResizeWidth = minResizeWidth,
        minResizeHeight = minResizeHeight,
        maxResizeWidth = maxResizeWidth,
        maxResizeHeight = maxResizeHeight,
        preview = preview,
        applicationIcon = applicationIcon,
        applicationLabel = applicationLabel,
        lastUpdateTime = lastUpdateTime,
        label = label,
        description = description,
    )
}
