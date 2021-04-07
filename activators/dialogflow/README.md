<p align="center">
    <img src="https://marketplace.mypurecloud.com/43a28e86-6158-4f86-b705-0b7471e24cfe/applogo_d49fd7e8.png" height="128" width="128"/>
</p>

<h1 align="center">Dialogflow NLU activator</h1>

Allows to use [Google Dialogflow](https://dialogflow.com) NLU engine as a states activator in JAICF.

_Built on top of [Google Dialogflow API Client for Java](https://github.com/googleapis/java-dialogflow)._

## How to use

#### 1. Include Dialogflow dependency to your _build.gradle_

```kotlin
implementation("com.just-ai.jaicf:dialogflow:$jaicfVersion")
```

**Replace `$jaicfVersion` with the latest version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

#### 2. Use Dialogflow `activator` in your scenario actions

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

#### Native Dialogflow API

You can also obtain a native [Dialogflow's query result](https://github.com/googleapis/java-dialogflow/blob/master/proto-google-cloud-dialogflow-v2/src/main/java/com/google/cloud/dialogflow/v2/QueryResult.java) object by `activator.dialogflow?.queryResult`.

#### 3. Create Dialogflow agent

Go to [Dialogflow Console](https://dialogflow.cloud.google.com/#/newAgent) and create a new agent.
Here you can define a collection of intents that should be recognised from the users' requests.

#### 4. Obtain service account

To use Dialogflow API you have to obtain a service account JSON.
Click on the cog icon on the left side bar of your agent and then on the link from the "Service Account" field.

Then click on the triple dots near the service account name and select "Create key" and then "Create".
A JSON file will be downloaded. Copy this file somewhere in your JAICF project.

#### 5. Configure Dialogflow activator

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

Dialogflow provides [slot filling feature](https://cloud.google.com/dialogflow/es/docs/intents-actions-parameters#required) enabling your JAICF scenario to resolve an intent after all required intent's parameters (slots) were retrieved from the user.
You don't need to make some special changes in your scenarios to use this feature - JAICF handles all slot filling related logic for you.

> Please note that Dialogflow may cancel a slot filling process once the user says "Cancel" command in the middle.
In this case JAICF sends the cancelling user's phrase back to the Dialogflow to recognise a new intent.

## Query parameters and session entities

Dialogflow can accept arbitrary parameters and list of [session entities](https://cloud.google.com/dialogflow/es/docs/entities-session) with every user's request to recognise intent properly.

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
Dialogflow will use this entity recognising an intent from the user's query.

> `provideParameters` function invoked each time the JAICF bot receives a user's request right before to send a request to the Dialogflow API
