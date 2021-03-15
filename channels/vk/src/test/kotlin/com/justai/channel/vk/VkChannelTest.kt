package com.justai.channel.vk

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.vk.VkChannel
import com.justai.jaicf.channel.vk.VkChannelConfiguration
import com.justai.jaicf.channel.vk.VkEvent
import com.justai.jaicf.channel.vk.vk

val config = VkChannelConfiguration(
    "3ac0354ebf99e038416134f79229e46558201dd26900a96dfb1547eabf95847575e8e587f0a56225e75d2",
    200553944 // groupId
)
val bot = Scenario(vk) {
    state("buttons") {
        activators {
            regex("buttons")
        }
        action {
            reactions.say("You said: ${request.input}", "this", "is", "nice")
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

    state("async") {
        activators {
            regex("async")
        }
        action {
            Thread.sleep(10000)
            reactions.say("some text")
        }
    }

    state("photo") {
        activators {
            event(VkEvent.PHOTO)
        }
        action {
            reactions.say("i'm in PHOTO")
        }
    }

    state("document") {
        activators {
            event(VkEvent.DOCUMENT)
        }
        action {
            reactions.say("i'm in document")
        }
    }

    state("audio") {
        activators {
            event(VkEvent.AUDIO)
        }
        action {
            reactions.say("i'm in audio")
        }
    }

    state("multipleAttachments") {
        activators {
            event(VkEvent.MULTIPLE_ATTACHMENTS)
        }
        action {
            reactions.say("I'm in state multipleAttachments")
        }
    }

    fallback {
        reactions.say("You said: ${request.input}")
    }
}

fun main() {
    VkChannel(BotEngine(bot, activators = arrayOf(RegexActivator)), config).run()
}