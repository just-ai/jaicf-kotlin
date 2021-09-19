---
layout: default
title: Introduction
nav_order: 5
permalink: Introduction
---

![JAICF Components](assets/images/jaicf-components.png)

JAICF is a Kotlin framework for conversational context-aware chatbot and voice assistant development.

It enables any Kotlin developer to build and run an enterprise-level cross-platform agents meaning that a single JAICF scenario can work simultaneously over multiple channels like Amazon Alexa, Google Actions, Facebook Messenger and others.

This chapter describes main concepts and parts of JAICF to help you understand how it can be used in your project.

# JAICF idea

The main idea of JAICF is to combine multiple platform libraries, NLU engines, persistence layers and servers in the single framework that manages a dialogue state transparently for the developer.

> This means that **JAICF is not a NLU engine or voice assistant itself**. JAICF provides a comprehensive open source architecture mixing all these components into conversational agents framework enabling the developer to create a context-aware dialogue and connect it to multiple platforms like Amazon Alexa, Facebook Messenger and etc.

# Scenario DSL

Scenario DSL is a power of JAICF. It enables a developer to write a dialogue scenario of the conversational agent in a declarative manner using [Kotlin context-oriented capabilities](https://proandroiddev.com/an-introduction-context-oriented-programming-in-kotlin-2e79d316b0a2). Here is a simple example:

```kotlin
val MainScenario = Scenario {
    state("main") {
        activators {
            intent(DialogflowIntent.WELCOME)
        }

        action {
            reactions.say("Hi there!")
        }
    }

    fallback {
        reactions.say("I have nothing to say yet...")
        actions {
            reactions.say("Bye bye!")
            reactions.endConversation()
        }
    }
}
```

Each JAICF conversational agent (chatbot or voice assistant skill) contains at least one scenario that implements a dialogue logic in terms of _states_, _activators_, _action_, _reactions_ and other components.

> Learn more about JAICF DSL [here](Scenario-DSL).

# JAICF components

![JAICF components](assets/images/jaicf-components-2.png)

JAICF is a modular framework that contains multiple components.
All these components are used by JAICF to glue scenarios with persistent layers, channels, and NLU engines transparently for the developer.

> Learn more about how to install JAICF components using different build tools [here](Installing).

## Activators

Activators are used to activate a state of the dialogue scenario.
In fact _every activator tries to handle a user's request_ to your JAICF agent and find the next state of the scenario regarding the current one. Each JAICF agent should have at least one activator configuration. 

_Dialogflow, Rasa, and JAICP Caila - are examples of activators in JAICF._

> Learn more about activators [here](Natural-Language-Understanding).

## Channels

Channel is an interface between the JAICF dialogue scenario and some text or voice platform.

_Amazon Alexa, Google Actions, Facebook Messenger, and Slack - are examples of channels in JAICF._

JAICF is a multi-channel (or multi-platform) framework meaning that a single agent can work simultaneously in many channels.

> Learn more about channels [here](Channels).

## Managers

During the agent's lifecycle, there are some data that should be persisted between the user's requests to the agent.
Dialogue state details, some arbitrary user- or session-related variables - are examples of such data.

JAICF provides a persistence layer that loads and stores data transparently for the developer using managers.

_MapDB, Mongo DB - are examples of managers in JAICF._

> Learn more about managers [here](Environments).

## Servers

To make your agent available for incoming requests from users, there should be some server started that holds your JAICF project configuration and proxies requests from the different channels.
JAICF provides a ready to use helpers that can be used to easily run such a server and deploy it to any cloud like Heroku. Here is an example of [Ktor](https://ktor.io/) Netty server usage:

```kotlin
fun main() {
    embeddedServer(Netty, 8000) {
        routing {
            httpBotRouting(
                "/alexa" to AlexaChannel(gameClockBot),
                "/actions" to ActionsFulfillment.dialogflow(gameClockBot)
            )
        }
    }.start(wait = true)
}
```

> Learn more about different servers [here](Environments).

# Where to go next

Here you've learned about the main concepts and components of JAICF.
Good next step after this is to dive into each component details by links provided above or learn the code of [examples](https://github.com/just-ai/jaicf-kotlin/tree/master/examples) that show how to use these components in practice.
Also if you didn't try our [Quick Start](Quick-Start) yet, it's a good time to do this!