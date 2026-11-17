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
package com.eblan.launcher.platform.di

import com.eblan.launcher.domain.platform.AppWidgetHostWrapper
import com.eblan.launcher.domain.platform.AppWidgetManagerWrapper
import com.eblan.launcher.domain.platform.ContentResolverWrapper
import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.domain.platform.IconPackManager
import com.eblan.launcher.domain.platform.JaroWinklerSimilarityWrapper
import com.eblan.launcher.domain.platform.LauncherAppsWrapper
import com.eblan.launcher.domain.platform.PackageManagerWrapper
import com.eblan.launcher.platform.accessibilitymanager.AndroidAccessibilityManagerWrapper
import com.eblan.launcher.platform.accessibilitymanager.DefaultAccessibilityManagerWrapper
import com.eblan.launcher.platform.colorscheme.DefaultMaterialColorSchemeResolver
import com.eblan.launcher.platform.colorscheme.MaterialColorSchemeResolver
import com.eblan.launcher.platform.contentresolver.DefaultContentResolverWrapper
import com.eblan.launcher.platform.filemanager.DefaultFileManager
import com.eblan.launcher.platform.iconkeygenerator.DefaultIconKeyGenerator
import com.eblan.launcher.platform.iconpackmanager.AndroidIconPackManager
import com.eblan.launcher.platform.iconpackmanager.DefaultIconPackManager
import com.eblan.launcher.platform.imageserializer.AndroidImageSerializer
import com.eblan.launcher.platform.imageserializer.DefaultImageSerializer
import com.eblan.launcher.platform.jarowinklersimilarity.DefaultJaroWinklerSimilarityWrapper
import com.eblan.launcher.platform.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.platform.launcherapps.DefaultLauncherAppsWrapper
import com.eblan.launcher.platform.launcherapps.DefaultPinItemRequestWrapper
import com.eblan.launcher.platform.launcherapps.PinItemRequestWrapper
import com.eblan.launcher.platform.locale.DefaultLocaleWrapper
import com.eblan.launcher.platform.locale.LocaleWrapper
import com.eblan.launcher.platform.notificationmanager.AndroidNotificationManagerWrapper
import com.eblan.launcher.platform.notificationmanager.DefaultNotificationManagerWrapper
import com.eblan.launcher.platform.packagemanager.AndroidPackageManagerWrapper
import com.eblan.launcher.platform.packagemanager.DefaultPackageManagerWrapper
import com.eblan.launcher.platform.rolemanager.AndroidRoleManagerWrapper
import com.eblan.launcher.platform.rolemanager.DefaultRoleManagerWrapper
import com.eblan.launcher.platform.settings.AndroidSettingsWrapper
import com.eblan.launcher.platform.settings.DefaultSettingsWrapper
import com.eblan.launcher.platform.statusbarmanager.AndroidStatusBarManagerWrapper
import com.eblan.launcher.platform.statusbarmanager.DefaultStatusBarManagerWrapper
import com.eblan.launcher.platform.usermanager.AndroidUserManagerWrapper
import com.eblan.launcher.platform.usermanager.DefaultUserManagerWrapper
import com.eblan.launcher.platform.wallpapermanager.AndroidWallpaperManagerWrapper
import com.eblan.launcher.platform.wallpapermanager.DefaultWallpaperManagerWrapper
import com.eblan.launcher.platform.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.platform.widgetmanager.AndroidAppWidgetManagerWrapper
import com.eblan.launcher.platform.widgetmanager.DefaultAppWidgetHostWrapper
import com.eblan.launcher.platform.widgetmanager.DefaultAppWidgetManagerWrapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface PlatformModule {
    @Binds
    @Singleton
    fun accessibilityManagerWrapper(impl: DefaultAccessibilityManagerWrapper): AndroidAccessibilityManagerWrapper

    @Binds
    @Singleton
    fun contentResolverWrapper(impl: DefaultContentResolverWrapper): ContentResolverWrapper

    @Binds
    @Singleton
    fun fileManager(impl: DefaultFileManager): FileManager

    @Binds
    @Singleton
    fun iconKeyGenerator(impl: DefaultIconKeyGenerator): IconKeyGenerator

    @Binds
    @Singleton
    fun iconPackManager(impl: DefaultIconPackManager): IconPackManager

    @Binds
    @Singleton
    fun androidIconPackManager(impl: DefaultIconPackManager): AndroidIconPackManager

    @Binds
    @Singleton
    fun jaroWinklerSimilarityWrapper(impl: DefaultJaroWinklerSimilarityWrapper): JaroWinklerSimilarityWrapper

    @Binds
    @Singleton
    fun launcherAppsWrapper(impl: DefaultLauncherAppsWrapper): LauncherAppsWrapper

    @Binds
    @Singleton
    fun androidLauncherAppsWrapper(impl: DefaultLauncherAppsWrapper): AndroidLauncherAppsWrapper

    @Binds
    @Singleton
    fun pinItemRequestWrapper(impl: DefaultPinItemRequestWrapper): PinItemRequestWrapper

    @Binds
    @Singleton
    fun localeWrapper(impl: DefaultLocaleWrapper): LocaleWrapper

    @Binds
    @Singleton
    fun notificationManagerWrapper(impl: DefaultNotificationManagerWrapper): AndroidNotificationManagerWrapper

    @Binds
    @Singleton
    fun packageManagerWrapper(impl: DefaultPackageManagerWrapper): PackageManagerWrapper

    @Binds
    @Singleton
    fun androidPackageManagerWrapper(impl: DefaultPackageManagerWrapper): AndroidPackageManagerWrapper

    @Binds
    @Singleton
    fun roleManagerWrapper(impl: DefaultRoleManagerWrapper): AndroidRoleManagerWrapper

    @Binds
    @Singleton
    fun settingsWrapper(impl: DefaultSettingsWrapper): AndroidSettingsWrapper

    @Binds
    @Singleton
    fun statusBarManagerWrapper(impl: DefaultStatusBarManagerWrapper): AndroidStatusBarManagerWrapper

    @Binds
    @Singleton
    fun userManagerWrapper(impl: DefaultUserManagerWrapper): AndroidUserManagerWrapper

    @Binds
    @Singleton
    fun wallpaperManagerWrapper(impl: DefaultWallpaperManagerWrapper): AndroidWallpaperManagerWrapper

    @Binds
    @Singleton
    fun androidAppWidgetManagerWrapper(impl: DefaultAppWidgetManagerWrapper): AndroidAppWidgetManagerWrapper

    @Binds
    @Singleton
    fun appWidgetManagerWrapper(impl: DefaultAppWidgetManagerWrapper): AppWidgetManagerWrapper

    @Binds
    @Singleton
    fun androidAppWidgetHostWrapper(impl: DefaultAppWidgetHostWrapper): AndroidAppWidgetHostWrapper

    @Binds
    @Singleton
    fun appWidgetHostWrapper(impl: DefaultAppWidgetHostWrapper): AppWidgetHostWrapper

    @Binds
    @Singleton
    fun androidImageSerializer(impl: DefaultImageSerializer): AndroidImageSerializer

    @Binds
    @Singleton
    fun materialColorSchemeResolver(impl: DefaultMaterialColorSchemeResolver): MaterialColorSchemeResolver
}
