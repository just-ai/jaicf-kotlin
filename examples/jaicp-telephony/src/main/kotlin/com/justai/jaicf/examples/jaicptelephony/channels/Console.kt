package com.justai.jaicf.examples.jaicptelephony.channels

import com.justai.jaicf.channel.ConsoleChannel
import com.justai.jaicf.examples.jaicptelephony.telephonyCallScenario

fun main() {
    ConsoleChannel(telephonyCallScenario).run("/start")
}