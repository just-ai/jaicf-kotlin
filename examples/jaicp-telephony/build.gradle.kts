plugins {
    `jaicf-kotlin`
}

dependencies {

    implementation(project(":core"))
    implementation(project(":channels:jaicp"))
    implementation(project(":activators:caila"))

    implementation("ch.qos.logback:logback-classic:1.2.3")
}
