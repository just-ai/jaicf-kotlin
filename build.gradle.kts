val coreProject: Project
    get() = rootProject.project("core")

buildscript {
    repositories {
        google()
        jcenter()
        maven("https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
        classpath("com.github.breadmoirai:github-release:2.2.12")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.10.0")
    }
}

allprojects {

    group = "com.justai.jaicf"
    version = "0.2.0"

    repositories {
        google()
        jcenter()
        mavenCentral()
        mavenLocal()
    }

}

apply {
    from("release/github.gradle")
}