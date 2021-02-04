plugins {
    `jaicf-kotlin`
    `jaicf-kotlin-serialization`
    `jaicf-publish`
}

dependencies {
    core()
    api(ktor("ktor-client-cio"))
    api(ktor("ktor-client-logging-jvm"))
    api(ktor("ktor-client-serialization-jvm"))
}
