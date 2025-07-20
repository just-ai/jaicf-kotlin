package com.justai.jaicf.examples.llm

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.action.llmAction
import com.justai.jaicf.activator.regex.RegexActivator
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.examples.llm.channel.ConsoleChannel
import com.justai.jaicf.examples.llm.props.llmProps

/**
 * Simple scenario that uses llmAction to handle specific user requests (starting from ! symbol).
 * "catchAll" state processes all other requests instead of LLM.
 * llmAction processes the origin user request through an LLM API and then invokes action block with LLMActivatorContext.
 * In this case, you may omit LLMActivator in BotEngine config because llmAction uses LLM API under the hood and don't activate any states of scenario.
 * Also, llmAction makes it possible to call `reactions.go()` from other states of the scenario to the state with llmAction because it processes origin request.
 *
 * IMPORTANT! Set up your OPENAI_API_KEY and OPENAI_BASE_URL env before running
 */
private val scenario = Scenario {
    state("catchAll") {
        activators {
            catchAll()
        }
        action {
            reactions.say("TYPE WITH ! PREFIX TO ASK ASSISTANT")
        }
    }

    state("assistant") {
        activators {
            regex("^!.*")
        }

        llmAction(llmProps)
    }
}

fun main() {
    val bot = BotEngine(
        scenario = scenario,
        activators = arrayOf(RegexActivator)
    )
    ConsoleChannel(bot).run("start")
}