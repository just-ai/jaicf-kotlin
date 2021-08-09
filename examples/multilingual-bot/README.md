# Multilingual Bot Example

This example demonstrates using multiple BotEngines and Scenarios with separate language-based NLU providers **inside
one channel**.

## About BotRouting

This multilingual bot is built in top of `BotRouting` feature. This feature allows using multiple configured `BotEngine`
inside one channel:

```kotlin
val EnEngine = BotEngine(
    scenario = EnScenario,
    activators = arrayOf(RegexActivator, CailaIntentActivator.Factory(CailaNLUSettings("caila-token")))
)

val RuEngine = BotEngine(
    scenario = RuScenario,
    activators = arrayOf(RegexActivator, CailaIntentActivator.Factory(CailaNLUSettings("caila-token")))
)

val MainBotEngine = BotEngine(MainScenario, activators = arrayOf(RegexActivator))

val MultilingualBotEngine = BotRoutingEngine(
    main = "main" to MainBotEngine,
    routables = mapOf("en" to EnEngine, "ru" to RuEngine)
)

```

`BotRoutingEngine` - is another implementation of `BotEngine` allowing to route requests from one `BotEngine`
to another. The `main` engine will process requests by default. Developer can use `routing` methods to route current
request (or next ones) to another `BotEngine`.

#### Language detection

This example uses `CAILA` detect-language REST-API method to extract language from requests and further send it to
target bot.

```kotlin
fallback {
    when (val lang = LanguageDetectService.detectLanguage(request.input)) {
        null -> {
            reactions.say("Sorry, I can't get it! Please, select your language")
            reactions.buttons("English" to "/En", "Русский" to "/Ru")
        }
        else -> routing.route(lang.name, "/Welcome")
    }
}
```

#### BotRoutingAPI

`BotRoutingAPI` methods are available from `action` blocks in states via `routing` extension.

```kotlin
state("selectLang") {
    activators {
        regex("Select language")
    }
    action {
        // Route to main engine and process request in state "/Main".        
        routing.route("main", "/Main")
    }
}
```

#### Shared BotContext

BotContext inside `main` engine and `routables` is shared, data can be transferred from one engine to another
with `context` property inside `action` context.

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

val SampleRoutingEngine = BotRoutingEngine(
    main = "main" to BotEngine(main),
    routables = mapOf("sc1" to BotEngine(sc1))
)
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
