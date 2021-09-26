---
layout: default
title: Ktor
permalink: Ktor
parent: Environments
---

![](/assets/images/env/ktor.png)

[Ktor](https://ktor.io) is a framework for building asynchronous servers.
It allows you to quickly setup any HTTP server like Netty or Jetty and serve your JAICF agent.

# How to use

This server can be used with any channel that implements [HttpBotChannel](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/channel/http/HttpBotChannel.kt) interface.

> Learn more about channels [here](Channels).

#### 1. Append Ktor dependency to build.gradle

```kotlin
implementation("io.ktor:ktor-server-netty:1.3.1")
```

or

```kotlin
implementation("io.ktor:ktor-server-jetty:1.3.1")
```

#### 2. Configure routing and run server

Use `httpBotRouting` extension to create a routing for [HttpBotChannel](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/channel/http/HttpBotChannel.kt) endpoints.

```kotlin
fun main() {
    embeddedServer(Netty, 8000) {
        routing {
            httpBotRouting(
                "/alexa" to AlexaChannel(gameClockBot),
                "/actions" to ActionsFulfillment.dialogflow(gameClockBot)
            )
        }
    }.start(wait = true)
}
```

The same for `Jetty` server.