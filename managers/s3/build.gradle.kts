import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin AWS S3 Bot Context Manager"
ext[POM_DESCRIPTION] = "AWS S3 BotContextManager implementation to store your JAICF bot's context"

plugins {
    id("org.jetbrains.kotlin.jvm")
    `jaicf-publish`
}

dependencies {
    core()
    api(libs.jackson.module.kotlin)
    api(libs.aws.s3)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.bundles.junit)
    testImplementation(libs.mockk)
    testImplementation(testFixtures(project(":core")))
}

tasks.named<Test>("test") { useJUnitPlatform() }