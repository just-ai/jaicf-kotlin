plugins {
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)

    core()
    implementation(project(":activators:llm"))
    implementation(libs.jline)

    testImplementation(testFixtures(project(":activators:llm")))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
}

tasks {
    test {
        useJUnitPlatform()
    }
}