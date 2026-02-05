plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    id("com.google.devtools.ksp") version "2.2.0-2.0.2" apply false
    id("com.gradleup.shadow") version "9.3.1" apply false
    id("maven-publish")
}

allprojects {
    group = "com.mcmlr"
    version = "1.5.2"
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "java")


    tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        mergeServiceFiles()
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = project.group as String
                artifactId = project.name
                version = project.version as String

                from(components["java"])
            }
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.3.0"))

    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}
