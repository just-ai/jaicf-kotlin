---
layout: default
title: activators
permalink: activators
parent: Scenario DSL
has_children: true
---

Each scenario's state can be _activated_ by any of configured activator - a rule that tries to handle the user's request and find an appropriate state of scenario to execute it's action block. 
Activators function is a builder function of scenario that can be used to define which activators could activate the state.

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
        activators(fromState = "/other") {
            event("event1")
        }
    }
}
```

In the example above "inner" state can be activated once a dialogue is in state "/other". You can think about this as a
mix-in feature.

# Available activators

JAICF provides a set of available activators that can be used in your scenarios.

* [intent](intent) to activate state by _some particular_ intent recognised by [NLU](Natural-Language-Understanding) service
* [anyIntent](anyIntent) to activate state by _any_ intent
* [event](event) to activate state by _some specific_ event received from the [channel](Channels)
* [anyEvent](anyEvent) to activate state by _any_ event
* [regex](regex) to activate state by matching the user's input with some _regular expression_
* [catchAll](catchAll) to activate state by ant user's input

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

