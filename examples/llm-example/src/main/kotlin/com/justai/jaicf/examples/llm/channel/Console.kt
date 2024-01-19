package com.justai.jaicf.examples.llm.channel

import com.justai.jaicf.channel.ConsoleChannel
import com.justai.jaicf.examples.llm.llmBot

fun main() {
    ConsoleChannel(llmBot).run("/start")
}
