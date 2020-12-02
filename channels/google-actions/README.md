<p align="center">
    <img src="https://developers.google.com/assistant/assistant.png" width="128" height="128"/>
</p>

<h1 align="center">Google Actions channel</h1>

Allows to create custom voice actions for [Google Actions](https://developers.google.com/assistant) platform.

_Built on top of [Actions on Google Java/Kotlin Client Library](https://github.com/actions-on-google/actions-on-google-java)_.

## Usage example

Here is an [example](https://github.com/just-ai/jaicf-kotlin/tree/master/examples/game-clock) that shows how to use this channel in practice.

## How to use

#### 1. Include Google Actions dependency to your _build.gradle_

```kotlin
implementation("com.justai.jaicf:google-actions:$jaicfVersion")
```

**Replace `$jaicfVersion` with the latest version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

#### 2. Use Google Actions `request` and `reactions` in your scenarios' actions

```kotlin
state("launch") {
    activators {
        intent(DialogflowIntent.WELCOME)
    }

    action {
        // Google Actions request
        val actionsRequest = request.actions?.request
        val user = actionsRequest?.user

        // Dialogflow named entities extracted from the user's query
        // IN CASE YOU'RE USING ActionsDialogflowActivator
        val slots = activator.actionsDialogflow?.slots

        // Use Actions-specified response builders
        reactions.actions?.endConversation()
        reactions.actions?.userStorage?.put("key", "value")
        reactions.actions?.playAudio("https://address.com/audio.mp3")
        
        // Or use standard response builders
        reactions.say("Hi gamers! ${breakMs(300)}" +
                    "Game clock keeps track of the time for each player during the board game session." +
                    "$break500ms Are you ready to start a game?")

        buttons("Yes", "No")
    }
}
```

> Learn more about Actions SDK [ActionRequest](https://github.com/actions-on-google/actions-on-google-java/blob/master/src/main/kotlin/com/google/actions/api/ActionRequest.kt) and [ActionsReactions](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/google-actions/src/main/kotlin/com/justai/jaicf/channel/googleactions/ActionsReactions.kt).

#### Native response builder

You can also build a response directly via `reactions.actions?.response?.builder` native builder.

```kotlin
action {
    reactions.actions?.response?.run {
        builder.add(BasicCard())
        builder.add(CarouselBrowse())
        // etc.
    }
}
```

> Learn more about native response builder [here](https://github.com/actions-on-google/actions-on-google-java/blob/master/src/main/kotlin/com/google/actions/api/response/ResponseBuilder.kt).

#### 3. Configure activator and webhook
 
Use [Dialogflow](https://dialogflow.cloud.google.com/) to create your Google Actions agent.
Create a new agent with corresponding intents and entities.
Then configure a Dialogflow fulfillment _and enable it on every intent of the agent_.

To configure Dialogflow fulfillment add `ActionsDialogflowActivator` to the array of activators in your JAICF project configuration:

```kotlin
val helloWorldBot = BotEngine(
    model = MainScenario.model,
    activators = arrayOf(
        ActionsDialogflowActivator
    )
)
```

#### 4. Create and run Google Actions

Using [JAICP](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/jaicp)

_For local development:_
```kotlin
fun main() {
    JaicpPollingConnector(
        botApi = helloWorldBot,
        accessToken = "your JAICF project token",
        channels = listOf(
            ActionsFulfillmentDialogflow(useDataStorage = true)
        )
    ).runBlocking()
}
```

_For cloud production:_
```kotlin
fun main() {
    JaicpServer(
        botApi = helloWorldBot,
        accessToken = "your JAICF project token",
        channels = listOf(
            ActionsFulfillmentDialogflow(useDataStorage = true)
        )
    ).start(wait = true)
}
```

Using [Ktor](https://github.com/just-ai/jaicf-kotlin/wiki/Ktor)

```kotlin
fun main() {
    embeddedServer(Netty, 8000) {
        routing {
            httpBotRouting(
                "/" to ActionsFulfillment.dialogflow(helloWorldBot)
            )
        }
    }.start(wait = true)
}
```

Using [Spring Boot](https://github.com/just-ai/jaicf-kotlin/wiki/Spring-Boot)

```kotlin
@WebServlet("/")
class AlexaController: HttpBotChannelServlet(
    ActionsFulfillment.dialogflow(helloWorldBot)
)
```

After this you have to obtain a public URL of your fulfillment webhook and provide it to the agent's settings created via [Dialogflow Console](https://dialogflow.com).

>Please note that you have to manually enable fulfillment option for every intent of your Dialogflow agent in this case.

## User storage

Google Actions provides a built-in [user storage](https://developers.google.com/assistant/conversational/storage-user) that persists any user's data in the internal session and user related storages.
Thus you don't have to set-up any third-party database to store dialogue's state and any arbitrary data stored via [context](https://github.com/just-ai/jaicf-kotlin/wiki/context).

Just define `useDataStorage` property in the builder to enable this feature:

```kotlin
ActionsFulfillment.dialogflow(helloWorldBot, useDataStorage = true)
```

> JAICF transparently serializes and deserializes your context's data

## Using SSML

Google Actions support [Speech Synthesis Markup Language](https://developers.google.com/assistant/actions/reference/ssml) enabling you to control how Google Assistant generates the speech.
For example, you can add pauses and other speech effects.

To use these SSML tags in your scenarios you can use it directly or via helper functions:

```kotlin
reactions.say("Hello there! $break500ms Let's listen this audio ${audio("https://address/audio.mp3")}")
```

> Learn more about available SSML helpers [here](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/helpers/ssml/SSML.kt).