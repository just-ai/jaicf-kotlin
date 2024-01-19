package com.justai.jaicf.activator.llm

import com.justai.jaicf.activator.ActivationRuleMatcher
import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.BaseActivator
import com.justai.jaicf.activator.event.EventActivator
import com.justai.jaicf.activator.event.EventByNameActivationRule
import com.justai.jaicf.activator.llm.client.LLMClient
import com.justai.jaicf.activator.llm.client.LLMRequest
import com.justai.jaicf.activator.llm.client.openai.OpenAIClient
import com.justai.jaicf.activator.selection.ActivationSelector
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.api.hasQuery
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.scenario.ScenarioModel

class LLMActivator(
    private val model: ScenarioModel,
    private val setting: LLMSettings = LLMSettings(),
    private val client: LLMClient = OpenAIClient(setting),
) : BaseActivator(model), EventActivator {
    override val name = "llmActivator"

    override fun canHandle(request: BotRequest) = request.hasQuery()

    override fun provideRuleMatcher(botContext: BotContext, request: BotRequest): ActivationRuleMatcher {
        val messages = botContext.llmChatHistory.toMutableList()
        when {
            request is LLMFunctionBotRequest -> LLMRequest.Message.function(request.name, request.result)
            request.input.isNotEmpty() -> LLMRequest.Message.user(request.input)
            else -> null
        }?.also(messages::add)

        val transitions = model.generateTransitions(botContext)
        val functions = transitions.map { it.rule }.filterIsInstance(LLMFunctionActivationRule::class.java).map { it.function }
        val llmSettings = setting.assign(botContext.llmSettings)
        val chatRequest = llmSettings.createChatRequest(messages).let { it.copy(
            functions = (it.functions.orEmpty() + functions).ifEmpty { null }
        ) }.let {
            LLMEncoding.trim(it, llmSettings.model!!)
        }

        val resp = client.chatCompletion(chatRequest)
        val message = resp.choices.first().message
        botContext.llmChatHistory = chatRequest.messages + message.toRequestMessage()

        val context = if (message.functionCall != null) {
            LLMFunctionActivatorContext(botContext, message.functionCall.name, message.functionCall.arguments)
        } else {
            LLMMessageActivatorContext(botContext, message.content!!)
        }

        return ruleMatcher<EventByNameActivationRule> {
            context.takeIf { ctx -> it.event == ctx.event }
        }
    }

    override fun activate(
        botContext: BotContext,
        request: BotRequest,
        selector: ActivationSelector,
        activation: ActivatorContext
    ): Activation? {
        val act = activation as LLMActivatorContext
        botContext.llmChatHistory = act.history.toList()
        return when {
            act is LLMFunctionActivatorContext && act.isComplete() ->
                LLMFunctionBotRequest(request, act.name, act.result)
            act.activate -> QueryBotRequest(request.clientId, "")
            else -> null
        }?.let { activate(botContext, it, selector) }
    }

    class Factory(private val settings: LLMSettings) : ActivatorFactory {
        override fun create(model: ScenarioModel) =
            LLMActivator(model, settings)
    }

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) = LLMActivator(model)
    }
}