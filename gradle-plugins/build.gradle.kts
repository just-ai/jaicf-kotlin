buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
    }
}

subprojects {
    group = "com.justai.jaicf"

    repositories {
        google()
        jcenter()
        mavenCentral()
        mavenLocal()
        maven(uri("https://jitpack.io"))
    }

    afterEvaluate {
        apply(from = rootProject.file("release/bintray.gradle"))
    }
}