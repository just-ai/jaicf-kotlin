plugins {
    `jaicf-github-release`
}

allprojects {
    group = "com.just-ai.jaicf"
    version = "1.1.1"

    repositories {
        google()
        jcenter()
        mavenCentral()
        maven(uri("https://jitpack.io"))
    }
}
