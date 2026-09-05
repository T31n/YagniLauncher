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
plugins {
    alias(libs.plugins.com.eblan.launcher.application)
    alias(libs.plugins.com.eblan.launcher.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.eblan.launcher"

    defaultConfig {
        applicationId = "com.eblan.launcher"
        versionCode = 96
        versionName = "0.9.6-alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(projects.common)
    implementation(projects.data.repository)
    implementation(projects.designSystem)
    implementation(projects.domain.framework)
    implementation(projects.domain.model)
    implementation(projects.domain.useCase)
    implementation(projects.domain.repository)
    implementation(projects.feature.action)
    implementation(projects.feature.editApplicationInfo)
    implementation(projects.feature.editGridItem)
    implementation(projects.feature.home)
    implementation(projects.feature.pin)
    implementation(projects.feature.settings.appDrawer)
    implementation(projects.feature.settings.experimental)
    implementation(projects.feature.settings.general)
    implementation(projects.feature.settings.gestures)
    implementation(projects.feature.settings.home)
    implementation(projects.feature.settings.settings)
    implementation(projects.framework.contentResolver)
    implementation(projects.framework.jaroWinklerSimilarity)
    implementation(projects.framework.notificationManager)
    implementation(projects.framework.resources)
    implementation(projects.service)
    implementation(projects.ui)

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
}