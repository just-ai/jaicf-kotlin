import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin LLM Activator Adapter"
ext[POM_DESCRIPTION] = "JAICF-Kotlin LLM Activator Adapter."

plugins {
    `jaicf-kotlin`
    `jaicf-kotlin-serialization`
    `jaicf-publish`
    `jaicf-junit`
}

dependencies {
    core()
    api(ktor("ktor-client-apache"))
    api(ktor("ktor-client-jackson"))
    api(ktor("ktor-client-logging-jvm"))
    implementation("com.knuddels:jtokkit:0.6.1")
    testImplementation("io.mockk:mockk" version { mockk })
    testImplementation(ktor("ktor-client-mock"))
}
