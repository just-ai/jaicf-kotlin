plugins {
    `jaicf-github-release`
}

allprojects {
    group = "com.just-ai.jaicf"
    version = "1.3.6"

    repositories {
        mavenCentral()
        maven(uri("https://jitpack.io"))
        google()
    }
}
