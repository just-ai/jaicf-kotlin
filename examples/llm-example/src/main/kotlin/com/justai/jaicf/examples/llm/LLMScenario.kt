package com.justai.jaicf.examples.llm

import com.fasterxml.jackson.databind.node.ObjectNode
import com.justai.jaicf.activator.llm.client.LLMRequest
import com.justai.jaicf.activator.llm.llmChatHistory
import com.justai.jaicf.activator.llm.scenario.llmDialogue
import com.justai.jaicf.activator.llm.scenario.llmFunction
import com.justai.jaicf.builder.Scenario
import java.time.LocalDate
import java.time.format.DateTimeFormatter

val LLMScenario = Scenario {
    state("start") {
        activators {
            regex("/start.*")
        }

        action {
            "Hello! I'm here to dream about your next travel! What are your interests in vacation trip?"
                .also { message ->
                    context.llmChatHistory = listOf(LLMRequest.Message.assistant(message))
                    reactions.say(message)
                }
            reactions.go("/travel")
        }
    }

    llmFunction("faq", "Find answer in FAQ. Use to search answers about currencies, weather, factual info from Google.", {
        "question" to "Question text"
    }) {
        val args = activator.parseArguments<ObjectNode>()
        reactions.say("Searching answer for \"${args["question"].asText()}\"")
        activator.result = "Answer was not found"
    }

    llmDialogue("travel") {
        systemPrompt = {
            """
                Current date is ${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}.
                Act as a travel agent. Speak friendly and dreamy to help user to dream about their next travel. 
                Speak in user's language.
            """.trimIndent()
        }

        modelPrompt = {
            "Current date is ${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
        }

        field("interests", "string array of user interests in travel") {
            "ask what are user's interests in travel"
        }

        field("adults", "number of adults in travel") {
            "ask what count of adults and kids in this travel"
        }

        field("kids", "number of kids in travel")

        field("date", "date of travel start defined by user") {
            "ask about start date of this travel"
        }

        field("place", "destination place name defined by user for traveling. must be city, country or island name") {
            "suggest destinations suited for this user and ask to pick one of them"
        }

        finish { model ->
            reactions.say(model.toPrettyString())
        }
    }
}
