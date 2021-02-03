plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://plugins.gradle.org/m2")
}

dependencies {
    implementation("com.github.breadmoirai:github-release:2.2.12")
    implementation(gradleApi())
}