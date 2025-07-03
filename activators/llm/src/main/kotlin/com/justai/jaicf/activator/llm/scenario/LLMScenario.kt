package com.justai.jaicf.activator.llm.scenario

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.DefaultLLMActionBlock
import com.justai.jaicf.activator.llm.DefaultLLMProps
import com.justai.jaicf.activator.llm.LLMActionBlock
import com.justai.jaicf.activator.llm.LLMActivator
import com.justai.jaicf.activator.llm.LLMPropsBuilder
import com.justai.jaicf.activator.llm.LLMTool
import com.justai.jaicf.activator.llm.llmMemory
import com.justai.jaicf.activator.llm.withSystemMessage
import com.justai.jaicf.builder.createModel
import com.justai.jaicf.model.scenario.Scenario
import com.openai.client.OpenAIClient


class LLMChatScenario(
    name: String,
    props: LLMPropsBuilder = DefaultLLMProps,
    action: LLMActionBlock = DefaultLLMActionBlock,
) : Scenario {

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
        action: LLMActionBlock = DefaultLLMActionBlock,
    ) : this(
        name = name,
        action = action,
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
    })

    override val model = createModel {
        llmChat(name, props, action)
    }

    val asBot by lazy {
        BotEngine(
            activators = arrayOf(LLMActivator),
            scenario = this,
        )
    }
}
