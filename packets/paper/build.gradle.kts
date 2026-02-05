plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":packets:base"))
    implementation(project(":packets:paper:1_20_5"))
    implementation(project(":packets:paper:1_21_8"))
    implementation(project(":packets:paper:1_21_11"))
    testImplementation(kotlin("test"))

    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}
