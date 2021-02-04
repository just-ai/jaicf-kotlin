plugins {
    `jaicf-kotlin`
    `jaicf-kotlin-serialization`
    `jaicf-publish`
}

dependencies {
    implementation(project(":core"))

    api("com.github.messenger4j:messenger4j:1.1.0")
}
