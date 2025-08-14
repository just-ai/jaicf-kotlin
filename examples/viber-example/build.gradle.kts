plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)

    core()
    implementation(project(":channels:jaicp"))
    implementation(project(":channels:viber"))

    implementation(libs.slf4j.simple)
    implementation(libs.slf4j.log4j12)
    implementation(libs.ktor.server.netty)
}
