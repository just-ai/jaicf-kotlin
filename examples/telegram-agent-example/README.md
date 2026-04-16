# Telegram Agent Example

A production-ready example of a Telegram bot built with JAICF framework, featuring:

- **LLM Agent Integration** - Uses OpenAI GPT-4.1-nano for intelligent conversations
- **Streaming Responses** - Real-time message updates for better user experience
- **Dual-Mode Operation** - Automatic polling (local dev) or webhook (production) detection
- **Tool Examples** - Bot mode checker and calculator tools
- **CAILA Deployment** - Full Docker + deployment pipeline with health checks

## Prerequisites

Before you begin, you'll need:

1. **Telegram Account** - To create a bot via [@BotFather](https://t.me/botfather)
2. **OpenAI API Key** - Get it from [platform.openai.com](https://platform.openai.com/api-keys)
3. **CAILA Account** (for deployment only) - Sign up at [caila.io](https://app.caila.io)
4. **Docker Hub Account** (for deployment only) - Sign up at [hub.docker.com](https://hub.docker.com)

## Getting Started

### 1. Create Your Telegram Bot

1. Open Telegram and message [@BotFather](https://t.me/botfather)
2. Send `/newbot` command
3. Follow the prompts to name your bot
4. Copy the bot token (format: `123456789:ABCdefGHIjklMNOpqrsTUVwxyz`)

### 2. Set Up Environment Variables

```bash
cd examples/telegram-agent-example
cp .env.example .env
```

Edit `.env` and add your credentials:

```bash
TELEGRAM_BOT_TOKEN=your_bot_token_here
OPENAI_API_KEY=sk-your_openai_key_here
OPENAI_BASE_URL=https://api.openai.com/v1
```

⚠️ **Security Warning**: Never commit the `.env` file to git! It's already in `.gitignore`.

### 3. Run Locally

From the project root:

```bash
./gradlew :telegram-agent-example:run
```

Or using the application plugin:

```bash
cd examples/telegram-agent-example
../../gradlew run
```

You should see:

```
🚀 Starting in POLLING mode
🏥 Health check: http://localhost:8080/health
💬 Bot is ready to receive messages...
```

### 4. Test Your Bot

1. Open Telegram and find your bot
2. Send a message: "Hello!"
3. Try the bot mode tool: "What mode are you running in?"
4. Try the calculator: "Calculate 5 * 3"

You'll see streaming responses with real-time updates!

## Architecture

### Operating Modes

The bot automatically detects which mode to run in based on environment variables:

#### Polling Mode (Local Development)

- **When**: `TELEGRAM_WEBHOOK_URL` is not set
- **How**: Bot actively requests updates from Telegram servers
- **Use case**: Local development and testing
- **Advantages**: Simple setup, no HTTPS required

#### Webhook Mode (Production)

- **When**: `TELEGRAM_WEBHOOK_URL` is set
- **How**: Telegram sends updates via HTTP POST to your server
- **Use case**: Production deployments (CAILA, cloud hosting)
- **Advantages**: More efficient, no constant polling

### Server Structure

The application runs a single Ktor server that handles:

1. **Health Check Endpoint** (`/health`) - Required by CAILA for monitoring
2. **Telegram Webhook Endpoint** (`/telegram`) - Receives updates in webhook mode

```
┌─────────────────────────────────────┐
│       Ktor Server (Port 8080)       │
├─────────────────────────────────────┤
│  GET  /health  → "OK"               │
│  POST /telegram → TelegramChannel   │
└─────────────────────────────────────┘
         │                  │
         │                  ├─ Polling Mode: Active requests
         │                  └─ Webhook Mode: Receives POST
         │
    Health checks        Telegram Updates
```

### Streaming Flow

1. User sends message to bot
2. LLM starts generating response
3. Initial placeholder message sent: "⏳ Thinking..."
4. Response chunks arrive every 300ms
5. Message updates in real-time using Telegram's `editMessageText` API
6. Final response displayed

## CAILA Deployment

### Prerequisites

1. **CAILA Credentials** - Get from [app.caila.io](https://app.caila.io)
2. **Docker Hub Account** - For hosting Docker images

### Setup Gradle Properties

Create or edit `~/.gradle/gradle.properties`:

```properties
caila.token=your-caila-api-token
caila.accountId=your-account-id
dockerUsername=your-docker-username
dockerPassword=your-docker-password
```

### Configure CAILA Environment Variables

In your CAILA dashboard, configure the following environment variables:

```bash
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_WEBHOOK_URL=https://{accountId}-telegram-agent-bot.app.caila.io/telegram
OPENAI_API_KEY=sk-your_openai_key
OPENAI_BASE_URL=https://api.openai.com/v1
```

**Note**: Replace `{accountId}` with your actual CAILA account ID in the webhook URL.

### Deploy to CAILA

From the project root:

```bash
./gradlew :telegram-agent-example:publishToCailaFromDocker
```

This will:
1. Build the application
2. Create a Docker image
3. Push to Docker Hub
4. Deploy to CAILA platform

Monitor the deployment in your CAILA dashboard. Once running, test the bot in Telegram!

## Project Structure

```
telegram-agent-example/
├── build.gradle.kts                 # Build config + CAILA plugin
├── README.md                         # This file
├── .env.example                      # Environment template
└── src/main/kotlin/
    └── com/justai/jaicf/examples/telegram/
        ├── TelegramAgentMain.kt     # Entry point
        ├── TelegramAgentBot.kt      # Agent definition
        └── tools/
            ├── BotModeTool.kt       # Mode checker tool
            └── Calculator.kt         # Calculator tool
```

## Customization

### Adding New Tools

1. Create a new tool file in `tools/` directory:

```kotlin
@JsonClassDescription("Your tool description")
data class YourTool(
    @field:JsonPropertyDescription("Parameter description")
    val param: String
)

val YourToolImpl = llmTool<YourTool> {
    // Tool implementation
    "result"
}
```

2. Add to `TelegramAgentBot.kt`:

```kotlin
tools = listOf(BotModeTool, CalcTool, YourToolImpl)
```

### Changing the Model

Edit `TelegramAgentBot.kt`:

```kotlin
model = "gpt-4o",  // Use more capable model
```

Available models: `gpt-4.1-nano`, `gpt-4o`, `gpt-4.1-mini`, etc.

### Customizing Streaming

Edit `TelegramAgentMain.kt`:

```kotlin
val streamConfig = StreamConfig(
    updateIntervalMs = 500,              // Update frequency
    initialPlaceholder = { "Typing..." }, // Initial message
    parseMode = ParseMode.HTML           // HTML formatting
)
```

### Adjusting Agent Instructions

Edit `TelegramAgentBot.kt`:

```kotlin
instructions = """
    Your custom instructions here.
    Define the bot's personality and capabilities.
"""
```

## Troubleshooting

### Bot Not Responding

**Problem**: Bot doesn't reply to messages

**Solutions**:
- Check `TELEGRAM_BOT_TOKEN` is correct
- Verify bot is running (check console output)
- Check OpenAI API key is valid
- Review logs for errors

### Webhook Failed

**Problem**: Webhook mode not working

**Solutions**:
- Verify `TELEGRAM_WEBHOOK_URL` format: `https://domain.com/telegram`
- Ensure URL is HTTPS (required by Telegram)
- Check server is accessible from internet
- Test health check endpoint: `curl https://your-domain.com/health`

### No Streaming

**Problem**: Messages don't update in real-time

**Solutions**:
- Verify `StreamConfig` is configured
- Check Telegram rate limits (1 edit/second per message)
- Increase `updateIntervalMs` if hitting rate limits
- Review Telegram API errors in logs

### Health Check Failing

**Problem**: `/health` endpoint returns error

**Solutions**:
- Check server is running on correct port
- Verify no firewall blocking port 8080
- Test locally: `curl http://localhost:8080/health`
- Review server startup logs

### Build Errors

**Problem**: Gradle build fails

**Solutions**:
- Run `./gradlew clean build` from project root
- Check all dependencies are resolved
- Verify Kotlin version compatibility
- Update Gradle wrapper if needed

### CAILA Deployment Issues

**Problem**: `publishToCailaFromDocker` fails

**Solutions**:
- Verify `~/.gradle/gradle.properties` has all credentials
- Check Docker is running: `docker ps`
- Verify Docker Hub login: `docker login`
- Test Docker build locally: `./gradlew :telegram-agent-example:dockerBuildImage`
- Check CAILA token is still valid

## Next Steps

### Extend the Bot

- **Add more tools**: Weather API, database lookups, web search
- **Multi-agent handoff**: Use patterns from `MultiAgentHandoff.kt` in llm-example
- **Persistent context**: Enable S3 context manager for conversation history
- **Rich media**: Add image/audio responses
- **Inline keyboards**: Telegram buttons for tool selection
- **User authentication**: Restrict bot to specific users

### Production Best Practices

- **Error handling**: Add comprehensive error handling
- **Logging**: Implement structured logging with correlation IDs
- **Monitoring**: Add metrics collection (Prometheus, Grafana)
- **Rate limiting**: Protect against abuse
- **Secrets management**: Use secret managers instead of environment variables
- **CI/CD**: Automate deployment pipeline
- **Testing**: Add unit and integration tests

## Resources

- [JAICF Documentation](https://github.com/just-ai/jaicf-kotlin)
- [Telegram Bot API](https://core.telegram.org/bots/api)
- [OpenAI API](https://platform.openai.com/docs)
- [CAILA Platform](https://app.caila.io)
- [Ktor Documentation](https://ktor.io)

## Support

For issues or questions:

- **JAICF Framework**: [GitHub Issues](https://github.com/just-ai/jaicf-kotlin/issues)
- **This Example**: Check troubleshooting section above
- **CAILA Platform**: [CAILA Support](https://app.caila.io)

## License

This example is part of the JAICF project and follows the same license.
