plugins {
    `jaicf-github-release`
}

allprojects {
    group = "com.just-ai.jaicf"
    version = "1.2.4-SNAPSHOT"

    repositories {
        mavenCentral()
        maven(uri("https://jitpack.io"))
        google()
    }
}
