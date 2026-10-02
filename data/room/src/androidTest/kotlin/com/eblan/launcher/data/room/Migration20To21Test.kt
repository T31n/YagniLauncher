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
import com.eblan.launcher.data.room.migration.Migration20To21
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class Migration20To21Test {
    private val testDatabase = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EblanDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migrate20To21_gridItemSettings() {
        val gridItemTables = listOf(
            "ApplicationInfoGridItemEntity" to false,
            "FolderGridItemEntity" to true,
            "ShortcutConfigGridItemEntity" to false,
            "ShortcutInfoGridItemEntity" to true,
            "WidgetGridItemEntity" to false,
        )

        helper.createDatabase(testDatabase, 20).use { db ->
            gridItemTables.forEach { (table, showLabel) ->
                val folderId = table.takeUnless {
                    it == "FolderGridItemEntity" || it == "WidgetGridItemEntity"
                }?.let { "FolderGridItemEntity" }
                insertGridItem(db, table, showLabel, folderId = folderId)

                if (table == "FolderGridItemEntity") {
                    insertGridItem(
                        db = db,
                        table = table,
                        showLabel = showLabel,
                        id = "FolderGridItemEntity_child",
                        folderId = table,
                    )
                }
            }
        }

        helper.runMigrationsAndValidate(
            testDatabase,
            21,
            true,
            Migration20To21(),
        ).use { db ->
            gridItemTables.forEach { (table, showLabel) ->
                db.query("SELECT * FROM `$table` WHERE `id` = '$table'").use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(table, cursor.getString(cursor.getColumnIndexOrThrow("id")))
                    assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("page")))
                    assertEquals(
                        if (showLabel) "TopIconBottomLabel" else "IconOnly",
                        cursor.getString(cursor.getColumnIndexOrThrow("layoutType")),
                    )
                    assertEquals(
                        "Start",
                        cursor.getString(cursor.getColumnIndexOrThrow("horizontalArrangement")),
                    )
                    assertEquals(
                        "Top",
                        cursor.getString(cursor.getColumnIndexOrThrow("verticalAlignment")),
                    )
                    assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("iconPadding")))
                    assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("textPadding")))
                    if (table != "WidgetGridItemEntity") {
                        assertEquals(
                            if (table == "FolderGridItemEntity") null else "FolderGridItemEntity",
                            cursor.getString(cursor.getColumnIndexOrThrow("folderId")),
                        )
                    }
                }
            }

            db.query(
                """
                SELECT `folderId` FROM `FolderGridItemEntity`
                WHERE `id` = 'FolderGridItemEntity_child'
                """.trimIndent(),
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(
                    "FolderGridItemEntity",
                    cursor.getString(cursor.getColumnIndexOrThrow("folderId")),
                )
            }
        }
    }

    private fun insertGridItem(
        db: SupportSQLiteDatabase,
        table: String,
        showLabel: Boolean,
        id: String = table,
        folderId: String? = null,
    ) {
        val columns = mutableListOf<Pair<String, String>>()
        db.query("PRAGMA table_info(`$table`)").use { cursor ->
            while (cursor.moveToNext()) {
                columns += cursor.getString(cursor.getColumnIndexOrThrow("name")) to
                    cursor.getString(cursor.getColumnIndexOrThrow("type"))
            }
        }

        val values = columns.map { (column, type) ->
            when (column) {
                "id" -> "'$id'"

                "folderId" -> folderId?.let { "'$it'" } ?: "NULL"

                "showLabel" -> if (showLabel) "1" else "0"

                else -> when (type) {
                    "INTEGER" -> "1"
                    "TEXT" -> "'seed'"
                    else -> error("Unexpected column type $type in $table.$column")
                }
            }
        }
        val columnNames = columns.joinToString { (column, _) -> "`$column`" }

        db.execSQL(
            "INSERT INTO `$table` ($columnNames) VALUES (${values.joinToString()})",
        )
    }
}
