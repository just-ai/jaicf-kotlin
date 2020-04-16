plugins {
    kotlin("jvm") version Version.kotlin
    kotlin("plugin.serialization") version Version.kotlin
    `maven-publish`
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib", Version.stdLib))

    implementation("javax.servlet:javax.servlet-api" version { javaxServletApi })

    api("org.jetbrains.kotlinx:kotlinx-serialization-runtime" version { sertializationRuntime })
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core" version { coroutinesCore })
    api("io.ktor:ktor-client-cio" version { ktor })
    api("io.ktor:ktor-client-logging-jvm" version { ktor })

    testCompile(kotlin("test-junit"))
    testCompile(kotlin("test"))
    testCompile(project(":channels:facebook"))
    testCompile(project(":channels:google-actions"))
    testCompile("org.junit.jupiter:junit-jupiter-api" version { jUnit })
    testRuntime("org.junit.jupiter:junit-jupiter-engine" version { jUnit })
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