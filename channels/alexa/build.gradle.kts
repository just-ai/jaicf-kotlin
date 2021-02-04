plugins {
    `jaicf-kotlin`
    `jaicf-publish`
}

dependencies {
    implementation(project(":core"))

    api("com.fasterxml.jackson.module:jackson-module-kotlin" version {jackson})
    api("com.amazon.alexa:ask-sdk:2.37.1")
}
