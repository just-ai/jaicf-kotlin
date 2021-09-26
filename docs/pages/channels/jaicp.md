---
layout: default
title: JAICP
permalink: JAICP
parent: Channels
---

<p align="center">
    <img src="/assets/images/channels/jaicp.svg" width="128" height="134"/>
</p>

<h1 align="center">JAICP Channel</h1>

This channel provides full [JAICP](https://just-ai.com/en/platform.php) infrastructural support for JAICF.
You can find quickstart JAICP guide [here](Quick-Start).

## About

JAICP is used to connect your bots to JAICP infrastructure. This infrastructure will provide:

* AI-assisted dialogs analytics
* Dialogs and logs storage
* Metrics for your bots
* Multiple channel implementations
* Telephony bots and smart calls
* Cloud hosting

JAICP supports multiple channels, including Facebook, WeChat, Google Assistant, ZenDesk, and many many others. Also,
there are JAICP-native channels, such as ChatWidget, ChatApi and Telephony channels.

> Learn more about using JAICP with channels [here](https://help.just-ai.com/#/docs/en/jaicf/jaicf).

## How to use

#### 1. Include JAICP channel dependency to your _build.gradle_

```kotlin
implementation("com.just-ai.jaicf:jaicp:$jaicfVersion")
```

**Replace `$jaicfVersion` with the latest version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

And use your preferred logger implementation to log incoming requests and responses for different channels. For example:

```kotlin
implementation("ch.qos.logback:logback-classic:1.2.3")
```

#### 2. Create project in [JAICP Application Panel](https://app.jaicp.com/register?utm_source=github&utm_medium=article&utm_campaign=quickstart)

![Create first project in JAICP](/assets/gifs/create-jaicf-project.gif)

#### 3. Create suitable `JaicpServer` or `JaicpPollingConnector` to connect your bot to JAICP infrastructure

Webhook can be created using [Ktor](https://ktor.io) or [Spring Boot](https://spring.io/projects/spring-boot). Here is
implementation example which uses provided Ktor Server:

 ```kotlin
JaicpServer(
    telephonyCallScenario,
    accessToken,
    channels = listOf(
        ChatWidgetChannel,
        TelephonyChannel,
        ChatApiChannel
    )
).start(wait = true)
 ```

And Spring Boot example:

```kotlin
@Bean
fun jaicpServlet() = ServletRegistrationBean(
        JaicpServlet(
            JaicpWebhookConnector(
                botApi = citiesGameBot,
                accessToken = accessToken,
                channels = listOf(
                    ChatWidgetChannel,
                    TelephonyChannel,
                    ChatApiChannel
                )
            )
        ),
        "/"
    ).apply {
        setLoadOnStartup(1)
    }
```

Then you can use the public webhook URL (using [ngrok](https://ngrok.com) for example) to register your channel in JAICP
Web Interface.

Or use **long polling** connection. This connection does not require public webhook URL, here is an example:

 ```kotlin
 JaicpPollingConnector(
    botApi = citiesGameBot,
    accessToken = accessToken,
    channels = listOf(ChatWidgetChannel, TelephonyChannel, ChatApiChannel)
).runBlocking()
 ```

**Access token** can be acquired after creating project in JAICP Web Interface.

# Channels

JAICP supports a [list of channels](https://help.just-ai.com/docs/en/channels/channels) for your bots.
This enables you to serve all your channel-specific configurations in the JAICP Console instead of your JAICF code.

 ```kotlin
 JaicpPollingConnector(
    botApi = citiesGameBot,
    accessToken = accessToken,
    channels = listOf(TelegramChannel, SlackChannel)
).runBlocking()
 ```

But moreover this makes it possible to use JAICP features like Live Chats and Analytics (see below).

## JAICP channels

JAICP also provides a set of additional channels:

* [Telephony channel](Telephony) to connect your bot to telephony line
* [Chat Widget channel](Chat-Widget) to insert your bot to your website
* [Chat API channel](Chat-API) to easily interract with your bot from any third-party system

# Features

## Live Chats

Connecting your bot via JAICP enables a lot of customer engagement platform integrations, such as [Chat2Desk](https://help.just-ai.com/#/docs/en/operator_channels/chat2desk/incoming_chat2desk)
, [Salesforce](https://help.just-ai.com/#/docs/en/operator_channels/salesforce/salesforce)
, [JivoSite](https://help.just-ai.com/#/docs/en/operator_channels/jivosite/jivosite), and [others](https://help.just-ai.com/docs/en/operator_channels/operator_channels).

After you've created a project in [JAICP Application Panel](https://app.jaicp.com/register?utm_source=github&utm_medium=article&utm_campaign=quickstart), you can create a customer engagement platform channel and connect it to your messaging channel.

If you added dependency on `jaicp` channel your channel's `JaicpCompatibleAsyncReactions` (such as `TelegramReactions`
, `FacebookReactions`, etc.) will receive extension method `switchToLiveChat` to switch to the live chat operator if the channel is configured in **JAICP App Console**.

```kotlin
state("HelpMe") {
    activators {
        intent("HelpMe")
    }
    action {
        reactions.telegram?.say("We will shortly find someone to help you!")
        reactions.telegram?.switchToLiveChat(message = "Client ${request.clientId} requested help.")
    }
}
```

## Analytics and Session Management

JAICP has a built-in **Session** mechanics to separate dialogs into sessions for better dialogue conversation analytics.
It not only allows to use session data from a channel request, but also create an internal session in channels that don't support sessions out of the box.

```kotlin
state("NewGame") {
    activators {
        regex("/start")
        intent("NewGame")
    }
    action {
        reactions.jaicp?.startNewSession()
        reactions.say("Hello there! Let's play a game of numbers!")
        reactions.say("I've picked a number from 1 to 100 and it's time for you to guess it right")
    }
}
```

In this case whenever a new game starts we create a new dialogue session. This will afterwards allow us, for example, to
generate some statistics like how many steps it takes for a client to guess a number.

### JAICP Analytics API

JAICP also provides a number of methods to mark client messages and conversations. For example,
you can define a result of conversation with `jaicpAnalytics.setSessionResult`.
This result will be shown in analytic charts in JAICP Application Console.

```kotlin
state("Are you happy with our bot?") {
  activators {
    intent("Yes")
  }
  action {
    reactions.say("Nice!")
    jaicpAnalytics.setSessionResult("")
  }
  
  state("Are you happy with our bot?") {
    activators {
      intent("Yes")
    }
    action { 
      reactions.say("Nice!")
      jaicpAnalytics.setSessionResult("Client is happy. Developer should get a raise.")
    }
  }
  
  state("Are you happy with our bot?") {
    activators {
      intent("No")
    }
    action {
      reactions.say("That's truly awful.")
      jaicpAnalytics.setSessionResult("Client is sad. Developer should keep on improving.")
    }
  }
}
```

> See more about analytics api [JAICP Help Portal](https://help.just-ai.com/#/docs/ru/JS_API/built_in_services/analytics/analytics) and [In source code](https://help.just-ai.com/#/docs/ru/JS_API/built_in_services/analytics/analytics)
