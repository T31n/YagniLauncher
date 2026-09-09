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
package com.eblan.launcher.framework.packagemanager

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.UserHandle
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.PackageManagerIconPackInfo
import com.eblan.launcher.framework.imageserializer.AndroidImageSerializer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

internal class DefaultPackageManagerWrapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val imageSerializer: AndroidImageSerializer,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : PackageManagerWrapper,
    AndroidPackageManagerWrapper {
    private val packageManager = context.packageManager

    override val hasSystemFeatureAppWidgets
        get() = packageManager.hasSystemFeature(PackageManager.FEATURE_APP_WIDGETS)

    override suspend fun getApplicationIcon(
        packageName: String,
        file: File,
    ): String? = withContext(ioDispatcher) {
        try {
            imageSerializer.createDrawablePath(
                drawable = packageManager.getApplicationIcon(
                    packageName,
                ),
                file = file,
            )

            file.absolutePath
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    override suspend fun getApplicationLabel(packageName: String): String? = withContext(ioDispatcher) {
        try {
            val applicationInfo =
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)

            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    override suspend fun getComponentName(packageName: String): String? = withContext(ioDispatcher) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)

        launchIntent?.component?.flattenToString()
    }

    override suspend fun isDefaultLauncher(): Boolean = withContext(ioDispatcher) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }

        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

        val defaultLauncherPackage = resolveInfo?.activityInfo?.packageName

        defaultLauncherPackage == context.packageName
    }

    override suspend fun getIconPackInfos(): List<PackageManagerIconPackInfo> {
        val intents = listOf(
            Intent("app.lawnchair.icons.THEMED_ICON"),
            Intent("org.adw.ActivityStarter.THEMES"),
            Intent("com.novalauncher.THEME"),
            Intent("org.adw.launcher.THEMES"),
        )

        val resolveInfos = mutableSetOf<ResolveInfo>()

        return withContext(ioDispatcher) {
            intents.forEach {
                resolveInfos.addAll(
                    packageManager.queryIntentActivities(
                        it,
                        PackageManager.GET_META_DATA,
                    ),
                )
            }

            resolveInfos.map { resolveInfo ->
                PackageManagerIconPackInfo(
                    packageName = resolveInfo.activityInfo.applicationInfo.packageName,
                    icon = resolveInfo.activityInfo.applicationInfo.loadIcon(packageManager)
                        .let {
                            imageSerializer.createByteArray(drawable = it)
                        },
                    label = resolveInfo.activityInfo.applicationInfo.loadLabel(packageManager)
                        .toString(),
                )
            }.distinct()
        }
    }

    override suspend fun getLastUpdateTime(packageName: String): Long = withContext(ioDispatcher) {
        try {
            packageManager.getPackageInfo(packageName, 0).lastUpdateTime
        } catch (_: PackageManager.NameNotFoundException) {
            0L
        }
    }

    override fun isSystem(flags: Int): Boolean = (flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0

    override suspend fun getUserBadgedLabel(
        label: CharSequence,
        userHandle: UserHandle,
    ): CharSequence = withContext(ioDispatcher) {
        packageManager.getUserBadgedLabel(label, userHandle)
    }
}
