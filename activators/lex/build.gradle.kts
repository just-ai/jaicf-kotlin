import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Lex Activator Adapter"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Lex Activator Adapter. Provides intent recognition and named entity extraction."

plugins {
    id("org.jetbrains.kotlin.jvm")
    `jaicf-publish`
}

dependencies {
    core()
    api(libs.jackson.module.kotlin)
    api(libs.lexruntimev2)

    implementation(libs.kotlin.stdlib)

    testImplementation(libs.mockk)
    testImplementation(libs.bundles.junit)
}

tasks.named<Test>("test") { useJUnitPlatform() }
