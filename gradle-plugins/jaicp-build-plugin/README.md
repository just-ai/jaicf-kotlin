<h1 align="center">JAICP Cloud build plugin</h1>

**This Gradle plugin must be used for deploying your JAICF project in a _JAICP Cloud_.**

It provides a `jaicpBuild` Gradle task that will be called during a _JAICP Cloud_ build process.
The `jaicpBuild` task uses the `shadowJar` task from [ShadowJar Gradle plugin](https://github.com/johnrengelman/shadow) 
with some _JAICP Cloud_ related configurations for building executable `.jar` file of your bot.

## How to use

### 1. Add plugin to your Gradle build

- Using `buildscript` block

_build.gradle.kts_
```kotlin
buildscript {
    repositories {
        maven(url = "https://dl.bintray.com/just-ai/jaicf")
    }

    dependencies {
        classpath("com.justai.jaicf", "jaicp-build-plugin", "0.1.0")
    }
}

apply(plugin = "com.justai.jaicf.jaicp-build-plugin")
```

- Using `plugins` and `pluginManagement` blocks

_build.gradle.kts_
```kotlin
plugins {
    id("com.justai.jaicf.jaicp-build-plugin") version "0.1.0"
}
```

_settings.gradle.kts_
```kotlin
pluginManagement {
    repositories {
        maven(url = "https://dl.bintray.com/just-ai/jaicf")
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.justai.jaicf.jaicp-build-plugin") {
                useModule("com.justai.jaicf:jaicp-build-plugin:${requested.version}")
            }
        }
    }
}
```

### 2. Configure your build

The plugin uses the `shadowJar` task for building a `.jar` file, so if you want to customize your `.jar` file
that will run in _JAICP Cloud_, you should just configure the `shadowJar` task.

**Note:** `archiveFileName` and `destinationDirectory` of the `shadowJar` task will be reconfigured 
during the `jaicpBuild` task execution, but will remain unchanged if the `jaicpBuild` isn't called, so 
you can use custom values for this properties for local `shadowJar` builds.

```kotlin
tasks.withType<ShadowJar> {
    configurations = listOf(project.configurations.compile.get())

    destinationDirectory.set(project.file("build/libs/local"))
    archiveFileName.set("filename-for-local-development.jar")

    manifest {
        attributes["Main-Class"] = "org.example.BotKt"
        attributes["Description"] = "This bot is built with JAICF"
    }
}
```

If you don't need to customize the `shadowJar` or if you want to use different main classes for local development 
and _JAICP Cloud_, you can provide `mainClassName` to the `jaicpBuld` task. In this case `mainClassName` will be 
set to provided one during the `jaicpBuild` execution.

```kotlin
tasks.withType<JaicpBuild> {
    mainClassName.set("org.example.BotKt")
}
```