plugins {
    `jaicf-kotlin`
    `jaicf-junit`
    `jaicf-publish`
}

dependencies {
    core()
    api(jackson())
    api("org.mongodb:mongodb-driver-sync:4.1.1")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:3.0.0")
}
