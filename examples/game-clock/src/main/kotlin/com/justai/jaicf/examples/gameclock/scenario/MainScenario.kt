package com.justai.jaicf.examples.gameclock.scenario

import com.justai.jaicf.builder.createModel
import com.justai.jaicf.channel.alexa.alexa
import com.justai.jaicf.channel.alexa.intent
import com.justai.jaicf.channel.alexa.model.AlexaEvent
import com.justai.jaicf.channel.alexa.model.AlexaIntent
import com.justai.jaicf.channel.googleactions.actions
import com.justai.jaicf.channel.googleactions.dialogflow.DialogflowIntent
import com.justai.jaicf.channel.googleactions.intent
import com.justai.jaicf.examples.gameclock.GameController
import com.justai.jaicf.helpers.ssml.break200ms
import com.justai.jaicf.helpers.ssml.break300ms
import com.justai.jaicf.helpers.ssml.break500ms
import com.justai.jaicf.helpers.ssml.breakMs
import com.justai.jaicf.model.scenario.Scenario

object MainScenario : Scenario {

    override val model = createModel {

        append(GameSetupScenario)
        append(GameLoopScenario)

        state("launch") {
            activators {
                event(AlexaEvent.LAUNCH)
                intent(DialogflowIntent.WELCOME)
            }

            action {
                val game = GameController(context)
                if (game.isReady()) {
                    reactions.go("/start")
                } else {
                    reactions.run {
                        say(
                            "Hi gamers! ${breakMs(300)}" +
                                    "Game clock keeps track of the time for each player during the board game session." +
                                    "$break500ms Are you ready to start a game?"
                        )
                        buttons("Yes", "No")
                    }
                }
            }

            state("yes") {
                activators {
                    intent(AlexaIntent.YES)
                    intent("YesIntent")
                }

                action {
                    reactions.go("/start")
                }
            }

            state("no") {
                activators {
                    intent(AlexaIntent.NO)
                    intent("NoIntent")
                }

                action {
                    reactions.go("/cancel")
                }
            }
        }

        state("cancel") {
            activators {
                intent(AlexaIntent.CANCEL)
            }

            action {
                reactions.run {
                    say("Okay $break200ms See you latter then! Bye bye!")

                    actions {
                        reactions.endConversation()
                    }

                    alexa {
                        reactions.stopAudioPlayer()
                        reactions.endSession()
                    }
                }
            }
        }

        state("start") {
            activators {
                intent("StartIntent")
            }

            action {
                val game = GameController(context)
                var gamers: Int? = null

                alexa.intent {
                    gamers = activator.slots["gamers"]?.value?.toInt()
                }

                actions.intent {
                    gamers = (activator.slots["gamers"] as? Number)?.toInt()
                }

                when {
                    gamers != null -> {
                        game.gamers = gamers
                        reactions.go(GameSetupScenario.state)
                    }
                    game.isReady() -> {
                        reactions.go("/restart")
                    }
                    else -> {
                        game.reset()
                        reactions.go(GameSetupScenario.state, GameLoopScenario.play)
                    }
                }
            }

        }

        state("restart") {

            action {
                val game = GameController(context)
                reactions.run {
                    say("Hello! Would you like to restart your previous game for ${game.gamers} gamers?")
                    buttons("Yes", "No")
                }
            }

            state("yes") {
                activators {
                    intent(AlexaIntent.YES)
                    intent("YesIntent")
                }

                action {
                    GameController(context).restart()
                    reactions.go(GameLoopScenario.play)
                }
            }

            state("no") {
                activators {
                    intent(AlexaIntent.NO)
                    intent("NoIntent")
                    catchAll()
                }

                action {
                    GameController(context).reset()
                    reactions.go(GameSetupScenario.state, GameLoopScenario.play)
                }
            }
        }

        state("help", noContext = true) {
            globalActivators {
                intent(AlexaIntent.HELP)
                intent("HelpIntent")
            }

            action {
                reactions.run {
                    say("Game clock keeps track of the time for each player during the board game session. ")
                    say("Just say me $break300ms Start new game $break200ms to start a new round.")

                    buttons("Start new game")
                }
            }
        }

        fallback {
            reactions.say("Sorry, I didn't get it... Please try again or say cancel to stop me.")
        }
    }
}