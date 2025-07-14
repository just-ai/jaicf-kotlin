package com.justai.jaicf.activator.llm.agent

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.*
import com.justai.jaicf.activator.llm.scenario.DefaultLLMOnlyIf
import com.justai.jaicf.activator.llm.scenario.llmState
import com.justai.jaicf.activator.llm.tool.LLMTool
import com.justai.jaicf.activator.llm.tool.LLMToolCallContext
import com.justai.jaicf.activator.llm.tool.LLMToolDefinition
import com.justai.jaicf.activator.llm.tool.LLMToolParameters
import com.justai.jaicf.activator.llm.tool.llmTool
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.builder.append
import com.justai.jaicf.builder.createModel
import com.justai.jaicf.context.manager.BotContextManager
import com.justai.jaicf.context.manager.InMemoryBotContextManager
import com.justai.jaicf.logging.ConversationLogger
import com.justai.jaicf.logging.Slf4jConversationLogger
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.scenario.Scenario
import com.openai.client.OpenAIClient
import com.openai.core.JsonValue
import kotlin.jvm.optionals.getOrNull


private fun stateName(agentName: String) = "/Agent/$agentName"

open class LLMAgent(
    override val name: String,
    override val props: LLMPropsBuilder = DefaultLLMProps,
    override val onlyIf: ActivationRule.OnlyIfContext.() -> Boolean = DefaultLLMOnlyIf,
    override val action: LLMActionBlock = DefaultLLMActionBlock,
) : LLMAgentScenario {
    var handoffs = listOf<LLMAgentWithRole>()
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

    override fun handoffs(vararg agents: LLMAgentWithRole) {
        handoffs += agents
    }

    override fun withoutMemory() = LLMAgent(
        name = name, onlyIf = onlyIf, action = action,
        props = {
            props()
            messages?.ifLLMMemory { messages = it.initial }
        },
    )

    override val model by lazy {
        createModel {
            llmState(
                state = stateName(name),
                onlyIf = onlyIf,
                props = {
                    props.invoke(this)
                    messages = messages ?: llmMemory(name)
                    setupHandoffProps(this@LLMAgent)
                }
            ) {
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

    val asBot by lazy { asBot() }

    fun asBot(
        defaultContextManager: BotContextManager = InMemoryBotContextManager,
        conversationLoggers: Array<ConversationLogger> = arrayOf(Slf4jConversationLogger()),
    ) = BotEngine(
        scenario = appendTo(Scenario {}),
        defaultContextManager = defaultContextManager,
        conversationLoggers = conversationLoggers,
    )

    val asTool by lazy { asTool() }

    override fun asTool(
        name: String,
        description: String?,
        parameters: LLMToolParameters
    ): LLMTool<JsonValue> = llmTool<JsonValue>(name, description, parameters) {
        val args = call.arguments.asObject().get()
        var input = args["input"]?.asString()?.getOrNull()
        if (args.size > 1 || input == null) {
            input = LLMTool.ArgumentsMapper.writeValueAsString(args)
        }
        process(input)
    }

    override fun <T> asTool(name: String, description: String?, parameters: Class<T>) =
        LLMTool(LLMToolDefinition.FromClass(parameters, name, description), {
            val input = LLMTool.ArgumentsMapper.writeValueAsString(call.arguments)
            process(input)
        })

    private fun LLMToolCallContext<*>.process(input: String) =
        QueryBotRequest(request.clientId, input).let {
            activator.api
                .createActivatorContext(botContext, it, activator, props)
                .awaitFinalContent()
        }
}