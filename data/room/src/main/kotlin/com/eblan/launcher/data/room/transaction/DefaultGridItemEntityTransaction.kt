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
package com.eblan.launcher.data.room.transaction

import androidx.room.withTransaction
import com.eblan.launcher.data.room.EblanDatabase
import com.eblan.launcher.data.room.dao.ApplicationInfoGridItemDao
import com.eblan.launcher.data.room.dao.FolderGridItemDao
import com.eblan.launcher.data.room.dao.ShortcutConfigGridItemDao
import com.eblan.launcher.data.room.dao.ShortcutInfoGridItemDao
import com.eblan.launcher.data.room.dao.WidgetGridItemDao
import com.eblan.launcher.data.room.entity.ApplicationInfoGridItemEntity
import com.eblan.launcher.data.room.entity.FolderGridItemEntity
import com.eblan.launcher.data.room.entity.ShortcutConfigGridItemEntity
import com.eblan.launcher.data.room.entity.ShortcutInfoGridItemEntity
import com.eblan.launcher.data.room.entity.WidgetGridItemEntity
import com.eblan.launcher.data.room.model.GridItemEntities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultGridItemEntityTransaction @Inject constructor(
    private val eblanDatabase: EblanDatabase,
    private val applicationInfoGridItemDao: ApplicationInfoGridItemDao,
    private val widgetGridItemDao: WidgetGridItemDao,
    private val shortcutInfoGridItemDao: ShortcutInfoGridItemDao,
    private val shortcutConfigGridItemDao: ShortcutConfigGridItemDao,
    private val folderGridItemDao: FolderGridItemDao,
) : GridItemEntityTransaction {
    override val gridItemEntitiesFlow: Flow<GridItemEntities> =
        eblanDatabase.invalidationTracker.createFlow(
            "ApplicationInfoGridItemEntity",
            "WidgetGridItemEntity",
            "ShortcutInfoGridItemEntity",
            "ShortcutConfigGridItemEntity",
            "FolderGridItemEntity",
        ).conflate()
            .map {
                eblanDatabase.withTransaction {
                    GridItemEntities(
                        applicationInfoGridItemEntities = applicationInfoGridItemDao.getApplicationInfoGridItemEntities(),
                        widgetGridItemEntities = widgetGridItemDao.getWidgetGridItemEntities(),
                        shortcutInfoGridItemEntities = shortcutInfoGridItemDao.getShortcutInfoGridItemEntities(),
                        shortcutConfigGridItemEntities = shortcutConfigGridItemDao.getShortcutConfigGridItemEntities(),
                        folderGridItemEntities = folderGridItemDao.getFolderGridItemEntities(),
                    )
                }
            }
            .distinctUntilChanged()

    override suspend fun getGridItemEntities(): GridItemEntities = eblanDatabase.withTransaction {
        GridItemEntities(
            applicationInfoGridItemEntities = applicationInfoGridItemDao.getApplicationInfoGridItemEntities(),
            widgetGridItemEntities = widgetGridItemDao.getWidgetGridItemEntities(),
            shortcutInfoGridItemEntities = shortcutInfoGridItemDao.getShortcutInfoGridItemEntities(),
            shortcutConfigGridItemEntities = shortcutConfigGridItemDao.getShortcutConfigGridItemEntities(),
            folderGridItemEntities = folderGridItemDao.getFolderGridItemEntities(),
        )
    }

    override suspend fun upsertGridItemEntitiesTransaction(
        applicationInfoGridItemEntities: List<ApplicationInfoGridItemEntity>,
        widgetGridItemEntities: List<WidgetGridItemEntity>,
        shortcutInfoGridItemEntities: List<ShortcutInfoGridItemEntity>,
        shortcutConfigGridItemEntities: List<ShortcutConfigGridItemEntity>,
        folderGridItemEntities: List<FolderGridItemEntity>,
    ) {
        eblanDatabase.withTransaction {
            folderGridItemDao.upsertFolderGridItemEntities(
                entities = folderGridItemEntities,
            )

            applicationInfoGridItemDao.upsertApplicationInfoGridItemEntities(
                entities = applicationInfoGridItemEntities,
            )

            widgetGridItemDao.upsertWidgetGridItemEntities(
                entities = widgetGridItemEntities,
            )

            shortcutInfoGridItemDao.upsertShortcutInfoGridItemEntities(
                entities = shortcutInfoGridItemEntities,
            )

            shortcutConfigGridItemDao.upsertShortcutConfigGridItemEntities(
                entities = shortcutConfigGridItemEntities,
            )
        }
    }

    override suspend fun deleteGridItemEntitiesTransaction(
        applicationInfoGridItemEntities: List<ApplicationInfoGridItemEntity>,
        widgetGridItemEntities: List<WidgetGridItemEntity>,
        shortcutInfoGridItemEntities: List<ShortcutInfoGridItemEntity>,
        shortcutConfigGridItemEntities: List<ShortcutConfigGridItemEntity>,
        folderGridItemEntities: List<FolderGridItemEntity>,
    ) {
        eblanDatabase.withTransaction {
            applicationInfoGridItemDao.deleteApplicationInfoGridItemEntities(
                entities = applicationInfoGridItemEntities,
            )

            widgetGridItemDao.deleteWidgetGridItemEntities(
                entities = widgetGridItemEntities,
            )

            shortcutInfoGridItemDao.deleteShortcutInfoGridItemEntities(
                entities = shortcutInfoGridItemEntities,
            )

            shortcutConfigGridItemDao.deleteShortcutConfigGridItemEntities(
                entities = shortcutConfigGridItemEntities,
            )

            folderGridItemDao.deleteFolderGridItemEntities(
                entities = folderGridItemEntities,
            )
        }
    }
}
