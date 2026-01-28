pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include("apps")
include("block")
include("system")
include("minetunes")
include("pluginengine")
include("folia")
include("packets")
include("packets:spigot")
findProject(":packets:spigot")?.name = "spigot"
include("packets:paper")
findProject(":packets:paper")?.name = "paper"
include("packets:base")
findProject(":packets:base")?.name = "base"
