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
import com.eblan.launcher.data.room.migration.Migration23To24
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class Migration23To24Test {
    private val testDatabase = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EblanDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migrate23To24_allowsSameWidgetComponentAcrossProfiles() {
        helper.createDatabase(testDatabase, 23).use { db ->
            db.execSQL(
                """
                INSERT INTO EblanAppWidgetProviderInfoEntity (
                    componentName,
                    serialNumber,
                    configure,
                    packageName,
                    targetCellWidth,
                    targetCellHeight,
                    minWidth,
                    minHeight,
                    resizeMode,
                    minResizeWidth,
                    minResizeHeight,
                    maxResizeWidth,
                    maxResizeHeight,
                    preview,
                    applicationLabel,
                    applicationIcon,
                    lastUpdateTime,
                    label,
                    description
                ) VALUES (
                    'com.example/.ClockWidget',
                    0,
                    NULL,
                    'com.example',
                    2,
                    2,
                    100,
                    100,
                    3,
                    50,
                    50,
                    400,
                    400,
                    '/data/files/widgets/preview_0.png',
                    'Clock',
                    '/data/files/icons/icon_0.png',
                    123456789,
                    'Clock Widget',
                    'Shows time'
                )
                """.trimIndent(),
            )
        }

        helper.runMigrationsAndValidate(
            testDatabase,
            24,
            true,
            Migration23To24(),
        ).use { db ->
            db.execSQL(
                """
                INSERT INTO EblanAppWidgetProviderInfoEntity (
                    componentName,
                    serialNumber,
                    configure,
                    packageName,
                    targetCellWidth,
                    targetCellHeight,
                    minWidth,
                    minHeight,
                    resizeMode,
                    minResizeWidth,
                    minResizeHeight,
                    maxResizeWidth,
                    maxResizeHeight,
                    preview,
                    applicationLabel,
                    applicationIcon,
                    lastUpdateTime,
                    label,
                    description
                ) VALUES (
                    'com.example/.ClockWidget',
                    10,
                    NULL,
                    'com.example',
                    2,
                    2,
                    100,
                    100,
                    3,
                    50,
                    50,
                    400,
                    400,
                    '/data/files/widgets/preview_10.png',
                    'Clock',
                    '/data/files/icons/icon_10.png',
                    123456789,
                    'Clock Widget',
                    'Shows time'
                )
                """.trimIndent(),
            )

            db.query(
                "SELECT serialNumber, preview FROM EblanAppWidgetProviderInfoEntity WHERE componentName = 'com.example/.ClockWidget' ORDER BY serialNumber ASC",
            ).use { cursor ->
                assertEquals(2, cursor.count)

                assertTrue(cursor.moveToFirst())
                assertEquals(0L, cursor.getLong(cursor.getColumnIndexOrThrow("serialNumber")))
                assertEquals(
                    "/data/files/widgets/preview_0.png",
                    cursor.getString(cursor.getColumnIndexOrThrow("preview")),
                )

                assertTrue(cursor.moveToNext())
                assertEquals(10L, cursor.getLong(cursor.getColumnIndexOrThrow("serialNumber")))
                assertEquals(
                    "/data/files/widgets/preview_10.png",
                    cursor.getString(cursor.getColumnIndexOrThrow("preview")),
                )
            }
        }
    }
}
