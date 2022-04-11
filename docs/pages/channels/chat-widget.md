---
layout: default
title: Chat Widget
permalink: Chat-Widget
parent: Channels
---

<p align="center">
    <img src="/assets/images/channels/jaicp.svg" width="128" height="134"/>
</p>

<h1 align="center">JAICP Chat Widget Channel</h1>

Chat Widget channel can be used to add a web widget onto your website and process incoming messages from visitors.

> ChatWidget JAICP Documentation can be found [here](https://help.just-ai.com/docs/en/channels/chatwidget/chatwidget).

# How to use

All you need to start using chat widget is to add JAICP dependencies to your project as described [here](JAICP) and [add a chat widget channel to your JAICF project in JAICP console](https://help.just-ai.com/docs/en/channels/chatwidget/chatwidget).

![Create first channel](/assets/gifs/create-chat-widget.gif)

# Chat widget API

## Reactions

Beaing a messenget-like channel, chat widget provides a set of reactions to response with text, image, buttons, audio and others reply types.

```kotlin
state("start") {
    globalActivators {
        intent("Hello")
        regex("/start")
    }
    action {
        reactions.say("Hi! Here's some questions I can help you with.")
        reactions.chatwidget?.buttons("How to save the earth", "How to stop drinking")
    }
}
```

Please refer to the [ChatWidgetReactions](https://github.com/just-ai/jaicf-kotlin/blob/master/channels/jaicp/src/main/kotlin/com/justai/jaicf/channel/jaicp/reactions/ChatWidgetReactions.kt) to learn more about available reactions.

## Passing parameters to Chat Widget

Sometimes it is necessary to be able to pass some parameters when loading the widget. For example, to let the bot know the user's ID, name or other data. 
Such parameters are transferred to the widget when the widget is opened on the website.

These parameters can be set to widget page, as it [referenced in widget documentation](https://help.just-ai.com/docs/en/channels/chatwidget/parameters_transfer), and retrieved in scenario from `reactions.chatwidget.jaicp.data` json object.

