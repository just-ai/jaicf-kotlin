plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

repositories {
    maven(uri("https://plugins.gradle.org/m2"))
    mavenCentral()
}

val kotlinVersion = "2.0.20"
val reflectVersion = "2.0.20"

dependencies {
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(kotlin("serialization", kotlinVersion))
    implementation(kotlin("reflect", reflectVersion))
    implementation("com.github.breadmoirai:github-release:2.2.12")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.4.32")
    implementation(gradleApi())
}