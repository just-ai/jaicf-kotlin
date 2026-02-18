package com.justai.jaicf.activator.llm.memory.transformers

import com.justai.jaicf.activator.llm.memory.MessagesTransform
import com.justai.jaicf.activator.llm.memory.transform
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.Encoding
import com.knuddels.jtokkit.api.EncodingRegistry
import com.knuddels.jtokkit.api.ModelType
import com.openai.models.ChatModel
import com.openai.models.chat.completions.ChatCompletionContentPartImage
import com.openai.models.chat.completions.ChatCompletionMessageParam
import com.openai.models.chat.completions.ChatCompletionUserMessageParam
import kotlin.jvm.optionals.getOrNull

/**
 * Creates a message transformer that trims messages to stay within a token limit.
 *
 * System and developer messages are always preserved in the result, regardless of token limits.
 * They are extracted before trimming and prepended to the final message list. This ensures
 * critical context and instructions are never lost during conversation history management.
 *
 * @param maxTokens Maximum number of tokens allowed in the message history
 * @param modelType The model type to use for token counting (default: GPT_4O_MINI)
 * @param registry The encoding registry to use for token counting
 * @return A MessagesTransform that trims messages while preserving conversation structure
 */
fun withTokenLimit(
    maxTokens: Long,
    modelType: ModelType = ModelType.GPT_4O_MINI,
    registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry(),
): MessagesTransform {
    val encoding = registry.getEncodingForModel(modelType)
    return { messages ->
        trimMessages(messages, maxTokens, encoding, modelType.name)
    }
}

/**
 * Creates a message transformer that trims messages to stay within a token limit.
 *
 * System and developer messages are always preserved in the result, regardless of token limits.
 * They are extracted before trimming and prepended to the final message list. This ensures
 * critical context and instructions are never lost during conversation history management.
 *
 * @param maxTokens Maximum number of tokens allowed in the message history
 * @param encodingName The encoding name to use for token counting (e.g., "cl100k_base", "o200k_base")
 * @param registry The encoding registry to use for token counting
 * @return A MessagesTransform that trims messages while preserving conversation structure
 */
fun withTokenLimit(
    maxTokens: Long,
    encodingName: String,
    registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry(),
): MessagesTransform {
    val encoding = registry.getEncoding(encodingName).orElseThrow {
        IllegalArgumentException("Unknown encoding: $encodingName")
    }
    return { messages ->
        trimMessages(messages, maxTokens, encoding, modelName = null)
    }
}

/**
 * Extension function to apply token limit trimming to a message list.
 *
 * System and developer messages are always preserved in the result, regardless of token limits.
 * They are extracted before trimming and prepended to the final message list. This ensures
 * critical context and instructions are never lost during conversation history management.
 *
 * @param maxTokens Maximum number of tokens allowed
 * @param modelType The model type to use for token counting (default: GPT_4O_MINI)
 * @param registry The encoding registry to use for token counting
 * @return Transformed message list
 */
fun List<ChatCompletionMessageParam>?.withTokenLimit(
    maxTokens: Long,
    modelType: ModelType = ModelType.GPT_4O_MINI,
    registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry(),
): List<ChatCompletionMessageParam> {
    val encoding = registry.getEncodingForModel(modelType)
    return transform { messages -> trimMessages(messages, maxTokens, encoding, modelType.name) }
}

/**
 * Extension function to apply token limit trimming to a message list.
 *
 * System and developer messages are always preserved in the result, regardless of token limits.
 * They are extracted before trimming and prepended to the final message list. This ensures
 * critical context and instructions are never lost during conversation history management.
 *
 * @param maxTokens Maximum number of tokens allowed
 * @param encodingName The encoding name to use for token counting (e.g., "cl100k_base", "o200k_base")
 * @param registry The encoding registry to use for token counting
 * @return Transformed message list
 */
fun List<ChatCompletionMessageParam>?.withTokenLimit(
    maxTokens: Long,
    encodingName: String,
    registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry(),
): List<ChatCompletionMessageParam> {
    val encoding = registry.getEncoding(encodingName).orElseThrow {
        IllegalArgumentException("Unknown encoding: $encodingName")
    }
    return transform { messages -> trimMessages(messages, maxTokens, encoding, modelName = null) }
}

/**
 * Extension function to apply token limit trimming to a message list using model name.
 *
 * Attempts to resolve the model name to a known ModelType. If the model is not recognized,
 * falls back to GPT_4O_MINI encoding.
 *
 * System and developer messages are always preserved in the result, regardless of token limits.
 * They are extracted before trimming and prepended to the final message list. This ensures
 * critical context and instructions are never lost during conversation history management.
 *
 * @param maxTokens Maximum number of tokens allowed
 * @param model The model to use for token counting (e.g., "gpt-4o", "gpt-3.5-turbo", "claude-3-5-sonnet")
 * @param registry The encoding registry to use for token counting
 * @return Transformed message list
 */
fun List<ChatCompletionMessageParam>?.withTokenLimit(
    maxTokens: Long,
    model: ChatModel,
    registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry(),
): List<ChatCompletionMessageParam> {
    val modelName = model.value().name
    val modelType = ModelType.fromName(modelName).getOrNull() ?: ModelType.GPT_4O_MINI
    val encoding = registry.getEncodingForModel(modelType)
    return transform { messages -> trimMessages(messages, maxTokens, encoding, modelName) }
}

/**
 * Trims messages to fit within token limit while preserving:
 * - System and developer messages (never trimmed) - these are separated from conversation messages,
 *   their tokens are counted and reserved from the maxTokens budget, and they are always included
 *   at the start of the result regardless of whether they fit within the limit
 * - User-assistant role sequence
 * - Tool call-result pairs (both or neither)
 */
private fun trimMessages(
    messages: List<ChatCompletionMessageParam>,
    maxTokens: Long,
    encoding: Encoding,
    modelName: String?,
): List<ChatCompletionMessageParam> {
    if (messages.isEmpty()) return messages

    // Separate system/developer messages from conversational messages
    val systemMessages = messages.filter { it.isSystem() || it.isDeveloper() }
    val conversationMessages = messages.filter { !it.isSystem() && !it.isDeveloper() }

    // Calculate tokens for system messages (these are never trimmed)
    val systemTokens = systemMessages.sumOf { it.countTokens(encoding, modelName) }
    val availableTokens = maxTokens - systemTokens

    if (availableTokens <= 0) {
        // If system messages exceed limit, just return them
        return systemMessages
    }

    // Trim conversation messages to fit within available tokens
    val trimmedConversation = trimConversation(conversationMessages, availableTokens, encoding, modelName)

    // Combine system messages with trimmed conversation
    return systemMessages + trimmedConversation
}

/**
 * Trims conversation messages while maintaining role sequence and tool call pairs.
 */
private fun trimConversation(
    messages: List<ChatCompletionMessageParam>,
    maxTokens: Long,
    encoding: Encoding,
    modelName: String?,
): List<ChatCompletionMessageParam> {
    if (messages.isEmpty()) return messages

    var totalTokens = 0
    val result = mutableListOf<ChatCompletionMessageParam>()

    // Process messages from newest to oldest
    var i = messages.size - 1
    while (i >= 0 && totalTokens < maxTokens) {
        val msg = messages[i]
        val tokens = msg.countTokens(encoding, modelName)

        // Handle tool result messages - must be paired with preceding assistant message containing tool call
        if (msg.isTool()) {

            // Find the corresponding assistant message with tool call
            var assistantIdx = i - 1
            while (assistantIdx >= 0) {
                val candidate = messages[assistantIdx]
                if (candidate.isAssistant()) {
                    val toolCalls = candidate.asAssistant().toolCalls().getOrNull()
                    if (!toolCalls.isNullOrEmpty()) {
                        // Check if this tool call matches our tool result
                        val toolResultId = msg.asTool().toolCallId()
                        if (toolCalls.any { it.id() == toolResultId }) {
                            val assistantTokens = candidate.countTokens(encoding, modelName)
                            val pairTokens = tokens + assistantTokens

                            if (totalTokens + pairTokens <= maxTokens) {
                                // Add tool result
                                result.add(0, msg)
                                totalTokens += tokens

                                // Add all messages between assistant and tool result
                                for (j in (assistantIdx + 1) until i) {
                                    if (messages[j] !in result) {
                                        val intermediateTokens = messages[j].countTokens(encoding, modelName)
                                        result.add(0, messages[j])
                                        totalTokens += intermediateTokens
                                    }
                                }

                                // Add assistant message
                                result.add(0, candidate)
                                totalTokens += assistantTokens

                                // Move index to before assistant message
                                i = assistantIdx - 1
                            } else {
                                // Can't fit the pair, stop here
                                i = -1
                            }
                            break
                        }
                    }
                }
                assistantIdx--
            }

            if (assistantIdx < 0) {
                // No matching assistant message found, skip this tool result
                i--
            }
            continue
        }

        // Regular message (user, assistant without tool results pending)
        if (totalTokens + tokens <= maxTokens) {
            result.add(0, msg)
            totalTokens += tokens
            i--
        } else {
            // Would exceed limit, stop here
            break
        }
    }

    // Ensure we maintain proper user-assistant sequence
    return ensureRoleSequence(result)
}

/**
 * Ensures messages maintain proper user-assistant alternation.
 * Removes any orphaned assistant messages at the start.
 */
private fun ensureRoleSequence(messages: List<ChatCompletionMessageParam>): List<ChatCompletionMessageParam> {
    if (messages.isEmpty()) return messages

    // Find first user message
    val firstUserIdx = messages.indexOfFirst { it.isUser() }
    if (firstUserIdx == -1) {
        // No user messages, return empty (assistants/tools without user context don't make sense)
        return emptyList()
    }

    // Remove any messages before first user message
    return messages.subList(firstUserIdx, messages.size)
}

/**
 * Counts tokens for a user message content, handling both plain text and
 * array content parts (text + image URLs).
 *
 * For image URLs, fetches image dimensions to compute accurate token costs:
 * - Anthropic (claude-*): (width × height) / 750
 * - OpenAI / others: tiled cost based on OpenAI vision pricing
 *
 * Falls back to fixed estimates if dimensions cannot be fetched.
 */
private fun countUserContentTokens(
    content: ChatCompletionUserMessageParam.Content,
    encoding: Encoding,
    modelName: String?,
): Int {
    return when {
        content.isText() -> encoding.countTokens(content.asText())
        content.isArrayOfContentParts() -> content.asArrayOfContentParts().sumOf { part ->
            when {
                part.isText() -> encoding.countTokens(part.asText().text())
                part.isImageUrl() -> (part as ChatCompletionContentPartImage).countTokens(modelName)
                else -> 0
            }
        }
        else -> encoding.countTokens(content.toString())
    }
}

/**
 * Counts tokens in a message using the provided encoding.
 */
fun ChatCompletionMessageParam.countTokens(
    encoding: Encoding,
    modelName: String?,
): Int {
    return when {
        isSystem() -> {
            val system = asSystem()
            val contentTokens = encoding.countTokens(system.content().toString())
            val nameTokens = system.name().getOrNull()?.let { encoding.countTokens(it) } ?: 0
            contentTokens + nameTokens + 3 // 3 tokens for message structure
        }
        isDeveloper() -> {
            val developer = asDeveloper()
            val contentTokens = encoding.countTokens(developer.content().toString())
            contentTokens + 3 // 3 tokens for message structure
        }
        isUser() -> {
            val user = asUser()
            val contentTokens = countUserContentTokens(user.content(), encoding, modelName)
            val nameTokens = user.name().getOrNull()?.let { encoding.countTokens(it) } ?: 0
            contentTokens + nameTokens + 4 // 4 tokens for message structure
        }
        isAssistant() -> {
            val assistant = asAssistant()
            var tokens = 3 // Base tokens for message structure

            // Count content tokens
            assistant.content().getOrNull()?.let { content ->
                tokens += encoding.countTokens(content.toString())
            }

            // Count tool call tokens
            assistant.toolCalls().getOrNull()?.forEach { toolCall ->
                tokens += encoding.countTokens(toolCall.function().name())
                tokens += encoding.countTokens(toolCall.function().arguments())
                tokens += 3 // Structure tokens per tool call
            }

            // Count name tokens
            assistant.name().getOrNull()?.let { tokens += encoding.countTokens(it) }

            tokens
        }
        isTool() -> {
            val tool = asTool()
            val contentTokens = encoding.countTokens(tool.content().toString())
            val idTokens = encoding.countTokens(tool.toolCallId())
            contentTokens + idTokens + 3 // 3 tokens for message structure
        }
        else -> 0
    }
}
