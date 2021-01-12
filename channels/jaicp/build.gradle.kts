plugins {
    kotlin("jvm") version Version.kotlin
    kotlin("plugin.serialization") version Version.kotlin
    `maven-publish`
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib", Version.stdLib))

    implementation("org.apache.tomcat:servlet-api" version { tomcatServletApi })
    implementation("de.appelgriepsch.logback:logback-gelf-appender" version { logbackGelfAppender })
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j" version { coroutinesCore })

    api("org.jetbrains.kotlinx:kotlinx-serialization-json" version { serializationRuntime })
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core" version { coroutinesCore })
    api("io.ktor:ktor-client-cio" version { ktor })
    api("io.ktor:ktor-client-logging-jvm" version { ktor })
    api("io.ktor:ktor-client-json-jvm" version { ktor })
    api("io.ktor:ktor-client-serialization-jvm" version { ktor })
    api("io.ktor:ktor-server-netty" version { ktor })


    testImplementation("io.mockk:mockk" version { mockk })
    testImplementation("io.ktor:ktor-client-mock" version { ktor })
    testImplementation(kotlin("test-junit"))
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api" version { jUnit })
    testImplementation("org.junit.jupiter:junit-jupiter-engine" version { jUnit })
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