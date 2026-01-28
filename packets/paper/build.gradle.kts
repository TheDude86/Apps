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
    testImplementation(kotlin("test"))


//    compileOnly("org.spigotmc:spigot:1.21.6-R0.1-SNAPSHOT:remapped-mojang")
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}
