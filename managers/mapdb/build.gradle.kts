import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin MapDB Bot Context Manager"
ext[POM_DESCRIPTION] = "MapDB BotContextManager implementation to store your JAICF bot's context"

plugins {
    `jaicf-kotlin`
    `jaicf-publish`
    `jaicf-junit`
}

dependencies {
    core()
    api(jackson())
    api("org.mapdb:mapdb:3.0.8")
}