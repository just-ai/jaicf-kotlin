plugins {
    kotlin("jvm") version Version.kotlin
    kotlin("plugin.serialization") version Version.kotlin
    `maven-publish`
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib", Version.stdLib))

    implementation("javax.servlet:javax.servlet-api" version { javaxServletApi })
    implementation("de.appelgriepsch.logback:logback-gelf-appender" version { logbackGelfAppender })
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j" version { coroutinesCore })

    api("org.jetbrains.kotlinx:kotlinx-serialization-runtime" version { serializationRuntime })
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core" version { coroutinesCore })
    api("io.ktor:ktor-client-cio" version { ktor })
    api("io.ktor:ktor-client-logging-jvm" version { ktor })
    api("io.ktor:ktor-client-json-jvm" version { ktor })
    api("io.ktor:ktor-client-serialization-jvm" version { ktor })
    api("io.ktor:ktor-server-netty" version { ktor })

}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
        }
    }
}


apply {
    from(rootProject.file("release.gradle"))
}