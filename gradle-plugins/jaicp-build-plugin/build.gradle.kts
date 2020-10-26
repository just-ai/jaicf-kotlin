plugins {
    kotlin("jvm") version "1.3.61"
    `maven-publish`
    `java-gradle-plugin`
}

version = "0.1.0"

gradlePlugin {
    plugins {
        create("jaicp-build-plugin") {
            id = "com.justai.jaicf.jaicp-build-plugin"
            implementationClass = "com.justai.jaicf.plugins.jaicp.build.JaicpBuildPlugin"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib", "1.3.61"))
    implementation("com.github.jengelman.gradle.plugins", "shadow", "6.1.0")
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

apply(from = rootProject.file("release/bintray.gradle"))