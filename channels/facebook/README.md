<p align="center">
    <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/3/3b/Facebook_Messenger_logo.svg/1200px-Facebook_Messenger_logo.svg.png" width="128" height="128"/>
</p>

<h1 align="center">Facebook Messenger channel</h1>

Allows to create chatbots for [Facebook Messenger Platform](https://developers.facebook.com/docs/messenger-platform).

_Built on top of [messenger4j library](https://github.com/messenger4j/messenger4j)._

## How to use

#### 1. Include Facebook Messenger dependency to your _build.gradle_

```kotlin
implementation("com.justai.jaicf:facebook:$jaicfVersion")
```

**Replace `$jaicfVersion` with the latest version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

#### 2. Use Facebook Messenger `request` and `reactions` in your scenarios' actions

```kotlin
action {
    // Facebook Messenger request
    val fbRequest = request.facebook

    // Fetch user's profile
    val user = reactions.facebook?.queryUserProfile()
    
    // Use Messenger-specified response builders
    reactions.facebook?.audio("https://address.com/audio.mp3")
    reactions.facebook?.video("https://address.com/video.mp4")
    reactions.facebook?.file("https://address.com/file.doc")
    reactions.facebook?.sendResponse(
        MessagePayload.create(fbRequest.event.senderId(), MessagingType.RESPONSE, message)
    )

    // Or use standard response builders
    reactions.say("Hello ${user?.firstName()}")
    reactions.image("https://address.com/image.jpg")
    reactions.buttons("What can you do?")
}
```

Each native Facebook Messenger request event is wrapped by [FacebookBotRequest](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/facebook/src/main/kotlin/com/justai/jaicf/channel/facebook/api/FacebookBotRequest.kt) and can be accessed via `request.facebook?.event`.
Event contains additional request details depending from the event type and can be used in your scenario.

> Learn more about [FacebookReactions](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/facebook/src/main/kotlin/com/justai/jaicf/channel/facebook/FacebookReactions.kt) and [FacebookBotRequest](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/facebook/src/main/kotlin/com/justai/jaicf/channel/facebook/api/FacebookBotRequest.kt).

#### Native response builder

You can also build a response directly via `reactions.facebook?.sendResponse` method.

_Note that Facebook Messenger works as asynchronous webhook. This means that every reactions' method actually sends a response to the user._

#### 3. Create Facebook page and application

Create a new Facebook App as described [here](https://developers.facebook.com/docs/messenger-platform/getting-started/app-setup).
Once the app is created, generate and copy the **page access token** of the linked page and **application secret** of created app.

> You can obtain your app secret from **Settings > Basic** on the left side bar.

#### 4. Create and run Messenger webhook

Facebook Messenger requires you to serve a webhook for receiving a users' requests.
As well this webhook must verify a token that will be sent by Facebook Messenger Platform via GET request during the webhook configuration process.

Using [Ktor](https://ktor.io)

```kotlin
fun main() {
    val channel = FacebookChannel(
        helloWorldBot,
        FacebookPageConfig(
            pageAccessToken = "page access token",
            appSecret = "application secret",
            verifyToken = "any arbitrary string"
        )
    )

    embeddedServer(Netty, 8000) {
        routing {

            httpBotRouting("/" to channel)

            get("/") {
                call.respondText(
                    channel.verifyToken(
                        call.parameters["hub.mode"],
                        call.parameters["hub.verify_token"],
                        call.parameters["hub.challenge"]
                    )
                )
            }
        }
    }.start(wait = true)
}
```

#### 5. Setup Webhook

Obtain a public URL for your webhook (using [ngrok](https://ngrok.com) for example) and return to the Facebook application configuration.
Here you have to setup a [Webhook](https://developers.facebook.com/docs/messenger-platform/webhook) of your application.
Provide an obtained URL of your agent's webhook and the same arbitrary string you've used as _verifyToken_.

![](https://i.imgur.com/TboE0Sr.png)

#### 6. Test it

Once a webhook is saved, you can go to the page linked to your app and start chatting with it via Messenger.

_Please note that visitors of your page cannot use your chatbot before you've published it through the App Review process. To start the review go to the app Messenger settings, select a required options (like **pages_messaging**) and submit your request._