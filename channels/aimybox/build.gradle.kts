plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib", Version.stdLib))

    api("org.jetbrains.kotlinx:kotlinx-serialization-json" version { serializationRuntime })
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