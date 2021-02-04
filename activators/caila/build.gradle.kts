plugins {
    `jaicf-kotlin`
    `jaicf-kotlin-serialization`
    `jaicf-publish`
}

dependencies {
    implementation(project(":core"))

    api("io.ktor:ktor-client-cio" version { ktor })
    api("io.ktor:ktor-client-logging-jvm" version { ktor })
    api("io.ktor:ktor-client-serialization-jvm" version { ktor })
}
