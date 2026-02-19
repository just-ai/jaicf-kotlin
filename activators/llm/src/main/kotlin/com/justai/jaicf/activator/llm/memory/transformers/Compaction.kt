package com.justai.jaicf.activator.llm.memory.transformers

import com.justai.jaicf.activator.llm.DefaultLLMModel
import com.justai.jaicf.activator.llm.LLMMessage
import com.justai.jaicf.activator.llm.action.LLMActionAPI
import com.justai.jaicf.activator.llm.memory.MessagesTransform
import com.justai.jaicf.activator.llm.memory.transform
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.Encoding
import com.knuddels.jtokkit.api.EncodingRegistry
import com.knuddels.jtokkit.api.ModelType
import com.openai.client.OpenAIClient
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.openai.models.chat.completions.ChatCompletionMessageParam

const val CONVERSATION_SUMMARY_MESSAGE = "conversation_summary"

private const val DEFAULT_COMPACTION_PROMPT =
    "Summarize the conversation history below into a concise paragraph that preserves all key facts, " +
        "decisions, and context needed to continue the conversation. " +
        "Do not include greetings or filler. Be brief but complete. " +
        "Keep the conversation language."

/**
 * Creates a [MessagesTransform] that compacts conversation history into a summarized assistant message
 * once the total token count of non-system messages exceeds [maxTokens].
 *
 * When compaction is triggered, all non-system/developer messages are sent to the LLM with the given
 * [compactionPrompt] and replaced by a single assistant message containing the summary. System and
 * developer messages are always preserved.
 *
 * @param maxTokens Token threshold for non-system messages that triggers compaction
 * @param countingModel The model type to use for token counting (default: GPT_4O_MINI)
 * @param compactionModel The model name to use for summarization (default: gpt-4o-mini)
 * @param client Optional [OpenAIClient] to use; falls back to the default client from env
 * @param compactionPrompt System prompt used to instruct the LLM to summarize the history
 * @param registry The encoding registry to use for token counting
 * @return A [MessagesTransform] that compacts messages when the token threshold is exceeded
 */
fun withCompaction(
    maxTokens: Long,
    countingModel: ModelType = ModelType.GPT_4O_MINI,
    compactionModel: String = DefaultLLMModel.asString(),
    client: OpenAIClient = LLMActionAPI.get.defaultClient,
    compactionPrompt: String = DEFAULT_COMPACTION_PROMPT,
    registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry(),
): MessagesTransform {
    val encoding = registry.getEncodingForModel(countingModel)
    return { messages ->
        compactMessages(messages, maxTokens, encoding, countingModel.name, compactionModel, client, compactionPrompt)
    }
}

/**
 * Extension function to apply compaction to a message list.
 *
 * System and developer messages are always preserved in the result, regardless of compaction.
 * When compaction is triggered, conversation messages are summarized into a single assistant message.
 * If this list is an [LLMMemory], the compacted result is automatically persisted to the bot session.
 *
 * @param maxTokens Token threshold for non-system messages that triggers compaction
 * @param countingModel The model type to use for token counting (default: GPT_4O_MINI)
 * @param compactionModel The model name to use for summarization (default: gpt-4o-mini)
 * @param client Optional [OpenAIClient] to use; falls back to the default client from env
 * @param compactionPrompt System prompt used to instruct the LLM to summarize the history
 * @param registry The encoding registry to use for token counting
 * @return Transformed message list with compacted history if the threshold was exceeded
 */
fun List<ChatCompletionMessageParam>?.withCompaction(
    maxTokens: Long,
    countingModel: ModelType = ModelType.GPT_4O_MINI,
    compactionModel: String = DefaultLLMModel.asString(),
    client: OpenAIClient = LLMActionAPI.get.defaultClient,
    compactionPrompt: String = DEFAULT_COMPACTION_PROMPT,
    registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry(),
): List<ChatCompletionMessageParam> {
    val encoding = registry.getEncodingForModel(countingModel)
    return transform { messages -> compactMessages(messages, maxTokens, encoding, countingModel.name, compactionModel, client, compactionPrompt) }
}

private fun compactMessages(
    messages: List<ChatCompletionMessageParam>,
    maxTokens: Long,
    encoding: Encoding,
    modelName: String,
    compactionModel: String,
    client: OpenAIClient,
    compactionPrompt: String,
): List<ChatCompletionMessageParam> {
    val systemMessages = messages.filter { it.isSystem() || it.isDeveloper() }
    val conversationMessages = messages.filter { !it.isSystem() && !it.isDeveloper() }

    val totalTokens = conversationMessages.sumOf { it.countTokens(encoding, modelName) }
    if (totalTokens <= maxTokens) {
        return messages
    }

    // Build a plain-text dialogue string from user/assistant messages,
    // dropping tool results and tool-call-only assistant messages
    val dialogue = conversationMessages.mapNotNull { msg ->
        when {
            msg.isUser() -> {
                val content = msg.asUser().content()
                val text = when {
                    content.isText() -> content.asText()
                    content.isArrayOfContentParts() -> content.asArrayOfContentParts()
                        .filter { it.isText() }
                        .joinToString(" ") { it.asText().text() }
                    else -> null
                }
                text?.let { "user> $it" }
            }
            msg.isAssistant() -> {
                val content = msg.asAssistant().content().orElse(null) ?: return@mapNotNull null
                val text = when {
                    content.isText() -> content.asText()
                    else -> null
                }
                text?.let { "assistant> $it" }
            }
            else -> null
        }
    }.joinToString("\n")

    val params = ChatCompletionCreateParams.builder()
        .model(compactionModel)
        .messages(listOf(
            LLMMessage.user("$compactionPrompt\n\n$dialogue"),
        ))
        .build()

    val summary = client.chat().completions().create(params)
        .choices()
        .firstOrNull()
        ?.message()
        ?.content()
        ?.orElse(null)
        ?: return messages

    return systemMessages +
        LLMMessage.user("Summarize our conversation") +
        LLMMessage.assistant {
            name(CONVERSATION_SUMMARY_MESSAGE)
            content(summary)
        }
}
