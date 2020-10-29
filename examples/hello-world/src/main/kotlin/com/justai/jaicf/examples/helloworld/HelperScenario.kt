package com.justai.jaicf.examples.helloworld

import com.justai.jaicf.activator.catchall.catchAll
import com.justai.jaicf.activator.dialogflow.dialogflow
import com.justai.jaicf.channel.alexa.activator.alexaIntent
import com.justai.jaicf.hook.BotRequestHook
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.reactions.Reactions

object HelperScenario : Scenario() {

    init {
        state("helper") {
            state("ask4name") {
                activators {
                    catchAll()
                    intent("name")
                }

                action {
                    var name: String? = null

                    activator.dialogflow?.run {
                        name = slots["name"]?.stringValue
                    }

                    activator.alexaIntent?.run {
                        name = slots["firstName"]?.value
                    }

                    activator.catchAll?.run {
                        name = request.input
                    }

                    if (name.isNullOrBlank()) {
                        reactions.say("Sorry, I didn't get it. Could you repeat please?")
                    } else {
                        reactions.goBack(name)
                    }
                }
            }
        }
    }

}

fun Reactions.askForName(
    question: String,
    callbackState: String
) {
    say(question)
    changeState("/helper/ask4name", callbackState)
}