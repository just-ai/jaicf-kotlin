---
layout: default
title: BotContext delegates
permalink: BotContext-Delegates
parent: Features
---

[BotContext](context) allows to store data associated with user in a form of key-value pairs either on `client`, `session` or `temp` levels.
Manipulating such a data in a raw key-value form may be quite complicated and uncomfortable. So Kotlin [delegated properties](https://kotlinlang.org/docs/delegated-properties.html) comes to rescue.

JAICF provides helper functions that can be used in order to create delegated extension properties on `BotContext` encapsulating accesses to underlying key-value data storage.
This functions are `clientProperty`, `sessionProperty` and `tempProperty` built on top of respective key-value maps. Source code can be found [here](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/helpers/context/BotContextProperty.kt).

```kotlin
var BotContext.userName by clientProperty<String>()
```
This code creates read-write extension property `userName` on `BotContext` of type `String`. 

Accesses to this property will be reflected to accesses to property with key `userName` stored in `BotContext.client`.
```kotlin
action {
    context.userName = "John" // associates key "userName" with value "John" in BotContext.client
    val name = context.userName // reads the value associated with the key "userName" in BotContext.client
}
```

### default
The property defined above will throw an exception in case it is read before its value was set. 
But often it's useful to return some default value if the property value is not set yet or if the property is considered to be read-only. 
One can achieve that by providing default value inside a lambda expression:
```kotlin
var BotContext.isUserBlocked by clientProperty { false } // explicit type declaration can be omitted in that case
```
Inside a lambda expression `BotContext` is available as a lambda parameter. 
In case the property is considred to be computable but not assignable, it can be declared as a `val`
```kotlin
val BotContext.isUserBlocked by clientProperty { blackList.contains(it.clientId) }
```

### saveDefault
By default such properties will recompute default value on each access while some explicit value is not set. 
Sometimes it's desired behavior (as in the above example), but sometimes it's not. 
For example getting default value may consist of costly computations, or it must be computed only once by design.
In this case parameter `saveDefault` should be set to `true`:
```kotlin
val BotContext.sessionStartedAt by sessionProperty(saveDefault = true) { Instant.now() } // will be computed only once and immediately saved to `BotContext.session`
val BotContext.userInfo by clientProperty(saveDeafault = true) { httpClient.getUserInfo(it.clientId) } // requires long computation but will be computed only once

state("start") {
    action {
        val startTime = context.sessionStartedAt
        val userInfo = context.userInfo
        // ...
    }
}

state("hangup") {
    action {
        val duration = Instant.now() - context.sessionStartedAt
        // ...
    }
}
```

### removeOnNull
Kotlin properties must either be able to provide some value or throw an exception in other case, it cannot be "cleared" in any sense.
But sometimes an entry should be fully removed from underlying storage.
In this case parameter `removeOnNull` should be set to `true`. _Unfortunately, in that case the property must have a nullable type even if default value is not `null`_:
```kotlin
var BotContext.order by sessionProperty<Order?>(removeOnNull = true) { createEmptyOrder() }

state("addItem") {
    action {
        context.order!!.add(item)
    }
}

state("clearOrder") {
    action {
        context.order = null
    }
}
```

### key
In all of the above cases key of the property was not explicitely specified. In that case name of the property is used as a key in an underlying storage.
In case explicit key is required, it can be passed in a function via the `key` parameter:
```kotlin
var BotContext.isUserBlocked by clientProperty { false } // key: "isUserBlocked"
var BotContext.isUserBlocked by clientProperty("blockStatus") { false } // key: "blockStatus"
```

## Binding BotContext properties to other classes
It also possible to bind BotContext property to any class `BotContext` is accessible from, for example to `ActionContext`.

## withContext

Function `withContext` allows to bind `BotContext` delegated property to a receiver of any other type by defining a way to get a `BotContext` from the new receiver:
```kotlin
val DefaultActionContext.order by sessionProperty<Order?>(removeOnNull = true) { createEmptyOrder() } withContext { context }
```
Here `context` inside a lambda is obtained from `ActionContext.context`.

Now property `order` can be accessed on `ActionContext`:
```kotlin
action {
    order.add(item)
}
```

## with

Function `with` is similar to `withContext`, it also required defining a way to obtain `BotContext`, 
but it can be used only for properties, created by `clientProperty`, `sessionProperty` or `tempProperty` functions, 
contrary to `withContext` that can be used for any delegated property.
With this limitation function `with` allows to redefine default value of the property with new one that will be computed with new receiver instead of `BotContext`:
```kotlin
val DefaultActionContext.phoneNumber by clientProperty<String>(saveDefault = true).with({ context }) {
    request.telegram?.contact?.phoneNumber ?: error("No phone number found") // `request` from `DefaultActionContext`
}

state("getContact") {
    activators {
        event(TelegramEvent.CONTACT)
    }
    
    action {
        scheduleCall(phoneNumber) // here `phoneNumber` is delegated property on ActionContext
    }
}
```