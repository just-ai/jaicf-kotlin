---
layout: default
title: reactions
permalink: reactions
parent: Scenario DSL
---

[Reactions](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/reactions/Reactions.kt) is a main interface for building the response that should be sent back to the user.
It also contains functions for dialogue state managing.
That is why this abstraction has been named `Reactions` - it provides a ways to **react** somehow on the request from the user.

# How to use

Each channel in JAICF creates its own channel-related [Reactions](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/reactions/Reactions.kt) implementation and provides it through the [ActionContext](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/context/ActionContext.kt) to the action block of the scenario. Once the state is activated, JAICF executes its action block in context of this object, thus reactions interface becomes available for response building and dialogue state managing. Here is a simple example:

```kotlin
        state("fallback", noContext = true) {
            activators {
                catchAll()
            }

            action {
                reactions.say("I have nothing to say yet...")
                reactions.actions?.run {
                    say("Bye bye!")
                    endConversation()
                }
            }
        }
```

Here you can see how `reactions` is used.
Once a "fallback" state is activated, JAICF executes its action block.
Reactions instance contains `say` method to send a raw text in the response to the user.

> Channel that received a request and created this `Reactions` is responsive for this function invocation handling.

# Response building methods

Each `Reactions` instance contains functions that help to build a response(s).

### say

`say` method just appends a raw text reply to the response.
There is also `sayRandom` variation of this method that picks a random text to append to response.

### image

`image` method appends an image URL to the response.

_Please note that not every channel supports this type of reply._

### buttons

`buttons` method appends buttons array to the response. Can also activate states, for example:
```kotlin
reactions.buttons("How are you?" to "/HowAreYou", "Bye" to "/GoodBye")
```
And when user clicks the button `How are you?` state `/HowAreYou` will be activated.

_Please note that not every channel supports this type of reply._

# Channel-related reactions

You can also notice how channel-related reactions can be used.

In the example above there is a _null-safe_ invocation of `reactions.actions?` that works only in case the request was received by _Google Actions_ channel. In this case a channel-related reactions become available - like Actions-related `endConversation` function that finishes the Actions execution on a Google Assistant's side.

> To learn what channel-related functions are available, please look into a corresponding channel's [Reactions](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/reactions/Reactions.kt) implementation. For example there are [ActionsReactions](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/google-actions/src/main/kotlin/com/justai/jaicf/channel/googleactions/ActionsReactions.kt) for Google Actions, [AlexaReactions](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/alexa/src/main/kotlin/com/justai/jaicf/channel/alexa/AlexaReactions.kt) for Alexa and etc.

Also it is important to know that you can extend any channel-related `Reactions` if it doesn't contain methods you need. Because each `Reactions` holds a native response builder - thus you can use it directly.

# Dialogue state managing

`Reactions` also can be used to jump through the states in dialogue scenario.

```kotlin
        state("setup") {
            action {
                val game = GameController(context)
                if (game.gamers == null) {
                    reactions.say("Okay! Let's start a new game!")
                    reactions.go("/setup/gamers", "next")
                } else {
                    reactions.go("next")
                }
            }

            state("next") {
                action {
                    val game = GameController(context)
                    game.gamers = game.gamers ?: context.result as Int

                    reactions.run {
                        say("${game.gamers} gamers! Cool! Now you have to choose a color for each of you!")
                        go("/setup/colors", "../complete")
                    }
                }
            }

            state("complete") {
                action {
                    reactions.goBack()
                }
            }
        }
```

Here you can see how `reactions` can be used to programmatically change the current state of the dialogue.
Below you will find corresponding methods description.

### go

`go` is one of the most used state managing method.
It accepts the path (absolute or relative) to the next state to activate.
JAICF activates this state immediately meaning that it invokes state's action block and changes the current state to this one.

Also this method accepts an optional **callback state**.
This state will be activated by JAICF once some inner dialogue action invokes `goBack` or `changeStateBack` method.
It is a way to receive some result from the invocable state. It described in details below.

### changeState

`changeState` method is similar to `go` but JAICF doesn't invoke action block of the state in this case.

### goBack

`goBack` method accepts an optional arbitrary value and activates a state that has been previously provided as a callback via `go` or `changeState`.
The passed value can be obtained in the callback state via `context.result`:

In the example above you can see how it is used in "next" state that is passed previously as a callback state via `reactions.go("/setup/gamers", "next")`.

### changeStateBack

`changeStateBack` method is similar to `goBack` but JAICF doesn't invoke action block of the callback state in this case.