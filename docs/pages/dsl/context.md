---
layout: default
title: context
permalink: context
parent: Scenario DSL
---

[BotContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/context/BotContext.kt) instance is available through `context` in the action block.
It can be used by scenario to store temp-, session- and client-scoped arbitrary data, related to the current user.

JAICF transparently manages the state of `BotContext` storing and fetching it for each user's request using **managers**.

> Learn more about managers [here](Environments).

# How to use

```kotlin
state("name") {
    action {
        context.client["name"] = context.result
        reactions.say("Nice to meet you ${context.result}")
   }
}
```

Here the value from `context.result` is saved to the client-scoped data map in the `context`.
On the next request from the same user JAICF will fetch their `context` and "name" variable will be available via `context.client["name"]`.

### Delegation

Due to the [Kotlin property delegation feature](https://kotlinlang.org/docs/reference/delegated-properties.html#storing-properties-in-a-map), you can use context as a source to delegate typed properties in your own data classes.

```kotlin
class GameController(context: BotContext) {
    var gamers: Int? by context.client
    var currentTurn: Int? by context.client
}
```

With such declaration of `gamers` and `currentTurn` you can operate with these typed variables that will be automatically obtained and stored accordingly to the `context.client` map on read/write.
Thus you don't have to invoke a type-less `context["gamers"]` or `context["currentTurn"] = 1`.
Instead of this you can simply access an appropriate variable like `controller.gamers` or `controller.currentTurn = 1`.

## Client scope

Client scoped map persists values permanently and available through `context.client`.

## Session scope

Session scoped map persists values for the current session.
It is up to the channel to start a new session and clean this map.
It is available through `context.session`.

## Temp scope

Temp scoped map persists values only during the current request processing.
This is a way to pass values over different states between dialogue states when using `reactions.go()`.
It is available through `context.temp`.

```kotlin
state("first") {
    action {
        context.temp["foo"] = "bar"
        reactions.go("/second")
    }
}
state("second") {
    action {
        val foo = context.temp["foo"] as? String
        reactions.say(foo)
    }
}
```

## Result

`result` variable may contain arbitrary data passed back to the callback state.

```kotlin
state("main") {
    action {
        reactions.go("/helper/ask4name", "name")
    }
}

state("name") {
    action {
        reactions.say("Nice to meet you ${context.result}!")
    }
}
```

This variable is scoped to session as well and can be cleared by JAICF in case of new session starting.