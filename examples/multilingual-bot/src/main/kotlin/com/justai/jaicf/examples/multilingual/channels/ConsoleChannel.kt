package com.justai.jaicf.examples.multilingual.channels

import com.justai.jaicf.channel.ConsoleChannel
import com.justai.jaicf.examples.multilingual.MultilingualBotEngine

fun main(args: Array<String>) {
    ConsoleChannel(MultilingualBotEngine).run("/start")
}