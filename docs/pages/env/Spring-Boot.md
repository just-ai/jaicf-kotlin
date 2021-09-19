---
layout: default
title: Spring Boot
permalink: Spring-Boot
parent: Environments
---

![](https://commons.bmstu.wiki/images/5/59/Spring-boot-logo.png)

[Spring Boot](https://spring.io/projects/spring-boot) makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run".
It allows you to serve your JAICF agents as a HTTP servlets.

# How to use

This server can be used with any channel that implements [HttpBotChannel](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/channel/http/HttpBotChannel.kt) interface.

> Learn more about channels [here](Channels).

#### 1. Append Spring Boot dependencies to build.gradle

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.spring") version "1.3.61"
    id("org.springframework.boot") version "2.2.2.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "1.8"
  }
}
```

#### 2. Configure and run server

Use [HttpBotChannelServlet](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/channel/http/HttpBotChannelServlet.kt) for [HttpBotChannel](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/channel/http/HttpBotChannel.kt) endpoints.

Here is an example of [Amazon Alexa channel](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/alexa) configuration:

```kotlin
@Configuration
@ServletComponentScan
class Context {

    @WebServlet("/")
    class AlexaController: HttpBotChannelServlet(
        AlexaChannel(helloWorldBot)
    )
}
```

And then start a server:

```kotlin
fun main(args: Array<String>) {
  runApplication<BlogApplication>(*args) {
    setBannerMode(Banner.Mode.OFF)
  }
}
```
