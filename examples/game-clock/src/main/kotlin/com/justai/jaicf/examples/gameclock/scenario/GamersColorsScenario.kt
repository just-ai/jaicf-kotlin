package com.justai.jaicf.examples.gameclock.scenario

import com.justai.jaicf.channel.alexa.activator.alexaIntent
import com.justai.jaicf.channel.googleactions.dialogflow.actionsDialogflow
import com.justai.jaicf.helpers.ssml.break200ms
import com.justai.jaicf.helpers.ssml.fast
import com.justai.jaicf.helpers.ssml.ordinal
import com.justai.jaicf.examples.gameclock.GameController
import com.justai.jaicf.examples.gameclock.model.supportedColors
import com.justai.jaicf.helpers.ssml.break500ms
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.test.context.runInTest

object GamersColorsScenario: Scenario() {

    const val state = "/setup/colors"

    init {
        state(state) {
            action {
                val game = GameController(context)

                if (game.colors.isNullOrEmpty()) {
                    // this state didn't run before, thus setup initial values
                    game.currentGamer = 1
                    game.colors = mutableListOf()
                }

                reactions.say("$break200ms ${ordinal(game.currentGamer!!)} player! " +
                        random("What is your color?", "Pick your color.", "Say me your color."))

                reactions.buttons(*supportedColors.subtract(game.colors).toTypedArray())
            }

            state("color") {
                activators {
                    intent("GamerColorIntent")
                    catchAll()
                }

                action {
                    val game = GameController(context)
                    var color: String? = null

                    activator.alexaIntent?.run {
                        color = slots["color"]?.value
                    }

                    activator.actionsDialogflow?.run {
                        color = slots["color"] as? String
                    }

                    runInTest {
                        color = getVar("color") as? String
                    }

                    if (game.colors.isNullOrEmpty()) {
                        game.colors = mutableListOf()
                    }

                    if (color == null) {
                        reactions.say("Sorry! I didn't get that!" +
                                "Please tell me a color of ${ordinal(game.currentGamer!!)} player.")

                    } else if (game.colors.contains(color!!)) {
                        reactions.say("Looks like a $color color is already taken!" +
                                "Please choose another color.")

                    } else if (!supportedColors.contains(color!!)) {
                        reactions.say("Sorry, but I support only ${fast(supportedColors.joinToString())} $break500ms" +
                                "Please pick one of these.")

                    } else {
                        game.colors.add(color!!)
                        game.currentGamer = game.currentGamer?.inc()

                        if (game.currentGamer!! > game.gamers!!) {
                            reactions.goBack() // all players are set their colors, return back to the GameSetupScenario

                        } else {
                            reactions.run {
                                sayRandom("$color! Great choice!", "$color! Great!", "$color color! Cool!", "I like $color too!")
                                go("../") // ask the next player
                            }
                        }
                    }
                }
            }
        }
    }
}