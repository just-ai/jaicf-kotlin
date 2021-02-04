plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://plugins.gradle.org/m2")
}

val kotlinVersion = "1.4.10"
val reflectVersion = "1.4.0"

dependencies {
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(kotlin("serialization", kotlinVersion))
    implementation(kotlin("reflect", reflectVersion))
    implementation("com.github.breadmoirai:github-release:2.2.12")
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:0.10.0")
    implementation(gradleApi())
}