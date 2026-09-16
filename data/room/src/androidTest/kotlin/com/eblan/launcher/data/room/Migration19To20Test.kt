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
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.eblan.launcher.data.room.migration.Migration19To20
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class Migration19To20Test {
    private val testDatabase = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EblanDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migrate19To20_eblanApplicationInfoEntity() {
        helper.createDatabase(testDatabase, 19).use { db ->
            db.execSQL(
                """
                INSERT INTO EblanApplicationInfoTagEntity (id, name)
                VALUES (1, 'Work')
                """.trimIndent(),
            )

            db.execSQL(
                """
                INSERT INTO EblanApplicationInfoEntity (
                    componentName, serialNumber, packageName, icon, label,
                    customIcon, customLabel, isHidden, lastUpdateTime, `index`, flags
                ) VALUES (
                    'com.example.app/.MainActivity', 1001, 'com.example.app', 'icon.png', 'Example App',
                    'custom_icon.png', 'Custom App', 1, 123456, 7, 99
                )
                """.trimIndent(),
            )

            db.execSQL(
                """
                INSERT INTO EblanApplicationInfoTagCrossRefEntity (
                    componentName, serialNumber, id
                ) VALUES (
                    'com.example.app/.MainActivity', 1001, 1
                )
                """.trimIndent(),
            )
        }

        helper.runMigrationsAndValidate(
            testDatabase,
            20,
            true,
            Migration19To20(),
        ).use { db ->
            db.query(
                """
                SELECT * FROM EblanApplicationInfoEntity
                WHERE componentName = 'com.example.app/.MainActivity' AND serialNumber = 1001
                """.trimIndent(),
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())

                assertEquals(
                    "com.example.app/.MainActivity",
                    cursor.getString(cursor.getColumnIndexOrThrow("componentName")),
                )
                assertEquals(
                    1001L,
                    cursor.getLong(cursor.getColumnIndexOrThrow("serialNumber")),
                )
                assertEquals(
                    "com.example.app",
                    cursor.getString(cursor.getColumnIndexOrThrow("packageName")),
                )
                assertEquals(
                    "icon.png",
                    cursor.getString(cursor.getColumnIndexOrThrow("icon")),
                )
                assertEquals(
                    "Example App",
                    cursor.getString(cursor.getColumnIndexOrThrow("label")),
                )
                assertEquals(
                    "custom_icon.png",
                    cursor.getString(cursor.getColumnIndexOrThrow("customIcon")),
                )
                assertEquals(
                    "Custom App",
                    cursor.getString(cursor.getColumnIndexOrThrow("customLabel")),
                )
                assertEquals(
                    1,
                    cursor.getInt(cursor.getColumnIndexOrThrow("isHidden")),
                )
                assertEquals(
                    123456L,
                    cursor.getLong(cursor.getColumnIndexOrThrow("lastUpdateTime")),
                )
                assertEquals(
                    99,
                    cursor.getInt(cursor.getColumnIndexOrThrow("flags")),
                )
                assertEquals(
                    7,
                    cursor.getInt(cursor.getColumnIndexOrThrow("folderIndex")),
                )
                assertNull(cursor.getString(cursor.getColumnIndexOrThrow("folderId")))
            }

            db.query("SELECT * FROM EblanApplicationInfoTagEntity WHERE id = 1").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("Work", cursor.getString(cursor.getColumnIndexOrThrow("name")))
                assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("index")))
            }

            db.query(
                """
                SELECT * FROM EblanApplicationInfoTagCrossRefEntity
                WHERE componentName = 'com.example.app/.MainActivity'
                    AND serialNumber = 1001
                    AND id = 1
                """.trimIndent(),
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(
                    "com.example.app/.MainActivity",
                    cursor.getString(cursor.getColumnIndexOrThrow("componentName")),
                )
                assertEquals(
                    1001L,
                    cursor.getLong(cursor.getColumnIndexOrThrow("serialNumber")),
                )
                assertEquals(
                    1L,
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                )
            }
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate19To20_folderEblanApplicationInfoEntity() {
        helper.createDatabase(testDatabase, 19).close()

        helper.runMigrationsAndValidate(
            testDatabase,
            20,
            true,
            Migration19To20(),
        ).use { db ->
            db.execSQL(
                """
                INSERT INTO FolderEblanApplicationInfoEntity (
                    id, icon, label, `index`, folderIndex, folderId
                ) VALUES (
                    'folder_1', 'folder_icon.png', 'Folder', 3, 4, NULL
                )
                """.trimIndent(),
            )

            db.query("SELECT * FROM FolderEblanApplicationInfoEntity WHERE id = 'folder_1'").use { cursor ->
                assertTrue(cursor.moveToFirst())

                assertEquals(
                    "folder_1",
                    cursor.getString(cursor.getColumnIndexOrThrow("id")),
                )
                assertEquals(
                    "folder_icon.png",
                    cursor.getString(cursor.getColumnIndexOrThrow("icon")),
                )
                assertEquals(
                    "Folder",
                    cursor.getString(cursor.getColumnIndexOrThrow("label")),
                )
                assertEquals(
                    3,
                    cursor.getInt(cursor.getColumnIndexOrThrow("index")),
                )
                assertEquals(
                    4,
                    cursor.getInt(cursor.getColumnIndexOrThrow("folderIndex")),
                )
                assertNull(cursor.getString(cursor.getColumnIndexOrThrow("folderId")))
            }
        }
    }
}
