<p align="center">
    <img src="https://i.imgur.com/jMsp7uq.png" width="128" height="128"/>
</p>

<h1 align="center">Telegram messenger channel</h1>

Allows to create chatbots for [Telegram](https://core.telegram.org/bots).

_Built on top of [Kotlin Telegram Bot](https://github.com/kotlin-telegram-bot/kotlin-telegram-bot) library._

## How to use

#### 1. Include Telegram dependency to your _build.gradle_

```kotlin
repositories {
    jcenter()
    mavenCentral()
    maven(uri("https://jitpack.io"))
}
dependencies {
    implementation("com.justai.jaicf:telegram:$jaicfVersion")
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
    reactions.telegram?.image("https://address.com/image.jpg", "Image caption")
    reactions.telegram?.api?.sendAudio(message?.chat?.id, File("audio.mp3"))

    // Or use standard response builders
    reactions.say("Hello there!")
    reactions.image("https://address.com/image.jpg")
}
```

_Note that Telegram bot works as long polling. This means that every reactions' method actually sends a response to the user._

#### Native API

You can use native Telegram API via `reactions.telegram?.api`.
This enables you to build any response that Telegram supports using channel-specific features.
As well as fetch some data from Telegram bot API (like [getMe](https://core.telegram.org/bots/api#getme) for example).

> Learn more about available API methods [here](https://github.com/kotlin-telegram-bot/kotlin-telegram-bot/blob/master/telegram/src/main/kotlin/me/ivmg/telegram/Bot.kt).

#### 3. Create a bot in Telegram

Create a new bot using Telegram's `@BotFather` and any Telegram client as described [here](https://core.telegram.org/bots#6-botfather).
Copy your new bot's **access token** to the clipboard.

#### 4. Create and run Telegram channel

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