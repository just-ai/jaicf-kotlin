package com.justai.jaicf.examples.helloworld.channel

import com.justai.jaicf.channel.telegram.TelegramChannel
import com.justai.jaicf.examples.helloworld.helloWorldBot

fun main() {
    TelegramChannel(helloWorldBot, "580468601:AAFZaIXLiKbD7XRsGVHX74N-5nlXbttvEzc").run()
}