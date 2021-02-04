plugins {
    `jaicf-kotlin`
    `jaicf-publish`
}

dependencies {
    core()
    api(jackson())
    api("com.amazon.alexa:ask-sdk:2.37.1")
}
