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
package com.eblan.launcher.data.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration19To20 : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `EblanApplicationInfoTagCrossRefEntity_backup` (
                `componentName` TEXT NOT NULL,
                `serialNumber` INTEGER NOT NULL,
                `id` INTEGER NOT NULL,
                PRIMARY KEY(`componentName`, `serialNumber`, `id`)
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            INSERT OR IGNORE INTO `EblanApplicationInfoTagCrossRefEntity_backup` (`componentName`, `serialNumber`, `id`)
            SELECT `componentName`, `serialNumber`, `id`
            FROM `EblanApplicationInfoTagCrossRefEntity`
            """.trimIndent(),
        )

        db.execSQL("DROP TABLE `EblanApplicationInfoTagCrossRefEntity`")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `EblanApplicationInfoTagEntity_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `index` INTEGER NOT NULL
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            INSERT INTO `EblanApplicationInfoTagEntity_new` (`id`, `name`, `index`)
            SELECT `id`, `name`, 0
            FROM `EblanApplicationInfoTagEntity`
            """.trimIndent(),
        )

        db.execSQL("DROP TABLE `EblanApplicationInfoTagEntity`")
        db.execSQL("ALTER TABLE `EblanApplicationInfoTagEntity_new` RENAME TO `EblanApplicationInfoTagEntity`")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `FolderEblanApplicationInfoEntity` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `icon` TEXT,
                `label` TEXT NOT NULL,
                `index` INTEGER NOT NULL,
                `folderIndex` INTEGER NOT NULL,
                `folderId` TEXT,
                FOREIGN KEY(`folderId`) REFERENCES `FolderEblanApplicationInfoEntity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_FolderEblanApplicationInfoEntity_folderId`
            ON `FolderEblanApplicationInfoEntity` (`folderId`)
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `EblanApplicationInfoEntity_new` (
                `componentName` TEXT NOT NULL,
                `serialNumber` INTEGER NOT NULL,
                `packageName` TEXT NOT NULL,
                `icon` TEXT,
                `label` TEXT NOT NULL,
                `customIcon` TEXT,
                `customLabel` TEXT,
                `isHidden` INTEGER NOT NULL DEFAULT 0,
                `lastUpdateTime` INTEGER NOT NULL DEFAULT 0,
                `flags` INTEGER NOT NULL DEFAULT 0,
                `folderIndex` INTEGER NOT NULL DEFAULT -1,
                `folderId` TEXT DEFAULT NULL,
                PRIMARY KEY(`componentName`, `serialNumber`),
                FOREIGN KEY(`folderId`) REFERENCES `FolderEblanApplicationInfoEntity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            INSERT INTO `EblanApplicationInfoEntity_new` (
                `componentName`, `serialNumber`, `packageName`, `icon`, `label`,
                `customIcon`, `customLabel`, `isHidden`, `lastUpdateTime`, `flags`,
                `folderIndex`, `folderId`
            )
            SELECT
                `componentName`, `serialNumber`, `packageName`, `icon`, `label`,
                `customIcon`, `customLabel`, `isHidden`, `lastUpdateTime`, `flags`,
                `index`, NULL
            FROM `EblanApplicationInfoEntity`
            """.trimIndent(),
        )

        db.execSQL("DROP TABLE `EblanApplicationInfoEntity`")
        db.execSQL("ALTER TABLE `EblanApplicationInfoEntity_new` RENAME TO `EblanApplicationInfoEntity`")

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_EblanApplicationInfoEntity_folderId`
            ON `EblanApplicationInfoEntity` (`folderId`)
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `EblanApplicationInfoTagCrossRefEntity` (
                `componentName` TEXT NOT NULL,
                `serialNumber` INTEGER NOT NULL,
                `id` INTEGER NOT NULL,
                PRIMARY KEY(`componentName`, `serialNumber`, `id`),
                FOREIGN KEY(`componentName`, `serialNumber`) REFERENCES `EblanApplicationInfoEntity`(`componentName`, `serialNumber`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`id`) REFERENCES `EblanApplicationInfoTagEntity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_EblanApplicationInfoTagCrossRefEntity_id`
            ON `EblanApplicationInfoTagCrossRefEntity` (`id`)
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_EblanApplicationInfoTagCrossRefEntity_componentName_serialNumber`
            ON `EblanApplicationInfoTagCrossRefEntity` (`componentName`, `serialNumber`)
            """.trimIndent(),
        )

        db.execSQL(
            """
            INSERT OR IGNORE INTO `EblanApplicationInfoTagCrossRefEntity` (`componentName`, `serialNumber`, `id`)
            SELECT backup.`componentName`, backup.`serialNumber`, backup.`id`
            FROM `EblanApplicationInfoTagCrossRefEntity_backup` AS backup
            INNER JOIN `EblanApplicationInfoTagEntity` AS tag
                ON tag.`id` = backup.`id`
            INNER JOIN `EblanApplicationInfoEntity` AS app
                ON app.`componentName` = backup.`componentName`
               AND app.`serialNumber` = backup.`serialNumber`
            """.trimIndent(),
        )

        db.execSQL("DROP TABLE `EblanApplicationInfoTagCrossRefEntity_backup`")
    }
}
