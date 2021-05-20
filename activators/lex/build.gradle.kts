import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Lex Activator Adapter"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Lex Activator Adapter. Provides intent recognition and named entity extraction."

plugins {
    `jaicf-kotlin`
    `jaicf-publish`
}

dependencies {
    core()
    api(jackson())
    api(platform("software.amazon.awssdk:bom:2.14.3"))
    api("software.amazon.awssdk:lexruntimev2:2.15.69")
    api("software.amazon.awssdk:lexmodelsv2:2.15.69")

    testApi("io.mockk:mockk:1.10.0")
    testApi("org.junit.jupiter:junit-jupiter-api" version {jUnit})
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine" version {jUnit})
}

tasks {
    test {
        useJUnitPlatform()
    }
}
