package com.justai.jaicf.examples.jaicptelephony

import com.justai.jaicf.activator.caila.CailaIntentActivator
import com.justai.jaicf.activator.caila.CailaIntentActivatorContext
import com.justai.jaicf.activator.caila.caila
import com.justai.jaicf.channel.jaicp.channels.TelephonyEvents
import com.justai.jaicf.channel.jaicp.dto.telephony
import com.justai.jaicf.channel.jaicp.reactions.chatwidget
import com.justai.jaicf.channel.jaicp.reactions.telephony
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.model.scenario.Scenario

object TelephonyBotScenario : Scenario(), WithLogger {
    init {

        state("/ringing") {
            globalActivators {
                event(TelephonyEvents.ringing)
            }
            action {
                request.telephony?.let {
                    logger.debug("Incoming call from ${it.caller}")
                }
            }
        }

        state("Hello") {
            globalActivators {
//                intent("Hello")
            }
            action {
                reactions.say("Приветики")
            }
        }

        state("/playAudio") {
            globalActivators {
                regex("audio")
            }
            action {
                reactions.telephony?.audio("https://248305.selcdn.ru/demo_bot_static/1M.wav")
                reactions.say("somegovno")
                reactions.image("some-url")
                reactions.buttons("govnobuttons", "govnobuttons2")
            }
        }

        state("/da") {
            globalActivators {
                regex("da")
                regex("да")
                regex("вф")
            }
            action {
                reactions.say("нет")
                reactions.buttons("нет", "нет")
            }
        }

        state("/hangup") {
            globalActivators {
                regex("Enough")
            }
            action {
                reactions.say("Fine!")
                reactions.telephony?.hangup()
            }
        }

        state("/speechNotRecognized") {
            globalActivators {
                event(TelephonyEvents.speechNotRecognized)
            }
            action {
                reactions.say("I'm sorry, I can't hear you!")
                logger.debug("Transferring call from ${request.telephony?.caller} to operator")
                reactions.telephony?.transferCall("+79999999999")
            }
        }

        state("/catchHangup") {
            globalActivators {
                event(TelephonyEvents.hangup)
            }
            action {
                request.telephony?.let {
                    logger.info("Conversation ended with caller: ${it.caller}")
                }
            }
        }

        state("start") {
            globalActivators {
//                intent("Hello")
                regex("/start")
            }
            action {
                reactions.say("Hi! Here's some questions I can help you with.")
                reactions.chatwidget?.buttons("How to save the earth", "How to stop drinking")
            }
        }

        fallback {
            val find = skippedActivators
                .first { it is CailaIntentActivatorContext }



        }
    }
}
