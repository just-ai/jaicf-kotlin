---
layout: default
title: Telephony
permalink: Telephony
parent: Channels
---

<p align="center">
    <img src="/assets/images/channels/jaicp.svg" width="128" height="134"/>
</p>

<h1 align="center">JAICP Telephony Channel</h1>

TelephonyChannel can be used to process incoming calls and make smart outgoing calls with [JAICP](JAICP).

> TelephonyChannel JAICP Documentation can be found [here](https://help.just-ai.com/docs/en/telephony/telephony).

## How to use

All you need to start using telephony is to add JAICP dependencies to your project as described [here](JAICP) and [add a telephony channel to your JAICF project in JAICP console](https://help.just-ai.com/docs/en/telephony/telephone_channel).

_[Here is an example](https://github.com/just-ai/jaicf-kotlin/tree/master/examples/jaicp-telephony) of how to use telephony channel in your JAICF scenarios._

## Description

Telephony channel works as a regular voice-driven channel meaning that every user's request is recognised by the selected speech-to-text engine and then mapped to one of existing _intents_.
JAICF [scenario](Scenario-DSL) then activates a corresponding [state](state) and executes it's [action](action) block.

_Read more about natural language understanding [here](Natural-Language-Understanding)._

## Telephony events

Telephony channel can send a set of telephony-specific events.
for example, **TelephonyEvents.speechNotRecognized**, which will be sent if speech-to-text service failed to recognize user's query


```kotlin
state("noSpeech") {
    globalActivators {
        event(TelephonyEvents.speechNotRecognized)
    }
    
    action {
        reactions.say("Sorry, I can't hear you. Please repeat one more time.")
    }
}
```

Please refer to a [full list of supported telephony events](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/jaicp/src/main/kotlin/com/justai/jaicf/channel/jaicp/channels/TelephonyEvents.kt).

## Telephony reactions

Telephony channel provides a [wide range of channel-specific reactions](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/jaicp/src/main/kotlin/com/justai/jaicf/channel/jaicp/reactions/TelephonyReactions.kt) like sending audio response or barge-in (see below).

```kotlin
state("noSpeech") {
    globalActivators {
        event(TelephonyEvents.speechNotRecognized)
    }
    action {
        telephony {
            reactions.audio("https://www2.cs.uic.edu/~i101/SoundFiles/taunt.wav")
        }
    }
}
```

## Caller data

Caller's data can be accessed from **TelephonyBotRequest** class:

```kotlin
fallback {
    reactions.say("You said: ${request.input}")
    telephony {
        logger.info("Unrecognized message ${request.input} from caller: ${request.caller}")
    }
}
```

## Barge-in Feature

> Barge-In is a speech synthesis or audio playback interruption in telephony channel.

JAICF provides DSL methods to efficiently handle when client interrupts telephony bot.

```kotlin
val HelloBargeIn = Scenario(telephony) {
    state("start") {
        activators {
            regex("/start")
        }
        action {
            reactions.say(
                "Hello! My name is Jessica and I will help you to check your order details. Did you order an iPhone yesterday?",
                bargeInContext = "/WelcomeContext"
            )
        }
    }

    state("WelcomeContext") {
        state("Operator") {
            activators {
                intent("Operator")
            }
            action {
                reactions.say("Okay!")
                reactions.transferCall("<OPERATOR_NUMBER>")
            }
        }
    }
}
```

Let's imagine bot is calling a client and saying a welcome phrase. 
They may recognize that's a call from the bot and immediately try to interrupt it asking to switch the call to operator.

```kotlin
reactions.say(
    "Hello! My name is Jessica and I will help you check your order details. Did you order an iPhone yesterday?",
    bargeInContext = "/WelcomeContext"
)
```

This code defines that any client's speech interrupts the bot. 
The interrupting speech should be processed inside a particular context `/WelcomeContext`. 
In such case the bot will be interrupted only if client's input matches `Operator` intent, while any other phrase like `Hello! I'm listening!` or `Oh yeah!` in the middle of input will not interrupt synthesis.

> The idea behind this API is that we should allow interruption only if the bot knows what to answer.

### BargeIn Reactions API

`TelephonyReactions` interruptible methods `say` and `audio` accepts two arguments:

* `bargeInContext: String` defines a context in which BotEngine tries to find a state and resolve if we should interrupt speech synthesis on client's input or not.

* `bargeIn: Boolean` defines if we should try to select a state and resolve interruption with current `DialogContext`. 
  
Usage example for `bargeIn`:

```kotlin
val HelloBargeIn = Scenario(telephony) {
    val waitingState = "PlaySongWhileClientWaits"
    
    state("exampleAudio") {
        action {
            reactions.say("Let me play you a song while you're awaiting!")
            reactions.go(waitingState)
        }
    }

    state(waitingState) {
        action {
            reactions.audio("http://example.com/audio", bargeIn = true)
        }
    }

    state("HowMuchToWait") {
        activators {
            intent("HowMuchToWait")
        }
        action {
            reactions.say("We should find you an operator in 3 to 5 minutes. Keep waiting!")
            reactions.go(waitingState)
        }
    }

    state("AreYouHere") {
        activators {
            intent("AreYouHere")
        }
        action {
            reactions.say("Yeah, I'm here we're about to find an operator to answer your question. Keep waiting!")
            reactions.go(waitingState)
        }
    }
}
```

In this case the bot plays a song while the client waits for the operator. 
Bot doesn't interrupt song if the client says something to their friends. 
Bot replies only to questions like "How much left to await?".

### BargeIn Customization

JAICF provides an open class [BargeInProcessor](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/jaicp/src/main/kotlin/com/justai/jaicf/channel/jaicp/bargein/BargeInProcessor.kt) that performs low-level logics to resolve if client's input should interrupt speech synthesis or audio playback.
