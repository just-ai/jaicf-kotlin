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

dependencies {
    compileOnly(libs.kotlin.serialization)

    implementation(libs.github.release)
    implementation(libs.dokka.gradle.plugin)
    implementation(libs.kotlin.reflect)
    implementation(gradleApi())
}