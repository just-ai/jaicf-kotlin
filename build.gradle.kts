plugins {
    `jaicf-github-release`
}

allprojects {
    group = "com.just-ai.jaicf"
    version = "1.0.0-RC-SNAPSHOT"

    repositories {
        google()
        jcenter()
        mavenCentral()
        maven(uri("https://jitpack.io"))
    }
}