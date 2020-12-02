package com.justai.jaicf.examples.gameclock.scenario

import com.amazon.ask.model.interfaces.display.Image
import com.amazon.ask.model.interfaces.display.ImageInstance
import com.google.api.services.actions_fulfillment.v2.model.TableCard
import com.google.api.services.actions_fulfillment.v2.model.TableCardCell
import com.google.api.services.actions_fulfillment.v2.model.TableCardColumnProperties
import com.google.api.services.actions_fulfillment.v2.model.TableCardRow
import com.justai.jaicf.activator.intent.intent
import com.justai.jaicf.channel.alexa.*
import com.justai.jaicf.channel.alexa.model.AlexaEvent
import com.justai.jaicf.channel.alexa.model.AlexaIntent
import com.justai.jaicf.channel.googleactions.ActionsReactions
import com.justai.jaicf.channel.googleactions.actions
import com.justai.jaicf.examples.gameclock.GameController
import com.justai.jaicf.examples.gameclock.model.colorLink
import com.justai.jaicf.helpers.ssml.*
import com.justai.jaicf.model.scenario.Scenario
import kotlin.math.floor

object GameLoopScenario: Scenario() {

    private const val AUDIO_URL = "https://bitbucket.org/just-ai/examples/downloads/game-timer-1.mp3"
    private const val STARTED_AT = "started_at"

    const val play = "/play"

    init {

        state(play) {

            action {
                val game = GameController(context)
                game.restart()
                game.nextGamer()

                reactions.run {
                    say(
                        "Okay! We are ready to start the game! ${breakMs(300)} " +
                                "${game.currentColor()} player! You're the first! "
                    )

                    actions?.playStream(game.currentColor())

                    alexa?.run {
                        say(
                            "Just say me next $break300ms once you have finished your turn! " +
                                    "Say pause $break300ms for a short break! " +
                                    "Or say stop music $break300ms to finish the game."
                        )

                        playStream(game.currentColor())
                    }
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

                request.alexa?.run {
                    time = handlerInput.requestEnvelope.context.audioPlayer.offsetInMilliseconds
                }

                request.actions?.run {
                    time = System.currentTimeMillis() - (request.userStorage[STARTED_AT] as Number).toLong()
                }

                game.record(time)

                activator.intent?.run {
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

                activator.intent?.run {
                    reactions.say("$break1s ${game.currentColor()} player! It is your turn now!")
                }

                reactions.run {
                    alexa?.playStream(game.currentColor())
                    actions?.playStream(game.currentColor())
                }
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

                activator.intent?.run {
                    reactions.say("${game.currentColor()} player! Please continue your ${ordinal(
                        game.currentTurn()
                    )} turn.")
                }

                reactions.run {
                    alexa?.playStream(game.currentColor())
                    actions?.playStream(game.currentColor())
                }
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

                request.alexa?.run {
                    game.currentTime = handlerInput.requestEnvelope.context.audioPlayer.offsetInMilliseconds
                }

                activator.intent?.run {
                    reactions.say("Okay! Let's have a break! Once you are ready to continue, just say me play. Or say stop music to finish the game.")
                }

                reactions.alexa?.run {
                    stopAudioPlayer()
                    endSession()
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

                activator.intent?.run {
                    reactions.say("${game.currentColor()} player! Please continue your ${ordinal(
                        game.currentTurn()
                    )} turn.")
                }

                reactions.run {
                    alexa?.playStream(game.currentColor(), game.currentTime ?: 0)
                    actions?.playStream(game.currentColor(), true)
                }
            }
        }

        state("finish") {
            activators {
                intent(AlexaIntent.STOP)
                intent("FinishIntent")
            }

            action {
                val game = GameController(context)

                reactions.run {
                    say("The game is over! $break500ms Here is your overall timing.")

                    game.gamersTime.forEach { (color, time) ->
                        say("$break1s $color gamer $break200ms ${timeSpeech(time)}")
                    }

                    alexa?.run {
                        stopAudioPlayer()
                        endSession("$break1s I also sent this game summary to your Alexa app! Please check for details!")
                    }

                    actions?.run {
                        endConversation()
                        response.builder.add(
                            TableCard().apply {
                                title = "Overall timing"
                                subtitle = formatTime(game.overall!!)

                                columnProperties = listOf(
                                    TableCardColumnProperties().setHeader("Player"),
                                    TableCardColumnProperties().setHeader("Time")
                                )

                                rows = game.gamersTime.map {
                                    TableCardRow().setCells(listOf(
                                        TableCardCell().setText(it.key),
                                        TableCardCell().setText(formatTime(it.value))
                                    ))
                                }
                            }
                        )
                    }
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

