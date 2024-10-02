plugins {
    `jaicf-github-release`
}

allprojects {
    group = "com.just-ai.jaicf"
    version = "1.3.7-SNAPSHOT"

    repositories {
        mavenCentral()
        maven(uri("https://jitpack.io"))
        google()
    }
}
