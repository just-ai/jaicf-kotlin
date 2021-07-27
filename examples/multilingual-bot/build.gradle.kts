plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib", Version.stdLib))
    implementation(project(":core"))
    implementation(project(":channels:jaicp"))
    implementation(project(":channels:telegram"))
    implementation(project(":activators:caila"))
    implementation(ktor("ktor-client-jackson"))
    implementation("ch.qos.logback:logback-classic:1.2.3")

    testImplementation(kotlin("test-junit"))
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api" version { jUnit })
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine" version { jUnit })
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    test {
        useJUnitPlatform()
    }
}