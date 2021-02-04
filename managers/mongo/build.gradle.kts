plugins {
    `jaicf-kotlin`
    `jaicf-junit`
    `jaicf-publish`
}

dependencies {
    implementation(project(":core"))

    api("org.mongodb:mongodb-driver-sync:4.1.1")
    api("com.fasterxml.jackson.module:jackson-module-kotlin" version {jackson})
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:3.0.0")
}
