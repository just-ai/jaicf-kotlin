---
layout: default
title: Hooks
permalink: Hooks
parent: Features
---

Hooks can be used to intercept any phase of each request processing.
This mechanism allows you to log, modify or completely interrupt a request.

## How to use

You can handle different hooks right in the scenario:

```kotlin
val HelloWorldScenario = Scenario {
    handle<BotRequestHook> {
        // This handler will be invoked right after a new request was received
    }

    handle<BeforeProcessHook> { 
        // This handler will be invoked right before each request processing
    }

    handle<BeforeActionHook> { 
        // This handler be invoked right before each action block execution
    }

    handle<AfterActionHook> { 
        // This handler will be invoked right after each action block execution
    }

    handle<AfterProcessHook> {
        // This handler will be invoked once a request processing is complete
    }

    handle<ActionErrorHook> { 
        // This handler will be invoked if action block crashed with an exception
    }
}
```

You can also add handlers outside of scenario using `BotEngine` instance:

```kotlin
val helloWorldBot = BotEngine(
    scenario = HelloWorldScenario,
    activators = arrayOf(
        AlexaActivator,
        dialogflowActivator,
        RegexActivator,
        BaseEventActivator,
        CatchAllActivator
    )
).apply { 
    hooks.addHookAction<BotRequestHook> { 
        ...
    }
}
```

## Request interruption

Your handler can completely interrupt a request processing.
Just throw a [BotHookException](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/hook/BotHookException.kt) this way:

```kotlin
Scenario {
    handle<BotRequestHook> {
        throw BotHookException("I'm on vacation and won't process any request!")
    }
}
```