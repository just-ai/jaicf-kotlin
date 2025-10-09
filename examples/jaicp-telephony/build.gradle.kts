plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(libs.kotlin.stdlib)

    core()
    implementation(project(":channels:jaicp"))
    implementation(project(":activators:caila"))

    implementation(libs.logback.classic)
}
