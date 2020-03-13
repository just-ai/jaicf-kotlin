val coreProject: Project
    get() = rootProject.project("core")

buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.10.0")
    }
}

allprojects {

    group = "com.justai.jaicf"
    version = "0.1.0"

    repositories {
        google()
        jcenter()
        mavenCentral()
        mavenLocal()
        maven(url = "https://jitpack.io")
    }

}