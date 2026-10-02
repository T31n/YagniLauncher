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

class Migration20To21 : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
        listOf(
            "ApplicationInfoGridItemEntity",
            "FolderGridItemEntity",
            "ShortcutConfigGridItemEntity",
            "ShortcutInfoGridItemEntity",
            "WidgetGridItemEntity",
        ).forEach { table ->
            db.execSQL("CREATE TABLE `${table}_backup` AS SELECT * FROM `$table`")
        }

        db.execSQL("DROP TABLE `ApplicationInfoGridItemEntity`")
        db.execSQL("DROP TABLE `ShortcutConfigGridItemEntity`")
        db.execSQL("DROP TABLE `ShortcutInfoGridItemEntity`")
        db.execSQL("DROP TABLE `WidgetGridItemEntity`")
        db.execSQL("DROP TABLE `FolderGridItemEntity`")

        db.execSQL(
            """
            CREATE TABLE `FolderGridItemEntity` (
                `id` TEXT NOT NULL,
                `page` INTEGER NOT NULL,
                `startColumn` INTEGER NOT NULL,
                `startRow` INTEGER NOT NULL,
                `columnSpan` INTEGER NOT NULL,
                `rowSpan` INTEGER NOT NULL,
                `associate` TEXT NOT NULL,
                `label` TEXT NOT NULL,
                `override` INTEGER NOT NULL,
                `icon` TEXT,
                `index` INTEGER NOT NULL,
                `folderId` TEXT,
                `iconSize` INTEGER NOT NULL,
                `textColor` TEXT NOT NULL,
                `textSize` INTEGER NOT NULL,
                `singleLineLabel` INTEGER NOT NULL,
                `horizontalAlignment` TEXT NOT NULL,
                `verticalArrangement` TEXT NOT NULL,
                `customTextColor` INTEGER NOT NULL,
                `customBackgroundColor` INTEGER NOT NULL,
                `padding` INTEGER NOT NULL,
                `cornerRadius` INTEGER NOT NULL,
                `layoutType` TEXT NOT NULL,
                `horizontalArrangement` TEXT NOT NULL,
                `verticalAlignment` TEXT NOT NULL,
                `iconPadding` INTEGER NOT NULL,
                `textPadding` INTEGER NOT NULL,
                `doubleTap_eblanActionType` TEXT NOT NULL,
                `doubleTap_serialNumber` INTEGER NOT NULL,
                `doubleTap_componentName` TEXT NOT NULL,
                `swipeUp_eblanActionType` TEXT NOT NULL,
                `swipeUp_serialNumber` INTEGER NOT NULL,
                `swipeUp_componentName` TEXT NOT NULL,
                `swipeDown_eblanActionType` TEXT NOT NULL,
                `swipeDown_serialNumber` INTEGER NOT NULL,
                `swipeDown_componentName` TEXT NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`folderId`) REFERENCES `FolderGridItemEntity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            INSERT INTO `FolderGridItemEntity` (
                `id`, `page`, `startColumn`, `startRow`, `columnSpan`, `rowSpan`,
                `associate`, `label`, `override`, `icon`, `index`, `folderId`,
                `iconSize`, `textColor`, `textSize`, `singleLineLabel`,
                `horizontalAlignment`, `verticalArrangement`, `customTextColor`,
                `customBackgroundColor`, `padding`, `cornerRadius`, `layoutType`,
                `horizontalArrangement`, `verticalAlignment`, `iconPadding`, `textPadding`,
                `doubleTap_eblanActionType`, `doubleTap_serialNumber`, `doubleTap_componentName`,
                `swipeUp_eblanActionType`, `swipeUp_serialNumber`, `swipeUp_componentName`,
                `swipeDown_eblanActionType`, `swipeDown_serialNumber`, `swipeDown_componentName`
            )
            SELECT
                `id`, `page`, `startColumn`, `startRow`, `columnSpan`, `rowSpan`,
                `associate`, `label`, `override`, `icon`, `index`, NULL,
                `iconSize`, `textColor`, `textSize`, `singleLineLabel`,
                `horizontalAlignment`, `verticalArrangement`, `customTextColor`,
                `customBackgroundColor`, `padding`, `cornerRadius`,
                CASE WHEN `showLabel` = 0 THEN 'IconOnly' ELSE 'TopIconBottomLabel' END,
                'Start', 'Top', 0, 0,
                `doubleTap_eblanActionType`, `doubleTap_serialNumber`, `doubleTap_componentName`,
                `swipeUp_eblanActionType`, `swipeUp_serialNumber`, `swipeUp_componentName`,
                `swipeDown_eblanActionType`, `swipeDown_serialNumber`, `swipeDown_componentName`
            FROM `FolderGridItemEntity_backup`
            """.trimIndent(),
        )

        db.execSQL(
            """
            UPDATE `FolderGridItemEntity`
            SET `folderId` = (
                SELECT `folderId`
                FROM `FolderGridItemEntity_backup`
                WHERE `FolderGridItemEntity_backup`.`id` = `FolderGridItemEntity`.`id`
            )
            WHERE `id` IN (
                SELECT `id` FROM `FolderGridItemEntity_backup` WHERE `folderId` IS NOT NULL
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE `ApplicationInfoGridItemEntity` (
                `id` TEXT NOT NULL,
                `page` INTEGER NOT NULL,
                `startColumn` INTEGER NOT NULL,
                `startRow` INTEGER NOT NULL,
                `columnSpan` INTEGER NOT NULL,
                `rowSpan` INTEGER NOT NULL,
                `associate` TEXT NOT NULL,
                `componentName` TEXT NOT NULL,
                `packageName` TEXT NOT NULL,
                `icon` TEXT,
                `label` TEXT NOT NULL,
                `override` INTEGER NOT NULL,
                `serialNumber` INTEGER NOT NULL,
                `customIcon` TEXT,
                `customLabel` TEXT,
                `index` INTEGER NOT NULL,
                `folderId` TEXT,
                `iconSize` INTEGER NOT NULL,
                `textColor` TEXT NOT NULL,
                `textSize` INTEGER NOT NULL,
                `singleLineLabel` INTEGER NOT NULL,
                `horizontalAlignment` TEXT NOT NULL,
                `verticalArrangement` TEXT NOT NULL,
                `customTextColor` INTEGER NOT NULL,
                `customBackgroundColor` INTEGER NOT NULL,
                `padding` INTEGER NOT NULL,
                `cornerRadius` INTEGER NOT NULL,
                `layoutType` TEXT NOT NULL,
                `horizontalArrangement` TEXT NOT NULL,
                `verticalAlignment` TEXT NOT NULL,
                `iconPadding` INTEGER NOT NULL,
                `textPadding` INTEGER NOT NULL,
                `doubleTap_eblanActionType` TEXT NOT NULL,
                `doubleTap_serialNumber` INTEGER NOT NULL,
                `doubleTap_componentName` TEXT NOT NULL,
                `swipeUp_eblanActionType` TEXT NOT NULL,
                `swipeUp_serialNumber` INTEGER NOT NULL,
                `swipeUp_componentName` TEXT NOT NULL,
                `swipeDown_eblanActionType` TEXT NOT NULL,
                `swipeDown_serialNumber` INTEGER NOT NULL,
                `swipeDown_componentName` TEXT NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`folderId`) REFERENCES `FolderGridItemEntity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            INSERT INTO `ApplicationInfoGridItemEntity` (
                `id`, `page`, `startColumn`, `startRow`, `columnSpan`, `rowSpan`,
                `associate`, `componentName`, `packageName`, `icon`, `label`, `override`,
                `serialNumber`, `customIcon`, `customLabel`, `index`, `folderId`,
                `iconSize`, `textColor`, `textSize`, `singleLineLabel`, `horizontalAlignment`,
                `verticalArrangement`, `customTextColor`, `customBackgroundColor`, `padding`,
                `cornerRadius`, `layoutType`, `horizontalArrangement`, `verticalAlignment`,
                `iconPadding`, `textPadding`, `doubleTap_eblanActionType`, `doubleTap_serialNumber`,
                `doubleTap_componentName`, `swipeUp_eblanActionType`, `swipeUp_serialNumber`,
                `swipeUp_componentName`, `swipeDown_eblanActionType`, `swipeDown_serialNumber`,
                `swipeDown_componentName`
            )
            SELECT
                `id`, `page`, `startColumn`, `startRow`, `columnSpan`, `rowSpan`,
                `associate`, `componentName`, `packageName`, `icon`, `label`, `override`,
                `serialNumber`, `customIcon`, `customLabel`, `index`, `folderId`,
                `iconSize`, `textColor`, `textSize`, `singleLineLabel`, `horizontalAlignment`,
                `verticalArrangement`, `customTextColor`, `customBackgroundColor`, `padding`,
                `cornerRadius`,
                CASE WHEN `showLabel` = 0 THEN 'IconOnly' ELSE 'TopIconBottomLabel' END,
                'Start', 'Top', 0, 0,
                `doubleTap_eblanActionType`, `doubleTap_serialNumber`, `doubleTap_componentName`,
                `swipeUp_eblanActionType`, `swipeUp_serialNumber`, `swipeUp_componentName`,
                `swipeDown_eblanActionType`, `swipeDown_serialNumber`, `swipeDown_componentName`
            FROM `ApplicationInfoGridItemEntity_backup`
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE `ShortcutConfigGridItemEntity` (
                `id` TEXT NOT NULL,
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
                `customIcon` TEXT,
                `customLabel` TEXT,
                `index` INTEGER NOT NULL,
                `folderId` TEXT,
                `iconSize` INTEGER NOT NULL,
                `textColor` TEXT NOT NULL,
                `textSize` INTEGER NOT NULL,
                `singleLineLabel` INTEGER NOT NULL,
                `horizontalAlignment` TEXT NOT NULL,
                `verticalArrangement` TEXT NOT NULL,
                `customTextColor` INTEGER NOT NULL,
                `customBackgroundColor` INTEGER NOT NULL,
                `padding` INTEGER NOT NULL,
                `cornerRadius` INTEGER NOT NULL,
                `layoutType` TEXT NOT NULL,
                `horizontalArrangement` TEXT NOT NULL,
                `verticalAlignment` TEXT NOT NULL,
                `iconPadding` INTEGER NOT NULL,
                `textPadding` INTEGER NOT NULL,
                `doubleTap_eblanActionType` TEXT NOT NULL,
                `doubleTap_serialNumber` INTEGER NOT NULL,
                `doubleTap_componentName` TEXT NOT NULL,
                `swipeUp_eblanActionType` TEXT NOT NULL,
                `swipeUp_serialNumber` INTEGER NOT NULL,
                `swipeUp_componentName` TEXT NOT NULL,
                `swipeDown_eblanActionType` TEXT NOT NULL,
                `swipeDown_serialNumber` INTEGER NOT NULL,
                `swipeDown_componentName` TEXT NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`folderId`) REFERENCES `FolderGridItemEntity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            INSERT INTO `ShortcutConfigGridItemEntity` (
                `id`, `page`, `startColumn`, `startRow`, `columnSpan`, `rowSpan`, `associate`,
                `componentName`, `packageName`, `activityIcon`, `activityLabel`, `applicationIcon`,
                `applicationLabel`, `override`, `serialNumber`, `shortcutIntentName`,
                `shortcutIntentIcon`, `shortcutIntentUri`, `customIcon`, `customLabel`, `index`,
                `folderId`, `iconSize`, `textColor`, `textSize`, `singleLineLabel`,
                `horizontalAlignment`, `verticalArrangement`, `customTextColor`,
                `customBackgroundColor`, `padding`, `cornerRadius`, `layoutType`,
                `horizontalArrangement`, `verticalAlignment`, `iconPadding`, `textPadding`,
                `doubleTap_eblanActionType`, `doubleTap_serialNumber`, `doubleTap_componentName`,
                `swipeUp_eblanActionType`, `swipeUp_serialNumber`, `swipeUp_componentName`,
                `swipeDown_eblanActionType`, `swipeDown_serialNumber`, `swipeDown_componentName`
            )
            SELECT
                `id`, `page`, `startColumn`, `startRow`, `columnSpan`, `rowSpan`, `associate`,
                `componentName`, `packageName`, `activityIcon`, `activityLabel`, `applicationIcon`,
                `applicationLabel`, `override`, `serialNumber`, `shortcutIntentName`,
                `shortcutIntentIcon`, `shortcutIntentUri`, `customIcon`, `customLabel`, `index`,
                `folderId`, `iconSize`, `textColor`, `textSize`, `singleLineLabel`,
                `horizontalAlignment`, `verticalArrangement`, `customTextColor`,
                `customBackgroundColor`, `padding`, `cornerRadius`,
                CASE WHEN `showLabel` = 0 THEN 'IconOnly' ELSE 'TopIconBottomLabel' END,
                'Start', 'Top', 0, 0,
                `doubleTap_eblanActionType`, `doubleTap_serialNumber`, `doubleTap_componentName`,
                `swipeUp_eblanActionType`, `swipeUp_serialNumber`, `swipeUp_componentName`,
                `swipeDown_eblanActionType`, `swipeDown_serialNumber`, `swipeDown_componentName`
            FROM `ShortcutConfigGridItemEntity_backup`
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE `ShortcutInfoGridItemEntity` (
                `id` TEXT NOT NULL,
                `page` INTEGER NOT NULL,
                `startColumn` INTEGER NOT NULL,
                `startRow` INTEGER NOT NULL,
                `columnSpan` INTEGER NOT NULL,
                `rowSpan` INTEGER NOT NULL,
                `associate` TEXT NOT NULL,
                `shortcutId` TEXT NOT NULL,
                `packageName` TEXT NOT NULL,
                `shortLabel` TEXT NOT NULL,
                `longLabel` TEXT NOT NULL,
                `icon` TEXT,
                `override` INTEGER NOT NULL,
                `serialNumber` INTEGER NOT NULL,
                `isEnabled` INTEGER NOT NULL,
                `eblanApplicationInfoIcon` TEXT,
                `customIcon` TEXT,
                `customShortLabel` TEXT,
                `index` INTEGER NOT NULL,
                `folderId` TEXT,
                `iconSize` INTEGER NOT NULL,
                `textColor` TEXT NOT NULL,
                `textSize` INTEGER NOT NULL,
                `singleLineLabel` INTEGER NOT NULL,
                `horizontalAlignment` TEXT NOT NULL,
                `verticalArrangement` TEXT NOT NULL,
                `customTextColor` INTEGER NOT NULL,
                `customBackgroundColor` INTEGER NOT NULL,
                `padding` INTEGER NOT NULL,
                `cornerRadius` INTEGER NOT NULL,
                `layoutType` TEXT NOT NULL,
                `horizontalArrangement` TEXT NOT NULL,
                `verticalAlignment` TEXT NOT NULL,
                `iconPadding` INTEGER NOT NULL,
                `textPadding` INTEGER NOT NULL,
                `doubleTap_eblanActionType` TEXT NOT NULL,
                `doubleTap_serialNumber` INTEGER NOT NULL,
                `doubleTap_componentName` TEXT NOT NULL,
                `swipeUp_eblanActionType` TEXT NOT NULL,
                `swipeUp_serialNumber` INTEGER NOT NULL,
                `swipeUp_componentName` TEXT NOT NULL,
                `swipeDown_eblanActionType` TEXT NOT NULL,
                `swipeDown_serialNumber` INTEGER NOT NULL,
                `swipeDown_componentName` TEXT NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`folderId`) REFERENCES `FolderGridItemEntity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            INSERT INTO `ShortcutInfoGridItemEntity` (
                `id`, `page`, `startColumn`, `startRow`, `columnSpan`, `rowSpan`, `associate`,
                `shortcutId`, `packageName`, `shortLabel`, `longLabel`, `icon`, `override`,
                `serialNumber`, `isEnabled`, `eblanApplicationInfoIcon`, `customIcon`,
                `customShortLabel`, `index`, `folderId`, `iconSize`, `textColor`, `textSize`,
                `singleLineLabel`, `horizontalAlignment`, `verticalArrangement`, `customTextColor`,
                `customBackgroundColor`, `padding`, `cornerRadius`, `layoutType`,
                `horizontalArrangement`, `verticalAlignment`, `iconPadding`, `textPadding`,
                `doubleTap_eblanActionType`, `doubleTap_serialNumber`, `doubleTap_componentName`,
                `swipeUp_eblanActionType`, `swipeUp_serialNumber`, `swipeUp_componentName`,
                `swipeDown_eblanActionType`, `swipeDown_serialNumber`, `swipeDown_componentName`
            )
            SELECT
                `id`, `page`, `startColumn`, `startRow`, `columnSpan`, `rowSpan`, `associate`,
                `shortcutId`, `packageName`, `shortLabel`, `longLabel`, `icon`, `override`,
                `serialNumber`, `isEnabled`, `eblanApplicationInfoIcon`, `customIcon`,
                `customShortLabel`, `index`, `folderId`, `iconSize`, `textColor`, `textSize`,
                `singleLineLabel`, `horizontalAlignment`, `verticalArrangement`, `customTextColor`,
                `customBackgroundColor`, `padding`, `cornerRadius`,
                CASE WHEN `showLabel` = 0 THEN 'IconOnly' ELSE 'TopIconBottomLabel' END,
                'Start', 'Top', 0, 0,
                `doubleTap_eblanActionType`, `doubleTap_serialNumber`, `doubleTap_componentName`,
                `swipeUp_eblanActionType`, `swipeUp_serialNumber`, `swipeUp_componentName`,
                `swipeDown_eblanActionType`, `swipeDown_serialNumber`, `swipeDown_componentName`
            FROM `ShortcutInfoGridItemEntity_backup`
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE `WidgetGridItemEntity` (
                `id` TEXT NOT NULL,
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
                `singleLineLabel` INTEGER NOT NULL,
                `horizontalAlignment` TEXT NOT NULL,
                `verticalArrangement` TEXT NOT NULL,
                `customTextColor` INTEGER NOT NULL,
                `customBackgroundColor` INTEGER NOT NULL,
                `padding` INTEGER NOT NULL,
                `cornerRadius` INTEGER NOT NULL,
                `layoutType` TEXT NOT NULL,
                `horizontalArrangement` TEXT NOT NULL,
                `verticalAlignment` TEXT NOT NULL,
                `iconPadding` INTEGER NOT NULL,
                `textPadding` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            INSERT INTO `WidgetGridItemEntity` (
                `id`, `page`, `startColumn`, `startRow`, `columnSpan`, `rowSpan`, `associate`,
                `appWidgetId`, `packageName`, `componentName`, `configure`, `minWidth`, `minHeight`,
                `resizeMode`, `minResizeWidth`, `minResizeHeight`, `maxResizeWidth`, `maxResizeHeight`,
                `targetCellHeight`, `targetCellWidth`, `preview`, `label`, `icon`, `override`,
                `serialNumber`, `iconSize`, `textColor`, `textSize`, `singleLineLabel`,
                `horizontalAlignment`, `verticalArrangement`, `customTextColor`,
                `customBackgroundColor`, `padding`, `cornerRadius`, `layoutType`,
                `horizontalArrangement`, `verticalAlignment`, `iconPadding`, `textPadding`
            )
            SELECT
                `id`, `page`, `startColumn`, `startRow`, `columnSpan`, `rowSpan`, `associate`,
                `appWidgetId`, `packageName`, `componentName`, `configure`, `minWidth`, `minHeight`,
                `resizeMode`, `minResizeWidth`, `minResizeHeight`, `maxResizeWidth`, `maxResizeHeight`,
                `targetCellHeight`, `targetCellWidth`, `preview`, `label`, `icon`, `override`,
                `serialNumber`, `iconSize`, `textColor`, `textSize`, `singleLineLabel`,
                `horizontalAlignment`, `verticalArrangement`, `customTextColor`,
                `customBackgroundColor`, `padding`, `cornerRadius`,
                CASE WHEN `showLabel` = 0 THEN 'IconOnly' ELSE 'TopIconBottomLabel' END,
                'Start', 'Top', 0, 0
            FROM `WidgetGridItemEntity_backup`
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE INDEX `index_FolderGridItemEntity_folderId`
            ON `FolderGridItemEntity` (`folderId`)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX `index_ApplicationInfoGridItemEntity_folderId`
            ON `ApplicationInfoGridItemEntity` (`folderId`)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX `index_ShortcutConfigGridItemEntity_folderId`
            ON `ShortcutConfigGridItemEntity` (`folderId`)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX `index_ShortcutInfoGridItemEntity_folderId`
            ON `ShortcutInfoGridItemEntity` (`folderId`)
            """.trimIndent(),
        )

        listOf(
            "ApplicationInfoGridItemEntity",
            "FolderGridItemEntity",
            "ShortcutConfigGridItemEntity",
            "ShortcutInfoGridItemEntity",
            "WidgetGridItemEntity",
        ).forEach { table ->
            db.execSQL("DROP TABLE `${table}_backup`")
        }
    }
}
