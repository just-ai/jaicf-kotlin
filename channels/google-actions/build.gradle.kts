plugins {
    `jaicf-kotlin`
    `jaicf-publish`
}

dependencies {
    core()
    api(jackson())
    api("com.google.actions:actions-on-google:1.8.0")
}