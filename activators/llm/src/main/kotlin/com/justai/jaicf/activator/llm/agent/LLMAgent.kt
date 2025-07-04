package com.justai.jaicf.activator.llm.agent

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.DefaultLLMActionBlock
import com.justai.jaicf.activator.llm.DefaultLLMProps
import com.justai.jaicf.activator.llm.LLMActionBlock
import com.justai.jaicf.activator.llm.LLMPropsBuilder
import com.justai.jaicf.activator.llm.LLMTool
import com.justai.jaicf.activator.llm.llmAction
import com.justai.jaicf.activator.llm.llmMemory
import com.justai.jaicf.activator.llm.scenario.DefaultLLMOnlyIf
import com.justai.jaicf.activator.llm.withSystemMessage
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.builder.append
import com.justai.jaicf.builder.createModel
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.scenario.Scenario
import com.openai.client.OpenAIClient


private fun stateName(agentName: String) = "/Agent/$agentName"

interface LLMAgentScenario : Scenario {
    val name: String
    val props: LLMPropsBuilder
    val onlyIf: ActivationRule.OnlyIfContext.() -> Boolean
    val action: LLMActionBlock

    fun handoff(vararg agents: AgentWithRole)
    fun withRole(role: String) = AgentWithRole(this, role)
}

class AgentWithRole(
    internal val agent: LLMAgentScenario,
    val role: String,
) : LLMAgentScenario by agent

open class LLMAgent(
    override val name: String,
    override val props: LLMPropsBuilder = DefaultLLMProps,
    override val onlyIf: ActivationRule.OnlyIfContext.() -> Boolean = DefaultLLMOnlyIf,
    override val action: LLMActionBlock = DefaultLLMActionBlock,
) : LLMAgentScenario {
    var handoffs = listOf<AgentWithRole>()
        private set

    constructor(
        name: String,
        model: String? = null,
        temperature: Double? = null,
        topP: Double? = null,
        maxTokens: Long? = null,
        frequencyPenalty: Double? = null,
        presencePenalty: Double? = null,
        responseFormat: Class<*>? = null,
        instructions: String? = null,
        tools: List<LLMTool<*>>? = null,
        client: OpenAIClient? = null,
        onlyIf: ActivationRule.OnlyIfContext.() -> Boolean = DefaultLLMOnlyIf,
        action: LLMActionBlock = DefaultLLMActionBlock,
    ) : this(
        name = name,
        action = action,
        onlyIf = onlyIf,
        props = {
            setModel(model)
            setTemperature(temperature)
            setTopP(topP)
            setMaxTokens(maxTokens)
            setFrequencyPenalty(frequencyPenalty)
            setPresencePenalty(presencePenalty)
            setResponseFormat(responseFormat)
            setClient(client)
            setTools(tools)
            setMessages(
                llmMemory(name, instructions?.let { withSystemMessage(it) })
            )
        }
    )

    override fun handoff(vararg agents: AgentWithRole) {
        handoffs += agents
    }

    override val model by lazy {
        createModel {
            state(stateName(name)) {
                activators {
                    catchAll().onlyIf(onlyIf)
                }

                llmAction({
                    props.invoke(this)
                    messages = messages ?: llmMemory(name)
                    setupHandoffProps(this@LLMAgent)
                }) {
                    try {
                        action(this)
                    } catch (e: HandoffException) {
                        val state = stateName(e.agentName)
                        scenario.states.keys.find { it.endsWith(state) }?.also { path ->
                            reactions.handoff(path, e.messages)
                        } ?: throw e
                    }
                }
            }
        }
    }

    private fun appendTo(scenario: Scenario, appended: Set<String> = emptySet()): Scenario {
        val state = stateName(name)
        return if (appended.contains(state)) {
            scenario
        } else {
            var next = scenario append this
            handoffs.forEach {
                if (it.agent is LLMAgent) {
                    next = it.agent.appendTo(next, appended + state)
                }
            }
            return next
        }
    }

    val asBot by lazy {
        BotEngine(appendTo(Scenario {}))
    }
}