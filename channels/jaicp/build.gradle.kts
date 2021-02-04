plugins {
    `jaicf-kotlin`
    `jaicf-kotlin-serialization`
    `jaicf-publish`
    `jaicf-junit`
}

dependencies {
    implementation(project(":core"))

    implementation("org.apache.tomcat:servlet-api" version { tomcatServletApi })
    implementation("de.appelgriepsch.logback:logback-gelf-appender" version { logbackGelfAppender })
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j" version { coroutinesCore })

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
}