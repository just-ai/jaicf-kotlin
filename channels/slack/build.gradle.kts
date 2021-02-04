plugins {
    `jaicf-kotlin`
    `jaicf-publish`
}

dependencies {
    implementation(project(":core"))

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core" version { coroutinesCore })
    api("com.slack.api:bolt:1.0.1")
}