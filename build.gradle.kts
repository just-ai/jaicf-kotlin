plugins {
    `jaicf-github-release`
}

allprojects {
    group = "com.justai.jaicf"
    version = "0.13.1"

    repositories {
        google()
        jcenter()
        mavenCentral()
        maven(uri("https://jitpack.io"))
    }
}