import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("dev.arbjerg.lavalink.gradle-plugin") version "1.0.15"
}

base {
    archivesName = "lavalyrics-plugin"
}

lavalinkPlugin {
    name = "lavalyrics-plugin"
    apiVersion = gitHash("436763fe09b32fe26ee40cd238e3d1e910749cf9")
    serverVersion = gitHash("436763fe09b32fe26ee40cd238e3d1e910749cf9")
    configurePublishing = false
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(project(":main"))
    implementation(project(":protocol"))
    implementation(project(":plugin-api"))
    compileOnly("dev.arbjerg.lavalink:Lavalink-Server:436763fe09b32fe26ee40cd238e3d1e910749cf9-SNAPSHOT")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = base.archivesName.get()
        }
    }
}
