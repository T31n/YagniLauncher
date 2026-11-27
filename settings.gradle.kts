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
include(":domain:platform")
include(":domain:grid")
include(":domain:model")
include(":domain:repository")
include(":domain:use-case")
include(":feature:action")
include(":feature:edit-application-info")
include(":feature:edit-folder-application-info")
include(":feature:edit-grid-item")
include(":feature:home")
include(":feature:pin")
include(":feature:settings:app-drawer")
include(":feature:settings:experimental")
include(":feature:settings:folder")
include(":feature:settings:general")
include(":feature:settings:gestures")
include(":feature:settings:home")
include(":feature:settings:settings")
include(":platform")
include(":ui")
