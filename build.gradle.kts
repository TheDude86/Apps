plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    id("com.google.devtools.ksp") version "2.2.0-2.0.2" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

apply(plugin = "maven-publish")

group = "com.mcmlr"
version = "0.2.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":apps"))
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
