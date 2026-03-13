plugins {
    id("com.gradle.plugin-publish") version "1.2.1"
    kotlin("jvm") version "2.0.20"
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
}

group = "com.justai.jaicf"
version = "1.0.0"

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    val kotlinVersion = "2.0.20"
    val ktorVersion = "2.3.7"
    val dockerJavaApplicationPluginVersion = "9.4.0"

    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(kotlin("stdlib", kotlinVersion))
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("com.bmuschko.docker-java-application:com.bmuschko.docker-java-application.gradle.plugin:$dockerJavaApplicationPluginVersion")
}

gradlePlugin {
    plugins {
        create("caila-publish-plugin") {
            id = "com.justai.jaicf.caila-publish-plugin"
            displayName = "CAILA publish plugin"
            implementationClass = "com.justai.jaicf.plugins.caila.publish.CailaPublishPlugin"
            description = "Is used for deploying JAICF projects in a CAILA MLOps Platform."
        }
    }
}

