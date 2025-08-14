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
    implementation(project(":activators:llm"))

    implementation(libs.slf4j.simple)
    implementation(libs.slf4j.log4j12)
    implementation(libs.jline)

    testImplementation(testFixtures(project(":activators:llm")))
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