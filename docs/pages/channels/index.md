---
layout: default
title: Channels
nav_order: 6
permalink: Channels
has_children: true
---

Channel in terms of JAICF is a messaging or voice platform that actually receives a user's requests through some user interface.

# Currently supported platforms

### JAICP

[JAICP](https://app.jaicp.com) (_Just AI Conversational Platform_) provides a wide range of channels and features for your chat and voice bots.
Like [telephony](Telephony), [web widget](Chat-Widget), live operators and others.
Moreover JAICP transparently handles queries and reactions to store it to the internal database providing you with comprehensive conversations analytics tool.

Learn more about JAICP and how to use it [here](JAICP).

### Voice assistants

* [Aimybox](Aimybox)
* [Amazon Alexa](Alexa)
* [Google Actions](Google-Actions)

### Messengers

* [Facebook Messenger](Facebook-Messenger)
* [Slack](Slack)
* [Telegram](Telegram)
* [Viber](Viber)
* [Алиса](Yandex-Alice)

# Multi-channel support

JAICF is a multi-channel (or multi-platform) framework, meaning that a single conversational agent could be connected to multiple channels and work via multiple channels **simultaneously**.

This can be achieved _without a lack of channel-related features_, because JAICF is built on top of native channel libraries and provides an access to the native request and response objects.

## Some examples

```kotlin
state("main") {

    activators {
        catchAll()
        event(AlexaEvent.LAUNCH)
    }

    action {
        var name = context.client["name"]

        if (name == null) {
            request.telegram?.run {
                name = message.chat.firstName ?: message.chat.username
            }
            request.facebook?.run {
                name = reactions.facebook?.queryUserProfile()?.firstName()
            }
        }
    }
}
```

In this example you can see how the user's name could be retrieved from the different channel-specific request data.
Thanks to Kotlin extensions feature, it is possible to use `request.telegram?`, `request.facebook?` and others null-safe variables to have an access to the channel-related native requests.

> Learn more about request [here](request).

Here is another example that shows how to build a channel-specified responses.

```kotlin
state("cancel") {
    activators {
        intent(AlexaIntent.CANCEL)
        intent("CancelIntent")
    }

    action {
        reactions.run {
            say("Okay $break200ms See you latter then! Bye bye!")

            actions?.endConversation()

            alexa?.run {
                stopAudioPlayer()
                endSession()
            }

        }
    }
}
```

In the code snippet above you can see, how `reactions` object is used to build a response from an agent.
You can use some common response builder methods like `say` to send a simple text or speech response.
At the same time a channel-related native methods could be used as well via null-safe variables like `reactions.actions?` for Google Actions, `reactions.alexa?` for Alexa and etc.

> Learn more about reactions [here](reactions).

As you can see, every channel in JAICF **defines its own** [BotRequest](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/api/BotRequest.kt) and [Reactions](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/reactions/Reactions.kt) **implementations** enabling the conversational agent to achieve a channel-specific features.

This makes JAICF a multi-platform solution for chatbots and voice assistants building.

# Channel types

Channels are divided by the protocol type that is used in different platforms.
Some channels like Alexa or Google Assistant are synchronous webhooks meaning that a single response is allowed for each user's request.
Others can be asynchronous webhooks, long polling and websockets like Facebook Messenger, Slack or Telegram, meaning that multiple responses are allowed to send for every user's request.

# How to use

To make it possible to connect your JAICF agent to some channel or channels, you have to provide channel-specific configuration.
Here is an example:

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

Here you can see how a single [Ktor server](Ktor) can be started to serve requests for both webhook endpoints of your JAICF `gameClockBot`.

Here is another example that shows how can be served a Facebook Messenger requests:

```kotlin
fun main() {
    val channel = FacebookChannel(
        helloWorldBot,
        FacebookPageConfig(
            pageAccessToken = "EAAIBNxZCCzjoBADyorVAY21KniOikUxVYjhmnZBElHpeN1vr9lEJzXJdLGUsvcvwTRMmNwwZBZBDEZCBPXlZB0UuwU1o3CZCdm0WJILg1ucoNB9ezKeZBbOvy29prWeZAuLA4L5G9lg5yZBZCfwnLAPEZB9W3YLvO20uZBCfHtARowF8PPG2VKk6YAmPZC",
            appSecret = "11deaea42beda58ddfef1b1eeab57338",
            verifyToken = "jaicf-verify-token"
        )
    )

    embeddedServer(Netty, 8000) {
        routing {

            httpBotRouting("/" to channel)

            get("/") {
                call.respondText(
                    channel.verifyToken(
                        call.parameters["hub.mode"],
                        call.parameters["hub.verify_token"],
                        call.parameters["hub.challenge"]
                    )
                )
            }
        }
    }.start(wait = true)
}
```

Some channels could be configured much easier:

```kotlin
fun main() {
    TelegramChannel(helloWorldBot, "580468601:AAHaMg4gOsN2A_zvIO6-PouVk3GcZ_WVrdI").run()
}
```

> Please learn more about how to connect to the specified channel in the channel-related library documentation [here](https://github.com/just-ai/jaicf-kotlin/tree/master/channels).