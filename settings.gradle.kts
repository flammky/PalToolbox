pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
    }

    plugins {
        kotlin("jvm").version("1.9.22")
        id("org.jetbrains.compose").version("1.6.0")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}


rootProject.name = "PalToolbox"
include("unrealengine")

dependencyResolutionManagement {

    versionCatalogs.create("libs") {

        library(
            "androidx.compose.material3.material3",
            "androidx.compose.material3",
            "material3"
        ).version("1.2.0")
    }
}
