---
layout: default
title: Natural Language Understanding
nav_order: 7
permalink: Natural-Language-Understanding
has_children: true
---

![NLU](/assets/images/nlu.png)

# Introduction to NLU

Natural Language Understanding engines (NLU) enable conversational agents to recognize the meaning of user's text requests.

For example, the user's phrase like _"Could you please book a meeting room for tomorrow?"_ can be recognised by NLU engine as **"book_room"** _intent_.

Moreover, each intent could contain some _named entities_ - parts of the phrase that can be formatted to any type of data.
In the example above there is a date entity, recognised from the **"tomorrow"** word and can be formatted by NLU engine as date type containing language-independent date representation (10.20.2020 for example).

_This allows a conversational agent to react on particular language-independent intents and operate with corresponding named entities to implement a desired functionality._

# JAICF and NLU

JAICF doesn't implement a NLU engine itself.
Instead of this JAICF uses a third-party libraries and services that implement this functionality and provides a ready to use NLU modules for your projects.

_Thus you're free to pick an appropriate NLU implementation that satisfies your requirements (as language support, pricing and etc.) and use it in your JAICF-based project._

The most important here is that **JAICF uses not only intents**.
As you will see below, there is also possible to use [events](event) and [regular expressions](Regex-Activator).

# Activators

Every JAICF dialogue scenario contains a set of _states_ that can be activated by _activators_.
Here is a simple example:

```kotlin
val HelloWorldScenario = Scenario {
    
    state("main") {
        activators {
            intent(DialogflowIntent.WELCOME) // Dialogflow's WELCOME intent
            event(AlexaEvent.LAUNCH)         // Alexa's LAUNCH event
            regex("/start")                  // Simple regular expression
        }

        action {
            ...
        }
    }
}
```

Here you can see how activators are used to define that a particular state of the dialogue can be activated through some _intents_, _events_ or _regex_.

> Learn more about activators [here](activators).

_Once the user says something that can be recognised as "WELCOME" intent, the "main" state is activated and its action block is executed by JAICF. The same regarding Alexa's "LAUNCH" event and "/start" regex._

> Learn more about scenario DSL, states and actions [here](Scenario-DSL).

Here you can see that dialogue scenario is actually independent from the NLU implementation.
All you have to know during the writing the scenario - which intent, event or regex can activate a particular state of the dialogue.
It is up to you how to design a dialogue using intents, events and regex as a state activators.

# JAICF activator types

As you can see, activators in terms of JAICF are responsible for recognising the user's input and activating a corresponding states of the dialogue.
JAICF provides the next types of activators:

* [Intent activators](intent): 
  - [Just AI CAILA](Caila)
  - [Rasa](Rasa)
  - [Google Dialogflow](Dialogflow)  
  - [Amazon Lex](Lex)  
  - [Amazon Alexa](Alexa)
  
* [Event activator](event)
* [Regex activator](regex)
* [CatchAll activator](catchAll)

Please learn more about each of them to understand how it works.

> Some of intent activators are channel-related (like Alexa or Actions Dialogflow Fulfillment) and others could be used with any channels.

# Configuring activators

Every JAICF dialogue agent should use some activators to be able to recognise a users' inputs and activate states.
To make it possible you have to provide activators configuration for `BotEngine` instance that holds your scenario.
For example:

```kotlin
val dialogflowActivator = DialogflowIntentActivator.Factory(
    DialogflowConnector(DialogflowAgentConfig(
        language = "en",
        credentialsResourcePath = "/dialogflow_account.json"
    ))
)

val helloWorldBot = BotEngine(
    scenario = HelloWorldScenario,
    activators = arrayOf(
        AlexaActivator,
        dialogflowActivator
    )
)
```

Here you can see how an array of activators is configured for `helloWorldBot` instance that holds a `HelloWorldScenario`.
Some activators have to be instantiated with an additional configuration (like `DialogflowIntentActivator` in this example).
Others can be just passed to `activators` array as is (`AlexaActivator`, `CatchAllActivator` and etc.).

You can find how to use each particular activator in the corresponding documentation.

## Activators order

It is important to notice that the order of activators in the `activators` array matters.
Meaning that JAICF will check each activator one by one starting from the first one if it can handle the user's request.
In the case if activator handles the request and returns a corresponding dialogue's state, JAICF stops the traversing of the activators array and activates returned state.

_For example, [CatchAllActivator](catchAll) is usually placed the last because it can handle any query request but should be used only in case when no one activator before handled it._
