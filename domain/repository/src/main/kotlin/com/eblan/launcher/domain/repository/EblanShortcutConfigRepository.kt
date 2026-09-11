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

import com.eblan.launcher.domain.model.shortcutconfig.DeleteEblanShortcutConfig
import com.eblan.launcher.domain.model.shortcutconfig.EblanShortcutConfig
import kotlinx.coroutines.flow.Flow

interface EblanShortcutConfigRepository {
    val eblanShortcutConfigsFlow: Flow<List<EblanShortcutConfig>>

    suspend fun getEblanShortcutConfigs(): List<EblanShortcutConfig>

    suspend fun upsertEblanShortcutConfigs(eblanShortcutConfigs: List<EblanShortcutConfig>)

    suspend fun deleteEblanShortcutConfig(
        serialNumber: Long,
        packageName: String,
    )

    suspend fun deleteEblanShortcutConfigs(deleteEblanShortcutConfigs: List<DeleteEblanShortcutConfig>)

    suspend fun getEblanShortcutConfigsByPackageName(
        serialNumber: Long,
        packageName: String,
    ): List<EblanShortcutConfig>
}
