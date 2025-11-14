package com.justai.jaicf.examples.telemetry

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.reactions.buttons
import com.justai.jaicf.reactions.toState
import com.justai.jaicf.telemetry.sendTelemetrySpan

/**
 * Simple scenario to demonstrate telemetry
 */
val TelemetryScenario = Scenario {
    
    state("main") {
        activators {
            regex("start")
            regex("hello")
            catchAll()
        }
        
        action {
            val name = "jaicf.custom.event"
            val attributes = mapOf(
                "jaicf.custom.type" to "Custom type",
                "jaicf.custom.client_id" to "Client Id",
                "jaicf.custom.input" to "Test Input"
            )
            sendTelemetrySpan(name, attributes)

            reactions.run {
                say("Hello! I'm a bot with telemetry enabled.")
                say("You can see traces of all bot operations in the logs.")
                buttons(
                    "Tell me a joke" toState "/joke",
                    "Calculate" toState "/calculate",
                    "Help" toState "/help"
                )
            }
        }
    }
    
    state("joke") {
        activators {
            regex("joke")
            regex("tell.*joke")
        }
        
        action {
            val jokes = listOf(
                "Why do programmers prefer dark mode? Because light attracts bugs!",
                "How many programmers does it take to change a light bulb? None, that's a hardware problem.",
                "Why do Java developers wear glasses? Because they can't C#!"
            )
            
            reactions.run {
                say(jokes.random())
                say("Want another one?")
                buttons(
                    "Yes, another joke!" toState "/joke",
                    "Back to main" toState "/main"
                )
            }
        }
    }
    
    state("calculate") {
        activators {
            regex("calculate")
            regex("calc")
            regex("math")
        }
        
        action {
            reactions.run {
                say("Let me calculate something...")
                // Simulate some processing
                Thread.sleep(100)
                
                val result = (1..100).sum()
                say("The sum of numbers from 1 to 100 is: $result")
                
                buttons(
                    "Calculate again" toState "/calculate",
                    "Back to main" toState "/main"
                )
            }
        }
    }
    
    state("help") {
        activators {
            regex("help")
        }
        
        action {
            reactions.run {
                say("This is a demo bot with OpenTelemetry integration.")
                say("Try these commands:")
                say("- 'joke' - Tell a joke")
                say("- 'calculate' - Do some calculations")
                say("- 'hello' - Go to main menu")
                
                buttons("Back to main" toState "/main")
            }
        }
    }
    
    state("error") {
        activators {
            regex("error")
            regex("fail")
        }
        
        action {
            throw RuntimeException("This is a test error for telemetry demo")
        }
    }
    
    fallback {
        reactions.run {
            say("Sorry, I didn't understand that.")
            say("Try 'hello' to start or 'help' for available commands.")
        }
    }
}

