<p align="center">
    <img src="https://marketplace.mypurecloud.com/43a28e86-6158-4f86-b705-0b7471e24cfe/applogo_d49fd7e8.png" height="128" width="128"/>
</p>

<h1 align="center">Dialogflow NLU activator</h1>

Allows to use [Google Dialogflow](https://dialogflow.com) NLU engine as a states activator in JAICF.

_Built on top of [Google Dialogflow API Client for Java](https://github.com/googleapis/java-dialogflow)._

## How to use

#### 1. Include Dialogflow dependency to your _build.gradle_

```kotlin
implementation("com.justai.jaicf:dialogflow:$jaicfVersion")
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
    model = HelloWorldScenario.model,
    activators = arrayOf(
        dialogflowActivator
    )
)
```