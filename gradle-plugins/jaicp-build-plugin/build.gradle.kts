plugins {
    kotlin("jvm")
}

version = "0.1.1"

gradlePlugin {
    plugins {
        create("jaicp-build-plugin") {
            id = "com.just-ai.jaicf.jaicp-build-plugin"
            displayName = "JAICP Cloud build plugin"
            implementationClass = "com.just-ai.jaicf.plugins.jaicp.build.JaicpBuildPlugin"
            description = "Is used for deploying JAICF projects in a JAICP Cloud."
        }
    }
}

dependencies {
    implementation(kotlin("stdlib", "2.2.0"))
    implementation("com.github.jengelman.gradle.plugins", "shadow", "[5.0.0,)")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    test {
        useJUnitPlatform()
    }
}

