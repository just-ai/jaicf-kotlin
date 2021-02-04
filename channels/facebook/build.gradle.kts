plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.justai.jaicf.plugins.internal.publish")
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib", Version.stdLib))

    api("com.github.messenger4j:messenger4j:1.1.0")
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
