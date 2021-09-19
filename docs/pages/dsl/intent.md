---
layout: default
title: intent
permalink: intent
parent: Scenario DSL
---

This activator builder appends intent name to the list of state activators:

```kotlin
state("state1") {
  activators {
    intent("MyIntent")
    intent("AnotherIntent")
  }
  ...
}
```

Means that this state can be activated by this intent recognised by one of the configured NLU activators.

> Learn more about intents [here](Natural-Language-Understanding)

## Activator context

Once an intent was matched, the IntentActivatorContext becomes available in the action's block of the state:

```kotlin
state("state1") {
  activators {
    intent("MyIntent")
    intent("AnotherIntent")
  }
  
  action {
    val intent = activator.intent?.intent
    val confidence = activator.intent?.confidence
  }
}
```

## Additional data

Particular intent activator may produce detailed info.
For example named entities.

Please refer to the [appropriate intent activator docs](Natural-Language-Understanding#jaicf-activator-types) to learn what data could be retrieved from the activator context.