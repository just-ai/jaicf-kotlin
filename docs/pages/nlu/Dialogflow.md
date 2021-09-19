---
layout: default
title: Dialogflow
permalink: Dialogflow
parent: Natural Language Understanding
---

<p align="center">
    <img src="https://assets.dialogflow.com/common/assets/img/unnamed.png" height="128" alt="Dialogflow ES logo"/>
</p>

<h1 align="center">Dialogflow ES NLU activator</h1>

Allows to use [Google Dialogflow ES](https://cloud.google.com/dialogflow/es/docs) NLU engine as a states activator in JAICF.

_Built on top of [Google Dialogflow ES API Client for Java](https://github.com/googleapis/java-dialogflow)._

## How to use

#### 1. Include Dialogflow ES dependency to your _build.gradle_

```kotlin
implementation("com.just-ai.jaicf:dialogflow:$jaicfVersion")
```

**Replace `$jaicfVersion` with the latest version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

#### 2. Use Dialogflow ES `activator` in your scenario actions

```kotlin
state("launch") {
    activators {
        intent("Default Welcome Intent") // or DialogflowIntent.WELCOME
    }

    action {
        // Recognised named entities
        val slots = activator.dialogflow?.slots

        // Fulfillment messages if any
        val textResponses = activator.dialogflow?.textResponses
        val simpleResponses = activator.dialogflow?.simpleResponses
    }
}
```

> Learn more about [DialogflowActivatorContext](https://github.com/just-ai/jaicf-kotlin/blob/master/activators/dialogflow/src/main/kotlin/com/justai/jaicf/activator/dialogflow/DialogflowActivatorContext.kt).

#### Native Dialogflow ES API

You can also obtain a native [Dialogflow ES query result](https://github.com/googleapis/java-dialogflow/blob/master/proto-google-cloud-dialogflow-v2/src/main/java/com/google/cloud/dialogflow/v2/QueryResult.java) object by `activator.dialogflow?.queryResult`.

#### 3. Create Dialogflow ES agent

Go to [Dialogflow ES Console](https://dialogflow.cloud.google.com/#/newAgent) and create a new agent.
Here you can define a collection of intents that should be recognized from the users' requests.

#### 4. Obtain service account

To use Dialogflow ES API you have to obtain a service account JSON. Go
to [docs](https://cloud.google.com/dialogflow/es/docs/quick/setup#sa-create) to know how to create a service account key
and download JSON key file.

A JSON file will be downloaded. Copy this file somewhere in your JAICF project.

#### 5. Configure Dialogflow ES activator

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
        dialogflowActivator
    )
)
```

## Slot filling

Dialogflow ES
provides [slot filling feature](https://cloud.google.com/dialogflow/es/docs/intents-actions-parameters#required)
enabling your JAICF scenario to resolve an intent after all required intent parameters (slots) were retrieved from the
user. You don't need to make some special changes in your scenarios to use this feature - JAICF handles all slot-filling-related logic for you.

> Please note that Dialogflow ES may cancel a slot filling process once the user says "Cancel" command in the middle.
In this case, JAICF sends the canceling user's phrase back to the Dialogflow ES to recognize a new intent.

## Query parameters and session entities

Dialogflow ES can accept arbitrary parameters and list
of [session entities](https://cloud.google.com/dialogflow/es/docs/entities-session) with every user's request to
recognize intent properly.

To use this feature you have to provide your own `QueryParametersProvider` implementation to the `DialogflowIntentActivator`:

```kotlin
val dialogflowActivator = DialogflowIntentActivator.Factory(
    connector = DialogflowConnector(DialogflowAgentConfig(
        language = "en",
        credentialsResourcePath = "/dialogflow_account.json"
    )),
    queryParametersProvider = object : QueryParametersProvider {
        override fun provideParameters(botContext: BotContext, request: BotRequest): QueryParameters {
            return QueryParameters.newBuilder().addSessionEntityTypes(
               SessionEntityType.newBuilder().setName("color")
                   .addEntities(
                       EntityType.Entity.newBuilder().setValue("#000").addAllSynonyms(listOf("black", "none", "empty"))
                   )
                   .addEntities(
                       EntityType.Entity.newBuilder().setValue("#fff").addAllSynonyms(listOf("white", "bright"))
                   )
            ).build()
        }
    }
)
```

This shows how you can dynamically append `color` session entity to the request.
Dialogflow ES will use this entity to recognize an intent from the user's query.

> `provideParameters` function invoked each time the JAICF bot receives a user's request right before sending a request
> to the Dialogflow ES API
