<h1 align="center">JAICP Cloud build plugin</h1>

[![Download](https://api.bintray.com/packages/just-ai/jaicf/jaicp-build-plugin/images/download.svg) ](https://bintray.com/just-ai/jaicf/jaicp-build-plugin/_latestVersion)

**This Gradle plugin must be used for deploying your JAICF project in a _JAICP Cloud_.**

It provides a `jaicpBuild` Gradle task that will be called during a _JAICP Cloud_ build process.
The `jaicpBuild` task uses the `shadowJar` task from [ShadowJar Gradle plugin](https://github.com/johnrengelman/shadow) 
with some _JAICP Cloud_ related configurations for building executable `.jar` file of your bot.

## How to use

### 1. Add plugin to your Gradle build

- Using `plugins` block

_build.gradle.kts_
```kotlin
plugins {
    id("com.justai.jaicf.jaicp-build-plugin") version "0.1.1"
}
```

- Using `buildscript` block

_build.gradle.kts_
```kotlin
buildscript {
    repositories {
        maven(url = "https://dl.bintray.com/just-ai/jaicf")
    }

    dependencies {
        classpath("com.justai.jaicf", "jaicp-build-plugin", "0.1.1")
    }
}

apply(plugin = "com.justai.jaicf.jaicp-build-plugin")
```

### 2. Configure your build

The plugin uses the `shadowJar` task for building a `.jar` file, so if you want to customize your `.jar` file
that will run in _JAICP Cloud_, you should just configure the `shadowJar` task.

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

**Note:** `archiveFileName` and `destinationDirectory` of the `shadowJar` task will be reconfigured 
during the `jaicpBuild` task execution, but will remain unchanged if the `jaicpBuild` isn't called, so 
you can use custom values for these properties for local `shadowJar` builds. 

During _JAICP Cloud_ build these properties values can be accessed via the following project properties:
`com.justai.jaicf.jaicp.build.jarFileName` and `com.justai.jaicf.jaicp.build.jarDestinationDir`.


## jaicpBuild task configuration

The only property of `jaicpBuild` task is `mainClassName`.
By default, it will be inherited from `shadowJar` configuration, but if you don't need to customize the `shadowJar` 
or if you want to use different main classes for local development  and _JAICP Cloud_, you can provide `mainClassName` 
directly to the `jaicpBuld` task. 
In this case `mainClassName` will be set to provided one during the `jaicpBuild` execution.

```kotlin
tasks.withType<JaicpBuild> {
    mainClassName.set("org.example.BotKt")
}
```

## ShadowJar versions support

By default, the latest verion of the `ShadowPlugin` applies, but any versions greater than `5.0.0` are also supported.
If you want to use a custom `ShadowJar` version, just apply the `ShadowJar` plugin with version specified:
```
plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.justai.jaicf.jaicp-build-plugin") version "0.1.1"
}
```