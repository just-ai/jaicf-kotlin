plugins {
    `jaicf-github-release`
}

allprojects {

    group = "com.justai.jaicf"
    version = "0.12.0"

    repositories {
        google()
        jcenter()
        mavenCentral()
        mavenLocal()
        maven(uri("https://jitpack.io"))
    }

}