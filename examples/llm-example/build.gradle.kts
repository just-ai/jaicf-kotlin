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
    implementation(project(":telemetry:opentelemetry"))
    implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.43.0")
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.sdk.trace)
    implementation("io.opentelemetry:opentelemetry-exporter-logging:1.43.0")

    testImplementation(testFixtures(project(":activators:llm")))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.opentelemetry.sdk)
    testImplementation(libs.opentelemetry.sdk.trace)
}

tasks {
    test {
        useJUnitPlatform()
    }
}