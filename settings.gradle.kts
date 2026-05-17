enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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

rootProject.name = "Sakuwise"

include(":app")

// Core modules
include(":core:common")
include(":core:model")
include(":core:domain")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:crypto")
include(":core:designsystem")
include(":core:ui")
include(":core:testing")

// Feature modules
include(":feature:onboarding")
include(":feature:dashboard")
include(":feature:plan")
include(":feature:transaction")
include(":feature:asset")
include(":feature:settings")
include(":feature:donation")
