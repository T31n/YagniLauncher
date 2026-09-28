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

import com.eblan.launcher.libs
import kotlinx.benchmark.gradle.BenchmarksExtension
import kotlinx.benchmark.gradle.JvmBenchmarkTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.allopen.gradle.AllOpenExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class JvmBenchmarkLibraryConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply(libs.plugins.com.eblan.launcher.jvmLibrary.get().pluginId)
            pluginManager.apply(libs.plugins.kotlinx.benchmark.get().pluginId)
            pluginManager.apply(libs.plugins.kotlin.allopen.get().pluginId)

            extensions.configure<AllOpenExtension> {
                annotation("org.openjdk.jmh.annotations.State")
            }

            val sourceSets = extensions.getByType<SourceSetContainer>()
            sourceSets.create("benchmark")

            sourceSets.named("benchmark").configure {
                val kotlinSrc = extensions.getByName("kotlin") as SourceDirectorySet
                kotlinSrc.setSrcDirs(listOf(project.file("src/$name/src")))
                java.setSrcDirs(listOf(project.file("src/$name/src")))
                resources.setSrcDirs(listOf(project.file("src/$name/resources")))
            }

            extensions.configure<KotlinJvmProjectExtension> {
                this.target.compilations.getByName("benchmark")
                    .associateWith(this.target.compilations.getByName("main"))
            }

            dependencies {
                add("benchmarkImplementation", libs.kotlinx.benchmark.runtime)
            }

            extensions.configure<BenchmarksExtension> {
                targets.register("benchmark") {
                    if (this is JvmBenchmarkTarget) {
                        jmhVersion = libs.versions.jmh.get()
                    }
                }
            }
        }
    }
}