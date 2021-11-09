---
layout: default
title: onlyIf
permalink: onlyIf
parent: Scenario DSL
---

An `onlyIf` clause is used for fine customization of [activation rules](activators). An `onlyIf` clause
attached to an activation rule works like post-match condition that will either confirm activation by this rule, or reject it 
based on user-defined predicate.

An `onlyIf` function expects a predicate lambda-function as an argument bringing [context](context),
[request](request) and [activator](activator) in a lambda context.


For example, this `intent` rule will be activated only if an entry with key `name` is in a client context:
```kotlin
        activators {
            intent("MakeOrder").onlyIf { context.client.containsKey("name") }
        }
```

## Chaining

Multiple `onlyIf` conditions can be attached to a single activation rule by chaining working as a logical `and`:
```kotlin
        activators {
            intent("MakeOrder")
                .onlyIf { context.client.containsKey("name") }
                .onlyIf { activator.intent!!.confidence >= 0.7 }
        }

```

## Built-in predicates

JAICF provides several handy `onlyIf` predicates out of the box:

### `disableIf`

Opposite to an `onlyIf`
```kotlin
        activators {
            intent("TextMeBack").disableIf { request is TelephonyBotRequest }
        }
```

### `onlyIfInClient`, `onlyIfInSession`

The rule will be activated only if an entry with a given `key` is in client/session context, and `value` has a given type (_optional_), and a given `predicate` is true (_optional_)
```kotlin
        activators {
            intent("MakeOrder").onlyIfInClient<Int>("age") { it >= 18 }
            intent("MakeOrder").onlyIfInSession<Int>("age") { it >= 18 }
        }
```

### `onlyIfNotInClient`, `onlyIfNotInSession`

The rule will be activated only if an entry with a given `key` is not in client/session context
```kotlin
        activators {
            intent("Name").onlyIfInNotInClient("name")
            intent("Name").onlyIfInNotInSession("name")
        }
```

### `onlyFrom`

The rule will be activated only if the current request is received from a given channel, or the rule was activated by a given [Activator](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/activator/Activator.kt), or both
```kotlin
        activators {
            intent("MakeOrder").onlyFrom(telegram)
            intent("MakeOrder").onlyFrom(dialogflow)
            intent("MakeOrder").onlyFrom(facebook and caila)
        }
```

### `onlyIfBargeIn`, `disableIfBargeIn`

The rule will be activated (rejected) only if the current request is a telephony barge-in request
```kotlin
        activators {
            intent("WhatsYourName").disableIfBargeIn()
            intent("Details").onlyIfBargeIn()
        }   
```
