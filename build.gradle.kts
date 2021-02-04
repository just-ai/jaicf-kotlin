plugins {
    id("com.justai.jaicf.plugins.internal.github")
}

allprojects {

    group = "com.justai.jaicf"
    version = "0.13.0"

    repositories {
        google()
        jcenter()
        mavenCentral()
        mavenLocal()
        maven(uri("https://jitpack.io"))
    }

}