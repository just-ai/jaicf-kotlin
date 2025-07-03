package com.justai.jaicf.activator.llm.agent

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.DefaultLLMActionBlock
import com.justai.jaicf.activator.llm.DefaultLLMProps
import com.justai.jaicf.activator.llm.LLMActionBlock
import com.justai.jaicf.activator.llm.LLMPropsBuilder
import com.justai.jaicf.activator.llm.llmAction
import com.justai.jaicf.activator.llm.llmMemory
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.builder.append
import com.justai.jaicf.builder.createModel
import com.justai.jaicf.model.scenario.Scenario
import com.openai.models.chat.completions.ChatCompletionMessageParam


internal fun agentStateName(agentName: String) = "/Agent/$agentName"

class LLMAgent(
    val name: String,
    val role: String,
    props: LLMPropsBuilder = DefaultLLMProps,
    action: LLMActionBlock = DefaultLLMActionBlock,
) : Scenario {
    var handoffs = listOf<HandoffAgent>()
        private set

    override val model = createModel {
        state(agentStateName(name)) {
            activators {
                catchAll()
            }

            llmAction({
                props.invoke(this)
                messages = messages ?: llmMemory(name)
                setupHandoffProps(this@LLMAgent)
            }) {
                try {
                    action(this)
                } catch (e: HandoffException) {
                    val state = agentStateName(e.agentName)
                    scenario.states.keys.find { it.endsWith(state) }?.also { path ->
                        context.handoffMessages = e.messages
                        reactions.go(path)
                    } ?: throw e
                }
            }
        }
    }

    fun handoff(vararg agents: LLMAgent) =
        handoff(*agents.map { HandoffAgent(it) }.toTypedArray())

    fun handoff(vararg agents: HandoffAgent) = apply {
        handoffs += agents
    }

    fun asHandoff(role: String = this.role) = HandoffAgent(this, role)

    val asBot by lazy {
        BotEngine(Scenario {
            append(this@LLMAgent)
            handoffs.forEach { append(it.agent) }
        })
    }
}

class HandoffException(
    val agentName: String,
    val messages: List<ChatCompletionMessageParam>,
) : Exception()