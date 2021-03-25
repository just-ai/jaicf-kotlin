import com.gradle.publish.PluginBundleExtension

plugins {
    kotlin("jvm") version "1.3.61" apply false
    id("com.gradle.plugin-publish") version "0.12.0" apply false
}

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.10.0")
    }
}

subprojects {
    group = "com.just-ai.jaicf"

    repositories {
        google()
        jcenter()
        mavenCentral()
        mavenLocal()
        maven(uri("https://jitpack.io"))
    }

    apply(plugin = "com.gradle.plugin-publish")
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "org.gradle.java-gradle-plugin")

    extensions.configure<PluginBundleExtension> {
        website = "https://framework.just-ai.com"
        vcsUrl = "https://github.com/just-ai"
        tags = listOf("jaicf")

        mavenCoordinates {
            groupId = project.group.toString()
            artifactId = project.name
        }
    }

    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>(project.name) {
                from(components["java"])
            }
        }
    }

    apply(from = rootProject.file("release/bintray.gradle"))
}