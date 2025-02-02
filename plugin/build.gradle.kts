import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("dev.arbjerg.lavalink.gradle-plugin") version "1.1.2"
}

base {
    archivesName = "lavalyrics-plugin"
}

lavalinkPlugin {
    name = "lavalyrics-plugin"
    apiVersion = "4.0.5"
    serverVersion = "4.0.8"
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

tasks.named("jar") {
    dependsOn(":plugin-api:jar")
    dependsOn(":main:jar")
}

dependencies {
    implementation(projects.main)
    implementation(projects.protocol)
    implementation(projects.pluginApi)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = base.archivesName.get()
        }
    }
}
