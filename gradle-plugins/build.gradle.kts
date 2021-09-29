import com.gradle.publish.PluginBundleExtension

plugins {
    kotlin("jvm") version "1.3.61" apply false
    id("com.gradle.plugin-publish") version "0.12.0" apply false
}

subprojects {
    group = "com.just-ai.jaicf"

    repositories {
        mavenCentral()
        maven(uri("https://plugins.gradle.org/m2"))
        maven(uri("https://jitpack.io"))
        google()
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
}