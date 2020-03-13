package com.justai.jaicf.examples.helloworld.channel

import com.justai.jaicf.channel.ConsoleChannel
import com.justai.jaicf.examples.helloworld.helloWorldBot

fun main() {
    ConsoleChannel(helloWorldBot).run()
}