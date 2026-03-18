# Migration Guide: Telegram Channel Update

This guide is for developers who use JAICF in their projects with the Telegram channel and are updating to a newer version where the underlying library has changed.

## What Changed

JAICF Telegram channel now uses `java-telegram-bot-api` (Pengrad) instead of `kotlin-telegram-bot`. The old library had slow development and limited support. The new library offers active development, clean API, and better performance.

## Do You Need to Migrate?

**Most likely NO.** If you're only using standard JAICF reactions API, nothing needs to change:

```kotlin
// ✅ These work exactly as before - no changes needed
reactions.say("Hello!")
reactions.image("https://example.com/image.png")
reactions.buttons("Button 1", "Button 2")
request.telegram?.text
request.telegram?.chatId
```

**You only need to migrate if:**
- You access the native Telegram API via `reactions.telegram?.api`
- You import Telegram types directly (e.g., `Message`, `Update`, `ParseMode`)
- You use custom keyboards or advanced Telegram features

## Breaking Changes (Advanced Usage Only)

### 1. Native API Access

If you use `reactions.telegram?.api`, the type has changed:

```kotlin
// Before: com.github.kotlintelegrambot.Bot
// After:  com.pengrad.telegrambot.TelegramBot
```

### 2. Import Statements

Update imports if you use Telegram types directly:

**Before:**
```kotlin
import com.github.kotlintelegrambot.entities.ParseMode
```

**After:**
```kotlin
import com.pengrad.telegrambot.model.request.ParseMode
```

### 3. ParseMode Constants

```kotlin
// Before: ParseMode.MARKDOWN
// After:  ParseMode.Markdown
```

### 4. Message Field Access

Pengrad uses getter methods:

```kotlin
// Before: message.text
// After:  message.text()
```

## What Stays the Same

**The public JAICF API remains unchanged:**

```kotlin
// Channel creation
TelegramChannel(botApi, "YOUR_BOT_TOKEN").run()

// Reactions API
reactions.say("Hello!")
reactions.image("https://example.com/image.png")
reactions.buttons("Button 1", "Button 2")

// Extension properties
request.telegram?.text
request.telegram?.callback
request.telegram?.chatId

// Request types
TelegramTextRequest
TelegramQueryRequest
TelegramLocationRequest
// ... all other request types
```

## Migration Examples

### Native API Calls

**Before:**
```kotlin
import com.github.kotlintelegrambot.entities.ChatId

reactions.telegram?.api?.sendMessage(
    ChatId.fromId(chatId),
    "Custom message",
    parseMode = ParseMode.MARKDOWN
)
```

**After:**
```kotlin
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.model.request.ParseMode

val chatId = request.telegram?.chatId ?: return@action
reactions.telegram?.api?.execute(
    SendMessage(chatId, "Custom message")
        .parseMode(ParseMode.Markdown)
)
```

### Custom Keyboards

**Before:**
```kotlin
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup

val keyboard = InlineKeyboardMarkup.create(
    listOf(listOf(
        InlineKeyboardButton.CallbackData("Button", callbackData = "data")
    ))
)
```

**After:**
```kotlin
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup

val keyboard = InlineKeyboardMarkup(
    arrayOf(
        InlineKeyboardButton("Button").callbackData("data")
    )
)
```

## Summary

- **Standard JAICF API** → No changes needed
- **Native API access** → Update to Pengrad methods
- **Type imports** → Change package names
- **ParseMode** → Update constant names

For more examples, see the [Telegram channel README](./README.md).

## Need Help?

If you encounter issues:
1. Check the [java-telegram-bot-api docs](https://github.com/pengrad/java-telegram-bot-api)
2. Open an issue in [JAICF repository](https://github.com/just-ai/jaicf-kotlin)
