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
package com.eblan.launcher.platform.launcherapps

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.LauncherUserInfo
import android.content.pm.ShortcutInfo
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Process.myUserHandle
import android.os.UserHandle
import android.os.UserManager
import androidx.core.content.ContextCompat
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.launcherapps.EblanUser
import com.eblan.launcher.domain.model.launcherapps.EblanUserType
import com.eblan.launcher.domain.model.launcherapps.FastLauncherAppsActivityInfo
import com.eblan.launcher.domain.model.launcherapps.FastLauncherAppsShortcutInfo
import com.eblan.launcher.domain.model.launcherapps.LauncherAppsActivityInfo
import com.eblan.launcher.domain.model.launcherapps.LauncherAppsShortcutInfo
import com.eblan.launcher.domain.model.launcherapps.ShortcutConfigActivityInfo
import com.eblan.launcher.domain.model.launcherapps.ShortcutQuery
import com.eblan.launcher.domain.model.launcherapps.ShortcutQueryFlag
import com.eblan.launcher.domain.model.userdata.ColorSchemeSettings
import com.eblan.launcher.domain.model.userdata.ColorSource
import com.eblan.launcher.domain.model.userdata.IconShape
import com.eblan.launcher.domain.model.userdata.Theme
import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.domain.platform.LauncherAppsWrapper
import com.eblan.launcher.platform.imageserializer.AndroidImageSerializer
import com.eblan.launcher.platform.usermanager.AndroidUserManagerWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

internal class DefaultLauncherAppsWrapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val androidUserManagerWrapper: AndroidUserManagerWrapper,
    private val imageSerializer: AndroidImageSerializer,
    private val fileManager: FileManager,
    private val iconKeyGenerator: IconKeyGenerator,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : LauncherAppsWrapper,
    AndroidLauncherAppsWrapper {
    private val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val packageManager = context.packageManager

    override val hasShortcutHostPermission
        get() = launcherApps.hasShortcutHostPermission()

    override val profileStateFlow: Flow<Int>
        get() = callbackFlow {
            var version = 0
            send(version)

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context,
                    intent: Intent,
                ) {
                    trySend(++version)
                }
            }

            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter().apply {
                    addAction(Intent.ACTION_USER_UNLOCKED)
                    addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
                    addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
                    addAction(Intent.ACTION_MANAGED_PROFILE_UNLOCKED)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        addAction(Intent.ACTION_PROFILE_AVAILABLE)
                        addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
                    }
                },
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )

            awaitClose {
                context.unregisterReceiver(receiver)
            }
        }

    override fun registerCallback(
        callback: LauncherApps.Callback,
        handler: Handler,
    ) {
        launcherApps.registerCallback(callback, handler)
    }

    override fun unregisterCallback(callback: LauncherApps.Callback) {
        launcherApps.unregisterCallback(callback)
    }

    override suspend fun getActivityListWithCacheIcons(
        iconTint: Boolean,
        colorSource: ColorSource,
        customSeedColor: Int,
        iconShape: IconShape,
        theme: Theme,
        colorSchemeSettings: ColorSchemeSettings,
    ): List<LauncherAppsActivityInfo> = withContext(ioDispatcher) {
        launcherApps.profiles.filterNot { currentUserHandle ->
            currentCoroutineContext().ensureActive()

            isPrivateSpaceEntryPointHidden(userHandle = currentUserHandle)
        }.flatMap { userHandle ->
            currentCoroutineContext().ensureActive()

            try {
                launcherApps.getActivityList(null, userHandle).map { currentLauncherActivityInfo ->
                    currentCoroutineContext().ensureActive()

                    currentLauncherActivityInfo.toLauncherAppsActivityInfo(
                        iconTint = iconTint,
                        colorSource = colorSource,
                        customSeedColor = customSeedColor,
                        iconShape = iconShape,
                        theme = theme,
                        colorSchemeSettings = colorSchemeSettings,
                    )
                }
            } catch (_: IllegalStateException) {
                emptyList()
            } catch (_: SecurityException) {
                emptyList()
            }
        }
    }

    override suspend fun getFastActivityList(): List<FastLauncherAppsActivityInfo> = withContext(ioDispatcher) {
        launcherApps.profiles.filterNot { currentUserHandle ->
            currentCoroutineContext().ensureActive()

            isPrivateSpaceEntryPointHidden(userHandle = currentUserHandle)
        }.flatMap { userHandle ->
            currentCoroutineContext().ensureActive()

            try {
                launcherApps.getActivityList(null, userHandle).map { currentLauncherActivityInfo ->
                    currentCoroutineContext().ensureActive()

                    currentLauncherActivityInfo.toFastLauncherAppsActivityInfo()
                }
            } catch (_: IllegalStateException) {
                emptyList()
            } catch (_: SecurityException) {
                emptyList()
            }
        }
    }

    override suspend fun getActivityListWithCacheIcons(
        serialNumber: Long,
        packageName: String,
        iconTint: Boolean,
        colorSource: ColorSource,
        customSeedColor: Int,
        iconShape: IconShape,
        theme: Theme,
        colorSchemeSettings: ColorSchemeSettings,
    ): List<LauncherAppsActivityInfo> = withContext(ioDispatcher) {
        val userHandle = androidUserManagerWrapper.getUserForSerialNumber(serialNumber)

        if (userHandle != null && isUserAvailable(userHandle = userHandle)) {
            try {
                launcherApps.getActivityList(packageName, userHandle).map { currentLauncherActivityInfo ->
                    currentCoroutineContext().ensureActive()

                    currentLauncherActivityInfo.toLauncherAppsActivityInfo(
                        iconTint = iconTint,
                        colorSource = colorSource,
                        customSeedColor = customSeedColor,
                        iconShape = iconShape,
                        theme = theme,
                        colorSchemeSettings = colorSchemeSettings,
                    )
                }
            } catch (_: IllegalStateException) {
                emptyList()
            } catch (_: SecurityException) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override suspend fun getFastActivityList(
        serialNumber: Long,
        packageName: String,
    ): List<FastLauncherAppsActivityInfo> = withContext(ioDispatcher) {
        val userHandle = androidUserManagerWrapper.getUserForSerialNumber(serialNumber)

        if (userHandle != null && isUserAvailable(userHandle = userHandle)) {
            try {
                launcherApps.getActivityList(packageName, userHandle).map { currentLauncherActivityInfo ->
                    currentCoroutineContext().ensureActive()

                    currentLauncherActivityInfo.toFastLauncherAppsActivityInfo()
                }
            } catch (_: IllegalStateException) {
                emptyList()
            } catch (_: SecurityException) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override suspend fun getShortcutsWithCacheIcons(shortcutQuery: ShortcutQuery?): List<LauncherAppsShortcutInfo>? = withContext(ioDispatcher) {
        if (hasShortcutHostPermission) {
            val shortcutQuery = LauncherApps.ShortcutQuery().apply {
                val shortcutQueryFlag = when (shortcutQuery?.shortcutQueryFlag) {
                    ShortcutQueryFlag.Pinned -> LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED

                    ShortcutQueryFlag.Dynamic -> LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC

                    ShortcutQueryFlag.Manifest -> LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST

                    null ->
                        LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                }

                setQueryFlags(shortcutQueryFlag)
                shortcutQuery?.packageName?.let(::setPackage)
            }

            launcherApps.profiles.filter { currentUserHandle ->
                currentCoroutineContext().ensureActive()

                isUserAvailable(userHandle = currentUserHandle)
            }.flatMap { userHandle ->
                currentCoroutineContext().ensureActive()

                try {
                    launcherApps.getShortcuts(shortcutQuery, userHandle)?.map { currentShortcutInfo ->
                        currentCoroutineContext().ensureActive()

                        currentShortcutInfo.toLauncherAppsShortcutInfo()
                    }.orEmpty()
                } catch (_: IllegalStateException) {
                    emptyList()
                } catch (_: SecurityException) {
                    emptyList()
                }
            }
        } else {
            null
        }
    }

    override suspend fun getFastShortcuts(shortcutQuery: ShortcutQuery?): List<FastLauncherAppsShortcutInfo>? = withContext(ioDispatcher) {
        if (hasShortcutHostPermission) {
            val shortcutQuery = LauncherApps.ShortcutQuery().apply {
                val shortcutQueryFlag = when (shortcutQuery?.shortcutQueryFlag) {
                    ShortcutQueryFlag.Pinned -> LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED

                    ShortcutQueryFlag.Dynamic -> LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC

                    ShortcutQueryFlag.Manifest -> LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST

                    null ->
                        LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                }

                setQueryFlags(shortcutQueryFlag)
                shortcutQuery?.packageName?.let(::setPackage)
            }

            launcherApps.profiles.filter { currentUserHandle ->
                currentCoroutineContext().ensureActive()

                isUserAvailable(userHandle = currentUserHandle)
            }.flatMap { userHandle ->
                currentCoroutineContext().ensureActive()

                try {
                    launcherApps.getShortcuts(shortcutQuery, userHandle)?.map { currentShortcutInfo ->
                        currentCoroutineContext().ensureActive()

                        currentShortcutInfo.toFastLauncherAppsShortcutInfo()
                    }.orEmpty()
                } catch (_: IllegalStateException) {
                    emptyList()
                } catch (_: SecurityException) {
                    emptyList()
                }
            }
        } else {
            null
        }
    }

    override suspend fun getShortcutsByPackageNameWithCacheIcons(
        serialNumber: Long,
        packageName: String,
    ): List<LauncherAppsShortcutInfo>? = withContext(ioDispatcher) {
        val userHandle = androidUserManagerWrapper.getUserForSerialNumber(serialNumber)

        if (hasShortcutHostPermission && userHandle != null && isUserAvailable(userHandle = userHandle)) {
            val shortcutQuery = LauncherApps.ShortcutQuery().apply {
                setPackage(packageName)

                setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED,
                )
            }

            try {
                launcherApps.getShortcuts(shortcutQuery, userHandle)?.map { currentShortcutInfo ->
                    currentCoroutineContext().ensureActive()

                    currentShortcutInfo.toLauncherAppsShortcutInfo()
                }
            } catch (_: IllegalStateException) {
                null
            } catch (_: SecurityException) {
                null
            }
        } else {
            null
        }
    }

    override suspend fun getShortcutConfigActivityListWithCacheIcons(
        serialNumber: Long,
        packageName: String,
    ): List<ShortcutConfigActivityInfo> = withContext(ioDispatcher) {
        val userHandle = androidUserManagerWrapper.getUserForSerialNumber(serialNumber)

        if (userHandle != null && isUserAvailable(userHandle = userHandle)) {
            try {
                launcherApps.getShortcutConfigActivityList(packageName, userHandle)
                    .map { currentLauncherActivityInfo ->
                        currentCoroutineContext().ensureActive()

                        currentLauncherActivityInfo.toLauncherAppsActivityInfo(
                            iconTint = false,
                            colorSource = ColorSource.None,
                            customSeedColor = 0,
                            iconShape = IconShape.None,
                            theme = Theme.System,
                        )
                    }
            } catch (_: IllegalStateException) {
                emptyList()
            } catch (_: SecurityException) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override fun startMainActivity(
        serialNumber: Long,
        componentName: String,
        sourceBounds: Rect,
    ) {
        val userHandle = androidUserManagerWrapper.getUserForSerialNumber(serialNumber)

        try {
            if (userHandle != null && isUserAvailable(userHandle = userHandle)) {
                launcherApps.startMainActivity(
                    ComponentName.unflattenFromString(componentName),
                    userHandle,
                    sourceBounds,
                    Bundle.EMPTY,
                )
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun getPinItemRequest(intent: Intent): LauncherApps.PinItemRequest = launcherApps.getPinItemRequest(intent)

    override fun startShortcut(
        serialNumber: Long,
        packageName: String,
        id: String,
        sourceBounds: Rect,
    ) {
        val userHandle = androidUserManagerWrapper.getUserForSerialNumber(serialNumber)

        try {
            if (userHandle != null && isUserAvailable(userHandle = userHandle)) {
                launcherApps.startShortcut(
                    packageName,
                    id,
                    sourceBounds,
                    null,
                    userHandle,
                )
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun startShortcut(
        packageName: String,
        id: String,
        sourceBounds: Rect,
    ) {
        try {
            if (isUserAvailable(userHandle = myUserHandle())) {
                launcherApps.startShortcut(
                    packageName,
                    id,
                    sourceBounds,
                    null,
                    myUserHandle(),
                )
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override suspend fun getShortcutBadgedIconByteArray(
        shortcutInfo: ShortcutInfo?,
        density: Int,
    ): ByteArray? = withContext(ioDispatcher) {
        if (shortcutInfo != null) {
            try {
                launcherApps.getShortcutBadgedIconDrawable(shortcutInfo, density)
                    ?.let { drawable ->
                        imageSerializer.createByteArray(drawable = drawable)
                    }
            } catch (_: IllegalStateException) {
                null
            } catch (_: SecurityException) {
                null
            }
        } else {
            null
        }
    }

    override fun startAppDetailsActivity(
        serialNumber: Long,
        componentName: String,
        sourceBounds: Rect,
    ) {
        val userHandle = androidUserManagerWrapper.getUserForSerialNumber(serialNumber) ?: return

        try {
            if (isUserAvailable(userHandle = userHandle)) {
                launcherApps.startAppDetailsActivity(
                    ComponentName.unflattenFromString(componentName),
                    userHandle,
                    sourceBounds,
                    Bundle.EMPTY,
                )
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override suspend fun getShortcutConfigIntent(
        serialNumber: Long,
        packageName: String,
        componentName: String,
    ): IntentSender? {
        if (!hasShortcutHostPermission) return null

        return withContext(ioDispatcher) {
            val userHandle = androidUserManagerWrapper.getUserForSerialNumber(serialNumber)

            val launcherActivityInfo = if (userHandle != null && isUserAvailable(userHandle = userHandle)) {
                try {
                    launcherApps.getShortcutConfigActivityList(packageName, userHandle)
                        .find { currentLauncherActivityInfo ->
                            currentLauncherActivityInfo.componentName.flattenToString() == componentName
                        }
                } catch (_: IllegalStateException) {
                    null
                } catch (_: SecurityException) {
                    null
                }
            } else {
                null
            }

            try {
                launcherActivityInfo?.let(launcherApps::getShortcutConfigActivityIntent)
            } catch (_: IllegalStateException) {
                null
            } catch (_: SecurityException) {
                null
            }
        }
    }

    override suspend fun getUser(serialNumber: Long): EblanUser {
        val userHandle = androidUserManagerWrapper.getUserForSerialNumber(serialNumber)
            ?: return EblanUser(
                serialNumber = serialNumber,
                eblanUserType = EblanUserType.Personal,
                isPrivateSpaceEntryPointHidden = false,
                isPrivateSpaceLocked = false,
                isQuietModeEnabled = false,
            )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val isQuietModeEnabled = androidUserManagerWrapper.isQuietModeEnabled(userHandle = userHandle)

            val launcherUserInfo = try {
                launcherApps.getLauncherUserInfo(userHandle)
            } catch (_: IllegalStateException) {
                null
            } catch (_: SecurityException) {
                null
            } ?: return EblanUser(
                serialNumber = serialNumber,
                eblanUserType = EblanUserType.Personal,
                isPrivateSpaceEntryPointHidden = isPrivateSpaceEntryPointHidden(userHandle = userHandle),
                isQuietModeEnabled = isQuietModeEnabled,
                isPrivateSpaceLocked = false,
            )

            val eblanUserType = when (launcherUserInfo.userType) {
                UserManager.USER_TYPE_PROFILE_CLONE -> EblanUserType.Clone
                UserManager.USER_TYPE_PROFILE_MANAGED -> EblanUserType.Work
                UserManager.USER_TYPE_PROFILE_PRIVATE -> EblanUserType.Private
                else -> EblanUserType.Personal
            }

            EblanUser(
                serialNumber = serialNumber,
                eblanUserType = eblanUserType,
                isPrivateSpaceEntryPointHidden = isPrivateSpaceEntryPointHidden(userHandle = userHandle),
                isPrivateSpaceLocked = eblanUserType == EblanUserType.Private &&
                    !isUserAvailable(userHandle = userHandle),
                isQuietModeEnabled = isQuietModeEnabled,
            )
        } else {
            val isWorkProfile = withContext(ioDispatcher) {
                try {
                    packageManager.getUserBadgedLabel("", userHandle).isNotBlank()
                } catch (_: Exception) {
                    false
                }
            }
            val eblanUserType = if (isWorkProfile) EblanUserType.Work else EblanUserType.Personal

            EblanUser(
                serialNumber = serialNumber,
                eblanUserType = eblanUserType,
                isPrivateSpaceEntryPointHidden = isPrivateSpaceEntryPointHidden(userHandle = userHandle),
                isQuietModeEnabled = androidUserManagerWrapper.isQuietModeEnabled(userHandle = userHandle),
                isPrivateSpaceLocked = false,
            )
        }
    }

    override fun pinShortcuts(
        packageName: String,
        shortcutIds: List<String>,
        serialNumber: Long,
    ) {
        val userHandle = androidUserManagerWrapper.getUserForSerialNumber(serialNumber)

        if (userHandle != null && isUserAvailable(userHandle = userHandle)) {
            try {
                launcherApps.pinShortcuts(
                    packageName,
                    shortcutIds,
                    userHandle,
                )
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    override fun getPrivateSpaceSettingsIntent(): IntentSender? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        try {
            launcherApps.privateSpaceSettingsIntent
        } catch (_: Exception) {
            null
        }
    } else {
        null
    }

    private fun isUserAvailable(userHandle: UserHandle): Boolean = androidUserManagerWrapper.isUserRunning(userHandle) &&
        androidUserManagerWrapper.isUserUnlocked(userHandle) &&
        !androidUserManagerWrapper.isQuietModeEnabled(userHandle)

    private fun isPrivateSpaceEntryPointHidden(userHandle: UserHandle): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA &&
        try {
            launcherApps.getLauncherUserInfo(userHandle)
                ?.userConfig
                ?.getBoolean(LauncherUserInfo.PRIVATE_SPACE_ENTRYPOINT_HIDDEN) == true
        } catch (_: Exception) {
            false
        }

    private suspend fun LauncherActivityInfo.toLauncherAppsActivityInfo(
        iconTint: Boolean,
        colorSource: ColorSource,
        customSeedColor: Int,
        iconShape: IconShape,
        theme: Theme,
        colorSchemeSettings: ColorSchemeSettings = ColorSchemeSettings(),
    ): LauncherAppsActivityInfo {
        val serialNumber = androidUserManagerWrapper.getSerialNumberForUser(user)

        val icon = try {
            getIcon(0)?.takeIf { currentDrawable ->
                currentDrawable.intrinsicWidth > 0 && currentDrawable.intrinsicHeight > 0
            }
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: IllegalStateException) {
            null
        } catch (_: SecurityException) {
            null
        }

        val activityIcon = getActivityIcon(
            componentName = componentName,
            drawable = icon,
            userHandle = user,
            iconTint = iconTint,
            colorSource = colorSource,
            customSeedColor = customSeedColor,
            iconShape = iconShape,
            serialNumber = serialNumber,
            theme = theme,
            colorSchemeSettings = colorSchemeSettings,
        )

        return LauncherAppsActivityInfo(
            serialNumber = serialNumber,
            componentName = componentName.flattenToString(),
            packageName = applicationInfo.packageName,
            activityIcon = activityIcon,
            activityLabel = label.toString(),
            lastUpdateTime = getApkLastModified(),
            flags = applicationInfo.flags,
        )
    }

    private fun LauncherActivityInfo.toFastLauncherAppsActivityInfo(): FastLauncherAppsActivityInfo = FastLauncherAppsActivityInfo(
        serialNumber = androidUserManagerWrapper.getSerialNumberForUser(user),
        componentName = componentName.flattenToString(),
        packageName = applicationInfo.packageName,
        lastUpdateTime = getApkLastModified(),
    )

    private suspend fun ShortcutInfo.toLauncherAppsShortcutInfo(): LauncherAppsShortcutInfo {
        val serialNumber = androidUserManagerWrapper.getSerialNumberForUser(userHandle)

        val shortcutBadgedIconDrawable = try {
            launcherApps.getShortcutBadgedIconDrawable(this, 0)
                ?.takeIf { currentDrawable ->
                    currentDrawable.intrinsicWidth > 0 && currentDrawable.intrinsicHeight > 0
                }
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: IllegalStateException) {
            null
        } catch (_: SecurityException) {
            null
        }

        val icon = shortcutBadgedIconDrawable?.let { drawable ->
            val directory = fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR)

            val file = File(
                directory,
                iconKeyGenerator.getShortcutIconKey(
                    serialNumber = serialNumber,
                    packageName = `package`,
                    id = id,
                ),
            )

            imageSerializer.createDrawablePath(
                drawable = drawable,
                file = file,
            )

            file.absolutePath
        }

        val shortcutQueryFlag = when {
            isDeclaredInManifest -> ShortcutQueryFlag.Manifest
            isDynamic -> ShortcutQueryFlag.Dynamic
            isPinned -> ShortcutQueryFlag.Pinned
            else -> ShortcutQueryFlag.Manifest
        }

        return LauncherAppsShortcutInfo(
            shortcutId = id,
            packageName = `package`,
            serialNumber = serialNumber,
            shortLabel = shortLabel.toString(),
            longLabel = longLabel.toString(),
            isEnabled = isEnabled,
            icon = icon,
            shortcutQueryFlag = shortcutQueryFlag,
            lastChangedTimestamp = lastChangedTimestamp,
            activityComponentName = activity?.flattenToString(),
            rank = rank,
        )
    }

    private fun ShortcutInfo.toFastLauncherAppsShortcutInfo(): FastLauncherAppsShortcutInfo = FastLauncherAppsShortcutInfo(
        shortcutId = id,
        packageName = `package`,
        serialNumber = androidUserManagerWrapper.getSerialNumberForUser(userHandle),
        lastChangedTimestamp = lastChangedTimestamp,
    )

    private fun LauncherActivityInfo.getApkLastModified(): Long = try {
        applicationInfo.sourceDir?.let { currentSourceDir ->
            File(currentSourceDir).lastModified()
        } ?: 0L
    } catch (_: SecurityException) {
        0L
    }

    private suspend fun getActivityIcon(
        componentName: ComponentName,
        drawable: Drawable?,
        userHandle: UserHandle,
        iconTint: Boolean,
        colorSource: ColorSource,
        customSeedColor: Int,
        iconShape: IconShape,
        serialNumber: Long,
        theme: Theme,
        colorSchemeSettings: ColorSchemeSettings = ColorSchemeSettings(),
    ): String? {
        if (drawable == null) return null

        val directoryName = if (!iconTint && iconShape == IconShape.None) {
            FileManager.ICONS_DIR
        } else {
            FileManager.THEMED_ICONS_DIR
        }

        val directory = fileManager.getFilesDirectory(directoryName)

        val file = File(
            directory,
            iconKeyGenerator.getActivityIconKey(
                serialNumber = serialNumber,
                componentName = componentName.flattenToString(),
            ),
        )

        val transformedDrawable = if (iconTint) {
            imageSerializer.getTintedAndShapedDrawable(
                drawable = drawable,
                colorSource = colorSource,
                customSeedColor = customSeedColor,
                theme = theme,
                iconShape = iconShape,
                colorSchemeSettings = colorSchemeSettings,
            )
        } else {
            imageSerializer.getShapedDrawable(drawable, iconShape)
        } ?: return null

        val badgedDrawable = try {
            packageManager.getUserBadgedIcon(transformedDrawable, userHandle)
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: IllegalStateException) {
            null
        } catch (_: SecurityException) {
            null
        } ?: return null

        imageSerializer.createDrawablePath(
            drawable = badgedDrawable,
            file = file,
        )

        return file.absolutePath
    }
}
