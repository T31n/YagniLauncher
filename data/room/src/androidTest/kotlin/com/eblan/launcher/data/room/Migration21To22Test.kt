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
import com.eblan.launcher.data.room.migration.Migration21To22
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class Migration21To22Test {
    private val testDatabase = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EblanDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migrate21To22() {
        helper.createDatabase(testDatabase, 21).use { db ->
            db.execSQL(
                """
                INSERT INTO EblanShortcutInfoEntity (
                    shortcutId,
                    serialNumber,
                    packageName,
                    shortLabel,
                    longLabel,
                    icon,
                    shortcutQueryFlag,
                    isEnabled,
                    lastChangedTimestamp
                ) VALUES (
                    'shortcut_1',
                    100,
                    'com.example.app',
                    'Short',
                    'Long',
                    'icon_path',
                    'Pinned',
                    1,
                    123456789
                )
                """.trimIndent(),
            )
        }

        helper.runMigrationsAndValidate(
            testDatabase,
            22,
            true,
            Migration21To22(),
        ).use { db ->
            db.query("SELECT * FROM EblanShortcutInfoEntity WHERE shortcutId = 'shortcut_1'")
                .use { cursor ->
                    assertTrue(cursor.moveToFirst())

                    assertEquals(
                        "shortcut_1",
                        cursor.getString(cursor.getColumnIndexOrThrow("shortcutId")),
                    )
                    assertEquals(
                        100L,
                        cursor.getLong(cursor.getColumnIndexOrThrow("serialNumber")),
                    )
                    assertEquals(
                        "com.example.app",
                        cursor.getString(cursor.getColumnIndexOrThrow("packageName")),
                    )
                    assertEquals(
                        "Short",
                        cursor.getString(cursor.getColumnIndexOrThrow("shortLabel")),
                    )
                    assertEquals(
                        "Long",
                        cursor.getString(cursor.getColumnIndexOrThrow("longLabel")),
                    )
                    assertEquals(
                        "icon_path",
                        cursor.getString(cursor.getColumnIndexOrThrow("icon")),
                    )
                    assertEquals(
                        "Pinned",
                        cursor.getString(cursor.getColumnIndexOrThrow("shortcutQueryFlag")),
                    )
                    assertEquals(
                        1,
                        cursor.getInt(cursor.getColumnIndexOrThrow("isEnabled")),
                    )
                    assertEquals(
                        123456789L,
                        cursor.getLong(cursor.getColumnIndexOrThrow("lastChangedTimestamp")),
                    )
                    assertNull(
                        cursor.getString(cursor.getColumnIndexOrThrow("activityComponentName")),
                    )
                    assertEquals(
                        0,
                        cursor.getInt(cursor.getColumnIndexOrThrow("rank")),
                    )
                }
        }
    }
}
