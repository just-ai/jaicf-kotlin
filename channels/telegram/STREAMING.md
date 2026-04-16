# Telegram Streaming Support

The Telegram channel in JAICF supports streaming responses, providing real-time updates to users as the response is being generated. This is particularly useful for long-running operations like LLM generation.

## Features

- **Real-time Updates**: Messages are updated in real-time as chunks arrive
- **Configurable Throttling**: Control how often messages are updated (default: 500ms)
- **Long Message Handling**: Automatically splits messages exceeding Telegram's 4096 character limit
- **Parse Mode Support**: Optional Markdown/HTML formatting during streaming
- **Error Handling**: Graceful handling of common Telegram API errors
- **Backward Compatible**: Existing code continues to work without changes

## Quick Start

The streaming feature works automatically when you use `reactions.streamOrSay()`:

```kotlin
val channel = TelegramChannel(
    botApi = myBot,
    telegramBotToken = "YOUR_BOT_TOKEN"
)

channel.startLongPolling()
```

In your scenario, use `streamOrSay()` to send streaming responses:

```kotlin
action {
    reactions.streamOrSay(flow {
        emit("Hello")
        delay(100)
        emit(" world")
        delay(100)
        emit("!")
    })
}
```

Users will see the message update in real-time in their Telegram chat. This works great with LLM agents or any other streaming source.

## Configuration

Customize streaming behavior with `StreamConfig`:

```kotlin
val channel = TelegramChannel(
    botApi = myBot,
    telegramBotToken = "YOUR_BOT_TOKEN",
    streamConfig = StreamConfig(
        updateIntervalMs = 300,              // Update every 300ms (default: 500)
        initialPlaceholder = { "..." },      // Lambda to generate initial message (default: { "..." })
        parseMode = ParseMode.Markdown       // Enable Markdown formatting (default: null)
    )
)
```

### Configuration Options

- **updateIntervalMs**: Interval in milliseconds between message updates during streaming
  - Lower values provide smoother updates but use more API calls
  - Higher values reduce API usage but feel less responsive
  - Default: 500ms

- **initialPlaceholder**: Lambda function to generate the initial message before streaming starts
  - Default: `{ "..." }`
  - Can access context (e.g., user language) to generate localized text
  - Examples: `{ "Thinking..." }`, `{ "Typing..." }`, `{ getUserLanguage().let { if (it == "ru") "Думаю..." else "Thinking..." } }`

- **parseMode**: Optional parse mode for message formatting
  - `ParseMode.Markdown` - Markdown formatting
  - `ParseMode.HTML` - HTML formatting
  - `null` - Plain text (default)

## How It Works

1. When streaming starts, an initial message is sent with the placeholder text
2. As chunks arrive, they are accumulated in a buffer
3. The message is updated at the configured interval using Telegram's `editMessageText` API
4. If the message exceeds ~3900 characters, a new message is sent and streaming continues
5. A final update ensures the complete text is delivered

## Long Message Handling

Telegram has a 4096 character limit per message. When streaming responses exceed this:

1. The current message is finalized with all content up to ~3900 characters
2. A new message is automatically sent
3. Streaming continues into the new message
4. This process repeats as needed for very long responses

All messages are saved to the messages list, so `buttons()` works correctly after streaming.

## Error Handling

The streaming implementation gracefully handles common Telegram API errors:

- **"message is not modified"**: Ignored (text hasn't changed)
- **"message to edit not found"**: Stops updates (message was deleted by user)
- **"message can't be edited"**: Stops updates (message too old, >48 hours)
- **"can't parse"**: Retries without parse mode
- Other errors are logged but don't break the flow

## Compatibility

- **Automatic Integration**: All scenarios using `reactions.streamOrSay()` automatically get streaming (including LLM agents)
- **No Breaking Changes**: Existing code using `say(text: String)` continues to work normally
- **Button Support**: The `buttons()` method works correctly after streaming

## Example: Custom Streaming Configuration

```kotlin
import com.justai.jaicf.channel.telegram.TelegramChannel
import com.justai.jaicf.channel.telegram.StreamConfig
import com.pengrad.telegrambot.model.request.ParseMode

val streamConfig = StreamConfig(
    updateIntervalMs = 300,
    initialPlaceholder = { "⏳ Generating response..." },
    parseMode = ParseMode.Markdown
)

val channel = TelegramChannel(
    botApi = myBot,
    telegramBotToken = System.getenv("TELEGRAM_BOT_TOKEN"),
    streamConfig = streamConfig
)

channel.run()
```

## Example: Multi-language Support

The lambda function allows you to dynamically generate placeholder text based on context:

```kotlin
// Example with capturing user language from context
class MyBot {
    private var userLanguage: String = "en" // Can be set based on user interaction

    fun createChannel(botApi: BotApi, token: String): TelegramChannel {
        val streamConfig = StreamConfig(
            updateIntervalMs = 300,
            initialPlaceholder = {
                when (userLanguage) {
                    "ru" -> "⏳ Генерирую ответ..."
                    "es" -> "⏳ Generando respuesta..."
                    "fr" -> "⏳ Génération de la réponse..."
                    else -> "⏳ Generating response..."
                }
            },
            parseMode = ParseMode.Markdown
        )

        return TelegramChannel(
            botApi = botApi,
            telegramBotToken = token,
            streamConfig = streamConfig
        )
    }
}
```

Or with a simple time-based placeholder:

```kotlin
val streamConfig = StreamConfig(
    updateIntervalMs = 300,
    initialPlaceholder = {
        val hour = LocalDateTime.now().hour
        when {
            hour < 12 -> "☀️ Good morning! Thinking..."
            hour < 18 -> "☀️ Thinking..."
            else -> "🌙 Thinking..."
        }
    },
    parseMode = ParseMode.Markdown
)
```

## Performance Considerations

- The default 500ms update interval balances responsiveness and API efficiency
- For very fast streaming sources (e.g., fast LLMs), consider increasing the interval to reduce API calls
- For slower streaming sources (e.g., slower LLMs or complex operations), the default or a lower value provides better user experience
- Telegram API has rate limits; the throttling mechanism helps avoid hitting them

## Migration from kotlin-telegram-bot

This implementation uses the Pengrad `java-telegram-bot-api` library, which replaced `kotlin-telegram-bot`. The streaming feature was implemented after this migration to leverage the stable `editMessageText` API.

See the [MIGRATION_GUIDE.md](./MIGRATION_GUIDE.md) for details on the library migration.
