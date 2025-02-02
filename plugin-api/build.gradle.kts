import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("java-library")
}

base {
    archivesName = "lavalyrics-plugin-api"
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17

    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(projects.main)
    api("org.jetbrains:annotations:24.0.1")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = base.archivesName.get()
        }
    }
}