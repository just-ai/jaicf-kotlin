---
layout: default
title: catchAll
permalink: catchAll
parent: activators
grand_parent: Scenario DSL
---

[CatchAllActivator](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/activator/catchall/CatchAllActivator.kt) can be used in JAICF project to handle any user's query request that wasn't handled by any other activator.

Usually this activator is used to handle all raw user's input for some purposes (for example, if user dictates a text of reminder note).

It also can be used as a fallback state to handle a request that can't be handled by any other state.
We recommend using a [fallback](fallback) builder for these purposes.

# How to use

All you need to use this activator in your JAICF project is to add `catchAll` activators to the scenarios and then append `CatchAllActivator` to the `BotEngine`'s array of activators.

## catchAll activator

```kotlin
state("fallback", noContext = true) {
    activators {
        catchAll()
    }

    action {
        reactions.say("Sorry, I didn't get it... Could you repeat please?")
    }
}
```

# CatchAll context

Once a `CatchAllActivator` activates some state, a [CatchAllActivatorContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/activator/catchall/CatchAllActivatorContext.kt) instance becomes available through an `activator.catchAll` variable in the action block of this state.
It doesn't contain any data but can be used to determine if the state was activated by `CatchAllActivator`.

_Note that is you need to obtain a raw user's request text, you can use `request.input` in your action block._

```kotlin
state("state1") {
    activators {
        intent("SomeIntent")
        catchAll()
    }

    action {
        activator.catchAll?.run {
            reactions.say("This state was activated by catchAll activator because you've said ${request.input}.")
        }
    }
}
```

> Learn more about request [here](request).