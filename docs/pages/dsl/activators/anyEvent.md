---
layout: default
title: anyEvent
permalink: anyEvent
parent: activators
grand_parent: Scenario DSL
---

Activates state if your bot has received any event.
Useful if you don't want to define exact event names.

Generates [EventActivatorContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/activator/event/EventActivatorContext.kt) instance.

```kotlin
state("state1") {
    activators {
        anyEvent()
    }
}
```

_Please note that this activator has the minimal priority in your scenario. Even if you placed it on the top of scenario file._
