import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin MapDB Bot Context Manager"
ext[POM_DESCRIPTION] = "MapDB BotContextManager implementation to store your JAICF bot's context"

plugins {
    alias(libs.plugins.kotlin.jvm)
    `jaicf-publish`
}

dependencies {
    core()
    api(libs.jackson.module.kotlin)
    api(libs.map.db)

    implementation(libs.eclipse.collections)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.bundles.junit)
    testImplementation(testFixtures(project(":core")))
}

tasks.named<Test>("test") { useJUnitPlatform() }
