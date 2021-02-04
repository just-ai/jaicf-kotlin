plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib", Version.stdLib))

    api("com.github.kotlin-telegram-bot:kotlin-telegram-bot:6.0.1") {
        exclude("com.github.kotlin-telegram-bot.kotlin-telegram-bot", "webhook")
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
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