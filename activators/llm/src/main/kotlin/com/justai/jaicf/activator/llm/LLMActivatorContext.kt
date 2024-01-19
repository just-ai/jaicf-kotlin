package com.justai.jaicf.activator.llm

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.justai.jaicf.activator.event.EventActivatorContext
import com.justai.jaicf.activator.llm.client.LLMRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.helpers.context.sessionProperty

var BotContext.llmSettings: LLMSettings by sessionProperty { LLMSettings() }
var BotContext.llmChatHistory by sessionProperty { emptyList<LLMRequest.Message>() }

val llmJsonMapper = jacksonObjectMapper()

val ActivatorContext.llmMessage
    get() = this as? LLMMessageActivatorContext

val ActivatorContext.llmFunction
    get() = this as? LLMFunctionActivatorContext

sealed class LLMActivatorContext(
    override val event: String,
    private val botContext: BotContext,
) : EventActivatorContext(event) {
    internal var activate = false
    val history = botContext.llmChatHistory.toMutableList()

    fun setSystemMessage(content: String) = also {
        history.removeIf { it.role.isSystem }
        history.add(LLMRequest.Message.system(content))
        botContext.llmChatHistory = history
    }

    fun activate() {
        activate = true
    }

    fun activateWithSystemMessage(content: String) = setSystemMessage(content).also { activate() }
}

data class LLMMessageActivatorContext(
    private val botContext: BotContext,
    val content: String,
) : LLMActivatorContext(LLMEvent.MESSAGE, botContext) {
    inline fun <reified T> fromJson(): T =
        llmJsonMapper.readValue(content, T::class.java)
}

data class LLMFunctionActivatorContext(
    private val botContext: BotContext,
    val name: String,
    val arguments: String,
) : LLMActivatorContext(LLMEvent.FUNCTION_CALL, botContext) {
    lateinit var result: String

    fun isComplete() = ::result.isInitialized

    inline fun <reified T> parseArguments(): T =
        llmJsonMapper.readValue(arguments, T::class.java)
}