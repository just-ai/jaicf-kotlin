<p align="center">
    <img src="http://pngimg.com/uploads/viber/viber_PNG25.png" width="128" height="128" alt="logo"/>
</p>

<h1 align="center">Viber messenger channel</h1>

Allows to create chatbots for [Viber](https://developers.viber.com/).

_Built on top of [Viber REST API](https://developers.viber.com/docs/api/rest-bot-api/#send-message)_

## How to use

#### 1. Include Viber dependency to your _build.gradle_

```kotlin
implementation("com.just-ai.jaicf:viber:$jaicfVersion")
```

**Replace `$jaicfVersion` with the latest version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

#### 2. Use Viber `request` and `reactions` in your scenarios' actions

```kotlin
action {
    // Viber incoming event
    val incomingEvent = request.viber?.event

    // Fetch sender
    val sender = incomingEvent?.sender

    // Use Viber-specified response builders
    reactions.viber?.location(23.243, 45.555)
    reactions.viber?.sticker(40127)
    reactions.viber?.inlineButtons {
        row("1", "2", "3")
        redirect("google.com", "https://google.com")
    }

    // Or use standard response builders
    reactions.say("Hello there!")
    reactions.image("https://address.com/image.jpg")
}
```

_Note that Viber bot works asynchronously. This means that every reaction method sends a response to the user._

> Refer to the [ViberReactions](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/viber/src/main/kotlin/com/justai/jaicf/channel/viber/ViberReactions.kt) class to learn more about available response builders.

#### Native API

You can use native Viber API directly via `reactions.viber?.viberApi`.
This enables you to fetch some data from Viber REST API (like [getOnline](https://developers.viber.com/docs/api/rest-bot-api/#get-online) for example).

```kotlin
action {
    reactions.viber?.run {
        viberApi.getOnlineStatus(listOf("01234567890="), authToken)
    }
}
```

> Learn more about available API methods [here](https://developers.viber.com/docs/api/rest-bot-api).

#### 3. Create a new bot in Viber

Create a new account for your bot as described [here](https://partners.viber.com/account/create-bot-account).
Copy your new bot's **access token** to the clipboard.

#### 4. Create and run Viber channel

Using [JAICP](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/jaicp)

_For local development:_
```kotlin
fun main() {
    JaicpPollingConnector(
        botApi = viberTestBot,
        accessToken = accessToken,
        channels = listOf(
            ViberChannel.Factory()
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
            ViberChannel.Factory()
        )
    ).start(wait = true)
}
```

Or locally using [Ktor](https://github.com/just-ai/jaicf-kotlin/wiki/Ktor):
```kotlin
fun main() {
    val viber = ViberChannel(
        viberTestBot,
        ViberBotConfig(
            botName = "<your_bot_name>",
            authToken = "<your_auth_token>"
        )
    )

    val server = GlobalScope.async {
        val server: NettyApplicationEngine = embeddedServer(Netty, 8000) {
            routing {
                httpBotRouting("/" to viber)
            }
        }
        server.start(wait = true)
    }

    viber.initWebhook("<your_webhook_url>") // Enter your url
    server.await()
}
```

_Note that you need to init webhook after start your server. Obtain a public URL for your webhook (
using [ngrok](https://ngrok.com) for example)._

## Events

Users can send not only text queries to your Viber bot. They can also send contacts, locations, etc.
These messages contain non-text queries and can be handled in your scenarios via `event` activators.

```kotlin
state("events") {
    activators {
        event(ViberEvent.CONTACT_MESSAGE)
        event(ViberEvent.STICKER_MESSAGE)
    }

    action {
        val location = request.viber?.location
        val contact = request.viber?.contact
    }
}
```

> Learn more about available incoming events [here](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/viber/src/main/kotlin/com/justai/jaicf/channel/viber/api/ViberEvent.kt).

## Buttons

Viber allows to send [keyboards](https://developers.viber.com/docs/tools/keyboards/).
You can use DSL for creating keyboards.

```kotlin
action {
    reactions.say("Click on the button below")
    reactions.viber?.keyboard {
        row("1", "2", "3")
        redirect("google.com", "https://google.com")
    }
}
```

You can specify style for keyboard buttons.
```kotlin
action {
    val style = ViberButton.Style(backgroundColor = "#fdebd0", textSize = Size.LARGE)
    reactions.viber?.keyboard(style) {
        row {
            reply("1", "one")
            redirect("google", "https://google.com")
        }
    }
}
```

Or assign your style to the default style of keyboard buttons.
```kotlin
ViberReactions.keyboardDefaultStyle = ViberButton.Style(backgroundColor = "#fdebd0", textSize = Size.LARGE)
```

You can also send inline buttons.
Although they are not supported by Viber directly, the framework can use RichMediaObject to emulate them.
Inline buttons you can send without any other message.

```kotlin
action {
    reactions.viber?.inlineButtons {
        row {
            reply("1", "one")
            redirect("google", "https://google.com")
        }
    }
}
```

You can also set a style for inline buttons locally using inlineButtonsDefaultStyle in ViberReactions.

_For more example you can learn [the example scenario](https://github.com/just-ai/jaicf-kotlin/blob/master/examples/viber-example/src/main/kotlin/com/justai/jaicf/examples/viber/ViberTestScenario.kt)_
