package com.justai.jaicf.examples.helloworld.channel

import com.justai.jaicf.channel.telegram.TelegramChannel
import com.justai.jaicf.examples.helloworld.helloWorldBot

fun main() {
    TelegramChannel(helloWorldBot, "580468601:AAHaMg4gOsN2A_zvIO6-PouVk3GcZ_WVrdI").run()
}