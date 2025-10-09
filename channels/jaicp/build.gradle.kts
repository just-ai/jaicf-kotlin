import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin JAICP Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin JAICP Channel implementation. Enables JAICF-Kotlin integration with JAICP infrastructure and channels"

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    `jaicf-publish`
}

dependencies {
    core()

    implementation(libs.tomcat.servlet.api)

    api(libs.kotlinx.coroutines.slf4j)
    api(libs.logback.gelf.appender)
    api(libs.kotlinx.coroutines.core)

    api(libs.ktor.client.cio.jvm)
    api(libs.ktor.client.logging)
    api(libs.ktor.client.json)
    api(libs.ktor.client.serialization)
    api(libs.ktor.kotlinx.serialization)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.server.netty)
    implementation(platform(libs.ktor.bom))

    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.mockk)
    testImplementation(libs.ktor.mockk)
    testImplementation(libs.bundles.junit)
}

tasks.named<Test>("test") { useJUnitPlatform() }
