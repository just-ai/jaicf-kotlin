---
layout: default
title: Aimybox
permalink: Aimybox
parent: Channels
---

<p align="center">
    <img src="/assets/images/channels/aimybox.png" width="128" height="134"/>
</p>

<h1 align="center">Aimybox channel</h1>

Allows to create skills for custom voice assistant applications built on top of [Aimybox SDK](https://aimybox.com).

## How to use

#### 1. Include Aimybox dependency to your _build.gradle_

```kotlin
implementation("com.just-ai.jaicf:aimybox:$jaicfVersion")
```

**Replace `$jaicfVersion` with the latest version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

#### 2. Use Aimybox `request` and `reactions` in your scenarios' actions

```kotlin
action {
    // Arbitrary JSON object passed from the device
    val data = request.aimybox?.data

    // Add custom replies
    reactions.aimybox?.question(true)
    reactions.aimybox?.say(text = "Hello!", tts = "hello, how are you?")
    reactions.aimybox?.buttons(UrlButton("Open websitte", "https://address.com"))
    
    // Or use standard response builders
    reactions.say("How are you?")
    reactions.buttons("Good", "Bad")
}
```

> Please refer to the [Aimybox HTTP API](https://help.aimybox.com/en/category/http-api-1vrvqsw/) to learn more about available reply types.

#### 3. Create and run Aimybox webhook

Using [JAICP](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/jaicp)

_For local development:_
```kotlin
fun main() {
    JaicpPollingConnector(
        botApi = helloWorldBot,
        accessToken = "your JAICF project token",
        channels = listOf(
            AimyboxChannel
        )
    ).runBlocking()
}
```

_For cloud production:_
```kotlin
fun main() {
    JaicpServer(
        botApi = helloWorldBot,
        accessToken = "your JAICF project token",
        channels = listOf(
            AimyboxChannel
        )
    ).start(wait = true)
}
```

Using [Ktor](https://github.com/just-ai/jaicf-kotlin/wiki/Ktor)

```kotlin
fun main() {
    embeddedServer(Netty, 8000) {
        routing {
            httpBotRouting("/" to AimyboxChannel(helloWorldBot))
        }
    }.start(wait = true)
}
```

Using [Spring Boot](https://github.com/just-ai/jaicf-kotlin/wiki/Spring-Boot)

```kotlin
@Bean
fun aimyboxServlet() {
    return ServletRegistrationBean(
        HttpBotChannelServlet(AimyboxChannel(helloWorldBot)),
        "/"
    ).apply {
        setLoadOnStartup(1)
    }
}
```

#### 4. Configure Aimybox

Then you can use the public webhook URL (using [ngrok](https://ngrok.com) for example) to register a custom voice skill via [Aimybox Console](https://app.aimybox.com) or provide this URL directly to the Aimybox initialisation block of your mobile application.

## Start event

If you send an empty query, Aimybox recognises this as a `START` event instead of query request:

```kotlin
state("launch") {
    activators {
        event(AimyboxEvent.START)
    }
}
```

This can be used to start a voice skill scenario. New user session will be started automatically.

> Do not miss to include a `BaseEventActivator` in activators array of the `BotEngine` initializer. 