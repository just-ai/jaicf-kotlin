package com.justai.jaicf.examples.jaicpexamples

import com.justai.jaicf.channel.jaicp.channels.TelephonyEvents
import com.justai.jaicf.channel.jaicp.dto.telephony
import com.justai.jaicf.channel.jaicp.reactions.telephony
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.model.scenario.Scenario

class CitiesGameBotModule : Scenario(), WithLogger {
    init {
        state("/ringing") {
            globalActivators {
                event(TelephonyEvents.ringing)
            }
            action {
                logger.debug("Incoming call from ${request.clientId}")
            }
        }

        state("/start") {
            globalActivators {
                regex("start")
                intent("/start")
            }
            action {
                reactions.apply {
                    say("Hello from ${reactions::class.java.name}")
                    say("Hmm. I dont know what to do... Well, lets play some good old cities game, you wanna?")

                }
            }
        }
        state("/playAudio") {
            globalActivators {
                regex("audio")
            }
            action {
                reactions.telephony?.audio("https://248305.selcdn.ru/demo_bot_static/1M.wav")
            }
        }

        state("/catchAll") {
            globalActivators {
                catchAll()
            }
            action {
                reactions.say("You said: ${request.input}")
                request.telephony?.let {
                    logger.info("Unrecognized message from caller: ${it.caller}")
                }
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
            }
        }
        state("/catchHangup") {
            globalActivators {
                event(TelephonyEvents.hangup)
            }
            action {
                logger.debug("User hanged up")
            }
        }
    }
}
