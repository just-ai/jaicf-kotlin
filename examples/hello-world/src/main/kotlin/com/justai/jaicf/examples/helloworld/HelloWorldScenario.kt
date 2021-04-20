package com.justai.jaicf.examples.helloworld

import com.justai.jaicf.activator.dialogflow.dialogflow
import com.justai.jaicf.activator.lex.lex
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.channel.aimybox.AimyboxEvent
import com.justai.jaicf.channel.aimybox.aimybox
import com.justai.jaicf.channel.alexa.alexa
import com.justai.jaicf.channel.alexa.intent
import com.justai.jaicf.channel.alexa.model.AlexaEvent
import com.justai.jaicf.channel.alexa.model.AlexaIntent
import com.justai.jaicf.channel.facebook.facebook
import com.justai.jaicf.channel.googleactions.dialogflow.DialogflowIntent
import com.justai.jaicf.channel.telegram.telegram
import com.justai.jaicf.reactions.buttons

val HelloWorldScenario = Scenario {

    append(context = "helper", HelperScenario)

    state("main") {

        activators {
            catchAll()
            event(AlexaEvent.LAUNCH)
            event(DialogflowIntent.WELCOME)
            event(AimyboxEvent.START)
        }

        action {
            var name = context.client["name"]

            if (name == null) {
                telegram {
                    name = request.message.chat.firstName ?: request.message.chat.username
                }
                facebook {
                    name = reactions.queryUserProfile()?.firstName()
                }
            }

            if (name == null) {
                reactions.askForName("Hello! What is your name?", "name")
            } else {
                reactions.run {
                    image("https://www.bluecross.org.uk/sites/default/files/d8/assets/images/118809lprLR.jpg")
                    sayRandom("Hello $name!", "Hi $name!", "Glad to hear you $name!")
                    buttons("Mew" to "/mew", "Wake up" to "wakeup")
                    aimybox?.endConversation()
                }
            }
        }

        state("inner", modal = true) {
            activators {
                catchAll()
            }

            action {
                reactions.apply {
                    sayRandom("What?", "Sorry, I didn't get that.")
                    say("Could you repeat please?")
                    changeState("/")
                }
            }
        }

        state("name") {
            action {
                context.client["name"] = context.result
                reactions.say("Nice to meet you ${context.result}")
            }
        }
    }

    state("stop") {
        activators {
            intent(AlexaIntent.STOP)
        }

        action(alexa.intent) {
            reactions.endSession("See you latter! Bye bye!")
        }
    }

    state("mew") {
        activators {
            regex("mew")
        }

        action {
            reactions.image("https://www.bluecross.org.uk/sites/default/files/d8/assets/images/118809lprLR.jpg")
        }
    }

    state("wakeup") {
        activators {
            intent("wake_up")
        }

        action(dialogflow) {
            val dt = activator.slots["date-time"]
            reactions.say("Okay! I'll wake you up ${dt?.stringValue}")
        }
    }

    state("coffee") {
        activators {
            intent("OrderCoffee")
        }

        action(lex) {
            val size = activator.slots["Size"]
            val type = activator.slots["Type"]
            reactions.say("You ordered a $size cup of $type coffee.")
        }
    }

    state("cancel") {
        activators {
            intent("cancel")
        }
        action {
            reactions.say("Okay, canceling.")
        }
    }
}
