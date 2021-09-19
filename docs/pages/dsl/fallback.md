---
layout: default
title: fallback
permalink: fallback
parent: Scenario DSL
---

Each time the user sends a request that can't be handled by any state of the scenario, a _fallback_ state will be activated without a changing the current dialogue's context.

```kotlin
val MainScenario = Scenario {
    state("state1") {
        activators {...}
        action {...}
    }
    
    fallback {
        reactions.say("Sorry, I didn't get that. Could you repeat please?")
    }
}
```

## Dialogue context

Fallback state doesn't change the current dialogue's context meaning that the next user's request will be processed in the same context as a previous one.

## Context-aware fallbacks

It's possible to use `fallback` as an inner state:

```kotlin
val MainScenario = Scenario {
    state("state1") {
        activators {...}
        action {...}

        fallback {
            reactions.say("Okay!")
        }
    }
    
    fallback {
        reactions.say("Sorry, I didn't get that. Could you repeat please?")
    }
}
```