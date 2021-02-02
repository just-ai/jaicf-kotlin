package com.justai.jaicf.examples.jaicptelephony

import com.justai.jaicf.channel.jaicp.channels.TelephonyEvents
import com.justai.jaicf.channel.jaicp.dto.telephony
import com.justai.jaicf.channel.jaicp.reactions.telephony
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.model.scenario.Scenario
import java.time.Instant
import java.time.temporal.ChronoUnit
import com.justai.jaicf.builder.*
import com.justai.jaicf.channel.jaicp.telephony

object TelephonyBotScenario : WithLogger, Scenario {

    override val model by startScenario(telephony) {

        state("ringing") {
            activators {
                event(TelephonyEvents.RINGING)
            }
            action {
                logger.debug("Incoming call from ${request.caller}")
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

        state("bye") {
            activators {
                intent("bye")
            }
            action {
                reactions.say("Bye-bye!")
                reactions.hangup()
            }
        }

        state("redial") {
            activators {
                regex("call me back")
            }
            action {
                reactions.say("Ok, I will call you back in a minute!")
                reactions.redial(
                    startDateTime = Instant.now().plus(1, ChronoUnit.MINUTES),
                    localTimeFrom = "12:00",
                    localTimeTo = "23:59",
                    retryIntervalInMinutes = 1,
                    maxAttempts = 2
                )
                reactions.hangup()
            }
        }

        state("refusal") {
            activators {
                regex("don't call me")
            }
            action {
                reactions.say("Ok, sorry!")
                reactions.setResult("CALLEE REFUSED")
                reactions.hangup()
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
                logger.info("Conversation ended with caller: ${request.caller}")
            }
        }

        fallback {
            reactions.say("You said ${request.input}")
            logger.info("Unrecognized message from caller: ${request.caller}")
        }
    }
}