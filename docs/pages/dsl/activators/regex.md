---
layout: default
title: regex
permalink: regex
parent: activators
grand_parent: Scenario DSL
---

Activates state if the user's query matches with the given regular expression.

> Learn more about regular expressions [here](https://en.wikipedia.org/wiki/Regular_expression).

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
