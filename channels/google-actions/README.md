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

Google Actions platform allows you to use any third-party NLU engine or use a [Dialogflow integration](https://dialogflow.com/docs/integrations/actions/integration).

#### Dialogflow

This is the most easy way to create an Actions project but it requires you to use Dialogflow as NLU engine for your project.
To configure Dialogflow fulfillment add `ActionsDialogflowActivator` to the array of activators in your JAICF agent's configuration:

```kotlin
val helloWorldBot = BotEngine(
    model = MainScenario.model,
    activators = arrayOf(
        ActionsDialogflowActivator
    )
)
```

#### Third-party NLU engine

In case of third-party NLU engine usage you're free to pick any NLU engine you wish to use.
Add its activator to the JAICF agent's configuration:

```kotlin
val helloWorldBot = BotEngine(
    model = MainScenario.model,
    activators = arrayOf(
        RasaIntentActivator.Factory(RasaApi("https://address.com"))
    )
)
```

#### 4. Create and run Actions webhook

The configuration of the Actions webhook depends on the NLU engine configuration (see the section above).

#### Dialogflow fulfillment webhook

If you decided to use Dialogflow as NLU engine, you have to use `ActionsFulfillment.dialogflow` builder this way:

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
_Please note that you have to manually enable fulfillment option for every intent of your Dialogflow agent in this case._

#### SDK fulfillment webhook

If you decided to use third-party NLU engine, you have to use `ActionsFulfillment.sdk` builder this way:

Using [Ktor](https://github.com/just-ai/jaicf-kotlin/wiki/Ktor)

```kotlin
fun main() {
    embeddedServer(Netty, 8000) {
        routing {
            httpBotRouting(
                "/" to ActionsFulfillment.sdk(helloWorldBot)
            )
        }
    }.start(wait = true)
}
```

And accordingly for [Spring Boot](https://github.com/just-ai/jaicf-kotlin/wiki/Spring-Boot).

Once you've obtained a public URL of the webhook (using [ngrok](https://ngrok.com) for example), you have to create Action project and upload the Action JSON package manually.

> Please refer to the [Actions SDK overview](https://developers.google.com/assistant/actions/actions-sdk) to learn how to do this.

## Using SSML

Google Actions support [Speech Synthesis Markup Language](https://developers.google.com/assistant/actions/reference/ssml) enabling you to control how Google Assistant generates the speech.
For example, you can add pauses and other speech effects.

To use these SSML tags in your scenarios you can use it directly or via helper functions:

```kotlin
reactions.say("Hello there! $break500ms Let's listen this audio ${audio("https://address/audio.mp3")}")
```

> Learn more about available SSML helpers [here](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/helpers/ssml/SSML.kt).