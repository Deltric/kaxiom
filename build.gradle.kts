plugins {
    kotlin("jvm") version "1.9.22"
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

kotlin {
    jvmToolchain(17)
}

group = "dev.kaxiom"
version = "1.0.2-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}

tasks.shadowJar {
    archiveClassifier.set("")

    dependencies {
        include(dependency("com.google.code.gson:gson"))
    }

    relocate("com.google.gson", "dev.kaxiom.shaded.com.google.gson")
}

tasks.named("build") {
    dependsOn("shadowJar")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Deltric/kaxiom")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}