pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "YagniLauncher"
include(":app")
include(":common")
include(":design-system")
include(":data:datastore")
include(":data:datastore-proto")
include(":data:repository")
include(":data:room")
include(":domain:common")
include(":domain:framework")
include(":domain:grid")
include(":domain:model")
include(":domain:repository")
include(":domain:use-case")
include(":feature:action")
include(":feature:edit-application-info")
include(":feature:edit-grid-item")
include(":feature:home")
include(":feature:pin")
include(":feature:settings:app-drawer")
include(":feature:settings:experimental")
include(":feature:settings:general")
include(":feature:settings:gestures")
include(":feature:settings:home")
include(":feature:settings:settings")
include(":framework:accessibility-manager")
include(":framework:content-resolver")
include(":framework:icon-pack-manager")
include(":framework:jaro-winkler-similarity")
include(":framework:launcher-apps")
include(":framework:notification-manager")
include(":framework:package-manager")
include(":framework:resources")
include(":framework:settings")
include(":framework:user-manager")
include(":framework:wallpaper-manager")
include(":framework:widget-manager")
include(":service")
include(":ui")
