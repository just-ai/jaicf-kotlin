plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://plugins.gradle.org/m2")
}

dependencies {
    implementation(kotlin("gradle-plugin", version = "1.4.10"))
    implementation(kotlin("serialization", version = "1.4.10"))
    implementation("com.github.breadmoirai:github-release:2.2.12")
    implementation(gradleApi())
}