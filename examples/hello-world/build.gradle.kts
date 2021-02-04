plugins {
    `jaicf-kotlin`
    `jaicf-junit`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation("org.slf4j:slf4j-simple" version {slf4j})
    implementation("org.slf4j:slf4j-log4j12" version {slf4j})
    implementation("io.ktor:ktor-server-netty" version {ktor})

    implementation(project(":channels:jaicp"))
    implementation(project(":channels:telegram"))
    implementation(project(":channels:alexa"))
    implementation(project(":channels:google-actions"))
    implementation(project(":channels:facebook"))
    implementation(project(":channels:aimybox"))

    implementation(project(":activators:dialogflow"))
}