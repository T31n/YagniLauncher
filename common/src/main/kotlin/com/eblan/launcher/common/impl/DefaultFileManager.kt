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
package com.eblan.launcher.common.impl

import android.content.Context
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.common.FileManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

internal class DefaultFileManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : FileManager {
    override suspend fun getFilesDirectory(name: String): File = withContext(ioDispatcher) {
        File(context.filesDir, name).apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun updateAndGetFilePath(
        directory: File,
        name: String,
        byteArray: ByteArray,
    ): String? = withContext(ioDispatcher) {
        val file = File(directory, name)

        val oldFile = readFileBytes(file = file)

        if (oldFile.contentEquals(byteArray)) {
            file.absolutePath
        } else {
            try {
                FileOutputStream(file).use { fos ->
                    fos.write(byteArray)
                }

                file.absolutePath
            } catch (_: IOException) {
                null
            }
        }
    }

    private fun readFileBytes(file: File): ByteArray? = if (file.exists()) {
        try {
            FileInputStream(file).use { fis ->
                fis.readBytes()
            }
        } catch (_: IOException) {
            null
        }
    } else {
        null
    }
}
