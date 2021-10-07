plugins {
    `kotlin-dsl`
}

repositories {
    maven(uri("https://plugins.gradle.org/m2"))
    mavenCentral()
}

val kotlinVersion = "1.4.21"
val reflectVersion = "1.4.21"

dependencies {
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(kotlin("serialization", kotlinVersion))
    implementation(kotlin("reflect", reflectVersion))
    implementation("com.github.breadmoirai:github-release:2.2.12")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.4.30")
    implementation(gradleApi())
}