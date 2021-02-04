plugins {
    `jaicf-kotlin`
    `jaicf-kotlin-serialization`
    `jaicf-publish`
    `jaicf-junit`
}

dependencies {
    core()
    api(ktor("ktor-client-cio"))
    api(ktor("ktor-client-serialization-jvm"))
}
