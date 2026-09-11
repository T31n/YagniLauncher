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
package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.application.DeleteEblanApplicationInfo
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.application.SyncEblanApplicationInfo
import kotlinx.coroutines.flow.Flow

interface EblanApplicationInfoRepository {
    val eblanApplicationInfosFlow: Flow<List<EblanApplicationInfo>>

    suspend fun getEblanApplicationInfos(): List<EblanApplicationInfo>

    suspend fun upsertEblanApplicationInfo(eblanApplicationInfo: EblanApplicationInfo)

    suspend fun updateEblanApplicationInfos(eblanApplicationInfos: List<EblanApplicationInfo>)

    suspend fun deleteEblanApplicationInfoByPackageName(
        serialNumber: Long,
        packageName: String,
    )

    suspend fun deleteEblanApplicationInfos(eblanApplicationInfos: List<EblanApplicationInfo>)

    suspend fun upsertSyncEblanApplicationInfos(syncEblanApplicationInfos: List<SyncEblanApplicationInfo>)

    suspend fun deleteSyncEblanApplicationInfos(deleteEblanApplicationInfos: List<DeleteEblanApplicationInfo>)

    suspend fun updateEblanApplicationInfo(eblanApplicationInfo: EblanApplicationInfo)

    suspend fun getEblanApplicationInfoByComponentName(
        serialNumber: Long,
        componentName: String,
    ): EblanApplicationInfo?

    suspend fun getEblanApplicationInfosByPackageName(
        serialNumber: Long,
        packageName: String,
    ): List<EblanApplicationInfo>

    fun getEblanApplicationInfoTagsFlow(
        serialNumber: Long,
        componentName: String,
    ): Flow<List<EblanApplicationInfoTag>>

    suspend fun getEblanApplicationInfosByTagId(id: Long): List<EblanApplicationInfo>

    suspend fun getEblanApplicationInfosWithoutTag(): List<EblanApplicationInfo>
}
