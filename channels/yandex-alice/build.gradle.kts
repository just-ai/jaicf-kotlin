plugins {
    `jaicf-kotlin`
    `jaicf-kotlin-serialization`
    `jaicf-publish`
}

dependencies {
    core()
    api(jackson())
    api(ktor("ktor-client-cio"))
    api(ktor("ktor-client-serialization-jvm"))
    implementation(kotlin("reflect", Version.reflect))
}
