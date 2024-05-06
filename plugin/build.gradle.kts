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
    apiVersion = "4.0.5"
    serverVersion = "4.0.5"
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
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = base.archivesName.get()
        }
    }
}
