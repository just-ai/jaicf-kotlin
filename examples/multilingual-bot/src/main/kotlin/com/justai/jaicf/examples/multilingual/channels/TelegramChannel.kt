package com.justai.jaicf.examples.multilingual.channels

import com.justai.jaicf.channel.telegram.TelegramChannel
import com.justai.jaicf.examples.multilingual.MultilingualBotEngine

fun main() {
    TelegramChannel(MultilingualBotEngine, "1869785891:AAExDU6Sit1Ru2n76p5vlu-OK0Za0XI2CLk").run()
}