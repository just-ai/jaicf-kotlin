import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin JAICP Channel"
ext[POM_DESCRIPTION] = "JAICF-Kotlin JAICP Channel implementation. Enables JAICF-Kotlin integration with JAICP infrastructure and channels"

plugins {
    `jaicf-kotlin`
    `jaicf-kotlin-serialization`
    `jaicf-publish`
    `jaicf-junit`
}

dependencies {
    core()

    implementation(`tomcat-servlet`())

    api("de.appelgriepsch.logback:logback-gelf-appender" version { logbackGelfAppender })
    api(kotlinx("kotlinx-coroutines-slf4j") version { coroutinesCore })
    api(`coroutines-core`())

    api(ktor("ktor-client-cio"))
    api(ktor("ktor-client-logging-jvm"))
    api(ktor("ktor-client-json-jvm"))
    api(ktor("ktor-client-serialization-jvm"))
    api(ktor("ktor-server-netty"))

    testImplementation("io.mockk:mockk" version { mockk })
    testImplementation("io.ktor:ktor-client-mock" version { ktor })
    testImplementation(kotlin("test-junit"))
    testImplementation(kotlin("test"))
}