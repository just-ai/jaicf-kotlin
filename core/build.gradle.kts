import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Core component"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Core component. Provides DSL, Tests API and multiple implementable interfaces."

plugins {
    id("org.jetbrains.kotlin.jvm")
    `jaicf-publish`
    `java-test-fixtures`
}

dependencies {
    api(libs.slf4j.api)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.tomcat.servlet.api)
    implementation(libs.ktor.server.core)
    implementation(libs.bundles.junit)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.bundles.junit)
    testImplementation(libs.logback.classic)

    testFixturesApi(libs.bundles.junit)
    testFixturesApi(libs.logback.classic)
}

tasks.named<Test>("test") { useJUnitPlatform() }
