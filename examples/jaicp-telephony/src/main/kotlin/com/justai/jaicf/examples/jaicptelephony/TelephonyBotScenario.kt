package com.justai.jaicf.examples.jaicptelephony

import com.justai.jaicf.channel.jaicp.JaicpEvents
import com.justai.jaicf.channel.jaicp.channels.TelephonyEvents
import com.justai.jaicf.channel.jaicp.dto.telephony
import com.justai.jaicf.channel.jaicp.reactions.chatwidget
import com.justai.jaicf.channel.jaicp.reactions.switchToOperator
import com.justai.jaicf.channel.jaicp.reactions.telephony
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.model.scenario.Scenario
import java.time.Instant
import java.time.temporal.ChronoUnit

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

        state("bye") {
            activators {
                intent("bye")
            }
            action {
                reactions.say("Bye-bye!")
                reactions.telephony?.hangup()
            }
        }

        state("redial") {
            activators {
                regex("call me back")
            }
            action {
                reactions.say("Ok, I will call you back in a minute!")
                reactions.telephony?.redial(
                    startDateTime = Instant.now().plus(1, ChronoUnit.MINUTES),
                    localTimeFrom = "12:00",
                    localTimeTo = "23:59",
                    retryIntervalInMinutes = 1,
                    maxAttempts = 2
                )
                reactions.telephony?.hangup()
            }
        }

        state("refusal") {
            activators {
                regex("don't call me")
            }
            action {
                reactions.say("Ok, sorry!")
                reactions.telephony?.setResult("CALLEE REFUSED")
                reactions.telephony?.hangup()
            }
        }

        state("speechNotRecognized") {
            activators {
                event(TelephonyEvents.SPEECH_NOT_RECOGNISED)
                event(JaicpEvents.liveChatFinished)
            }
            action {
                reactions.say("Sorry, I can't hear you! Could you repeat please?")
                reactions.chatwidget?.switchToOperator("asd")
                reactions.say("test")
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
