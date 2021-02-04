plugins {
    `jaicf-kotlin`
    `jaicf-junit`
}

repositories {
    mavenCentral()
}

dependencies {
    core()

    implementation(slf4j("slf4j-simple"))
    implementation(slf4j("slf4j-log4j12"))
    implementation(ktor("ktor-server-netty"))

    implementation(project(":channels:jaicp"))
    implementation(project(":channels:telegram"))
    implementation(project(":channels:alexa"))
    implementation(project(":channels:google-actions"))
    implementation(project(":channels:facebook"))
    implementation(project(":channels:aimybox"))

    implementation(project(":activators:dialogflow"))
}