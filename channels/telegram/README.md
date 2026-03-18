# Telegram channel

Allows you to create chatbots for [Telegram](https://core.telegram.org/bots).

_Built on top of [java-telegram-bot-api](https://github.com/pengrad/java-telegram-bot-api) library._

## Features

- **Polling Mode**: Long polling for local development and simple deployments
- **Webhook Mode**: Efficient webhook support for production environments
- **Streaming Responses**: Real-time response streaming with automatic message updates (great for LLM responses)
- **Media Support**: Send and receive photos, videos, audio, documents, and more
- **Interactive Elements**: Inline keyboards, reply keyboards, and callback queries
- **Payment Integration**: Support for Telegram Payments API
- **Custom Bot API**: Configure custom Bot API server URLs
- **Async Processing**: Configurable request execution with custom Executor

## Getting Started

### 1. Create Your Bot

1. Open Telegram and find [@BotFather](https://t.me/botfather)
2. Send `/newbot` and follow the instructions
3. Copy the bot token (format: `123456789:ABCdefGHIjklMNOpqrsTUVwxyz`)

### 2. Add Dependency

```kotlin
dependencies {
    implementation("com.just-ai.jaicf:telegram:$jaicfVersion")
}
```

## Polling Mode

Polling mode uses long polling to fetch updates from Telegram. Best for:
- Local development and testing
- Simple deployments without webhook setup
- When you don't have a public HTTPS endpoint

**The channel automatically starts in polling mode by default** when `TELEGRAM_WEBHOOK_URL` environment variable is not set.

### Example

```kotlin
import com.justai.jaicf.channel.telegram.TelegramChannel

fun main() {
    val botToken = System.getenv("TELEGRAM_BOT_TOKEN")
        ?: error("TELEGRAM_BOT_TOKEN is required")

    TelegramChannel(myBot, botToken).startLongPolling()
}
```

### How to Run

Set your bot token as an environment variable and make sure `TELEGRAM_WEBHOOK_URL` is **not** set:

```bash
export TELEGRAM_BOT_TOKEN=your_token_here
unset TELEGRAM_WEBHOOK_URL  # Make sure webhook URL is not set
./gradlew run
```

The channel will automatically detect that webhook mode is not configured and start polling.

## Auto Mode (Recommended)

The simplest way - let the channel automatically choose the right mode:

```kotlin
suspend fun main() {
    val botToken = System.getenv("TELEGRAM_BOT_TOKEN")
        ?: error("TELEGRAM_BOT_TOKEN is required")

    // Automatically detects mode from TELEGRAM_WEBHOOK_URL env variable
    TelegramChannel(myBot, botToken).start()
}
```

- If `TELEGRAM_WEBHOOK_URL` is set → starts webhook mode
- Otherwise → starts long polling mode

## Webhook Mode

Webhook mode receives updates via HTTP POST requests to your server. Best for:
- Production deployments
- Better resource utilization (no constant polling)
- Instant message delivery
- Running multiple bots on the same server

**The channel automatically skips polling** when `TELEGRAM_WEBHOOK_URL` environment variable is set or explicitly passed to the constructor.

### Requirements

- **HTTPS URL** with valid SSL certificate
- **Publicly accessible endpoint**

### Example

```kotlin
import com.justai.jaicf.channel.telegram.TelegramChannel

suspend fun main() {
    val botToken = System.getenv("TELEGRAM_BOT_TOKEN")
        ?: error("TELEGRAM_BOT_TOKEN is required")

    val telegram = TelegramChannel(myBot, botToken)

    // Uses TELEGRAM_WEBHOOK_URL from env and default Netty server on port 8000
    telegram.startWebhook()
}
```

**With custom server:**

```kotlin
suspend fun main() {
    val telegram = TelegramChannel(myBot, botToken)

    // Custom server configuration
    val server = embeddedServer(Netty, port = 9000) {
        routing {
            httpBotRouting("/bot" to telegram)
        }
    }
    telegram.startWebhook(server = server, webhookUrl = "https://your-domain.com/bot")
}
```

### Manual Webhook Setup

You can also set the webhook manually using curl:

```bash
# Set webhook
curl -X POST https://api.telegram.org/bot<YOUR_TOKEN>/setWebhook \
  -H "Content-Type: application/json" \
  -d '{"url": "https://your-domain.com/telegram"}'

# Check webhook status
curl https://api.telegram.org/bot<YOUR_TOKEN>/getWebhookInfo

# Delete webhook (to switch back to polling)
curl https://api.telegram.org/bot<YOUR_TOKEN>/deleteWebhook
```

## Configuration

### Basic Configuration

```kotlin
val channel = TelegramChannel(
    botApi = myBot,                          // Your BotApi instance
    telegramBotToken = "YOUR_BOT_TOKEN",     // Bot token from @BotFather
    telegramApiUrl = null,                   // Optional custom Bot API URL
    requestExecutor = DefaultRequestExecutor, // Optional custom Executor
    streamConfig = StreamConfig(),           // Optional streaming configuration
    webhookUrl = null                        // Optional webhook URL (auto-detected from env)
)
```

**Mode Detection:**
- If `webhookUrl` parameter or `TELEGRAM_WEBHOOK_URL` env variable is set → **webhook mode** (no polling)
- Otherwise → **polling mode** (automatically starts in `init` block)

### Explicit Mode Configuration

```kotlin
// Force webhook mode (skip polling)
val webhookChannel = TelegramChannel(
    botApi = myBot,
    telegramBotToken = "YOUR_BOT_TOKEN",
    webhookUrl = "https://your-domain.com/telegram"
)

// Force polling mode (even if env var is set)
val pollingChannel = TelegramChannel(
    botApi = myBot,
    telegramBotToken = "YOUR_BOT_TOKEN",
    webhookUrl = null  // Explicitly disable webhook mode
)
```

### Streaming Configuration

Configure real-time response streaming (useful for long-running operations like LLM generation):

```kotlin
import com.justai.jaicf.channel.telegram.streaming.StreamConfig
import com.pengrad.telegrambot.model.request.ParseMode

val channel = TelegramChannel(
    botApi = myBot,
    telegramBotToken = "YOUR_BOT_TOKEN",
    streamConfig = StreamConfig(
        updateIntervalMs = 500,                    // Update interval (default: 500ms)
        initialPlaceholder = { "..." },            // Initial message (default: "...")
        parseMode = ParseMode.Markdown             // Message formatting (default: Markdown)
    )
)
```

See [STREAMING.md](./STREAMING.md) for detailed documentation.

## Usage in Scenarios

### Basic Usage

```kotlin
import com.justai.jaicf.channel.telegram.telegram

action {
    // Access request data
    val text = request.telegram?.text
    val chatId = request.telegram?.chatId

    // Send messages
    reactions.say("Hello, world!")
    reactions.buttons("Button 1", "Button 2")

    // Send media
    reactions.telegram?.sendPhoto(
        url = "https://example.com/image.png",
        caption = "Check this out!"
    )
}
```

### Custom Keyboards

```kotlin
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.request.SendMessage

action {
    val chatId = request.telegram?.chatId ?: return@action

    val keyboard = InlineKeyboardMarkup(
        arrayOf(
            InlineKeyboardButton("Button 1").callbackData("btn1"),
            InlineKeyboardButton("Button 2").callbackData("btn2")
        )
    )

    reactions.telegram?.api?.execute(
        SendMessage(chatId, "Choose:")
            .replyMarkup(keyboard)
    )
}
```

## Advanced Features

### Streaming Responses

Supports real-time response streaming with automatic message updates. Particularly useful for long-running operations like LLM generation. See [STREAMING.md](./STREAMING.md) for details.

### Additional Features

- **Media**: Photos, videos, audio, documents
- **Location & Contact**: Share location and contact info
- **Payments**: Telegram Payments API integration
- **Inline Queries**: Respond to `@yourbot query` commands

For detailed API, see [TelegramReactions](./src/main/kotlin/com/justai/jaicf/channel/telegram/TelegramReactions.kt).

## Migration

If you're migrating from the old `kotlin-telegram-bot` library, see [MIGRATION_GUIDE.md](./MIGRATION_GUIDE.md) for a complete migration guide.

**Key changes:**
- Native API changed from `kotlin-telegram-bot` to `java-telegram-bot-api` (Pengrad)
- ParseMode constants changed: `ParseMode.MARKDOWN` → `ParseMode.Markdown`
- Message fields use getter methods: `message.text` → `message.text()`
- Public JAICF API remains unchanged

## Resources

- [Telegram Bot API Documentation](https://core.telegram.org/bots/api)
- [java-telegram-bot-api Library](https://github.com/pengrad/java-telegram-bot-api)
- [JAICF Documentation](https://help.jaicf.com/)
- [Telegram channel page](https://help.jaicf.com/Telegram)
