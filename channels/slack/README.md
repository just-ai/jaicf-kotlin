<p align="center">
    <img src="https://cdn4.iconfinder.com/data/icons/logos-and-brands/512/306_Slack_logo-512.png" width="128" height="128"/>
</p>

<h1 align="center">Slack channel</h1>

Allows to create conversational bots for [Slack messenger](https://api.slack.com/start/overview).

_Built on top of [Simple Slack API](https://github.com/Itiviti/simple-slack-api) library._

## How to use

#### 1. Include Slack dependency to your _build.gradle_

```kotlin
implementation("com.justai.jaicf:slack:$jaicfVersion")
```

**Replace `$jaicfVersion` with the latest version ![](https://img.shields.io/github/v/release/just-ai/jaicf-kotlin?color=%23000&label=&style=flat-square)**

#### 2. Use Slack `request` and `reactions` in your scenarios' actions

```kotlin
action {
    // Slack incoming message
    val message = request.slack?.message
    val user = message?.user
    val attachments = message?.attachments

    // Use Slack-specified response builders
    val preparedMessage = reactions.slack?.prepareMessage()
    reactions.slack?.sendMessage(
        preparedMessage?.addAttachment(...)
    )
    
    // Or use standard response builders
    reactions.say("Hello there!")
}
```

> Learn more about [SlackReactions](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/slack/src/main/kotlin/com/justai/jaicf/channel/slack/SlackReactions.kt) and [SlackPreparedMessage](https://github.com/Itiviti/simple-slack-api/blob/master/sources/src/main/java/com/ullink/slack/simpleslackapi/SlackPreparedMessage.java).

_Note that this Slack implementation uses [Real Time Messaging API](https://api.slack.com/rtm) meaning that each response builder method actually sends a reply to the user._

#### 3. Create Slack app with Bots feature enabled

Open [your Slack apps](https://api.slack.com/apps) and create a new one.
Enable **Bots** feature for it.
Copy an **Bot User OAuth Access Token** of your application.

#### 4. Run Slack channel

```kotlin
fun main() {
    SlackChannel(helloWorldBot, "Bot User OAuth Access Token").run()
}
```