import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin MongoDB Bot Context Manager"
ext[POM_DESCRIPTION] = "MongoDB BotContextManager implementation to store your JAICF bot's context"

plugins {
    `jaicf-kotlin`
    `jaicf-junit`
    `jaicf-publish`
}

dependencies {
    core()
    api(jackson())
    api("org.mongodb:mongodb-driver-sync:4.1.1")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:3.0.0")
    implementation(kotlin("reflect", Version.reflect))
}
