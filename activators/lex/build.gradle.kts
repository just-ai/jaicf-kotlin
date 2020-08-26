plugins {
    kotlin("jvm") version Version.kotlin
    `maven-publish`
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib", Version.stdLib))

    api(platform("software.amazon.awssdk:bom:2.14.3"))
    api("software.amazon.awssdk:lexruntime")

    testApi("io.mockk:mockk:1.10.0")
    testApi("org.junit.jupiter:junit-jupiter-api" version {jUnit})
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