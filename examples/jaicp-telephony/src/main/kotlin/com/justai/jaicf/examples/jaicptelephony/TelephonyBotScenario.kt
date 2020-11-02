package com.justai.jaicf.examples.jaicptelephony

import com.justai.jaicf.channel.jaicp.channels.TelephonyEvents
import com.justai.jaicf.channel.jaicp.dto.telephony
import com.justai.jaicf.channel.jaicp.reactions.telephony
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.model.scenario.Scenario

object TelephonyBotScenario : Scenario(), WithLogger {
    init {

        state("ringing") {
            activators {
                event(TelephonyEvents.RINGING)
            }
            action {
                request.telephony?.let {
                    logger.debug("Incoming call from ${it.caller}")
                }
            }
        }

        state("start") {
            activators {
                regex("/start")
                intent("hello")
            }
            action {
                reactions.say("Hello! Please say something to test speech recognition.")
            }
        }

        state("hangup") {
            activators {
                intent("bye")
            }
            action {
                reactions.say("Bye-bye!")
                reactions.telephony?.hangup()
            }
        }

        state("speechNotRecognized") {
            activators {
                event(TelephonyEvents.SPEECH_NOT_RECOGNISED)
            }
            action {
                reactions.say("Sorry, I can't hear you! Could you repeat please?")
            }
        }

        state("hangup") {
            activators {
                event(TelephonyEvents.HANGUP)
            }
            action {
                request.telephony?.let {
                    logger.info("Conversation ended with caller: ${it.caller}")
                }
            }
        }

        fallback {
            reactions.say("You said ${request.input}")
            request.telephony?.let {
                logger.info("Unrecognized message from caller: ${it.caller}")
            }
        }
    }
}
