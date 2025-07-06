package com.justai.jaicf.activator.llm.agent

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.justai.jaicf.activator.llm.LLMMessage
import com.justai.jaicf.activator.llm.LLMProps
import com.justai.jaicf.activator.llm.MessagesTransform
import com.justai.jaicf.activator.llm.tool.llmTool
import com.justai.jaicf.activator.llm.transform
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.helpers.context.tempProperty
import com.justai.jaicf.helpers.kotlin.ifTrue
import com.justai.jaicf.logging.GoReaction
import com.justai.jaicf.plugin.PathValue
import com.justai.jaicf.reactions.Reactions
import com.openai.models.chat.completions.ChatCompletionMessageParam
import kotlin.collections.toMutableList
import kotlin.jvm.optionals.getOrNull

const val HANDOFF_SYSTEM_NAME = "Handoff"
const val AGENT_PROMPT_PREFIX = "You are part of a multi-agent system, designed to make agent coordination and execution easy. Agents uses two primary abstractions: **Agents** and **Handoffs**. An agent encompasses instructions and tools and can hand off a conversation to another agent when appropriate. Handoffs are achieved by calling a `handoff` function. Transfers between agents are handled seamlessly in the background; do not mention or draw attention to these transfers in your conversation with the user."

class HandoffException(
    val agentName: String,
    val messages: List<ChatCompletionMessageParam>,
) : Exception()

@JsonClassDescription("Handoff conversation to another agent")
data class Handoff(
    val agentName: String,
)

internal val handoffTool = llmTool<Handoff> {}

internal var BotContext.handoffMessages
    by tempProperty<List<ChatCompletionMessageParam>> { emptyList() }

private val LLMAgent.handoffSystemMessage
    get() = LLMMessage.system {
        name(HANDOFF_SYSTEM_NAME)
        content(
            AGENT_PROMPT_PREFIX +
                handoffs.joinToString("\n", "\nYou can handoff conversation to these agents:\n") {
                    " - ${it.name}: ${it.role}"
                }
        )
    }

private val LLMAgent.handoffSystemMessageTransform: MessagesTransform
    get() = { messages ->
        messages.toMutableList().apply {
            none {
                it.isSystem() && it.asSystem().name().getOrNull() == HANDOFF_SYSTEM_NAME
            }.ifTrue {
                add(0, handoffSystemMessage)
            }
        }.toList()
    }

private val BotContext.handoffMessagesTransform: MessagesTransform
    get() = { messages ->
        messages.filter { it.isSystem() } + handoffMessages
    }

internal fun LLMProps.Builder.setupHandoffProps(agent: LLMAgent) {
    agent.handoffs.isNotEmpty().ifTrue {
        tool(handoffTool)
        messages = messages.transform(agent.handoffSystemMessageTransform)
    }
    context.handoffMessages.isNotEmpty().ifTrue {
        messages = messages.transform(context.handoffMessagesTransform)
    }
}

fun Reactions.handoff(
    @PathValue path: String,
    messages: List<ChatCompletionMessageParam>,
): GoReaction {
    botContext.handoffMessages = messages
    return go(path)
}