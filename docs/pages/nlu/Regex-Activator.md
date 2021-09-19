---
layout: default
title: Regular expressions
permalink: Regex-Activator
parent: Natural Language Understanding
---

[RegexActivator](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/activator/regex/RegexActivator.kt) can be used in JAICF project to handle users' requests that strictly match some regular expression.

> Learn more about regular expressions [here](https://en.wikipedia.org/wiki/Regular_expression).

# How to use

All you need to use regular expressions activator in your JAICF project is to add `regex` activators to the scenarios and then append `RegexActivator` to the `BotEngine`'s array of activators.

## regex activator

```kotlin
state("state1") {
    activators {
        regex("/start")
    }

    action {
        ...
    }
}
```

> Learn more about activators [here](activators).

## RegexActivator configuration

```kotlin
val helloWorldBot = BotEngine(
    scenario = HelloWorldScenario,
    activators = arrayOf(
        ...,
        RegexActivator,
        ...
    )
)
```

# Matcher

Once a `RegexActivator` activates some state, a [RegexActivatorContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/activator/regex/RegexActivatorContext.kt) instance becomes available through an `activator.regex` variable in the action block of this state.

```kotlin
state("state1") {
    activators {
        regex("[^\\d]*((?<number>\\d+).*)+")
    }

    action {
        val number = activator.regex?.group("number")
        reactions.say("Your number is $number")
    }
}
```

# Use cases

Regular expressions are of course not such flexible and smart as a machine learning driven approaches like NLU engines.
But in some cases it can be used to handle a strictly defined users' queries like "/start" message that is sent from Telegram messenger each time the user clicks on "Start" button of the chatbot.
