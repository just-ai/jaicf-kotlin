plugins {
    `jaicf-github-release`
}

allprojects {
    group = "com.just-ai.jaicf"
    version = "1.3.0"

    repositories {
        mavenCentral()
        maven(uri("https://jitpack.io"))
        google()
    }
}
