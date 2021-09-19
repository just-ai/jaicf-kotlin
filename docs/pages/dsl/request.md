---
layout: default
title: request
permalink: request
parent: Scenario DSL
---

[BotRequest](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/api/BotRequest.kt) contains a channel-related request data and available through `request` variable in the action block of the dialogue state.

In general this object contains at least type of the request, user's identifier and an input.
Each channel defines its own implementation of [BotRequest](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/api/BotRequest.kt) with additional channel-related request fields. It can be accessed via null-safe invocation of channel extension. For example:

```kotlin
state("main") {

    action {
        var name = context.client["name"]

        if (name == null) {
            request.telegram?.run {
                name = message.chat.firstName ?: message.chat.username
            }
            request.facebook?.run {
                name = reactions.facebook?.queryUserProfile()?.firstName()
            }
        }
    }
}
```

## Request type

JAICF defines three types of the request: _query_, _event_ and _intent_.

## User ID

Channel-related user's identifier is contained in `request.cluentId` variable.

## Input

`request.input` variable contains an input string that corresponds to the request's type.

For _event_ and _intent_ request it is a name of event or intent accordingly.
In case of _query_ request this variable contains a raw text of the user's request.