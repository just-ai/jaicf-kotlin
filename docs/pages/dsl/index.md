---
layout: default
title: Scenario DSL
nav_order: 8
permalink: Scenario-DSL
has_children: true
---

JAICF enables developer to create conversational dialogues in a declarative manner using a proprietary DSL.
This chapter describes in details how to use this DSL to build context-aware scenarios for chatbots and voice assistants.

# DSL usage example

Here is an [example of JAICF scenario](https://github.com/just-ai/jaicf-kotlin/blob/master/examples/game-clock/src/main/kotlin/com/justai/jaicf/examples/gameclock/scenario/MainScenario.kt) that can be used during this guide to learn how dialogue scenario's components work.

# Scenario declaration

Each scenario is an instance of the [Scenario](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/model/scenario/Scenario.kt) interface that should be built by using the [Scenario](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/builder/Scenario.kt) function.

There are multiple ways you can obtain and hold the Scenario object depends on your use-case:

* Option 1. The preferred one. Simply save your scenario in a variable.
```kotlin
val MainScenario = Scenario {
  // your scenario code
}
```
This variable can be either top-level or inside an object or a class.
```kotlin
object MainScenario {
  val scenario = Scenario {
    // your scenario code
  }
}
```

* Option 2. If you prefer using scenarios as objects or classes, you can inherit from Scenario interface and override `model` property using `createModel` function.
```kotlin
object MainScenario : Scenario {
  override val model = createModel {
    // your scenario code
  }
}
```

## Composing multiple scenarios

Big projects may contain multiple scenarios that should be merged into a single scenario before the `BotEngine` initialization.
You can use `append` infix function for this purposes.

```kotlin
val bot = BotEngine(
    scenario = MainScenario append SecondScenario append ThirdScenario,
    ...
)
```

# States

Each scenario contains at least one **state**.
State describes a particular state of the dialogue with JAICF agent and can contain nested states.
This enables you to build context-aware scenarios once inner state cannot be activated before its parent state is activated.

> Learn more about states [here](state)

# Fallback state

Each time the user sends a request that can't be handled by any state of the scenario, a _fallback_ state will be activated without a changing the current dialogue's context.

> Learn more about fallback state [here](fallback)

# Activators

Each state can be activated by some **activator**.
This means that state's **action** block will be invoked by JAICF once the state is activated.

Activators try to handle user's request and find a corresponding state of scenario.

```kotlin
state("launch") {
    activators {
        event(AlexaEvent.LAUNCH)
        intent(DialogflowIntent.WELCOME)
    }
}     
```

Here you can see that _launch_ state will be activated once the user says something that can be recognised by Alexa or Dialogflow as Alexa's LAUNCH event or Dialogflow's WELCOME intent.

> Learn more about activators [here](activators).

# Action

An action block of the state contains a Kotlin code that should be executed once the state is activated.
Each action block is executed in the context of [ActionContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/context/ActionContext.kt) instance.
It contains _request-related_ instances of [BotContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/context/BotContext.kt), [ActivatorContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/context/ActivatorContext.kt), [BotRequest](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/api/BotRequest.kt) and [Reactions](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/reactions/Reactions.kt).
These objects can be used in the action block to manage the dialogue and build a response.

As a rule [reactions](reactions) object is used to build a response and change the state of the dialogue.

```kotlin
state("stop") {
    activators {...}
    action {
        reactions.run {
            say("Okay $break200ms See you latter then! Bye bye!")
    
            actions?.endConversation()
            alexa?.run {
                stopAudioPlayer()
                endSession()
            }
    
        }
    }
}

```

> Learn more about action block [here](action).

# How it works

In general every JAICF agent contains one or more scenarios built using JAICF DSL.
Every user's request initially goes from some channel like Amazon Alexa, Google Actions or others.
JAICF manages a current state of dialogue and tries to find the next state to activate using activators configured for each state that is available for activation in this moment.

Every activator is evaluating by JAICF in the order they was declared in `BotEngine` configuration.
Once an activator can handle a request, it creates a concrete activator-specific [ActivatorContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/context/ActivatorContext.kt) instance.
JAICF selects the next state to activate and invokes its action block with [ActionContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/context/ActionContext.kt) containing all data regarding a request, activator and reactions.

Action block can use [reactions](reactions) to build a response and/or change the dialogue's state (jump to another dialogue state for example).

Once the execution is finished, JAICF returns a resulting response to the user.

## State activation algorithm

Once user's request was received via one of the channel, JAICF tries to find an appropriate state evaluating activators in the order they was declared in your `BotEngine` instance.
To select and activate dialogue state, JAICF:

1. Tries child states of the current one and states, that is available from the current state (_fromState_ property)
2. If there is no state that can be activated, JAICF then goes to the current's parent state and repeat step 1 until the state is not found