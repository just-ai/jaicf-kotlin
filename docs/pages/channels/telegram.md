---
title: Telegram
---

<p align="center">
    <img src="../../assets/images/channels/telegram.png" width="128" height="128"/>
</p>

<h1 align="center">Telegram messenger channel</h1>

Allows to create chatbots for [Telegram](https://core.telegram.org/bots).

_Built on top of [Kotlin Telegram Bot](https://github.com/kotlin-telegram-bot/kotlin-telegram-bot) library._

## How to use

#### 1. Include Telegram dependency to your _build.gradle_

```kotlin
implementation("com.just-ai.jaicf:telegram:$jaicfVersion")
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

> Learn more about available API methods [here](https://github.com/kotlin-telegram-bot/kotlin-telegram-bot/blob/main/telegram/src/main/kotlin/com/github/kotlintelegrambot/Bot.kt).

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
val HelloWorldScenario = Scenario {
    state("main") {
        activators {
            regex("/start")
        }

        action {
            reactions.say("Hello there!")
        }
    }
}
```

To make it work, just add `RegexActivator` to the array of activators in your agent's configuration:

```kotlin
val helloWorldBot = BotEngine(
    scenario = HelloWorldScenario,
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

## Additional Response Types

Beyond basic text and media, Telegram supports several other response types:

### Locations

Send location coordinates to the user:

```kotlin
action {
    reactions.telegram?.sendLocation(
        latitude = 55.751244f,
        longitude = 37.618423f,
        livePeriod = 3600  // Live location for 1 hour (optional)
    )
}
```

### Venues

Send venue information with location:

```kotlin
action {
    reactions.telegram?.sendVenue(
        latitude = 55.751244f,
        longitude = 37.618423f,
        title = "Red Square",
        address = "Moscow, Russia",
        foursquareId = "4b50f15df964a520491427e3"  // optional
    )
}
```

### Contacts

Share contact information:

```kotlin
action {
    reactions.telegram?.sendContact(
        phoneNumber = "+1234567890",
        firstName = "John",
        lastName = "Doe"
    )
}
```

### Voice Messages

Send voice messages:

```kotlin
action {
    reactions.telegram?.sendVoice(
        url = "https://example.com/voice.ogg",
        duration = 120  // duration in seconds (optional)
    )
}
```

### Video Notes

Send round video messages (video notes):

```kotlin
action {
    // From URL
    reactions.telegram?.sendVideoNote(
        url = "https://example.com/video.mp4",
        duration = 10,
        length = 320  // diameter of the video in pixels
    )

    // From file
    reactions.telegram?.sendVideoNote(
        file = File("video.mp4"),
        duration = 10,
        length = 320
    )
}
```

### Media Groups

Send multiple photos or videos as an album:

```kotlin
action {
    val mediaGroup = MediaGroup.from(
        InputMediaPhoto(
            media = "https://example.com/photo1.jpg",
            caption = "Photo 1"
        ),
        InputMediaPhoto(
            media = "https://example.com/photo2.jpg",
            caption = "Photo 2"
        )
    )

    reactions.telegram?.sendMediaGroup(mediaGroup)
}
```

## Streaming Support

JAICF supports streaming text responses to Telegram, which is useful for LLM integrations where responses are generated incrementally.

### Basic Streaming

```kotlin
action {
    val stream: Stream<String> = llmService.generateStream(request.input)
    reactions.telegram?.say(stream)  // Default 100ms debounce
}
```

### Custom Debounce Delay

You can control how frequently messages are updated during streaming:

```kotlin
action {
    val stream: Stream<String> = llmService.generateStream(request.input)
    reactions.telegram?.say(stream, debounceMs = 200L)  // Update every 200ms
}
```

### Custom Stream Processor

For advanced use cases, you can create custom stream processors with specialized splitting logic:

```kotlin
class CustomStreamProcessor(
    api: Bot,
    chatId: ChatId,
    debounceMs: Long,
    dispatcher: CoroutineDispatcher,
    parseMode: ParseMode?
) : TelegramStreamProcessor(api, chatId, debounceMs, dispatcher, parseMode) {
    override fun shouldSplitMessage(state: MessageState): Boolean {
        // Custom splitting logic - e.g., split at 2000 chars instead of default 3900
        return state.text.length > 2000
    }
}

val channel = TelegramChannel(
    botApi = botEngine,
    telegramBotToken = "YOUR_TOKEN",
    streamProcessorFactory = TelegramStreamProcessorFactory { api, chatId, debounceMs, dispatcher, parseMode ->
        CustomStreamProcessor(api, chatId, debounceMs, dispatcher, parseMode)
    }
)
```

## Message Aggregation

Telegram channel supports automatic message aggregation for handling multiple messages sent in quick succession, such as media groups or long text split into multiple messages.

### Basic Configuration

```kotlin
val channel = TelegramChannel(
    botApi = botEngine,
    telegramBotToken = "YOUR_TOKEN",
    aggregation = AggregationConfig(
        waitTimeMs = 500L,        // Wait 500ms for more messages
        useMediaGroupId = true,   // Use Telegram's media group ID for instant detection
        maxItems = 20             // Maximum items to aggregate
    )
)
```

### Accessing Aggregated Content

When messages are aggregated, you can access all content through helper extension properties:

```kotlin
action {
    val request = request.telegram ?: return@action

    // Check if request is aggregated
    if (request.isAggregated) {
        // Get all items
        val allItems = request.allItems

        // Get all photos from all messages
        val photos = request.allPhotos

        // Get all text messages
        val texts = request.allTexts

        // Filter by specific type
        val photoRequests = request.itemsOfType<TelegramPhotosRequest>()

        reactions.say("Received ${photos.size} photos and ${texts.size} text messages")
    }
}
```

Available extension properties for aggregated content:
- `allItems` - all requests as a list
- `allPhotos` - all photos from photo messages
- `allTexts` - all text strings from text messages
- `allVideos` - all videos from video messages
- `allDocuments` - all documents
- `allAudios` - all audio files
- `allVoices` - all voice messages
- `allVideoNotes` - all video notes
- `allStickers` - all stickers
- `allAnimations` - all animations
- `allLocations` - all locations
- `allContacts` - all contacts
- `allGames` - all games

### Custom Aggregation Strategy

You can implement custom aggregation logic:

```kotlin
class CustomAggregationStrategy : AggregationStrategy {
    override fun shouldAggregate(existing: TelegramBotRequest, new: TelegramBotRequest): Boolean {
        // Custom logic to decide if messages should be aggregated
        return existing.chatId == new.chatId &&
               existing is TelegramTextRequest &&
               new is TelegramTextRequest
    }

    override fun aggregate(existing: TelegramBotRequest, new: TelegramBotRequest): TelegramBotRequest {
        // Custom aggregation logic
        // ...
    }
}

val channel = TelegramChannel(
    botApi = botEngine,
    telegramBotToken = "YOUR_TOKEN",
    aggregation = AggregationConfig(
        strategy = CustomAggregationStrategy()
    )
)
```

## Parse Mode Configuration

You can configure the default parse mode for all messages sent by the bot:

```kotlin
val channel = TelegramChannel(
    botApi = botEngine,
    telegramBotToken = "YOUR_TOKEN",
    defaultParseMode = ParseMode.MARKDOWN_V2  // Use Markdown v2 instead of default v1
)
```

### Safe Message Sending with Fallback

The channel automatically handles parse mode errors. If a message fails to send with the specified parse mode (e.g., due to invalid formatting), it will automatically retry without parse mode:

```kotlin
action {
    // If this fails due to invalid Markdown, it will retry as plain text
    reactions.telegram?.say("This *might* have [invalid](markdown")
}
```

This automatic fallback applies to:
- `sendMessage` / `say`
- `sendPhoto` / `image`
- `sendVideo`
- `sendVoice`
- `sendDocument`

## Payments

You can accept payments for services or goods you provide from Telegram users.
To do this, you need to [connect a payment system and obtain its unique token](https://core.telegram.org/bots/payments#getting-a-token).

```kotlin
action {
    val info = PaymentInvoiceInfo(
        "title",
        "description",
        "unique payload",
        "381964478:TEST:67912",
        "unique-start-parameter",
        "USD",
        listOf(LabeledPrice("price", BigInteger.valueOf(20_00)))
    )
    reactions.telegram?.sendInvoice(info)
}
```

To learn about available currencies and more, you can read [the telegram payment documentation](https://core.telegram.org/bots/payments).

### Goods availability

Before proceeding with the payment, Telegram sends a request to bot to check the goods availability. 
This request triggers preCheckout event in scenario. Add the preCheckout as **a top level state**.

> Note that when paying in group chats, payment confirmation is sent to the user who sent the payment request, not the entire chat. So there will be created a separate context for the user. 
>If the user communicates with the user in a personal chat the context remains the same.

```kotlin
state("preCheckout") {
    activators {
        event(TelegramEvent.PRE_CHECKOUT)
    }

    action(telegram.preCheckout) {
        reactions.answerPreCheckoutQuery(request.preCheckoutQuery.id, true)
    }
}
```

> You always need to handle the telegramPreCheckout event in the script. Otherwise payments will fail, and all subsequent user messages will be handled in the CatchAll state.

Also you can handle successfulPayment event inside nested states in the TelegramPayment state

```kotlin
state("successfulPayment") {
    activators {
        event(TelegramEvent.SUCCESSFUL_PAYMENT)
    }

    action(telegram.successfulPayment) {
        reactions.say("We are glad you bought from us")
    }
}
```
