package com.justai.jaicf.examples.jaicptelephony

import com.justai.jaicf.channel.jaicp.channels.TelephonyEvents
import com.justai.jaicf.channel.jaicp.dto.telephony
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

        state("/start") {
            globalActivators {
                regex("start")
                intent("/start")
            }
            action {
                reactions.say("Hello from telephony channel! Say audio to play some audio example.")
                reactions.say("Or you can say enough to end this conversation.")
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
                request.telephony?.let {
                    logger.info("Conversation ended with caller: ${it.caller}")
                }
            }
        }

        fallback {
            reactions.say("You said: ${request.input}")
            request.telephony?.let {
                logger.info("Unrecognized message from caller: ${it.caller}")
            }
        }
    }
}
