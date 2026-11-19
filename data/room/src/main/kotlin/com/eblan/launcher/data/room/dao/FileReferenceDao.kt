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
package com.eblan.launcher.data.room.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface FileReferenceDao {
    /**
     * Returns every file path stored in any column of any table.
     *
     * This is the single place that knows which columns point to files in the cache directories.
     * When a new entity column stores an icon or preview path, add it here so the cache cleanup
     * keeps treating that file as referenced.
     */
    @Query(
        """
        SELECT icon FROM EblanApplicationInfoEntity WHERE icon IS NOT NULL
        UNION SELECT customIcon FROM EblanApplicationInfoEntity WHERE customIcon IS NOT NULL
        UNION SELECT preview FROM EblanAppWidgetProviderInfoEntity WHERE preview IS NOT NULL
        UNION SELECT applicationIcon FROM EblanAppWidgetProviderInfoEntity WHERE applicationIcon IS NOT NULL
        UNION SELECT icon FROM EblanShortcutInfoEntity WHERE icon IS NOT NULL
        UNION SELECT activityIcon FROM EblanShortcutConfigEntity WHERE activityIcon IS NOT NULL
        UNION SELECT applicationIcon FROM EblanShortcutConfigEntity WHERE applicationIcon IS NOT NULL
        UNION SELECT icon FROM EblanIconPackInfoEntity WHERE icon IS NOT NULL
        UNION SELECT icon FROM FolderEblanApplicationInfoEntity WHERE icon IS NOT NULL
        UNION SELECT icon FROM ApplicationInfoGridItemEntity WHERE icon IS NOT NULL
        UNION SELECT customIcon FROM ApplicationInfoGridItemEntity WHERE customIcon IS NOT NULL
        UNION SELECT icon FROM FolderGridItemEntity WHERE icon IS NOT NULL
        UNION SELECT preview FROM WidgetGridItemEntity WHERE preview IS NOT NULL
        UNION SELECT icon FROM WidgetGridItemEntity WHERE icon IS NOT NULL
        UNION SELECT icon FROM ShortcutInfoGridItemEntity WHERE icon IS NOT NULL
        UNION SELECT eblanApplicationInfoIcon FROM ShortcutInfoGridItemEntity WHERE eblanApplicationInfoIcon IS NOT NULL
        UNION SELECT customIcon FROM ShortcutInfoGridItemEntity WHERE customIcon IS NOT NULL
        UNION SELECT activityIcon FROM ShortcutConfigGridItemEntity WHERE activityIcon IS NOT NULL
        UNION SELECT applicationIcon FROM ShortcutConfigGridItemEntity WHERE applicationIcon IS NOT NULL
        UNION SELECT shortcutIntentIcon FROM ShortcutConfigGridItemEntity WHERE shortcutIntentIcon IS NOT NULL
        UNION SELECT customIcon FROM ShortcutConfigGridItemEntity WHERE customIcon IS NOT NULL
        """,
    )
    suspend fun getReferencedFilePaths(): List<String>
}
