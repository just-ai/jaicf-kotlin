import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Alexa Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Alexa Channel implementation. Enables JAICF-Kotlin integration with Amazon Alexa."

plugins {
    `jaicf-kotlin`
    `jaicf-publish`
}

dependencies {
    core()
    api(jackson())
    api("com.amazon.alexa:ask-sdk:2.37.1")
    implementation(kotlin("reflect", Version.reflect))
}
