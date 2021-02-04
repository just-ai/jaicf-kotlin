plugins {
    `jaicf-kotlin`
    `jaicf-kotlin-serialization`
    `jaicf-publish`
}

dependencies {
    implementation(project(":core"))

    api("com.fasterxml.jackson.module:jackson-module-kotlin" version {jackson})
    api("io.ktor:ktor-client-cio" version { ktor })
    api("io.ktor:ktor-client-serialization-jvm" version { ktor })
    api("io.ktor:ktor-client-logging-jvm" version { ktor })
}
