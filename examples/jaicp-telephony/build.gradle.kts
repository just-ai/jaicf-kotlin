plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib", Version.stdLib))

    implementation(project(":core"))
    implementation(project(":channels:jaicp"))
    implementation(project(":activators:caila"))

    implementation("ch.qos.logback:logback-classic:${Version.logback}")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}