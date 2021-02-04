plugins {
    `jaicf-kotlin`
    `jaicf-publish`
    `jaicf-junit`
}

dependencies {
    implementation(project(":core"))

    api("org.mapdb:mapdb:3.0.8")
}