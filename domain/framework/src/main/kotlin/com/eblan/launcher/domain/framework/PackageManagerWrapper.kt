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
package com.eblan.launcher.domain.framework

import com.eblan.launcher.domain.model.iconpackinfo.PackageManagerIconPackInfo
import java.io.File

interface PackageManagerWrapper {
    val hasSystemFeatureAppWidgets: Boolean

    suspend fun getApplicationIcon(
        packageName: String,
        file: File,
    ): String?

    suspend fun getApplicationLabel(packageName: String): String?

    suspend fun getComponentName(packageName: String): String?

    suspend fun getIconPackInfos(): List<PackageManagerIconPackInfo>

    suspend fun getLastUpdateTime(packageName: String): Long

    fun isSystem(flags: Int): Boolean
}
