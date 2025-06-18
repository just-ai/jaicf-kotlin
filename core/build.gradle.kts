import plugins.publish.POM_DESCRIPTION
import plugins.publish.POM_NAME

ext[POM_NAME] = "JAICF-Kotlin Core component"
ext[POM_DESCRIPTION] = "JAICF-Kotlin Core component. Provides DSL, Tests API and multiple implementable interfaces."

plugins {
    `jaicf-kotlin`
    `jaicf-publish`
    `jaicf-junit`
    `java-test-fixtures`
}

dependencies {
    api(slf4j("slf4j-api"))

    implementation(`tomcat-servlet`())
    implementation(ktor("ktor-server-core"))
    implementation("org.junit.jupiter:junit-jupiter-api" version { jUnit })
    implementation("org.junit.jupiter:junit-jupiter-params" version { jUnit })

    testImplementation(kotlin("test-junit"))
    testImplementation("ch.qos.logback:logback-classic:1.2.3")

    testFixturesApi("org.junit.jupiter:junit-jupiter-api" version { jUnit })
    testFixturesApi(kotlin("test-junit"))
    testFixturesApi("ch.qos.logback:logback-classic:1.2.3")
}
