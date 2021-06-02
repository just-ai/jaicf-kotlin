<p align="center">
    <img src="https://raw.githubusercontent.com/just-ai/jaicf-kotlin/master/channels/jaicp/JACP-icon.svg" width="128" height="134"/>
</p>

<h1 align="center">JAICP Channel</h1>

This channel is created to provide full [JAICP](https://just-ai.com/en/platform.php) infrastructural support for JAICF.
You can find quickstart guide [here](https://github.com/just-ai/jaicf-kotlin/wiki/Quick-Start).

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

**Replace `$jaicfVersion` with the latest
version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

And use your preferred logger implementation to log incoming requests and responses for different channels. For example:

```kotlin
implementation("ch.qos.logback:logback-classic:1.2.3")
```

#### 2. Create project in [JAICP Application Panel](https://app.jaicp.com/register?utm_source=github&utm_medium=article&utm_campaign=quickstart)

![Create first project in JAICP](https://i.imgur.com/5r35CCv.gif)

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

See full example for JAICP channel [here](https://github.com/just-ai/jaicf-kotlin/tree/master/examples/jaicp-telephony).

# Channels

## ChatWidgetChannel

> ChatWidget JAICP Documentation can be found [here](https://help.just-ai.com/#/docs/en/channels/chatwidget/chatwidget).

ChatWidgetChannel can be used to insert a widget onto your page and process incoming messages from it.

![Create first channel](https://i.imgur.com/wsfuFoh.gif)

Here is example usage:

```kotlin
state("start") {
    globalActivators {
        intent("Hello")
        regex("/start")
    }
    action {
        reactions.say("Hi! Here's some questions I can help you with.")
        reactions.chatwidget?.buttons("How to save the earth", "How to stop drinking")
    }
}
```

#### Passing parameters to Chat Widget

Sometimes it is necessary to be able to pass some parameters when loading the widget. For example, to let the bot know
the user's ID, name or other data. Such parameters are transferred to the widget when the widget is opened on the
website.

These parameters can be set to widget page, as
it [referenced in widget documentation](https://help.just-ai.com/#/docs/en/channels/chatwidget/parameters_transfer), and
retrieved in scenario from `reactions.chatwidget.jaicp.data` json object.

## TelephonyChannel

> TelephonyChannel JAICP Documentation can be found [here](https://help.just-ai.com/#/docs/en/telephony/telephony).

TelephonyChannel can be used to process incoming calls and make smart outgoing calls with JAICP. It provides a list of
TelephonyEvents, for example, **TelephonyEvents.speechNotRecognized**, which will be sent if ASR service cannot
recognize user query.

```kotlin
state("noSpeech") {
    globalActivators {
        event(TelephonyEvents.speechNotRecognized)
    }
}
```

You also can send an audio with **TelephonyReactions.audio** function. Here is the full example:

```kotlin
state("noSpeech") {
    globalActivators {
        event(TelephonyEvents.speechNotRecognized)
    }
    action {
        reactions.telephony?.audio("https://www2.cs.uic.edu/~i101/SoundFiles/taunt.wav")
    }
}
```

Client data can be accessed from **TelephonyBotRequest** class:

```kotlin
fallback {
    reactions.say("You said: ${request.input}")
    request.telephony?.let {
        logger.info("Unrecognized message ${request.input} from caller: ${it.caller}")
    }
}
```

### BargeIn Feature

> Barge-In is speech synthesis or audio playback interruption in telephony channel.

JAICF provides DSL methods to efficiently handle when client interrupts telephony bot. Let's look at following scenario:

```kotlin
val HelloBargeIn = Scenario(telephony) {
    state("start") {
        activators {
            regex("/start")
        }
        action {
            reactions.say(
                "Hello! My name is Jessica and I will help you check your order details. Did you order an iPhone yesterday?",
                bargeInContext = "/WelcomeContext"
            )
        }
    }

    state("WelcomeContext") {
        state("Operator") {
            activators {
                intent("Operator")
            }
            action {
                reactions.say("Okay!")
                reactions.transferCall("<OPERATOR_NUMBER>")
            }
        }
    }
}
```

Let's imagine we're calling a client and saying a welcome phrase. He or she may recognize that's a call from bot and
immediately ask for an operator.

Using

```kotlin
reactions.say(
    "Hello! My name is Jessica and I will help you check your order details. Did you order an iPhone yesterday?",
    bargeInContext = "/WelcomeContext"
)
```

we define that any text client interrupts bot should be processed inside a particular context `/WelcomeContext`. In this
case bot will be interrupted only if client input matches intent `Operator`, while any other phrase,
like `Hello! I'm listening!` or `Oh yeah!` in the middle of input will not interrupt synthesis.

> The idea behind this API is that we should allow interruption only if bot knows what to answer.

### BargeIn Reactions API

We provide two arguments to `TelephonyReactions`'s interruptible methods `say` and `audio`:

* `bargeInContext: String` defines a context in which BotEngine tries to find a state and resolve if we should interrupt
  on client input or not.


* `bargeIn: Boolean` defines if we should try select a state and resolve interruption with current `DialogContext`.
  Usage example for `bargeIn`:

```kotlin
val HelloBargeIn = Scenario(telephony) {
    val waitingState = "PlaySongWhileClientWaits"
    state("exampleAudio") {
        action {
            reactions.say("Let me play you a song while you're waiting!")
            reactions.go(waitingState)
        }
    }

    state(waitingState) {
        action {
            reactions.audio("http://example.com/audio", bargeIn = true)
        }
    }

    state("HowMuchToWait") {
        activators {
            intent("HowMuchToWait")
        }
        action {
            reactions.say("We should find you an operator in 3 to 5 minutes. Keep waiting!")
            reactions.go(waitingState)
        }
    }

    state("AreYouHere") {
        activators {
            intent("AreYouHere")
        }
        action {
            reactions.say("Yeah, I'm here we're about to find an operator to answer your question. Keep waiting!")
            reactions.go(waitingState)
        }
    }
}
```

In this case we play a client a song while he waits for the operator. We don't interrupt song if he says something to
his/her friends, we reply only to questions like "How much left to wait?" that we know what to respond.

### BargeIn Customization

We provide an open class `BargeInProcessor` which performs low-level logics to resolve if client input should interrupt
speech synthesis or audio playback.

## ChatApiChannel

> ChatApiChannel JAICP Documentation can be found [here](https://help.just-ai.com/#/docs/en/chat_api/chat_api).

ChatApiChannel can be used to process simple POST and GET requests with queries. The only reaction this channel can
process is `reactions.say`.

# Features

## Live Chats

Connecting your bot via JAICP enables a lot of customer engagement platform integrations, such
as [Chat2Desk](https://help.just-ai.com/#/docs/en/operator_channels/chat2desk/incoming_chat2desk)
, [Salesforce](https://help.just-ai.com/#/docs/en/operator_channels/salesforce/salesforce)
, [JivoSite](https://help.just-ai.com/#/docs/en/operator_channels/jivosite/jivosite), and many others.

After you created a project
in [JAICP Application Panel](https://app.jaicp.com/register?utm_source=github&utm_medium=article&utm_campaign=quickstart)
, you can create a customer engagement platform channel and connect it to your messaging channel.

#### Usage in scenario:

If you added dependency on `jaicp` channel your channel's `JaicpCompatibleAsyncReactions` (such as `TelegramReactions`
, `FacebookReactions`, etc.) will receive extension method receive method `switchToLiveChat` to switch to live chat
operator if channel is configured in **JAICP App Console**.

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
It not only allows to use session data from a channel request (for example, from Yandex-Alice), but also create an
internal session in channels which don't support sessions out of the box.

#### Usage in scenario:

```kotlin
state("NewGame") {
    activators {
        regex("/start")
        intent("NewGame")
    }
    action {
        reactions.jaicp?.startNewSession()
        reactions.say("Hello there! Let's play a game of numbers!")
        reacions.say("I've picked a number from 1 to 100 and it's time for you to guess it right")
    }
}
```

In this case whenever a new game starts we create a new dialogue session. This will afterwards allow us, for example, to
generate some statistics like how many steps it takes for a client to guess a number. 
