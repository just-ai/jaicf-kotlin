<p align="center">
    <img src="https://i.imgur.com/jMsp7uq.png" width="128" height="128"/>
</p>

<h1 align="center">Telegram messenger channel</h1>

Allows to create chatbots for [Telegram](https://core.telegram.org/bots).

_Built on top of [Kotlin Telegram Bot](https://github.com/kotlin-telegram-bot/kotlin-telegram-bot) library._

## How to use

#### 1. Include Telegram dependency to your _build.gradle_

```kotlin
implementation("com.justai.jaicf:telegram:$jaicfVersion")
```

**Replace `$jaicfVersion` with the latest version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

Also add _Jitpack_ to repositories:

```kotlin
repositories {
    mavenCentral()
    jcenter()
    maven(uri("https://jitpack.io"))
}
```

#### 2. Use Telegram `request` and `reactions` in your scenarios' actions

```kotlin
action {
    // Telegram incoming message
    val message = request.telegram?.message

    // Fetch username
    val username = message?.chat?.username
    
    // Use Telegram-specified response builders
    reactions.telegram?.say("Are you agree?", listOf("Yes", "No"))
    reactions.telegram?.image("https://address.com/image.jpg", "Image caption")
    reactions.telegram?.api?.sendAudio(message?.chat?.id, File("audio.mp3"))

    // Or use standard response builders
    reactions.say("Hello there!")
    reactions.image("https://address.com/image.jpg")
}
```

_Note that Telegram bot works as long polling. This means that every reactions' method actually sends a response to the user._

> Refer to the [TelegramReactions](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/telegram/src/main/kotlin/com/justai/jaicf/channel/telegram/TelegramReactions.kt) class to learn more about available response builders.

#### Native API

You can use native Telegram API directly via `reactions.telegram?.api`.
This enables you to build any response that Telegram supports using channel-specific features.
As well as fetch some data from Telegram bot API (like [getMe](https://core.telegram.org/bots/api#getme) for example).

```kotlin
action {
    val me = reactions.telegram?.run {
        api.getMe().first?.body()?.result
    }
}
```

> Learn more about available API methods [here](https://github.com/kotlin-telegram-bot/kotlin-telegram-bot/blob/master/telegram/src/main/kotlin/me/ivmg/telegram/Bot.kt).

#### 3. Create a new bot in Telegram

Create a new bot using Telegram's `@BotFather` and any Telegram client as described [here](https://core.telegram.org/bots#6-botfather).
Copy your new bot's **access token** to the clipboard.

#### 4. Create and run Telegram channel

Using [JAICP](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/jaicp)

_For local development:_
```kotlin
fun main() {
    JaicpPollingConnector(
        botApi = helloWorldBot,
        accessToken = "your JAICF project token",
        channels = listOf(
            TelegramChannel
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
            TelegramChannel
        )
    ).start(wait = true)
}
```

Or locally:
```kotlin
fun main() {
    TelegramChannel(helloWorldBot, "access token").run()
}
```

## Commands

Telegram enables users not only to send a text queries or use buttons.
It also provides an ability to send [commands](https://core.telegram.org/bots#commands) that start from slash.

The most known command of the Telegram is "/start" that is sending once the user starts using your chatbot.
Your scenario must handle this command via [regex activator](https://github.com/just-ai/jaicf-kotlin/wiki/Regex-Activator) to react on the first user's request.

```kotlin
object HelloWorldScenario: Scenario() {
    init {
        state("main") {
            activators {
                regex("/start")
            }
    
            action {
                reactions.say("Hello there!")
            }
        }
    }
}
```

To make it work, just add `RegexActivator` to the array of activators in your agent's configuration:

```kotlin
val helloWorldBot = BotEngine(
    model = HelloWorldScenario.model,
    activators = arrayOf(
        RegexActivator,
        CatchAllActivator
    )
)
```

The same way you can react on ony other Telegram commands.

## Events

User can send not only a text queries to your Telegram bot.
They can also send contacts and locations for example.
These messages contain non-text queries and can be handled in your scenarios via `event` activators.

```kotlin
state("events") {
    activators {
        event(TelegramEvent.LOCATION)
        event(TelegramEvent.CONTACT)
    }

    action {
        val location = request.telegram?.location
        val contact = request.telegram?.contact
    }
}
```

## Buttons

Telegram allows to add [keyboard](https://core.telegram.org/bots#keyboards) or [inline keyboard](https://core.telegram.org/bots#inline-keyboards-and-on-the-fly-updating) to the text message reply.
This means that it's not possible to add a keyboard without an actual text response.

```kotlin
action {
    reactions.say("Click on the button below")
    reactions.buttons("Click me", "Or me")
}
```

> This code generates [inline]((https://core.telegram.org/bots#inline-keyboards-and-on-the-fly-updating)) keyboard right below the text "Click on the button below".
Once the user clicks on any of these buttons, the title of the clicked one returns to the bot as a new query. 

To add any keyboard to the response, you can use a channel-specific methods:

```kotlin
action {
    // Append inline keyboard
    reactions.telegram?.say("Are you agree?", listOf("Yes", "No"))

    // Append arbitrary keyboard layout
    reactions.telegram?.say(
        "Could you please send me your contact?", 
        replyMarkup = KeyboardReplyMarkup(
            listOf(listOf(KeyboardButton("Send", requestContact = true), KeyboardButton("No")))
        )
    )
}
```

You can also remove keyboard sending a `ReplyKeyboardRemove` in the response:

```kotlin
action {
    reactions.telegram?.say("Okay then!", replyMarkup = ReplyKeyboardRemove())
}
```

> Refer to the [TelegramReactions](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/telegram/src/main/kotlin/com/justai/jaicf/channel/telegram/TelegramReactions.kt) class to learn more about buttons replies.
