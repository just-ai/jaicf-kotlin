plugins {
    kotlin("jvm") version Version.kotlin
    `maven-publish`
}

dependencies {
    implementation(kotlin("stdlib", Version.stdLib))

    api("org.slf4j:slf4j-api" version {slf4j})

    implementation("javax.servlet:javax.servlet-api" version {javaxServletApi})
    implementation("io.ktor:ktor-server-core" version {ktor})
    implementation("org.junit.jupiter:junit-jupiter-api" version {jUnit})
    implementation("com.github.h0tk3y:kotlin-fun:v0.9")

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