package com.justai.jaicf.examples.gameclock.scenario

import com.google.api.services.actions_fulfillment.v2.model.TableCard
import com.google.api.services.actions_fulfillment.v2.model.TableCardCell
import com.google.api.services.actions_fulfillment.v2.model.TableCardColumnProperties
import com.google.api.services.actions_fulfillment.v2.model.TableCardRow
import com.justai.jaicf.activator.intent.intent
import com.justai.jaicf.builder.createModel
import com.justai.jaicf.channel.alexa.AlexaReactions
import com.justai.jaicf.channel.alexa.alexa
import com.justai.jaicf.channel.alexa.model.AlexaEvent
import com.justai.jaicf.channel.alexa.model.AlexaIntent
import com.justai.jaicf.channel.googleactions.ActionsReactions
import com.justai.jaicf.channel.googleactions.actions
import com.justai.jaicf.examples.gameclock.GameController
import com.justai.jaicf.examples.gameclock.model.colorLink
import com.justai.jaicf.helpers.ssml.*
import com.justai.jaicf.model.scenario.Scenario
import kotlin.math.floor

object GameLoopScenario : Scenario {

    private const val AUDIO_URL = "https://bitbucket.org/just-ai/examples/downloads/game-timer-1.mp3"
    private const val STARTED_AT = "started_at"

    const val play = "/play"

    override val model = createModel {

        state(play) {

            action {
                val game = GameController(context)
                game.restart()
                game.nextGamer()

                reactions.say(
                    "Okay! We are ready to start the game! ${breakMs(300)} " +
                            "${game.currentColor()} player! You're the first! "
                )

                actions {
                    reactions.playStream(game.currentColor())
                }

                alexa {
                    reactions.say(
                        "Just say me next $break300ms once you have finished your turn! " +
                                "Say pause $break300ms for a short break! " +
                                "Or say stop music $break300ms to finish the game."
                    )

                    reactions.playStream(game.currentColor())
                }
            }
        }

        state("next") {
            activators {
                intent(AlexaIntent.NEXT)
                intent("NextIntent")
                event(AlexaEvent.NEXT)
            }

            action {
                val game = GameController(context)
                var time: Long = 0

                alexa {
                    time = request.handlerInput.requestEnvelope.context.audioPlayer.offsetInMilliseconds
                }

                actions {
                    time = System.currentTimeMillis() - (request.request.userStorage[STARTED_AT] as Number).toLong()
                }

                game.record(time)

                intent {
                    reactions.run {
                        say(
                            "${game.currentColor()} player! " +
                                    "Your ${ordinal(game.currentTurn())} turn took ${timeSpeech(time)}!"
                        )

                        if (game.currentTurn() > 1) {
                            say("$break200ms Your total time is ${timeSpeech(game.currentGamerOverall())} so far.")
                        }
                    }
                }

                game.nextGamer()

                intent {
                    reactions.say("$break1s ${game.currentColor()} player! It is your turn now!")
                }

                alexa { reactions.playStream(game.currentColor()) }
                actions { reactions.playStream(game.currentColor()) }
            }
        }

        state("prev") {
            activators {
                intent(AlexaIntent.PREVIOUS)
                intent("PrevIntent")
                event(AlexaEvent.PREV)
            }

            action {
                val game = GameController(context)
                game.prevGamer()

                intent {
                    reactions.say(
                        "${game.currentColor()} player! Please continue your ${
                            ordinal(
                                game.currentTurn()
                            )
                        } turn."
                    )
                }

                alexa { reactions.playStream(game.currentColor()) }
                actions { reactions.playStream(game.currentColor()) }
            }
        }

        state("pause") {
            activators {
                intent(AlexaIntent.PAUSE)
                intent("PauseIntent")
                event(AlexaEvent.PAUSE)
            }

            action {
                val game = GameController(context)

                alexa {
                    game.currentTime = request.handlerInput.requestEnvelope.context.audioPlayer.offsetInMilliseconds
                }

                intent {
                    reactions.say("Okay! Let's have a break! Once you are ready to continue, just say me play. Or say stop music to finish the game.")
                }

                alexa {
                    reactions.stopAudioPlayer()
                    reactions.endSession()
                }
            }
        }

        state("resume") {
            activators {
                intent(AlexaIntent.RESUME)
                intent("ResumeIntent")
                event(AlexaEvent.PLAY)
            }

            action {
                val game = GameController(context)

                intent {
                    reactions.say(
                        "${game.currentColor()} player! Please continue your ${
                            ordinal(
                                game.currentTurn()
                            )
                        } turn."
                    )
                }

                alexa { reactions.playStream(game.currentColor(), game.currentTime ?: 0) }
                actions { reactions.playStream(game.currentColor(), true) }
            }
        }

        state("finish") {
            activators {
                intent(AlexaIntent.STOP)
                intent("FinishIntent")
            }

            action {
                val game = GameController(context)

                reactions.say("The game is over! $break500ms Here is your overall timing.")

                game.gamersTime.forEach { (color, time) ->
                    reactions.say("$break1s $color gamer $break200ms ${timeSpeech(time)}")
                }

                alexa {
                    reactions.stopAudioPlayer()
                    reactions.endSession("$break1s I also sent this game summary to your Alexa app! Please check for details!")
                }

                actions {
                    reactions.endConversation()
                    reactions.response.builder.add(
                        TableCard().apply {
                            title = "Overall timing"
                            subtitle = formatTime(game.overall!!)

                            columnProperties = listOf(
                                TableCardColumnProperties().setHeader("Player"),
                                TableCardColumnProperties().setHeader("Time")
                            )

                            rows = game.gamersTime.map {
                                TableCardRow().setCells(
                                    listOf(
                                        TableCardCell().setText(it.key),
                                        TableCardCell().setText(formatTime(it.value))
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    private fun AlexaReactions.playStream(color: String?, offset: Long = 0) {
        playAudio(
            url = AUDIO_URL,
            offsetInMillis = offset,
            title = "$color player's turn",
            subtitle = "Say NEXT to pass a turn"
        )
    }

    private fun ActionsReactions.playStream(color: String?, continueTurn: Boolean = false) {
        if (!continueTurn) {
            userStorage?.put(STARTED_AT, System.currentTimeMillis())
        }

        buttons("Pass the turn", "Finish the game")

        playAudio(
            url = AUDIO_URL,
            name = "$color player's turn",
            description = "Say \"Next\" once you've finished",
            icon = com.google.api.services.actions_fulfillment.v2.model.Image()
                .setUrl(colorLink(color)).setAccessibilityText(color)
        )
    }
}

private fun timeSpeech(timeMs: Long): String {
    var seconds = floor(timeMs / 1000f).toInt()
    val minutes = floor(seconds / 60f).toInt()
    seconds %= 60

    var time = if (minutes == 0) "" else if (minutes == 1) "1 minute" else "$minutes minutes"
    if (time.isNotBlank() && seconds > 0) time += " and "
    if (seconds > 0) time += if (seconds == 1) "1 second" else "$seconds seconds"

    return time
}

private fun formatTime(timeMs: Long): String {
    var seconds = floor(timeMs / 1000f).toInt()
    val minutes = floor(seconds / 60f).toInt()
    seconds %= 60

    val m = when {
        minutes > 9 -> minutes.toString()
        else -> "0$minutes"
    }

    val s = when {
        seconds > 9 -> seconds.toString()
        else -> "0$seconds"
    }

    return "$m:$s"
}

