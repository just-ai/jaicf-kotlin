plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)

    core()
    implementation(project(":channels:jaicp"))
    implementation(project(":channels:telegram")) {
        exclude(group = "io.ktor")
    }
    implementation(project(":activators:caila"))

    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.logback.classic)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
}

tasks {
    test {
        useJUnitPlatform()
    }
}