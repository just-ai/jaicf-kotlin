import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Slack Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Aimybox Slack implementation. Enables JAICF-Kotlin integration with Slack"

plugins {
    `jaicf-kotlin`
    `jaicf-publish`
}

dependencies {
    core()
    api(`coroutines-core`())
    api("com.slack.api:bolt:1.6.1")
}