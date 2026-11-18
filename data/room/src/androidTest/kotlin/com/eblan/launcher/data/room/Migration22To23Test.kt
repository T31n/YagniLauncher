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
package com.eblan.launcher.data.room

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.eblan.launcher.data.room.migration.Migration22To23
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class Migration22To23Test {
    private val testDatabase = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EblanDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migrate22To23_applicationInfoGridItemEntity() {
        helper.createDatabase(testDatabase, 22).use { db ->
            insertFolderParent(db = db)

            db.execSQL(
                """
                INSERT INTO ApplicationInfoGridItemEntity (
                    id, page, startColumn, startRow, columnSpan, rowSpan, associate,
                    componentName, packageName, icon, label, `override`, serialNumber,
                    customIcon, customLabel, `index`, folderId, iconSize, textColor, textSize,
                    singleLineLabel, horizontalAlignment, verticalArrangement,
                    customTextColor, customBackgroundColor, padding, cornerRadius,
                    layoutType, horizontalArrangement, verticalAlignment,
                    iconPadding, textPadding,
                    doubleTap_eblanActionType, doubleTap_serialNumber, doubleTap_componentName,
                    swipeUp_eblanActionType, swipeUp_serialNumber, swipeUp_componentName,
                    swipeDown_eblanActionType, swipeDown_serialNumber, swipeDown_componentName
                ) VALUES (
                    'app_item', 2, 3, 4, 5, 6, 'app_associate',
                    'com.example.app/.MainActivity', 'com.example.app', 'app_icon', 'App label',
                    7, 8, 'app_custom_icon', 'App custom label', 9, 'folder_parent',
                    10, '#123456', 11, 12, 'End', 'Bottom', 13, 14, 15, 16,
                    'TopIconBottomLabel', 'Start', 'Top', 0, 0,
                    'double_app_action', 17, 'com.example.double/.Activity',
                    'up_app_action', 18, 'com.example.up/.Activity',
                    'down_app_action', 19, 'com.example.down/.Activity'
                )
                """.trimIndent(),
            )
        }

        helper.runMigrationsAndValidate(
            testDatabase,
            23,
            true,
            Migration22To23(),
        ).use { db ->
            db.query("SELECT * FROM ApplicationInfoGridItemEntity WHERE id = 'app_item'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("app_item", cursor.getString(cursor.getColumnIndexOrThrow("id")))
                assertEquals(2, cursor.getInt(cursor.getColumnIndexOrThrow("page")))
                assertEquals(3, cursor.getInt(cursor.getColumnIndexOrThrow("startColumn")))
                assertEquals(4, cursor.getInt(cursor.getColumnIndexOrThrow("startRow")))
                assertEquals(5, cursor.getInt(cursor.getColumnIndexOrThrow("columnSpan")))
                assertEquals(6, cursor.getInt(cursor.getColumnIndexOrThrow("rowSpan")))
                assertEquals("app_associate", cursor.getString(cursor.getColumnIndexOrThrow("associate")))
                assertEquals(
                    "com.example.app/.MainActivity",
                    cursor.getString(cursor.getColumnIndexOrThrow("componentName")),
                )
                assertEquals("com.example.app", cursor.getString(cursor.getColumnIndexOrThrow("packageName")))
                assertEquals("app_icon", cursor.getString(cursor.getColumnIndexOrThrow("icon")))
                assertEquals("App label", cursor.getString(cursor.getColumnIndexOrThrow("label")))
                assertEquals(7, cursor.getInt(cursor.getColumnIndexOrThrow("override")))
                assertEquals(8, cursor.getInt(cursor.getColumnIndexOrThrow("serialNumber")))
                assertEquals("app_custom_icon", cursor.getString(cursor.getColumnIndexOrThrow("customIcon")))
                assertEquals("App custom label", cursor.getString(cursor.getColumnIndexOrThrow("customLabel")))
                assertEquals(9, cursor.getInt(cursor.getColumnIndexOrThrow("index")))
                assertEquals("folder_parent", cursor.getString(cursor.getColumnIndexOrThrow("folderId")))
                assertEquals(10, cursor.getInt(cursor.getColumnIndexOrThrow("iconSize")))
                assertEquals("#123456", cursor.getString(cursor.getColumnIndexOrThrow("textColor")))
                assertEquals(11, cursor.getInt(cursor.getColumnIndexOrThrow("textSize")))
                assertEquals(12, cursor.getInt(cursor.getColumnIndexOrThrow("singleLineLabel")))
                assertEquals("End", cursor.getString(cursor.getColumnIndexOrThrow("horizontalAlignment")))
                assertEquals("Bottom", cursor.getString(cursor.getColumnIndexOrThrow("verticalArrangement")))
                assertEquals(13, cursor.getInt(cursor.getColumnIndexOrThrow("customTextColor")))
                assertEquals("System", cursor.getString(cursor.getColumnIndexOrThrow("backgroundColor")))
                assertEquals(14, cursor.getInt(cursor.getColumnIndexOrThrow("customBackgroundColor")))
                assertEquals(15, cursor.getInt(cursor.getColumnIndexOrThrow("padding")))
                assertEquals(16, cursor.getInt(cursor.getColumnIndexOrThrow("cornerRadius")))
                assertEquals("TopIconBottomLabel", cursor.getString(cursor.getColumnIndexOrThrow("layoutType")))
                assertEquals("Start", cursor.getString(cursor.getColumnIndexOrThrow("horizontalArrangement")))
                assertEquals("Top", cursor.getString(cursor.getColumnIndexOrThrow("verticalAlignment")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("iconPadding")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("textPadding")))
                assertEquals("double_app_action", cursor.getString(cursor.getColumnIndexOrThrow("doubleTap_eblanActionType")))
                assertEquals(17, cursor.getInt(cursor.getColumnIndexOrThrow("doubleTap_serialNumber")))
                assertEquals(
                    "com.example.double/.Activity",
                    cursor.getString(cursor.getColumnIndexOrThrow("doubleTap_componentName")),
                )
                assertEquals("up_app_action", cursor.getString(cursor.getColumnIndexOrThrow("swipeUp_eblanActionType")))
                assertEquals(18, cursor.getInt(cursor.getColumnIndexOrThrow("swipeUp_serialNumber")))
                assertEquals(
                    "com.example.up/.Activity",
                    cursor.getString(cursor.getColumnIndexOrThrow("swipeUp_componentName")),
                )
                assertEquals("down_app_action", cursor.getString(cursor.getColumnIndexOrThrow("swipeDown_eblanActionType")))
                assertEquals(19, cursor.getInt(cursor.getColumnIndexOrThrow("swipeDown_serialNumber")))
                assertEquals(
                    "com.example.down/.Activity",
                    cursor.getString(cursor.getColumnIndexOrThrow("swipeDown_componentName")),
                )
            }
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate22To23_folderGridItemEntity() {
        helper.createDatabase(testDatabase, 22).use { db ->
            insertFolderParent(db = db)

            db.execSQL(
                """
                INSERT INTO FolderGridItemEntity (
                    id, page, startColumn, startRow, columnSpan, rowSpan, associate,
                    label, `override`, icon, `index`, folderId, iconSize, textColor, textSize,
                    singleLineLabel, horizontalAlignment, verticalArrangement,
                    customTextColor, customBackgroundColor, padding, cornerRadius,
                    layoutType, horizontalArrangement, verticalAlignment,
                    iconPadding, textPadding,
                    doubleTap_eblanActionType, doubleTap_serialNumber, doubleTap_componentName,
                    swipeUp_eblanActionType, swipeUp_serialNumber, swipeUp_componentName,
                    swipeDown_eblanActionType, swipeDown_serialNumber, swipeDown_componentName
                ) VALUES (
                    'folder_child', 22, 23, 24, 25, 26, 'folder_associate',
                    'Folder child label', 27, 'folder_child_icon', 28, 'folder_parent',
                    29, '#223344', 30, 31, 'Center', 'Center', 32, 33, 34, 35,
                    'IconOnly', 'Start', 'Top', 0, 0,
                    'double_folder_action', 36, 'com.example.folder.double/.Activity',
                    'up_folder_action', 37, 'com.example.folder.up/.Activity',
                    'down_folder_action', 38, 'com.example.folder.down/.Activity'
                )
                """.trimIndent(),
            )
        }

        helper.runMigrationsAndValidate(
            testDatabase,
            23,
            true,
            Migration22To23(),
        ).use { db ->
            db.query("SELECT * FROM FolderGridItemEntity WHERE id = 'folder_child'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("folder_child", cursor.getString(cursor.getColumnIndexOrThrow("id")))
                assertEquals(22, cursor.getInt(cursor.getColumnIndexOrThrow("page")))
                assertEquals(23, cursor.getInt(cursor.getColumnIndexOrThrow("startColumn")))
                assertEquals(24, cursor.getInt(cursor.getColumnIndexOrThrow("startRow")))
                assertEquals(25, cursor.getInt(cursor.getColumnIndexOrThrow("columnSpan")))
                assertEquals(26, cursor.getInt(cursor.getColumnIndexOrThrow("rowSpan")))
                assertEquals("folder_associate", cursor.getString(cursor.getColumnIndexOrThrow("associate")))
                assertEquals("Folder child label", cursor.getString(cursor.getColumnIndexOrThrow("label")))
                assertEquals(27, cursor.getInt(cursor.getColumnIndexOrThrow("override")))
                assertEquals("folder_child_icon", cursor.getString(cursor.getColumnIndexOrThrow("icon")))
                assertEquals(28, cursor.getInt(cursor.getColumnIndexOrThrow("index")))
                assertEquals("folder_parent", cursor.getString(cursor.getColumnIndexOrThrow("folderId")))
                assertEquals(29, cursor.getInt(cursor.getColumnIndexOrThrow("iconSize")))
                assertEquals("#223344", cursor.getString(cursor.getColumnIndexOrThrow("textColor")))
                assertEquals(30, cursor.getInt(cursor.getColumnIndexOrThrow("textSize")))
                assertEquals(31, cursor.getInt(cursor.getColumnIndexOrThrow("singleLineLabel")))
                assertEquals("Center", cursor.getString(cursor.getColumnIndexOrThrow("horizontalAlignment")))
                assertEquals("Center", cursor.getString(cursor.getColumnIndexOrThrow("verticalArrangement")))
                assertEquals(32, cursor.getInt(cursor.getColumnIndexOrThrow("customTextColor")))
                assertEquals("System", cursor.getString(cursor.getColumnIndexOrThrow("backgroundColor")))
                assertEquals(33, cursor.getInt(cursor.getColumnIndexOrThrow("customBackgroundColor")))
                assertEquals(34, cursor.getInt(cursor.getColumnIndexOrThrow("padding")))
                assertEquals(35, cursor.getInt(cursor.getColumnIndexOrThrow("cornerRadius")))
                assertEquals("IconOnly", cursor.getString(cursor.getColumnIndexOrThrow("layoutType")))
                assertEquals("Start", cursor.getString(cursor.getColumnIndexOrThrow("horizontalArrangement")))
                assertEquals("Top", cursor.getString(cursor.getColumnIndexOrThrow("verticalAlignment")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("iconPadding")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("textPadding")))
                assertEquals("double_folder_action", cursor.getString(cursor.getColumnIndexOrThrow("doubleTap_eblanActionType")))
                assertEquals(36, cursor.getInt(cursor.getColumnIndexOrThrow("doubleTap_serialNumber")))
                assertEquals(
                    "com.example.folder.double/.Activity",
                    cursor.getString(cursor.getColumnIndexOrThrow("doubleTap_componentName")),
                )
                assertEquals("up_folder_action", cursor.getString(cursor.getColumnIndexOrThrow("swipeUp_eblanActionType")))
                assertEquals(37, cursor.getInt(cursor.getColumnIndexOrThrow("swipeUp_serialNumber")))
                assertEquals(
                    "com.example.folder.up/.Activity",
                    cursor.getString(cursor.getColumnIndexOrThrow("swipeUp_componentName")),
                )
                assertEquals("down_folder_action", cursor.getString(cursor.getColumnIndexOrThrow("swipeDown_eblanActionType")))
                assertEquals(38, cursor.getInt(cursor.getColumnIndexOrThrow("swipeDown_serialNumber")))
                assertEquals(
                    "com.example.folder.down/.Activity",
                    cursor.getString(cursor.getColumnIndexOrThrow("swipeDown_componentName")),
                )
            }
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate22To23_shortcutConfigGridItemEntity() {
        helper.createDatabase(testDatabase, 22).use { db ->
            insertFolderParent(db = db)

            db.execSQL(
                """
                INSERT INTO ShortcutConfigGridItemEntity (
                    id, page, startColumn, startRow, columnSpan, rowSpan, associate,
                    componentName, packageName, activityIcon, activityLabel, applicationIcon,
                    applicationLabel, `override`, serialNumber, shortcutIntentName,
                    shortcutIntentIcon, shortcutIntentUri, customIcon, customLabel, `index`,
                    folderId, iconSize, textColor, textSize, singleLineLabel,
                    horizontalAlignment, verticalArrangement, customTextColor,
                    customBackgroundColor, padding, cornerRadius,
                    layoutType, horizontalArrangement, verticalAlignment,
                    iconPadding, textPadding,
                    doubleTap_eblanActionType, doubleTap_serialNumber, doubleTap_componentName,
                    swipeUp_eblanActionType, swipeUp_serialNumber, swipeUp_componentName,
                    swipeDown_eblanActionType, swipeDown_serialNumber, swipeDown_componentName
                ) VALUES (
                    'shortcut_config_item', 42, 43, 44, 45, 46, 'shortcut_config_associate',
                    'com.example.config/.Activity', 'com.example.config', 'activity_icon',
                    'Activity label', 'application_icon', 'Application label', 47, 48,
                    'intent_name', 'intent_icon', 'intent_uri', 'custom_config_icon',
                    'Custom config label', 49, 'folder_parent', 50, '#334455', 51, 52,
                    'End', 'Bottom', 53, 54, 55, 56,
                    'TopIconBottomLabel', 'Start', 'Top', 0, 0,
                    'double_config_action', 57, 'com.example.config.double/.Activity',
                    'up_config_action', 58, 'com.example.config.up/.Activity',
                    'down_config_action', 59, 'com.example.config.down/.Activity'
                )
                """.trimIndent(),
            )
        }

        helper.runMigrationsAndValidate(
            testDatabase,
            23,
            true,
            Migration22To23(),
        ).use { db ->
            db.query("SELECT * FROM ShortcutConfigGridItemEntity WHERE id = 'shortcut_config_item'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("shortcut_config_item", cursor.getString(cursor.getColumnIndexOrThrow("id")))
                assertEquals(42, cursor.getInt(cursor.getColumnIndexOrThrow("page")))
                assertEquals(43, cursor.getInt(cursor.getColumnIndexOrThrow("startColumn")))
                assertEquals(44, cursor.getInt(cursor.getColumnIndexOrThrow("startRow")))
                assertEquals(45, cursor.getInt(cursor.getColumnIndexOrThrow("columnSpan")))
                assertEquals(46, cursor.getInt(cursor.getColumnIndexOrThrow("rowSpan")))
                assertEquals("shortcut_config_associate", cursor.getString(cursor.getColumnIndexOrThrow("associate")))
                assertEquals("com.example.config/.Activity", cursor.getString(cursor.getColumnIndexOrThrow("componentName")))
                assertEquals("com.example.config", cursor.getString(cursor.getColumnIndexOrThrow("packageName")))
                assertEquals("activity_icon", cursor.getString(cursor.getColumnIndexOrThrow("activityIcon")))
                assertEquals("Activity label", cursor.getString(cursor.getColumnIndexOrThrow("activityLabel")))
                assertEquals("application_icon", cursor.getString(cursor.getColumnIndexOrThrow("applicationIcon")))
                assertEquals("Application label", cursor.getString(cursor.getColumnIndexOrThrow("applicationLabel")))
                assertEquals(47, cursor.getInt(cursor.getColumnIndexOrThrow("override")))
                assertEquals(48, cursor.getInt(cursor.getColumnIndexOrThrow("serialNumber")))
                assertEquals("intent_name", cursor.getString(cursor.getColumnIndexOrThrow("shortcutIntentName")))
                assertEquals("intent_icon", cursor.getString(cursor.getColumnIndexOrThrow("shortcutIntentIcon")))
                assertEquals("intent_uri", cursor.getString(cursor.getColumnIndexOrThrow("shortcutIntentUri")))
                assertEquals("custom_config_icon", cursor.getString(cursor.getColumnIndexOrThrow("customIcon")))
                assertEquals("Custom config label", cursor.getString(cursor.getColumnIndexOrThrow("customLabel")))
                assertEquals(49, cursor.getInt(cursor.getColumnIndexOrThrow("index")))
                assertEquals("folder_parent", cursor.getString(cursor.getColumnIndexOrThrow("folderId")))
                assertEquals(50, cursor.getInt(cursor.getColumnIndexOrThrow("iconSize")))
                assertEquals("#334455", cursor.getString(cursor.getColumnIndexOrThrow("textColor")))
                assertEquals(51, cursor.getInt(cursor.getColumnIndexOrThrow("textSize")))
                assertEquals(52, cursor.getInt(cursor.getColumnIndexOrThrow("singleLineLabel")))
                assertEquals("End", cursor.getString(cursor.getColumnIndexOrThrow("horizontalAlignment")))
                assertEquals("Bottom", cursor.getString(cursor.getColumnIndexOrThrow("verticalArrangement")))
                assertEquals(53, cursor.getInt(cursor.getColumnIndexOrThrow("customTextColor")))
                assertEquals("System", cursor.getString(cursor.getColumnIndexOrThrow("backgroundColor")))
                assertEquals(54, cursor.getInt(cursor.getColumnIndexOrThrow("customBackgroundColor")))
                assertEquals(55, cursor.getInt(cursor.getColumnIndexOrThrow("padding")))
                assertEquals(56, cursor.getInt(cursor.getColumnIndexOrThrow("cornerRadius")))
                assertEquals("TopIconBottomLabel", cursor.getString(cursor.getColumnIndexOrThrow("layoutType")))
                assertEquals("Start", cursor.getString(cursor.getColumnIndexOrThrow("horizontalArrangement")))
                assertEquals("Top", cursor.getString(cursor.getColumnIndexOrThrow("verticalAlignment")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("iconPadding")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("textPadding")))
                assertEquals("double_config_action", cursor.getString(cursor.getColumnIndexOrThrow("doubleTap_eblanActionType")))
                assertEquals(57, cursor.getInt(cursor.getColumnIndexOrThrow("doubleTap_serialNumber")))
                assertEquals(
                    "com.example.config.double/.Activity",
                    cursor.getString(cursor.getColumnIndexOrThrow("doubleTap_componentName")),
                )
                assertEquals("up_config_action", cursor.getString(cursor.getColumnIndexOrThrow("swipeUp_eblanActionType")))
                assertEquals(58, cursor.getInt(cursor.getColumnIndexOrThrow("swipeUp_serialNumber")))
                assertEquals(
                    "com.example.config.up/.Activity",
                    cursor.getString(cursor.getColumnIndexOrThrow("swipeUp_componentName")),
                )
                assertEquals("down_config_action", cursor.getString(cursor.getColumnIndexOrThrow("swipeDown_eblanActionType")))
                assertEquals(59, cursor.getInt(cursor.getColumnIndexOrThrow("swipeDown_serialNumber")))
                assertEquals(
                    "com.example.config.down/.Activity",
                    cursor.getString(cursor.getColumnIndexOrThrow("swipeDown_componentName")),
                )
            }
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate22To23_shortcutInfoGridItemEntity() {
        helper.createDatabase(testDatabase, 22).use { db ->
            insertFolderParent(db = db)

            db.execSQL(
                """
                INSERT INTO ShortcutInfoGridItemEntity (
                    id, page, startColumn, startRow, columnSpan, rowSpan, associate,
                    shortcutId, packageName, shortLabel, longLabel, icon, `override`,
                    serialNumber, isEnabled, eblanApplicationInfoIcon, customIcon,
                    customShortLabel, `index`, folderId, iconSize, textColor, textSize,
                    singleLineLabel, horizontalAlignment, verticalArrangement,
                    customTextColor, customBackgroundColor, padding, cornerRadius,
                    layoutType, horizontalArrangement, verticalAlignment,
                    iconPadding, textPadding,
                    doubleTap_eblanActionType, doubleTap_serialNumber, doubleTap_componentName,
                    swipeUp_eblanActionType, swipeUp_serialNumber, swipeUp_componentName,
                    swipeDown_eblanActionType, swipeDown_serialNumber, swipeDown_componentName
                ) VALUES (
                    'shortcut_info_item', 62, 63, 64, 65, 66, 'shortcut_info_associate',
                    'shortcut_id', 'com.example.shortcut', 'Short label', 'Long label',
                    'shortcut_icon', 67, 68, 1, 'application_info_icon', 'custom_shortcut_icon',
                    'Custom short label', 69, 'folder_parent', 70, '#445566', 71, 72,
                    'End', 'Bottom', 73, 74, 75, 76,
                    'IconOnly', 'Start', 'Top', 0, 0,
                    'double_shortcut_action', 77, 'com.example.shortcut.double/.Activity',
                    'up_shortcut_action', 78, 'com.example.shortcut.up/.Activity',
                    'down_shortcut_action', 79, 'com.example.shortcut.down/.Activity'
                )
                """.trimIndent(),
            )
        }

        helper.runMigrationsAndValidate(
            testDatabase,
            23,
            true,
            Migration22To23(),
        ).use { db ->
            db.query("SELECT * FROM ShortcutInfoGridItemEntity WHERE id = 'shortcut_info_item'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("shortcut_info_item", cursor.getString(cursor.getColumnIndexOrThrow("id")))
                assertEquals(62, cursor.getInt(cursor.getColumnIndexOrThrow("page")))
                assertEquals(63, cursor.getInt(cursor.getColumnIndexOrThrow("startColumn")))
                assertEquals(64, cursor.getInt(cursor.getColumnIndexOrThrow("startRow")))
                assertEquals(65, cursor.getInt(cursor.getColumnIndexOrThrow("columnSpan")))
                assertEquals(66, cursor.getInt(cursor.getColumnIndexOrThrow("rowSpan")))
                assertEquals("shortcut_info_associate", cursor.getString(cursor.getColumnIndexOrThrow("associate")))
                assertEquals("shortcut_id", cursor.getString(cursor.getColumnIndexOrThrow("shortcutId")))
                assertEquals("com.example.shortcut", cursor.getString(cursor.getColumnIndexOrThrow("packageName")))
                assertEquals("Short label", cursor.getString(cursor.getColumnIndexOrThrow("shortLabel")))
                assertEquals("Long label", cursor.getString(cursor.getColumnIndexOrThrow("longLabel")))
                assertEquals("shortcut_icon", cursor.getString(cursor.getColumnIndexOrThrow("icon")))
                assertEquals(67, cursor.getInt(cursor.getColumnIndexOrThrow("override")))
                assertEquals(68, cursor.getInt(cursor.getColumnIndexOrThrow("serialNumber")))
                assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("isEnabled")))
                assertEquals(
                    "application_info_icon",
                    cursor.getString(cursor.getColumnIndexOrThrow("eblanApplicationInfoIcon")),
                )
                assertEquals("custom_shortcut_icon", cursor.getString(cursor.getColumnIndexOrThrow("customIcon")))
                assertEquals("Custom short label", cursor.getString(cursor.getColumnIndexOrThrow("customShortLabel")))
                assertEquals(69, cursor.getInt(cursor.getColumnIndexOrThrow("index")))
                assertEquals("folder_parent", cursor.getString(cursor.getColumnIndexOrThrow("folderId")))
                assertEquals(70, cursor.getInt(cursor.getColumnIndexOrThrow("iconSize")))
                assertEquals("#445566", cursor.getString(cursor.getColumnIndexOrThrow("textColor")))
                assertEquals(71, cursor.getInt(cursor.getColumnIndexOrThrow("textSize")))
                assertEquals(72, cursor.getInt(cursor.getColumnIndexOrThrow("singleLineLabel")))
                assertEquals("End", cursor.getString(cursor.getColumnIndexOrThrow("horizontalAlignment")))
                assertEquals("Bottom", cursor.getString(cursor.getColumnIndexOrThrow("verticalArrangement")))
                assertEquals(73, cursor.getInt(cursor.getColumnIndexOrThrow("customTextColor")))
                assertEquals("System", cursor.getString(cursor.getColumnIndexOrThrow("backgroundColor")))
                assertEquals(74, cursor.getInt(cursor.getColumnIndexOrThrow("customBackgroundColor")))
                assertEquals(75, cursor.getInt(cursor.getColumnIndexOrThrow("padding")))
                assertEquals(76, cursor.getInt(cursor.getColumnIndexOrThrow("cornerRadius")))
                assertEquals("IconOnly", cursor.getString(cursor.getColumnIndexOrThrow("layoutType")))
                assertEquals("Start", cursor.getString(cursor.getColumnIndexOrThrow("horizontalArrangement")))
                assertEquals("Top", cursor.getString(cursor.getColumnIndexOrThrow("verticalAlignment")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("iconPadding")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("textPadding")))
                assertEquals("double_shortcut_action", cursor.getString(cursor.getColumnIndexOrThrow("doubleTap_eblanActionType")))
                assertEquals(77, cursor.getInt(cursor.getColumnIndexOrThrow("doubleTap_serialNumber")))
                assertEquals(
                    "com.example.shortcut.double/.Activity",
                    cursor.getString(cursor.getColumnIndexOrThrow("doubleTap_componentName")),
                )
                assertEquals("up_shortcut_action", cursor.getString(cursor.getColumnIndexOrThrow("swipeUp_eblanActionType")))
                assertEquals(78, cursor.getInt(cursor.getColumnIndexOrThrow("swipeUp_serialNumber")))
                assertEquals(
                    "com.example.shortcut.up/.Activity",
                    cursor.getString(cursor.getColumnIndexOrThrow("swipeUp_componentName")),
                )
                assertEquals(
                    "down_shortcut_action",
                    cursor.getString(cursor.getColumnIndexOrThrow("swipeDown_eblanActionType")),
                )
                assertEquals(79, cursor.getInt(cursor.getColumnIndexOrThrow("swipeDown_serialNumber")))
                assertEquals(
                    "com.example.shortcut.down/.Activity",
                    cursor.getString(cursor.getColumnIndexOrThrow("swipeDown_componentName")),
                )
            }
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate22To23_widgetGridItemEntity() {
        helper.createDatabase(testDatabase, 22).use { db ->
            db.execSQL(
                """
                INSERT INTO WidgetGridItemEntity (
                    id, page, startColumn, startRow, columnSpan, rowSpan, associate,
                    appWidgetId, packageName, componentName, configure, minWidth, minHeight,
                    resizeMode, minResizeWidth, minResizeHeight, maxResizeWidth, maxResizeHeight,
                    targetCellHeight, targetCellWidth, preview, label, icon, `override`,
                    serialNumber, iconSize, textColor, textSize, singleLineLabel,
                    horizontalAlignment, verticalArrangement, customTextColor,
                    customBackgroundColor, padding, cornerRadius,
                    layoutType, horizontalArrangement, verticalAlignment,
                    iconPadding, textPadding
                ) VALUES (
                    'widget_item', 82, 83, 84, 85, 86, 'widget_associate',
                    87, 'com.example.widget', 'com.example.widget/.Provider', 'widget_config',
                    88, 89, 90, 91, 92, 93, 94, 95, 96, 'widget_preview',
                    'Widget label', 'widget_icon', 97, 98, 99, '#556677', 100, 101,
                    'End', 'Bottom', 102, 103, 104, 105,
                    'TopIconBottomLabel', 'Start', 'Top', 0, 0
                )
                """.trimIndent(),
            )
        }

        helper.runMigrationsAndValidate(
            testDatabase,
            23,
            true,
            Migration22To23(),
        ).use { db ->
            db.query("SELECT * FROM WidgetGridItemEntity WHERE id = 'widget_item'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("widget_item", cursor.getString(cursor.getColumnIndexOrThrow("id")))
                assertEquals(82, cursor.getInt(cursor.getColumnIndexOrThrow("page")))
                assertEquals(83, cursor.getInt(cursor.getColumnIndexOrThrow("startColumn")))
                assertEquals(84, cursor.getInt(cursor.getColumnIndexOrThrow("startRow")))
                assertEquals(85, cursor.getInt(cursor.getColumnIndexOrThrow("columnSpan")))
                assertEquals(86, cursor.getInt(cursor.getColumnIndexOrThrow("rowSpan")))
                assertEquals("widget_associate", cursor.getString(cursor.getColumnIndexOrThrow("associate")))
                assertEquals(87, cursor.getInt(cursor.getColumnIndexOrThrow("appWidgetId")))
                assertEquals("com.example.widget", cursor.getString(cursor.getColumnIndexOrThrow("packageName")))
                assertEquals(
                    "com.example.widget/.Provider",
                    cursor.getString(cursor.getColumnIndexOrThrow("componentName")),
                )
                assertEquals("widget_config", cursor.getString(cursor.getColumnIndexOrThrow("configure")))
                assertEquals(88, cursor.getInt(cursor.getColumnIndexOrThrow("minWidth")))
                assertEquals(89, cursor.getInt(cursor.getColumnIndexOrThrow("minHeight")))
                assertEquals(90, cursor.getInt(cursor.getColumnIndexOrThrow("resizeMode")))
                assertEquals(91, cursor.getInt(cursor.getColumnIndexOrThrow("minResizeWidth")))
                assertEquals(92, cursor.getInt(cursor.getColumnIndexOrThrow("minResizeHeight")))
                assertEquals(93, cursor.getInt(cursor.getColumnIndexOrThrow("maxResizeWidth")))
                assertEquals(94, cursor.getInt(cursor.getColumnIndexOrThrow("maxResizeHeight")))
                assertEquals(95, cursor.getInt(cursor.getColumnIndexOrThrow("targetCellHeight")))
                assertEquals(96, cursor.getInt(cursor.getColumnIndexOrThrow("targetCellWidth")))
                assertEquals("widget_preview", cursor.getString(cursor.getColumnIndexOrThrow("preview")))
                assertEquals("Widget label", cursor.getString(cursor.getColumnIndexOrThrow("label")))
                assertEquals("widget_icon", cursor.getString(cursor.getColumnIndexOrThrow("icon")))
                assertEquals(97, cursor.getInt(cursor.getColumnIndexOrThrow("override")))
                assertEquals(98, cursor.getInt(cursor.getColumnIndexOrThrow("serialNumber")))
                assertEquals(99, cursor.getInt(cursor.getColumnIndexOrThrow("iconSize")))
                assertEquals("#556677", cursor.getString(cursor.getColumnIndexOrThrow("textColor")))
                assertEquals(100, cursor.getInt(cursor.getColumnIndexOrThrow("textSize")))
                assertEquals(101, cursor.getInt(cursor.getColumnIndexOrThrow("singleLineLabel")))
                assertEquals("End", cursor.getString(cursor.getColumnIndexOrThrow("horizontalAlignment")))
                assertEquals("Bottom", cursor.getString(cursor.getColumnIndexOrThrow("verticalArrangement")))
                assertEquals(102, cursor.getInt(cursor.getColumnIndexOrThrow("customTextColor")))
                assertEquals("System", cursor.getString(cursor.getColumnIndexOrThrow("backgroundColor")))
                assertEquals(103, cursor.getInt(cursor.getColumnIndexOrThrow("customBackgroundColor")))
                assertEquals(104, cursor.getInt(cursor.getColumnIndexOrThrow("padding")))
                assertEquals(105, cursor.getInt(cursor.getColumnIndexOrThrow("cornerRadius")))
                assertEquals("TopIconBottomLabel", cursor.getString(cursor.getColumnIndexOrThrow("layoutType")))
                assertEquals("Start", cursor.getString(cursor.getColumnIndexOrThrow("horizontalArrangement")))
                assertEquals("Top", cursor.getString(cursor.getColumnIndexOrThrow("verticalAlignment")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("iconPadding")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("textPadding")))
            }
        }
    }

    private fun insertFolderParent(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO FolderGridItemEntity (
                id, page, startColumn, startRow, columnSpan, rowSpan, associate, label,
                `override`, icon, `index`, folderId, iconSize, textColor, textSize,
                singleLineLabel, horizontalAlignment, verticalArrangement, customTextColor,
                customBackgroundColor, padding, cornerRadius, layoutType, horizontalArrangement,
                verticalAlignment, iconPadding, textPadding, doubleTap_eblanActionType,
                doubleTap_serialNumber, doubleTap_componentName, swipeUp_eblanActionType,
                swipeUp_serialNumber, swipeUp_componentName, swipeDown_eblanActionType,
                swipeDown_serialNumber, swipeDown_componentName
            ) VALUES (
                'folder_parent', 1, 1, 1, 1, 1, 'folder_parent_associate',
                'Folder parent', 1, 'folder_parent_icon', 1, NULL, 1, '#000000', 1,
                1, 'Start', 'Top', 1, 1, 1, 1, 'TopIconBottomLabel', 'Start',
                'Top', 0, 0, 'parent_double_action', 1,
                'com.example.parent.double/.Activity', 'parent_up_action', 1,
                'com.example.parent.up/.Activity', 'parent_down_action', 1,
                'com.example.parent.down/.Activity'
            )
            """.trimIndent(),
        )
    }
}
