plugins {
    kotlin("jvm")
}

group = "com.mcmlr"
version = "1.5.0"

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/nms/")

    maven("https://hub.spigotmc.org/nexus/content/groups/public/")

    maven("https://libraries.minecraft.net/") {
        metadataSources {
            mavenPom()
            artifact()
            ignoreGradleMetadataRedirection()
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))

    compileOnly("org.spigotmc:spigot-api:1.21.6-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot:1.21.6-R0.1-SNAPSHOT:remapped-mojang")
}

tasks.test {
    useJUnitPlatform()
}