plugins {
    `jaicf-kotlin`
    `jaicf-publish`
}

dependencies {
    implementation(project(":core"))

    api("com.fasterxml.jackson.module:jackson-module-kotlin" version {jackson})
    api("com.google.actions:actions-on-google:1.8.0")
}