plugins {
    kotlin("jvm")
    id("io.github.patrick.remapper") version "1.4.0"
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(project(":folia"))
    implementation(project(":packets"))
    implementation(project(":packets:base"))

    //Spigot
    compileOnly("org.spigotmc:spigot:1.21.6-R0.1-SNAPSHOT:remapped-mojang")

    //Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.remap {
    version.set("1.21.6")
}

tasks.build {
    dependsOn(tasks.remap)
}

kotlin {
    jvmToolchain(21)
}