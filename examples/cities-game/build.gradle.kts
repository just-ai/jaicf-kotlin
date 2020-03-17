plugins {
    kotlin("jvm") version Version.kotlin
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":core"))
    implementation(project(":channels:google-actions"))
    implementation(project(":channels:jaicp"))
    implementation(project(":channels:telegram"))
    implementation(project(":channels:facebook"))
    implementation(project(":activators:caila"))

    implementation("io.ktor:ktor-server-netty" version {ktor})
    implementation("ch.qos.logback:logback-classic:1.2.3")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}