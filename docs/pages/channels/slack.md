---
layout: default
title: Slack
permalink: Slack
parent: Channels
---

<p align="center">
    <img src="/assets/images/channels/slack.png" width="128" height="128"/>
</p>

<h1 align="center">Slack channel</h1>

Allows to create conversational bots for [Slack messenger](https://api.slack.com/start/overview).

_Built on top of [Slack Bolt SDK](https://github.com/slackapi/java-slack-sdk) library._

## How to use

#### 1. Include Slack dependency to your _build.gradle_

```kotlin
implementation("com.just-ai.jaicf:slack:$jaicfVersion")
```

**Replace `$jaicfVersion` with the latest version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

#### 2. Use Slack `request` and `reactions` in your scenarios' actions

```kotlin
action {
    // Slack incoming request types
    val command = request.slack?.command // if slash command was received
    val event = request.slack?.event     // if some event was received
    val action = request.slack?.action   // if button was clicked

    // Use Slack-specified response builders
    reactions.slack?.respond(listOf(
        ActionsBlock.builder().build(),
        ContextBlock.builder().build(),
        FileBlock.builder().build(),
        ImageBlock.builder().build(),
        InputBlock.builder().build(),
        RichTextBlock.builder().build(),
        SectionBlock.builder().build(),
        DividerBlock()
    ))
    
    // Or use standard response builders
    reactions.say("Hello there!")
    reactions.image("https://address.com/image.jpg")
    reactions.buttons("Hi", "Bye")
}
```

> Learn more about [SlackReactions](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/slack/src/main/kotlin/com/justai/jaicf/channel/slack/SlackReactions.kt) and [Slack layout blocks](https://api.slack.com/reference/block-kit/blocks).

_Note that this Slack implementation uses [Events API](https://slack.dev/java-slack-sdk/guides/events-api) meaning that each response builder method actually sends a reply to the user._

#### 3. Create Slack app with Bots feature enabled

Open [your Slack apps](https://api.slack.com/apps) and create a new one.
Enable **Bots** feature for it.
Copy a **Bot User OAuth Access Token** from _OAuth & Permissions page_ and **Signing Secret** from _Basic Information page_ of your app.

#### 4. Run Slack channel

Using [JAICP](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/jaicp)

_For local development:_
```kotlin
fun main() {
    JaicpPollingConnector(
        botApi = helloWorldBot,
        accessToken = "your JAICF project token",
        channels = listOf(
            ActionsFulfillmentDialogflow
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
            ActionsFulfillmentDialogflow
        )
    ).start(wait = true)
}
```

Using [Ktor](https://github.com/just-ai/jaicf-kotlin/wiki/Ktor)

```kotlin
fun main() {
    embeddedServer(Netty, 8000) {
        routing {
            httpBotRouting("/" to SlackChannel(
                helloWorldBot,
                SlackChannelConfig(
                    botToken = "Bot User OAuth Access Token",
                    signingSecret = "Signing Secret"
                )
            ))
        }
    }.start(wait = true)
}
```

Using [Spring Boot](https://github.com/just-ai/jaicf-kotlin/wiki/Spring-Boot)

```kotlin
@WebServlet("/")
class SlackController: HttpBotChannelServlet(
    SlackChannel(helloWorldBot, SlackChannelConfig("Bot User OAuth Access Token", "Signing Secret"))
)
```

#### 5. Setup permissions and callback URLs

Obtain a public URL for your webhook (using [ngrok](https://ngrok.com) for example) and copy it to clipboard.
Go to the Slack app page and setup callback URL on _Event Subscriptions_, _Slash Commands_ and _Interactivity & Shortcuts_ pages.

Setup all required permission scopes on _OAuth & Permissions_ page:
* app_mentions:read
* chat:write
* commands
* im:history
* users.profile:read

You can also add more scopes you need.

> Please learn more about Slack scopes [here](https://api.slack.com/scopes).

_Reinstall the app to your workspace if needed._

## Slash commands usage

To react on [slash commands](https://api.slack.com/interactivity/slash-commands) from your scenarios, you have to add `regex` activator with corresponding slash command pattern:

```kotlin
state("mew") {
    activators {
        regex("/mew")
    }
}
```

_Also add `RegexActivator` to the activators list of your [agent configuration](https://github.com/just-ai/jaicf-kotlin/wiki/Regex-Activator)._

## Actions usage

You can respond with buttons on users' requests.
By default the click on the button just sends another request to your scenario with the text of the button.
If you need to receive another value, you can create buttons this way:

```kotlin
action {
    reactions.slack?.buttons(
        "Yes" to "/yes",
        "Nope" to "/no"
    )
}
```

Or add `ActionsBlock` manually:

```kotlin
action {
    reactions.slack?.respond(listOf(
        ActionsBlock.builder().elements(
            ButtonElement.builder()
                .text(PlainTextObject("Button text", false))
                .value("button value")
                .actionId("action-id")
                ...
                .build()
        ).build()
    ))
}
```

## Slack events usage

Slack sends [events](https://api.slack.com/events-api) to your agent each time the user writes a query, uses a slash command or when some non-conversational event happens.
Your scenario can react on any of these events via `event` activator:

```kotlin
state("state1") {
    activators {
        event("user_typing")
    }
    action {
        val event = request.slack?.event as UserTypingEvent
    }
}
```

You can subscribe to any [event Slack supports](https://api.slack.com/events) marked with **Events API label**.

Once an event was received, `request.slack?.event` with `payload` becomes available in the action block.
You can use this variables to fetch event's details. See an example above.

_To receive events you have to add `BaseEventActivator` to the activators list of your [agent configuration](https://github.com/just-ai/jaicf-kotlin/wiki/Event-Activator)._

## Native Slack API usage

`SlackReactions` object provides a direct access to the native SDK interfaces named `context` and `client`.
Using these interfaces you can perform any operations allowed by Slack:

```kotlin
action {
    val ctx = reactions.slack?.context
    val client = reactions.slack?.client
    
    // Receive users list
    val users = client?.usersList(
        UsersListRequest.builder().token(ctx?.botToken).build()
    )
}
```

> Learn more about available Slack API methods [here](https://github.com/slackapi/java-slack-sdk/blob/master/slack-api-client/src/main/java/com/slack/api/methods/MethodsClient.java).

### Fetch user's profile

The most frequently used feature of Slack API in the context of dialogue scenario is fetching the user's profile.
There is a special function provided by `SlackReactions` that can be used for this:

```kotlin
action {
    val profile = reactions.slack?.getUserProfile(request.clientId)
}
```

> Learn more about user profile interface [here](https://github.com/slackapi/java-slack-sdk/blob/master/slack-api-model/src/main/java/com/slack/api/model/User.java).

## Composing responses

You can use both standard response builders and native Slack builder to compose responses in your scenarios.

### Native builder usage

`SlackReactions` provides a `respond` function for native response building:

```kotlin
action {
    reactions.slack?.respond(
        listOf(
            ActionsBlock.builder().build(),
            ContextBlock.builder().build(),
            FileBlock.builder().build(),
            ImageBlock.builder().build(),
            InputBlock.builder().build(),
            RichTextBlock.builder().build(),
            SectionBlock.builder().build(),
            DividerBlock()
        )
    )
}
```

> Learn more about available Slack layout blocks [here](https://api.slack.com/reference/block-kit/blocks).
