plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.justai.jaicf.plugins.internal.publish")
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib", Version.stdLib))

    api("org.jetbrains.kotlinx:kotlinx-serialization-json" version { serializationRuntime })
    api("io.ktor:ktor-client-cio" version { ktor })
    api("io.ktor:ktor-client-serialization-jvm" version { ktor })

    testImplementation("org.junit.jupiter:junit-jupiter-api" version {jUnit})
    testRuntime("org.junit.jupiter:junit-jupiter-engine" version {jUnit})
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
