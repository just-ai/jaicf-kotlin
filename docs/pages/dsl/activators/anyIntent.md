---
layout: default
title: anyIntent
permalink: anyIntent
parent: activators
grand_parent: Scenario DSL
---

Activates state if a NLU service has recognised any intent. Useful if you don't want to define exact intent names.

Generates an instance of [IntentActivatorContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/activator/intent/IntentActivatorContext.kt)
depending on the particular [NLU](Natural-Language-Understanding) service that has recognised an intent.

```kotlin
state("state1") {
    activators {
        anyIntent()
    }
}
```

_Please note that this activator has the minimal priority in your scenario. Even if you placed it on the top of scenario file._
