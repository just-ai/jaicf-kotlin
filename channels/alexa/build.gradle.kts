plugins {
    kotlin("jvm")
    id("com.justai.jaicf.plugins.internal.publish")
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib", Version.stdLib))

    api("com.fasterxml.jackson.module:jackson-module-kotlin" version {jackson})
    api("com.amazon.alexa:ask-sdk:2.37.1")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}