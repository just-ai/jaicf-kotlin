plugins {
    `jaicf-github-release`
}

allprojects {
    group = "com.justai.jaicf"
    version = "0.14.0-BETA"

    repositories {
        google()
        jcenter()
        mavenCentral()
        maven(uri("https://jitpack.io"))
    }
}