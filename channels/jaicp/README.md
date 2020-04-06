<p align="center">
    <img src="https://just-ai.com/en/img/jaicp_black_v.svg" width="128" height="134"/>
</p>

<h1 align="center">JAICP Channel</h1>

This channel is created to provide full [JAICP](https://just-ai.com/en/platform.php) infrastructural support for JAICF.

JAICP is just about to go public. [Contact us](https://join.slack.com/t/jaicf/shared_invite/zt-clzasfyq-f4gv8hf3JHD4RmpMtrt0Aw) to get early access.
 
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

#### 2. Create project in JAICP Web Interface

> This section soon will become public, but not right now.

#### 3. Create suitable `JaicpWebhookConnector` or `JaicpPollingConnector` to connect your bot to JAICP infrastructure
Webhook can be created using [Ktor](https://ktor.io) or [Spring Boot](https://spring.io/projects/spring-boot). Here is Ktor implementation example:
 ```kotlin
 embeddedServer(Netty, 8000) {
     routing {
         httpBotRouting(
             "/" to JaicpWebhookConnector(
                 botApi = citiesGameBot,
                 accessToken = accessToken,
                 channels = listOf(
                     ChatWidgetChannel,
                     TelephonyChannel,
                     ChatApiChannel
                 )
             )
         )
     }
 }.start(wait = true)
 ```
And Spring Boot example:
```kotlin
@Bean
fun jaicpServlet() = ServletRegistrationBean(
    HttpBotChannelServlet(
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
 **Access token** can be aquired after creating project in JAICP Web Interface.
 
 See full example for JAICP channel [here](https://github.com/just-ai/jaicf-kotlin/tree/master/examples/jaicp-telephony).

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
You also can send audio with **TelephonyReactions.audio** function. Here is the full example:
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
## ChatWidgetChannel

ChatWidgetChannel can be used to insert a widget onto your page and process incoming messages from it.
 
> Guides how to create a widget and embed it into your website will soon be public

## ChatApiChannel

ChatApiChannel can be used to process simple POST and GET requests with queries.

> Guides how to create a chatapi and requests examples will soon be public