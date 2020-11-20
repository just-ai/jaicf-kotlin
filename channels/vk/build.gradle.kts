plugins {
    kotlin("jvm") version Version.kotlin
    `maven-publish`
}

val vkSdk = "1.0.6"
val log4jAdapter = "2.14.0"

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib", Version.stdLib))

    api("com.vk.api:sdk:$vkSdk")
    api("ch.qos.logback:logback-classic:1.2.3")
    api("io.ktor:ktor-client-cio" version { ktor })
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core" version { coroutinesCore })

    testImplementation("org.junit.jupiter:junit-jupiter-api" version { jUnit })
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