import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin MongoDB Bot Context Manager"
ext[POM_DESCRIPTION] = "MongoDB BotContextManager implementation to store your JAICF bot's context"

plugins {
    alias(libs.plugins.kotlin.jvm)
    `jaicf-publish`
}

dependencies {
    core()
    api(libs.jackson.module.kotlin)
    api(libs.mongodb.driver.sync)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.embed.mongo)
    testImplementation(libs.bundles.junit)
    testImplementation(testFixtures(project(":core")))
}

tasks.named<Test>("test") { useJUnitPlatform() }
