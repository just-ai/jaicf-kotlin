import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Lex Activator Adapter"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Lex Activator Adapter. Provides intent recognition and named entity extraction."

plugins {
    `jaicf-kotlin`
    `jaicf-publish`
    `jaicf-junit`
}

dependencies {
    core()
    api(jackson())
    api("software.amazon.awssdk:lexruntimev2:2.15.69")

    testImplementation("io.mockk:mockk" version { mockk })
}
