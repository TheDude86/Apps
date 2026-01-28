plugins {
    kotlin("jvm")
}

group = "com.mcmlr"
version = "1.5.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":packets:base"))
    implementation(project(":packets:spigot"))
    implementation(project(":packets:paper"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}