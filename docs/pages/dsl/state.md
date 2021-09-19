---
layout: default
title: state
permalink: state
parent: Scenario DSL
---

State is a main builder function of [ScenarioBuilder](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/builder/ScenarioBuilder.kt) that is a superclass for each dialogue scenario in JAICF.

State describes a particular state of the dialogue and can contain inners states.
This enables to create context-aware agents.

```kotlin
val mainScenario = Scenario {
    state("launch") {

        state("yes") {
            ...
        }

        state("no") {
            ...
        }
    }

    state("fallback", noContext = true) {
        ...
    }
}
```

# How it works

At the starting point only _top-level states_ are available to activate (only "launch" and "fallback" states from the example above).
This means that "yes" and "no" states will be available for user only if they previously sent request, that activates "launch" state which is parent for "yes" and "no".

The next request from the same user will be checked by activators of "yes" and "no" states.
If no one could handle this request, root states' activators will be checked then.

# State path

Each state has a path in a hierarchy of states in the JAICF agent. You can think about state's path as about path of the file in the file system of your OS. Each state has only a single parent, but can contain multiple inner states (or zero inner states).

From the example above, there are next states in the scenario:

* /launch
* /launch/yes
* /launch/no
* /fallback

You have to provide the name for every state of the scenario.
There is also slashes available to use - thus you can define actual hierarchy of states.

```kotlin
val GameColorsScenario = Scenario {
    state("/setup/colors") {
        ...
    }
}
```

Here "/setup/colors" state looks like a top-level, but is hidden from the user because is placed on second level under "/setup". Once "/setup" state is activated, "/setup/colors" becomes visible and available for activation.

# State paramerters

Here is a set of additional parameters that control scenario behaviour.

## noContext

If set to **true**, this flag instructs JAICF not to switch the dialogue's context once this state was activated. As a rule this flag is used with catchAll() activator to create a fallback state that reacts on every unhandled request.

```kotlin
state("fallback", noContext = true) {
    activators {
        catchAll()
    }

    action {
        reactions.say("Sorry, I didn't get it... Please try again or say cancel to stop me.")
    }
}
```

## modal

If set to **true**, this flag instructs JAICF to ignore all other state activators for next request once this state was activated. Usually this flag is used for states that handle an arbitrary text requests.

```kotlin
state("fallback", modal = true) {
    activators {
        catchAll()
    }

    action {
        reactions.say("You said ${request.input}.")
    }
}
```