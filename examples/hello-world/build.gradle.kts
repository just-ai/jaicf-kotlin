plugins {
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)

    core()
    implementation(libs.slf4j.log4j12)
    implementation(libs.slf4j.simple)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)

    implementation(project(":channels:jaicp"))
    implementation(project(":channels:telegram")) {
        exclude(group = "io.ktor")
    }
    implementation(project(":channels:alexa"))
    implementation(project(":channels:google-actions"))
    implementation(project(":channels:facebook"))
    implementation(project(":channels:aimybox"))
    implementation(project(":channels:viber"))

    implementation(project(":activators:dialogflow"))
    implementation(project(":activators:lex"))

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
}

tasks {
    test {
        useJUnitPlatform()
    }
}
