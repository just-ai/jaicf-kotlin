package com.justai.jaicf.examples.helloworld.channel

import com.justai.jaicf.channel.slack.SlackChannel
import com.justai.jaicf.examples.helloworld.helloWorldBot

fun main() {
    SlackChannel(helloWorldBot, "xoxb-48837248149-716093332418-LJB3yBFijOjXmy4V0jCHFPix").run()
}