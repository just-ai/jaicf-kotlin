import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin MapDB Bot Context Manager"
ext[POM_DESCRIPTION] = "MapDB BotContextManager implementation to store your JAICF bot's context"

plugins {
    `jaicf-kotlin`
    `jaicf-publish`
    `jaicf-junit`
}

sourceSets.all {
    configurations.getByName(runtimeClasspathConfigurationName) {
        attributes.attribute(Attribute.of("org.gradle.jvm.environment", String::class.java), "standard-jvm")
    }
    configurations.getByName(compileClasspathConfigurationName) {
        attributes.attribute(Attribute.of("org.gradle.jvm.environment", String::class.java), "standard-jvm")
    }
}

dependencies {
    core()
    api(jackson())
    api("org.mapdb:mapdb:3.0.8")

    testImplementation(testFixtures(project(":core")))
}