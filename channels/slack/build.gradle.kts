plugins {
    `jaicf-kotlin`
    `jaicf-publish`
}

dependencies {
    core()
    api(`coroutines-core`())
    api("com.slack.api:bolt:1.0.1")
}