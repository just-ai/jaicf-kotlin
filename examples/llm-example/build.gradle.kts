plugins {
    id("org.jetbrains.kotlin.jvm")
    application
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
    implementation(libs.slf4j.simple)

    testImplementation(testFixtures(project(":activators:llm")))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
}

application {
    mainClass.set("com.justai.jaicf.examples.llm.LLMTelemetryAgentExampleKt")
}

tasks.named<JavaExec>("run") {
    environment("OPENAI_API_KEY", System.getenv("OPENAI_API_KEY") ?: "")
    environment("OPENAI_BASE_URL", System.getenv("OPENAI_BASE_URL") ?: "")
}

tasks {
    test {
        useJUnitPlatform()
    }
}