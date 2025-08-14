plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)

    core()
    implementation(project(":channels:jaicp"))
    implementation(project(":channels:alexa"))
    implementation(project(":channels:google-actions"))

    implementation(libs.ktor.server.netty)

    implementation(libs.slf4j.log4j12)
    implementation(libs.slf4j.simple)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
}

tasks {
    test {
        useJUnitPlatform()
    }
    build {
        dependsOn(shadowJar)
    }
}