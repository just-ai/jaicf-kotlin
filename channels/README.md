# Supported channels list

You can connect your JAICF conversational agent to the channels listed below.

### Voice assistants

* [Aimybox](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/aimybox)
* [Amazon Alexa](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/alexa)
* [Google Actions](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/google-actions)
* [Yandex Alice](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/yandex-alice)

### Messengers

* [Facebook Messenger](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/facebook)
* [Slack](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/slack)
* [Telegram](https://github.com/just-ai/jaicf-kotlin/tree/master/channels/telegram)

# How to use

Please refer to the selected channel page to learn how to connect your JAICF conversational agent to this channel.

> Learn more about JAICF channels [here](https://github.com/just-ai/jaicf-kotlin/wiki/Channels).

# How to create a new channel

You can learn how to create a new channel connection investigating existing implementations source code.

In general, every channel in JAICF contains its own [BotRequest](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/api/BotRequest.kt) and [Reactions](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/reactions/Reactions.kt).
Depending on the type of connection it can implement [HttpBotChannel](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/channel/http/HttpBotChannel.kt) (in case of webhook type) or [BotChannel](https://github.com/just-ai/jaicf-kotlin/blob/master/core/src/main/kotlin/com/justai/jaicf/channel/BotChannel.kt) directly (in case of websocket or long polling).
