<p align="center">
    <img src="https://raw.githubusercontent.com/just-ai/jaicf-kotlin/master/channels/jaicp/JACP-icon.svg" width="128" height="134"/>
</p>

<h1 align="center">JAICP Channel</h1>

This channel is created to provide full [JAICP](https://just-ai.com/en/platform.php) infrastructural support for JAICF. You can find quickstart guide [here](https://github.com/just-ai/jaicf-kotlin/wiki/Quick-Start).
 
## About
JAICP is used to connect your bots to JAICP infrastructure. This infrastructure will provide:  
* AI-assisted dialogs analytics
* Dialogs and logs storage
* Metrics for your bots
* Multiple channel implementations
* Telephony bots and smart calls
* Cloud hosting

JAICP supports multiple channels, including Facebook, WeChat, Google Assistant, ZenDesk, and many many others. 
Also, there are JAICP-native channels, such as ChatWidget, ChatApi and Telephony channels. 

## How to use

#### 1. Include JAICP channel dependency to your _build.gradle_

```kotlin
implementation("com.justai.jaicf:jaicp:$jaicfVersion")
```

**Replace `$jaicfVersion` with the latest version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

And use your preferred logger implementation to log incoming requests and responses for different channels. For example:
```kotlin
implementation("ch.qos.logback:logback-classic:1.2.3")
```

#### 2. Create project in [JAICP Application Panel](https://app.jaicp.com/register?utm_source=github&utm_medium=article&utm_campaign=quickstart)

![Create first project in JAICP](https://i.imgur.com/5r35CCv.gif)

#### 3. Create suitable `JaicpServer` or `JaicpPollingConnector` to connect your bot to JAICP infrastructure
Webhook can be created using [Ktor](https://ktor.io) or [Spring Boot](https://spring.io/projects/spring-boot). Here is implementation example which uses provided Ktor Server:
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
                ChatApiChannel)
            )
        ),
        "/"
    ).apply {
        setLoadOnStartup(1)
    }
```
Then you can use the public webhook URL (using [ngrok](https://ngrok.com) for example) to register your channel in JAICP Web Interface.

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
 
 ## ChatWidgetChannel

ChatWidgetChannel can be used to insert a widget onto your page and process incoming messages from it.

![Create first channel](https://i.imgur.com/wsfuFoh.gif)

Here is example usage:
```kotlin
state("start"){
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
Sometimes it is necessary to be able to pass some parameters when loading the widget. 
For example, to let the bot know the user's ID, name or other data. 
Such parameters are transferred to the widget when the widget is opened on the website.

These parameters can be set to widget page, as it [referenced in widget documentation](https://help.just-ai.com/#/docs/en/channels/chatwidget/parameters_transfer), 
and retrieved in scenario from `reactions.chatwidget.jaicp.data` json object.

## TelephonyChannel

TelephonyChannel can be used to process incoming calls and make smart outgoing calls with JAICP. 
It provides a list of TelephonyEvents, for example, **TelephonyEvents.speechNotRecognized**, which will be send 
if ASR service cannot recognize user query.
```kotlin
state("/playAudio") {
    globalActivators {
        event(TelephonyEvents.speechNotRecognized)
    }
}
```
You also can send an audio with **TelephonyReactions.audio** function. Here is the full example:
```kotlin
state("/playAudio") {
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

## ChatApiChannel

ChatApiChannel can be used to process simple POST and GET requests with queries. The only reaction this channel can process is `reactions.say`.
