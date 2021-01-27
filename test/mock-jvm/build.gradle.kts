plugins {
    kotlin("jvm") version Version.kotlin
    `maven-publish`
}

dependencies {
    implementation(kotlin("stdlib", Version.stdLib))
    implementation(project(":core"))

    api("org.slf4j:slf4j-api" version { slf4j })
    api("io.mockk:mockk" version { mockk })
    api("org.jetbrains.kotlinx:atomicfu" version { atomicfu })
    api("org.junit.jupiter:junit-jupiter-api" version { jUnit })

    testImplementation(project(":channels:telegram"))
    testImplementation(project(":channels:aimybox"))
    testImplementation(project(":activators:caila"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine" version { jUnit })
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