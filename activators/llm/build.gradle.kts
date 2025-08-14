import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin LLM Activator Adapter"
ext[POM_DESCRIPTION] = "JAICF-Kotlin LLM Activator Adapter."

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    `jaicf-publish`
    `java-test-fixtures`
}

dependencies {
    core()
    api(libs.okhttp)
    api(libs.okhttp.logging.interceptor)
    api(libs.kotlinx.coroutines.core)
    api(libs.openai.java) {
        exclude("com.squareup.okhttp3", "okhttp")
        exclude("com.squareup.okhttp3", "logging-interceptor")
    }
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.datatype.jdk8)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.modelcontextprotocol)

    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlin.stdlib)

    api(libs.ktor.client)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.jackson)

    testImplementation(libs.ktor.mockk)
    testImplementation(libs.bundles.junit)

    testFixturesImplementation(project(":core"))
    testFixturesImplementation(libs.bundles.junit)
}

tasks.named<Test>("test") { useJUnitPlatform() }
