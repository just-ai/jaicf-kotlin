package com.justai.jaicf.activator.llm.agent

import com.justai.jaicf.activator.llm.LLMProps
import com.justai.jaicf.activator.llm.MessagesTransform
import com.justai.jaicf.activator.llm.tool.llmTool
import com.justai.jaicf.activator.llm.transform
import com.justai.jaicf.activator.llm.withSystemMessage
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.helpers.context.tempProperty
import com.justai.jaicf.helpers.kotlin.ifTrue
import com.justai.jaicf.logging.GoReaction
import com.justai.jaicf.plugin.PathValue
import com.justai.jaicf.reactions.Reactions
import com.openai.models.chat.completions.ChatCompletionMessageParam

const val HANDOFF_TOOL_NAME = "handoff_to_agent"

private const val HANDOFF_TOOL_DESCRIPTION = "Handoff conversation to another agent"
private const val HANDOFF_PROMPT_PREFIX = "You are part of a multi-agent system, designed to make agent coordination and execution easy. Agents uses two primary abstractions: **Agents** and **Handoffs**. An agent encompasses instructions and tools and can hand off a conversation to another agent when appropriate. Handoffs are achieved by calling a `handoff` function. Transfers between agents are handled seamlessly in the background; do not mention or draw attention to these transfers in your conversation with the user."

class HandoffException(
    val agentName: String,
    val messages: List<ChatCompletionMessageParam>,
) : Exception()

data class Handoff(val agent: String)

internal var BotContext.handoffMessages
    by tempProperty<List<ChatCompletionMessageParam>> { emptyList() }

private val LLMAgent.handoffSystemMessage
    get() = HANDOFF_PROMPT_PREFIX +
        handoffs.joinToString("\n", "\nYou can handoff conversation to one of these agents:\n") {
            " - ${it.name}: ${it.role}"
        }

private val LLMAgent.handoffTool
    get() = llmTool<Handoff>(
        name = HANDOFF_TOOL_NAME,
        description = HANDOFF_TOOL_DESCRIPTION,
        parameters = {
            str(
                name = "agent",
                description = "Agent name to handoff conversation to",
                values = handoffs.map { it.name },
                required = true,
            )
        }
    ) {}

private val BotContext.handoffMessagesTransform: MessagesTransform
    get() = { messages ->
        messages.filter { it.isSystem() } + handoffMessages
    }

internal fun LLMProps.Builder.setupHandoffProps(agent: LLMAgent) {
    agent.handoffs.isNotEmpty().ifTrue {
        tool(agent.handoffTool)
        messages = messages.withSystemMessage("Handoff", agent.handoffSystemMessage)
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