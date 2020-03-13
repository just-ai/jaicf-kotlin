package com.justai.jaicf.examples.citiesgame

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
        state("/testAudio") {
            globalActivators {
                regex("аудио")
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
                reactions.say("Вы сказали: ${request.input}")
                val trunk = request.telephony?.trunk
                logger.info(trunk)
            }
        }

        state("/testHangup") {
            globalActivators {
                regex("да")
            }
            action {
                reactions.telephony?.hangup()
            }
        }

        state("/speechNotRecognized") {
            globalActivators {
                event(TelephonyEvents.speechNotRecognized)
            }
            action {
                reactions.say("Извините, вас не слышно!")
            }
        }
        state("/hangup") {
            globalActivators {
                event(TelephonyEvents.hangup)
            }
            action {
                logger.debug("User hanged up")
            }
        }

        state("/yes") {
            globalActivators {
                regex("yes")
            }
            action {
                reactions.say("Fine then")
            }
        }
        state("/no") {
            globalActivators {
                regex("no")
            }
            action {
                reactions.say("Ok. We won't play...")
            }
        }
    }
}
