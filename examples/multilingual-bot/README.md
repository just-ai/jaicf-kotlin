# Multilingual Bot Example

This example demonstrates using multiple BotEngines and Scenarios with separate language-based NLU providers **inside
one channel**.

## About BotRouting

This multilingual bot is built in top of `BotRouting` feature. This feature allows using multiple configured `BotEngine`
inside one channel.

Let's look at the example below:

```kotlin
val MultilingualBotEngine = BotRoutingEngine(
    main = MainBot.engine,
    routables = mapOf(RuBot.EngineName to RuBot.Engine, EnBot.EngineName to EnBot.Engine)
)
```

Here we declare a `BotRoutingEngine` - an `BotEngine` implementation which allows routing requests from one `BotEngine`
to another. We define a `main` engine, which will process requests if they are not routed to another specified engine
and `routables`map with engines we can route to.

#### Language detection

We can use automated `Language Detector` (built on top of CAILA NLU) to get client language from request, so we create a
fallback state inside `MainScenario`, which routes request to target Scenarios based on which language client uses.

```kotlin
fallback {
    when (val lang = LanguageDetectService.detectLanguage(request.input)) {
        null -> {
            reactions.say("Sorry, I can't get it! Please, select your language")
            reactions.buttons("Русский" to "/Ru", "English" to "/En")
        }
        else -> routing.route(lang.name, defaultTargetState)
    }
}
```

#### BotRoutingAPI

We can use `BotRoutingAPI` methods inside from `action` blocks in states via `routing` extension.

```kotlin
state("selectLang") {
    activators {
        regex("Select language")
    }
    action {
        routing.route("main", "/Main")
    }
}
```

#### Shared BotContext

BotContext inside `main` engine and `routables` is shared, so we can easily transfer data from one engine to another
with just `context` property inside `action` context.

The example below demonstrates simplified usage of shared context between bots.

```kotlin
private val main = Scenario {
    fallback {
        if (request.input == "sc1") {
            context.temp["my-temp-data"] = "test"
            routing.route("sc1")
        }
    }
}
private val sc1 = Scenario {
    fallback {
        val tempData = context.temp["my-temp-data"] as String
        reactions.say("Hello from SC1. Your temp data: $tempData")
    }
}
```

## How to use

This project has **JAICP Access Tokens** with CAILA NLU provider _built-in inside source code_, therefore you can just
run it in any channel you want.

Exported CAILA NLU projects for English or Russian languages can be found at `src/main/resources`. You can import it
inside your own projects and modify content.

#### Running in JAICP

1. Create a new JAICF project in [JAICP Application Panel](https://app.jaicp.com).
2. Create a Chat Widget channel.
3. Copy your project's API key from _Project Properties_.
4. Paste it to `jaicp.properties`.
5. Run JaicpPoller class

#### Running in Telegram

```kotlin
fun main() {
    TelegramChannel(MultilingualBotEngine, YOUR TELEGRAM TOKEN).run()
}
```
