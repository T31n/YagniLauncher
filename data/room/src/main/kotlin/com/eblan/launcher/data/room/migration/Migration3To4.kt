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

class Migration3To4 : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        updateEblanApplicationInfoEntities(db)

        updateEblanAppWidgetProviderInfoEntities(db)

        updateEblanShortcutConfigEntities(db)

        updateApplicationInfoGridItemEntities(db)

        updateWidgetGridItemEntities(db)
    }

    private fun updateEblanApplicationInfoEntities(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                    CREATE TABLE IF NOT EXISTS `EblanApplicationInfoEntity_new` (
                        `packageName` TEXT NOT NULL,
                        `serialNumber` INTEGER NOT NULL,
                        `componentName` TEXT NOT NULL,
                        `icon` TEXT,
                        `label` TEXT,
                        PRIMARY KEY(`packageName`, `serialNumber`)
                    )
            """.trimIndent(),
        )

        db.execSQL(
            """
                    INSERT INTO `EblanApplicationInfoEntity_new` (`packageName`, `serialNumber`, `componentName`, `icon`, `label`)
                    SELECT `packageName`, `serialNumber`, 
                           COALESCE(`componentName`, '') AS `componentName`, 
                           `icon`, `label`
                    FROM `EblanApplicationInfoEntity`
            """.trimIndent(),
        )

        db.execSQL("DROP TABLE `EblanApplicationInfoEntity`")
        db.execSQL("ALTER TABLE `EblanApplicationInfoEntity_new` RENAME TO `EblanApplicationInfoEntity`")
    }

    private fun updateEblanAppWidgetProviderInfoEntities(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                    CREATE TABLE IF NOT EXISTS `EblanAppWidgetProviderInfoEntity_new` (
                        `componentName` TEXT NOT NULL,
                        `serialNumber` INTEGER NOT NULL,
                        `configure` TEXT,
                        `packageName` TEXT NOT NULL,
                        `targetCellWidth` INTEGER NOT NULL,
                        `targetCellHeight` INTEGER NOT NULL,
                        `minWidth` INTEGER NOT NULL,
                        `minHeight` INTEGER NOT NULL,
                        `resizeMode` INTEGER NOT NULL,
                        `minResizeWidth` INTEGER NOT NULL,
                        `minResizeHeight` INTEGER NOT NULL,
                        `maxResizeWidth` INTEGER NOT NULL,
                        `maxResizeHeight` INTEGER NOT NULL,
                        `preview` TEXT,
                        `label` TEXT,
                        `icon` TEXT,
                        PRIMARY KEY(`componentName`)
                    )
            """.trimIndent(),
        )

        db.execSQL(
            """
                    INSERT INTO `EblanAppWidgetProviderInfoEntity_new` (
                        `componentName`, `serialNumber`, `configure`, `packageName`,
                        `targetCellWidth`, `targetCellHeight`, `minWidth`, `minHeight`,
                        `resizeMode`, `minResizeWidth`, `minResizeHeight`, `maxResizeWidth`,
                        `maxResizeHeight`, `preview`, `label`, `icon`
                    )
                    SELECT 
                        `componentName`, 0 AS `serialNumber`, `configure`, `packageName`,
                        `targetCellWidth`, `targetCellHeight`, `minWidth`, `minHeight`,
                        `resizeMode`, `minResizeWidth`, `minResizeHeight`, `maxResizeWidth`,
                        `maxResizeHeight`, `preview`, `label`, `icon`
                    FROM `EblanAppWidgetProviderInfoEntity`
            """.trimIndent(),
        )

        db.execSQL("DROP TABLE `EblanAppWidgetProviderInfoEntity`")
        db.execSQL("ALTER TABLE `EblanAppWidgetProviderInfoEntity_new` RENAME TO `EblanAppWidgetProviderInfoEntity`")
    }

    private fun updateEblanShortcutConfigEntities(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                    CREATE TABLE IF NOT EXISTS `EblanShortcutConfigEntity` (
                        `componentName` TEXT NOT NULL,
                        `packageName` TEXT NOT NULL,
                        `serialNumber` INTEGER NOT NULL,
                        `activityIcon` TEXT,
                        `activityLabel` TEXT,
                        `applicationIcon` TEXT,
                        `applicationLabel` TEXT,
                        PRIMARY KEY(`componentName`, `serialNumber`)
                    )
            """.trimIndent(),
        )

        db.execSQL(
            """
                    CREATE TABLE IF NOT EXISTS `ShortcutConfigGridItemEntity` (
                        `id` TEXT NOT NULL,
                        `folderId` TEXT,
                        `page` INTEGER NOT NULL,
                        `startColumn` INTEGER NOT NULL,
                        `startRow` INTEGER NOT NULL,
                        `columnSpan` INTEGER NOT NULL,
                        `rowSpan` INTEGER NOT NULL,
                        `associate` TEXT NOT NULL,
                        `componentName` TEXT NOT NULL,
                        `packageName` TEXT NOT NULL,
                        `activityIcon` TEXT,
                        `activityLabel` TEXT,
                        `applicationIcon` TEXT,
                        `applicationLabel` TEXT,
                        `override` INTEGER NOT NULL,
                        `serialNumber` INTEGER NOT NULL,
                        `shortcutIntentName` TEXT,
                        `shortcutIntentIcon` TEXT,
                        `shortcutIntentUri` TEXT,
                        `iconSize` INTEGER NOT NULL,
                        `textColor` TEXT NOT NULL,
                        `textSize` INTEGER NOT NULL,
                        `showLabel` INTEGER NOT NULL,
                        `singleLineLabel` INTEGER NOT NULL,
                        `horizontalAlignment` TEXT NOT NULL,
                        `verticalArrangement` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
            """.trimIndent(),
        )
    }

    private fun updateApplicationInfoGridItemEntities(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                    CREATE TABLE IF NOT EXISTS `ApplicationInfoGridItemEntity_new` (
                        `id` TEXT NOT NULL,
                        `folderId` TEXT,
                        `page` INTEGER NOT NULL,
                        `startColumn` INTEGER NOT NULL,
                        `startRow` INTEGER NOT NULL,
                        `columnSpan` INTEGER NOT NULL,
                        `rowSpan` INTEGER NOT NULL,
                        `associate` TEXT NOT NULL,
                        `componentName` TEXT NOT NULL,
                        `packageName` TEXT NOT NULL,
                        `icon` TEXT,
                        `label` TEXT,
                        `override` INTEGER NOT NULL,
                        `serialNumber` INTEGER NOT NULL,
                        `iconSize` INTEGER NOT NULL,
                        `textColor` TEXT NOT NULL,
                        `textSize` INTEGER NOT NULL,
                        `showLabel` INTEGER NOT NULL,
                        `singleLineLabel` INTEGER NOT NULL,
                        `horizontalAlignment` TEXT NOT NULL,
                        `verticalArrangement` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
            """.trimIndent(),
        )

        db.execSQL(
            """
                    INSERT INTO `ApplicationInfoGridItemEntity_new`
                    SELECT `id`, `folderId`, `page`, `startColumn`, `startRow`,
                           `columnSpan`, `rowSpan`, `associate`,
                           COALESCE(`componentName`, '') AS `componentName`,
                           `packageName`, `icon`, `label`, `override`, `serialNumber`,
                           `iconSize`, `textColor`, `textSize`, `showLabel`, `singleLineLabel`,
                           `horizontalAlignment`, `verticalArrangement`
                    FROM `ApplicationInfoGridItemEntity`
            """.trimIndent(),
        )

        db.execSQL("DROP TABLE `ApplicationInfoGridItemEntity`")
        db.execSQL("ALTER TABLE `ApplicationInfoGridItemEntity_new` RENAME TO `ApplicationInfoGridItemEntity`")
    }

    private fun updateWidgetGridItemEntities(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                    CREATE TABLE IF NOT EXISTS `WidgetGridItemEntity_new` (
                        `id` TEXT NOT NULL,
                        `folderId` TEXT,
                        `page` INTEGER NOT NULL,
                        `startColumn` INTEGER NOT NULL,
                        `startRow` INTEGER NOT NULL,
                        `columnSpan` INTEGER NOT NULL,
                        `rowSpan` INTEGER NOT NULL,
                        `associate` TEXT NOT NULL,
                        `appWidgetId` INTEGER NOT NULL,
                        `packageName` TEXT NOT NULL,
                        `componentName` TEXT NOT NULL,
                        `configure` TEXT,
                        `minWidth` INTEGER NOT NULL,
                        `minHeight` INTEGER NOT NULL,
                        `resizeMode` INTEGER NOT NULL,
                        `minResizeWidth` INTEGER NOT NULL,
                        `minResizeHeight` INTEGER NOT NULL,
                        `maxResizeWidth` INTEGER NOT NULL,
                        `maxResizeHeight` INTEGER NOT NULL,
                        `targetCellHeight` INTEGER NOT NULL,
                        `targetCellWidth` INTEGER NOT NULL,
                        `preview` TEXT,
                        `label` TEXT,
                        `icon` TEXT,
                        `override` INTEGER NOT NULL,
                        `serialNumber` INTEGER NOT NULL,
                        `iconSize` INTEGER NOT NULL,
                        `textColor` TEXT NOT NULL,
                        `textSize` INTEGER NOT NULL,
                        `showLabel` INTEGER NOT NULL,
                        `singleLineLabel` INTEGER NOT NULL,
                        `horizontalAlignment` TEXT NOT NULL,
                        `verticalArrangement` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
            """.trimIndent(),
        )

        db.execSQL(
            """
                    INSERT INTO `WidgetGridItemEntity_new`
                    SELECT `id`, `folderId`, `page`, `startColumn`, `startRow`,
                           `columnSpan`, `rowSpan`, `associate`, `appWidgetId`,
                           `packageName`, `componentName`, `configure`,
                           `minWidth`, `minHeight`, `resizeMode`, `minResizeWidth`, `minResizeHeight`,
                           `maxResizeWidth`, `maxResizeHeight`, `targetCellHeight`, `targetCellWidth`,
                           `preview`, `label`, `icon`, `override`, `serialNumber`,
                           `iconSize`, `textColor`, `textSize`, `showLabel`, `singleLineLabel`,
                           `horizontalAlignment`, `verticalArrangement`
                    FROM `WidgetGridItemEntity`
            """.trimIndent(),
        )

        db.execSQL("DROP TABLE `WidgetGridItemEntity`")
        db.execSQL("ALTER TABLE `WidgetGridItemEntity_new` RENAME TO `WidgetGridItemEntity`")
    }
}
