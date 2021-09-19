---
layout: default
title: action
permalink: action
parent: Scenario DSL
---

An action block of the state contains a Kotlin code that will be executed once the state is activated. Each action block
is executed in the context
of [ActionContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/context/ActionContext.kt)
instance. It contains _request-related_ instances
of [BotContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/context/BotContext.kt)
, [ActivatorContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/context/ActivatorContext.kt)
, [BotRequest](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/api/BotRequest.kt)
and [Reactions](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/reactions/Reactions.kt)
. These objects can be used in the action block to manage the dialogue and build a response.

As a rule [reactions](reactions) interface is used to build a response and change the state of the dialogue.

```kotlin
action {
    reactions.run {
        // Say goodbye in all channels
        say("Okay $break200ms See you latter then! Bye bye!")

        // End conversation in Google Actions channel
        actions?.endConversation()

        // Stop player and end session in Alexa channel
        alexa?.run {
            stopAudioPlayer()
            endSession()
        }
    }
}
```

## Type-specific actions

Every built-in channel or activator provides its own `ChannelTypeToken`, `ActivatorTypeToken`, or `ContextTypeToken` (
see [TypeToken](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/generic/TypeToken.kt)
for more information).

Type tokens are named the same way as channels and activators (e.g. `alexa`, `telegram`, `caila`, `dialogflow`, etc.).
Some type tokens can be specified further (e.g. `alexa.intent`, `alexa.event`, `telegram.location`, etc.). You can find
a list of provided type tokens in files with `*TypeToken.kt` suffix (
e.g. [AlexaTypeToken.kt](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/alexa/src/main/kotlin/com/justai/jaicf/channel/alexa/AlexaTypeToken.kt))
.

Type tokens can be used inside `action` blocks as an easy way to execute a piece of code only if `ActivatorContext`
and/or `BotRequest` matches with the given type token:

```kotlin
action {
    var name: String? = null
    alexa.intent {
        name = activator.slots["name"]
    }
    telegram {
        name = request.message.chat.firstName
    }
    facebook {
        name = reactions.queryUserProfile()?.firstName()
    }
}
```

Type tokens can be composed with each other (
see [Compositions.kt](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/generic/Compositions.kt)):

```kotlin
action {
    (telegram and caila) {
        reactions.sendContact(activator.slots["phone"], activator.slots["name"])
    }
}
```

Also, you can pass a type token directly to an `action` block as a parameter:

```kotlin
state("hello") {
    action(telegram and caila) {
        reactions.sendContact(activator.slots["phone"], activator.slots["name"])
    }
}
```

**NOTE**: type token doesn't work as an activation rule, meaning that if a state was correctly activated,
but `BotRequest` or `ActivatorContext` doesn't match the given type token, `BotEngine` **will not** try to find another
state to execute an action.

## Actions are extendable

You can write extension functions and properties used inside `action` block with `DefaultActionContext`. Extensions
allow to write your functions which already have `request`, `reactions`, `context` and `activator` properties
accessible.

```kotlin
state("doSomething") {
    action {
        doSomething()
    }
}
```

```kotlin
fun DefaultActionContext.doSomething() {
    reactions.say("something")
    context.temp["something"] = "something"
}
```

## Interfaces

There is a list of interfaces that are available in each `action` block:

* [request](request)
* [reactions](reactions)
* [context](context)
* [activator](activator)
