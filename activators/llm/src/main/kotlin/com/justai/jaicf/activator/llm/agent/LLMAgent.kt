package com.justai.jaicf.activator.llm.agent

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.llm.*
import com.justai.jaicf.activator.llm.scenario.DefaultLLMOnlyIf
import com.justai.jaicf.activator.llm.scenario.llmState
import com.justai.jaicf.activator.llm.tool.*
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


private val DefaultAgentToolParams: LLMToolParameters = {
    str("input", "Request text", true)
}

fun agentStateName(agentName: String) = "/Agent/$agentName"
fun LLMAgent.withProps(builder: LLMPropsBuilder) = props.withProps(builder)

open class LLMAgent(
    open val name: String,
    open val props: LLMPropsBuilder = DefaultLLMProps,
    open val onlyIf: ActivationRule.OnlyIfContext.() -> Boolean = DefaultLLMOnlyIf,
    open val action: LLMActionBlock = DefaultLLMActionBlock,
) : Scenario {
    var handoffs = listOf<LLMAgentWithRole>()
        private set

    constructor(other: LLMAgent) : this(
        name = other.name,
        props = other.props,
        onlyIf = other.onlyIf,
        action = other.action,
    ) {
        this.handoffs = other.handoffs
    }

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

    fun handoffs(vararg agents: LLMAgentWithRole) {
        handoffs += agents
    }

    fun withoutMemory() = LLMAgent(
        name = name, onlyIf = onlyIf, action = action,
        props = withProps {
            messages?.ifLLMMemory { messages = it.initial }
        },
    )

    override val model by lazy {
        createModel {
            llmState(
                state = agentStateName(name),
                onlyIf = onlyIf,
                block = action,
                props = withProps {
                    messages = messages ?: llmMemory(name)
                    setupHandoffProps(this@LLMAgent)
                }
            )
        }
    }

    internal fun appendTo(scenario: Scenario, appended: Set<String> = emptySet()): Scenario {
        val state = agentStateName(name)
        return if (appended.contains(state)) {
            scenario
        } else {
            var next = scenario append this
            handoffs.forEach {
                next = it.appendTo(next, appended + state)
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

    fun asTool(
        name: String = this.name,
        description: String? = (this as? LLMAgentWithRole)?.role,
        parameters: LLMToolParameters = DefaultAgentToolParams,
    ): LLMTool<JsonValue> = llmTool<JsonValue>(name, description, parameters) {
        val args = call.arguments.asObject().get()
        var input = args["input"]?.asString()?.getOrNull()
        if (args.size > 1 || input == null) {
            input = LLMTool.ArgumentsMapper.writeValueAsString(args)
        }
        process(input)
    }

    fun <T> asTool(
        name: String = this.name,
        description: String? = (this as? LLMAgentWithRole)?.role,
        parameters: Class<T>,
    ) = LLMTool(LLMToolDefinition.FromClass(parameters, name, description), {
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