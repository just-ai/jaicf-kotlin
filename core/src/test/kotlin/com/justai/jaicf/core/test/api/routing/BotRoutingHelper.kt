package com.justai.jaicf.core.test.api.routing

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.api.routing.routing
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.model.scenario.Scenario

object BotRoutingHelper {
    val defaultRoutingSubscenario = Scenario {
        state("route") {
            activators {
                regex("route .*")
            }
            action {
                val target = request.input.replace("route ", "")
                reactions.say("Routing current request to $target")
                routing.route(target)
            }
        }

        state("changeBot") {
            activators {
                regex("changeBot .*")
            }
            action {
                val target = request.input.replace("changeBot  ", "")
                reactions.say("Changing bot to $target")
                routing.changeBot(target)
            }
        }

        state("routeBack") {
            activators {
                regex("routeBack")
            }
            action {
                reactions.say("routing back")
                routing.routeBack()
            }
        }

        state("changeBotBack") {
            activators {
                regex("changeBotBack")
            }
            action {
                reactions.say("changing bot back")
                routing.changeBotBack()
            }
        }
    }

    fun createEchoScenario(name: String) = Scenario {
        fallback { reactions.say("$name: Fallback") }
        append(defaultRoutingSubscenario)
    }

    fun createEngine(scenario: Scenario) = BotEngine(scenario, activators = arrayOf(RegexActivator))

    fun createEchoEngine(name: String) = BotEngine(createEchoScenario(name), activators = arrayOf(RegexActivator))
}