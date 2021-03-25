plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://plugins.gradle.org/m2")
}

val kotlinVersion = "1.4.21"
val reflectVersion = "1.4.21"

dependencies {
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(kotlin("serialization", kotlinVersion))
    implementation(kotlin("reflect", reflectVersion))
    implementation("com.github.breadmoirai:github-release:2.2.12")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:0.10.0")
    implementation(gradleApi())
}