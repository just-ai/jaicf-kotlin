---
layout: default
title: activators
permalink: activators
parent: Scenario DSL
---

Each scenario's state can be _activated_ by any of configured activator - a rule that tries to handle the user's request
and find an appropriate state to activate. Activators function is a builder function of scenario that can be used to
define which activators could activate the state.

# How it works

```kotlin
state("launch") {
    activators {
        regex("/start")
        event(AlexaEvent.LAUNCH)
        intent(DialogflowIntent.WELCOME)
    }

    action {
        ...
    }
}
```

Here is a top-level state "launch" that can be activated by Alexa's LAUNCH event, Dialogflow's WELCOME intent or regular
expression that matches any input with "/start" character sequence.

> Learn more about different activators [here](Natural-Language-Understanding).

# globalActivators

By default activators will activate the state only if the parent state was activated previously. You can configure
activators to be global - in this case the state can be activated by these activators as it was a top-level.

```kotlin
state("main") {
    state("inner") {
        activators {
            intent("some_intent")
        }
        globalActivators {
            event("activate_inner_state")
        }
    }
}
```

In the example above state "inner" could be activated by two different activators - intent activator with "some_intent"
intent's name and event activator with "activate_inner_state" event's name. Once the intent activator could activate
this state only in case the parent "main" state was activated previously, the event activator is a global and can
activate "inner" state from everywhere.

# fromState

Activators function accepts an optional state path from where it is available. It means that you can "override" a
parent's state path to make a state available from some other state.

```kotlin
state("main") {
    state("inner") {
        activators("/other") {
            event("event1")
        }
    }
}
```

In the example above "inner" state can be activated once a dialogue is in state "/other". You can think about this as a
mix-in feature.

# Available activators

JAICF provides a set of available activators that can be used in your scenarios.

### intent

Activates state if a NLU service has recognised the intent with the given name.

Generates an instance
of [IntentActivatorContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/activator/intent/IntentActivatorContext.kt)
depending on the particular NLU service that has recognised an intent.

```kotlin
state("state1") {
    activators {
        intent("MyIntent")
        intent("AnotherIntent")
    }

    action {
        val intent = activator.intent?.intent
    }
}
```

### anyIntent

Activates state if a NLU service has recognised any intent. Useful if you don't want to define exact intent names.

Generates an instance
of [IntentActivatorContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/activator/intent/IntentActivatorContext.kt)
depending on the particular NLU service that has recognised an intent.

```kotlin
state("state1") {
    activators {
        anyIntent()
    }
}
```

### event

Activates state if your bot has received a named event.

Generates [EventActivatorContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/activator/event/EventActivatorContext.kt)
instance.
_Learn more about event activator [here](event)._

```kotlin
state("state1") {
    activators {
        event(AlexaEvent.LAUNCH)
    }

    action {
        val event = activator.event?.event
    }
}
```

### anyEvent

Activates state if your bot has received any event.

Generates [EventActivatorContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/activator/event/EventActivatorContext.kt)
instance.

```kotlin
state("state1") {
    activators {
        anyEvent()
    }
}
```

### regex

Activates state if the user's query matches with the given regular expression.

Generates [RegexActivatorContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/activator/regex/RegexActivatorContext.kt)
that contains a matched pattern and matching result.
_Learn more about regex activator [here](Regex-Activator)._

```kotlin
state("state1") {
    activators {
        regex(".*")
        regex(Pattern.compile("[a-z]+"))
    }

    action {
        val pattern = activator.regex?.pattern
        val matcher = activator.regex?.matcher
    }
}
```

### catchAll

Activates state if any query was received.
_Learn more about catchAll activator [here](CatchAll-Activator)._

```kotlin
state("state1") {
    activators {
        catchAll()
    }

    action {
        val query = request.input
    }
}
```

### Writing custom activation rules

It's easy to implement your own activation rule inside scenario. See the example how to create an intent activator which
activates all intens with common prefix:

```kotlin
// your activation rule
class IntentByPrefixActivationRule(
    private val commonPrefix: String
) : IntentActivationRule({
    it.intent.startsWith(commonPrefix) // your core activation logic
})

// your extension method which registers an activation rule
fun ActivationRulesBuilder.intentByPrefix(commonPrefix: String) = rule(IntentByPrefixActivationRule(commonPrefix))
```

And here is its usage example:
```kotlin
activators {
    intentByPrefix("smalltalk__")
}
```

