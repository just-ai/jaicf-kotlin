plugins {
    `jaicf-kotlin`
}

dependencies {
    core()

    implementation(project(":core"))
    implementation(project(":channels:jaicp"))
    implementation(project(":activators:caila"))

    implementation("ch.qos.logback:logback-classic:1.2.3")
}
