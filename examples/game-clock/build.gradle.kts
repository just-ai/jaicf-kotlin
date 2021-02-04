plugins {
    `jaicf-kotlin`
    `jaicf-junit`
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    core()

    implementation(project(":channels:jaicp"))
    implementation(project(":channels:alexa"))
    implementation(project(":channels:google-actions"))

    implementation(ktor("ktor-server-netty"))

    implementation(slf4j("slf4j-simple"))
    implementation(slf4j("slf4j-log4j12"))
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}