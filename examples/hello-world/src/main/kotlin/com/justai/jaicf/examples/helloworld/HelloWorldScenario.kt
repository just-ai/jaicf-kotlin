package com.justai.jaicf.examples.helloworld

import com.justai.jaicf.channel.aimybox.AimyboxEvent
import com.justai.jaicf.channel.aimybox.aimybox
import com.justai.jaicf.channel.alexa.*
import com.justai.jaicf.channel.alexa.activator.alexaIntent
import com.justai.jaicf.channel.alexa.model.AlexaEvent
import com.justai.jaicf.channel.alexa.model.AlexaIntent
import com.justai.jaicf.channel.facebook.api.facebook
import com.justai.jaicf.channel.facebook.facebook
import com.justai.jaicf.channel.slack.slack
import com.justai.jaicf.channel.telegram.telegram
import com.justai.jaicf.model.scenario.Scenario

object HelloWorldScenario: Scenario(
    dependencies = listOf(HelperScenario)
) {

    init {
        state("main") {

            activators {
                catchAll()
                event(AlexaEvent.LAUNCH)
                event(AimyboxEvent.START)
            }

            action {
                var name = context.client["name"]

                if (name == null) {
                    request.telegram?.run {
                        name = message.chat.firstName ?: message.chat.username
                    }
                    request.facebook?.run {
                        name = reactions.facebook?.queryUserProfile()?.firstName()
                    }
                    request.slack?.run {
                        name = reactions.slack?.getUserProfile(request.clientId)?.realName
                    }
                }

                if (name == null) {
                    reactions.askForName("Hello! What is your name?", "name")
                } else {
                    reactions.run {
                        image("https://www.bluecross.org.uk/sites/default/files/d8/assets/images/118809lprLR.jpg")
                        sayRandom("Hello $name!", "Hi $name!", "Glad to hear you $name!")
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

            action {
                withAlexa {
                    reactions.endSession("See you latter! Bye bye!")
                }
            }
        }

        state("mew") {
            activators {
                regex("/mew")
            }

            action {
                reactions.image("https://www.bluecross.org.uk/sites/default/files/d8/assets/images/118809lprLR.jpg")
                reactions.slack?.buttons("Show more" to "/mew")
            }
        }
    }
}