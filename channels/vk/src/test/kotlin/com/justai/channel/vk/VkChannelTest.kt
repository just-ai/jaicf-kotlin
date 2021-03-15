package com.justai.channel.vk

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.vk.VkChannel
import com.justai.jaicf.channel.vk.VkChannelConfiguration
import com.justai.jaicf.channel.vk.vk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

val config = VkChannelConfiguration(
    "3ac0354ebf99e038416134f79229e46558201dd26900a96dfb1547eabf95847575e8e587f0a56225e75d2",
    200553944 // groupId
)
val bot = Scenario {
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

fun main() {
    VkChannel(BotEngine(bot, activators = arrayOf(RegexActivator)), config).runPolling()
}