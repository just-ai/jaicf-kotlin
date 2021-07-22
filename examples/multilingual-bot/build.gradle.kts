plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib", Version.stdLib))
    implementation(project(":core"))
    implementation(project(":channels:jaicp"))
    implementation(project(":activators:caila"))

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