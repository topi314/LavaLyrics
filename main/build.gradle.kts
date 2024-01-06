plugins {
   `java-library`
}

base {
    archivesName = "lavalyrics"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    compileOnly("dev.arbjerg:lavaplayer:2.0.4")
    compileOnly("org.jetbrains:annotations:24.0.1")
    implementation("org.jetbrains.kotlin:kotlin-annotations-jvm:1.9.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = base.archivesName.get()
        }
    }
}

