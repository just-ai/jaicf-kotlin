plugins {
    `jaicf-kotlin`
    `jaicf-junit`
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(project(":channels:jaicp"))
    implementation(project(":channels:alexa"))
    implementation(project(":channels:google-actions"))

    implementation("io.ktor:ktor-server-netty" version {ktor})

    implementation("org.slf4j:slf4j-simple" version {slf4j})
    implementation("org.slf4j:slf4j-log4j12" version {slf4j})
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}