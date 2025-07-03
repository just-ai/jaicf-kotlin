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
    api(okHttp("okhttp"))
    api(okHttp("logging-interceptor"))
    api(`coroutines-core`())
    api("com.openai:openai-java:2.11.0") {
        exclude("com.squareup.okhttp3", "okhttp")
        exclude("com.squareup.okhttp3", "logging-interceptor")
    }
    implementation(jackson())
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:${Version.jackson}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Version.jackson}")
    testImplementation("io.mockk:mockk" version { mockk })
    testImplementation(ktor("ktor-client-mock"))
}
