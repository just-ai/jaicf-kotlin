<p align="center">
    <img src="https://m.media-amazon.com/images/G/01/mobile-apps/dex/avs/docs/ux/branding/mark1._TTH_.png" width="200" height="200"/>
</p>

<h1 align="center">Amazon Alexa channel</h1>

Allows to create custom voice skills for [Amazon Alexa](https://developer.amazon.com/en-US/docs/alexa/custom-skills/understanding-custom-skills.html).

_Built on top of [Alexa Skills Kit SDK for Java](https://github.com/alexa/alexa-skills-kit-sdk-for-java)._

## Usage example

Here is an [example](https://github.com/just-ai/jaicf-kotlin/tree/master/examples/game-clock) that shows how to use this channel in practice.

## How to use

#### 1. Include Alexa dependency to your _build.gradle_

```kotlin
implementation("com.justai.jaicf:alexa:$jaicfVersion")
```

**Replace `$jaicfVersion` with the latest version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

#### 2. Use Alexa `request`, `activator` and `reactions` in your scenarios' actions

```kotlin
state("launch") {

    activators {
        event(AlexaEvent.LAUNCH)
    }

    action {
        // Amazon SDK HandlerInput
        val input = request.alexa?.handlerInput
        val envelope = input?.requestEnvelope
    
        // Alexa named entities slots extracted from the user's query
        val slots = activator.alexaIntent?.slots
    
        // Use Alexa-specific response builders
        reactions.alexa?.endSession("See you later. Bye bye!")
        reactions.alexa?.playAudio("https://address.com/audio.mp3")
        reactions.alexa?.sendProgressiveResponse("Just a moment please...")
        
        // Or use standard response builders
        reactions.say("How are you?")
    }
}
```

> Learn more about Amazon SDK [HandlerInput](https://github.com/alexa/alexa-skills-kit-sdk-for-java/blob/2.0.x/ask-sdk-core/src/com/amazon/ask/dispatcher/request/handler/HandlerInput.java) and [AlexaReactions](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/alexa/src/main/kotlin/com/justai/jaicf/channel/alexa/AlexaReactions.kt).

#### Native response builder

You can also build a response directly via `reactions.alexa?.response?.builder` native builder. See details [below](#composing-responses).

#### 3. Configure Alexa Activator

Alexa recognises user intents. That is why it is not possible to use third-party NLU engine.
To make this channel work, you have to add [AlexaActivator](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/alexa/src/main/kotlin/com/justai/jaicf/channel/alexa/activator/AlexaActivator.kt) to the activators array of your agent's configuration:

```kotlin
val helloWorldBot = BotEngine(
    model = MainScenario.model,
    activators = arrayOf(
        AlexaActivator
    )
)
```

#### 4. Create and run Alexa webhook

Using [JAICP](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/jaicp)

_For local development:_
```kotlin
fun main() {
    JaicpPollingConnector(
        botApi = helloWorldBot,
        accessToken = "your JAICF project token",
        channels = listOf(
            AlexaChannel
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
            AlexaChannel
        )
    ).start(wait = true)
}
```

Using [Ktor](https://github.com/just-ai/jaicf-kotlin/wiki/Ktor)

```kotlin
fun main() {
    embeddedServer(Netty, 8000) {
        routing {
            httpBotRouting("/" to AlexaChannel(helloWorldBot))
        }
    }.start(wait = true)
}
```

Using [Spring Boot](https://github.com/just-ai/jaicf-kotlin/wiki/Spring-Boot)

```kotlin
@WebServlet("/")
class AlexaController: HttpBotChannelServlet(
    AlexaChannel(helloWorldBot)
)
```

#### 5. Configure Alexa Custom Skill

Create and sign-in to your **Developer account** on [Alexa Developer Console](https://developer.amazon.com/alexa/console/ask).

Create new custom skill and setup a **HTTPS Endpoint** on the Build tab using your public URL of the webhook.
_Select "My development endpoint is a sub-domain of a domain that has a wildcard certificate from a certificate authority" in the drop-down list._

You can obtain a public URL of your webhook using [ngrok](https://ngrok.com) for example.

#### 6. Provide custom intents

Alexa implements its own NLU engine that recognises a user's requests and extracts named entities from their phrases.
You have to provide at least one custom intent to the intents list on the left side bar of Alexa Developer Console.

#### 7. Start testing

To start talking with your Alexa custom voice skill just click on **Save Model** and then **Test** tab.
Here you have to enable **Development mode** to start testing your skill via test console or physical Amazon Echo device.

Just start test using command like _"Start [your skill name]"_.

## Alexa intents and events

Alexa provides a [built-in intents and events](https://developer.amazon.com/en-US/docs/alexa/custom-skills/standard-built-in-intents.html) that can be used in your JAICF scenarios.

> Please refer to the [AlexaIntent](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/alexa/src/main/kotlin/com/justai/jaicf/channel/alexa/model/AlexaIntent.kt) and [AlexaEvent](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/alexa/src/main/kotlin/com/justai/jaicf/channel/alexa/model/AlexaEvent.kt).

```kotlin
object MainScenario : Scenario() {
    init {

        state("launch") {
            activators {
                event(AlexaEvent.LAUNCH)
            }

            action {
                reactions.say("Hi gamers! ${breakMs(300)}" +
                            "Game clock keeps track of the time for each player during the board game session." +
                            "$break500ms Are you ready to start a game?")
            }
        }

        state("cancel") {
            activators {
                intent(AlexaIntent.CANCEL)
            }

            action {
                reactions.alexa?.endSession("Okay $break200ms See you latter then! Bye bye!")
            }
        }
    }
}
```

## Composing responses

You can use both standard and native response builders to compose responses.

```kotlin
action {
    reactions.say("This is a standard way to compose a response.")

    // Or use native builder
    reactions.alexa?.response?.run {
        // Response with card
        builder.withSimpleCard("Card title", "Card text")
        builder.withStandardCard("Card title", "Card text", Image(...))
        builder.withAskForPermissionsConsentCard(listOf(...))
    
        // Display templates
        builder.addRenderTemplateDirective(...)

        // etc.
    }
}
```

> Learn more about available response builders [here](https://github.com/alexa/alexa-skills-kit-sdk-for-java/blob/2.0.x/ask-sdk-core/src/com/amazon/ask/response/ResponseBuilder.java).

### Using SSML

Alexa supports [Speech Synthesis Markup Language](https://developer.amazon.com/en-US/docs/alexa/custom-skills/speech-synthesis-markup-language-ssml-reference.html) enabling you to control how Alexa generates the speech.
For example, you can add pauses and other speech effects.

To use these SSML tags in your scenarios you can use it directly or via helper functions:

```kotlin
reactions.say("Hello there! $break500ms Let's listen this audio ${audio("https://address/audio.mp3")}")
```

> Learn more about available SSML helpers [here](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/helpers/ssml/SSML.kt).