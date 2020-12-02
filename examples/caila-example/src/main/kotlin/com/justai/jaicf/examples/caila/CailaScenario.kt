package com.justai.jaicf.examples.caila

import com.justai.jaicf.activator.caila.caila
import com.justai.jaicf.model.scenario.Scenario

object CailaScenario: Scenario() {
    init {

        state("smalltalk", noContext = true) {
            activators {
                anyIntent()
            }
            action {
                activator.caila?.topIntent?.answer?.let {
                    reactions.say(it)
                }
            }
        }

        state("pricing") {
            activators {
                intent("/FAQ/Pricing")
            }
            action {
                reactions.say("What product are you interested in?")
            }
        }

        state("faq") {
            activators {
                intent("/FAQ")
            }
            action {
                activator.caila?.topIntent?.answer?.let {
                    reactions.say(it)
                }
            }
        }

        fallback {
            reactions.say("Didn't get it, sorry")
        }
    }
}