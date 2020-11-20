package com.justai.channel.vk

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.channel.vk.VkChannel
import com.justai.jaicf.channel.vk.VkChannelConfiguration
import com.justai.jaicf.channel.vk.vk
import com.justai.jaicf.model.scenario.Scenario
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

val config = VkChannelConfiguration(
    "GroupToken",
    123 // groupId
)
val bot = object : Scenario() {
    init {
        state("buttons") {
            activators {
                regex("buttons")
            }
            action {
                reactions.vk?.say("You said: ${request.input}", "what", "the", "fuck", "is", "that")
            }
        }

        state("image") {
            activators {
                regex("image")
            }
            action {
                reactions.image("https://www.bluecross.org.uk/sites/default/files/d8/assets/images/118809lprLR.jpg")
            }
        }

        state("text") {
            activators {
                regex("text")
            }
            action {
                runBlocking { delay(10000) }
                reactions.say("some text")
            }
        }

        fallback {
            reactions.say("fallback")
        }
    }
}

fun main() {
    VkChannel(BotEngine(bot.model, activators = arrayOf(RegexActivator)), config).runPolling()
}