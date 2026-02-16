package com.justai.jaicf.channel.telegram.streaming

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.justai.jaicf.channel.telegram.helpers.findOptimalSplitPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import java.util.stream.Stream

/**
 * Processes streaming messages for Telegram with debouncing and automatic message splitting.
 * Can be extended to customize debounce timing or message splitting behavior.
 */
open class TelegramStreamProcessor(
    protected val api: Bot,
    protected val chatId: ChatId,
    protected val debounceMs: Long = DEFAULT_DEBOUNCE_MS,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    protected val parseMode: ParseMode? = null
) {
    protected val debounceScope = CoroutineScope(dispatcher + SupervisorJob())

    /**
     * Processes a stream of text chunks into Telegram messages.
     * Automatically handles message splitting when content exceeds Telegram's limits.
     * Uses debouncing to batch rapid updates efficiently.
     *
     * @param stream the stream of text chunks to process
     * @return the full accumulated text that was sent
     */
    open fun processStream(stream: Stream<String>): String {
        val fullText = StringBuilder()
        val messageStates = mutableListOf<MessageState>()
        var currentMessageState: MessageState? = null

        val debouncer = MessageDebouncer(debounceMs, debounceScope)

        stream.forEach { chunk ->
            fullText.append(chunk)

            if (currentMessageState == null) {
                currentMessageState = createMessageState(chunk, debouncer)
                    .also(messageStates::add)
            } else {
                currentMessageState?.text?.append(chunk)
            }

            currentMessageState?.let { state ->
                if (shouldSplitMessage(state)) {
                    val (messageToSend, remainder) = splitMessage(state)

                    state.text.clear().append(messageToSend)
                    scheduleMessageUpdate(state)
                    
                    // Flush the current message before creating a new one to avoid race conditions
                    runBlocking(debounceScope.coroutineContext) {
                        state.debouncer.flush()
                    }

                    currentMessageState = if (remainder.isNotEmpty()) {
                        createMessageState(remainder, debouncer)
                            .also(messageStates::add)
                    } else {
                        null
                    }
                } else {
                    scheduleMessageUpdate(state)
                }
            }
        }

        runBlocking(debounceScope.coroutineContext) {
            messageStates.forEach { it.debouncer.flush() }
        }

        return fullText.toString()
    }

    /**
     * Creates a new message state for tracking a message being streamed.
     * Can be overridden to customize message state creation.
     */
    protected open fun createMessageState(text: String, debouncer: MessageDebouncer): MessageState {
        return MessageState(
            text = StringBuilder(text),
            messageId = null,
            debouncer = debouncer
        )
    }

    /**
     * Determines if a message should be split based on its length.
     * Can be overridden to customize splitting logic.
     */
    protected open fun shouldSplitMessage(state: MessageState): Boolean {
        return state.text.length > SAFE_MESSAGE_LIMIT
    }

    /**
     * Splits a message into a part to send and a remainder.
     * Can be overridden to customize splitting behavior.
     */
    protected open fun splitMessage(state: MessageState): Pair<String, String> {
        val textToProcess = state.text.toString()
        val splitPoint = findOptimalSplitPoint(textToProcess, SAFE_MESSAGE_LIMIT)

        val messageToSend = textToProcess.take(splitPoint).trim()
        val remainder = textToProcess.substring(splitPoint).trim()

        return messageToSend to remainder
    }

    /**
     * Schedules a debounced update for a message state.
     * Can be overridden to customize update behavior.
     */
    protected open fun scheduleMessageUpdate(messageState: MessageState) {
        messageState.debouncer.debounce {
            val textToSend = messageState.text.toString()

            if (messageState.messageId == null) {
                val result = api.sendMessage(chatId, textToSend, parseMode).getOrNull()
                messageState.messageId = result?.messageId
            } else {
                api.editMessageText(chatId, messageState.messageId, text = textToSend, parseMode = parseMode)
            }
        }
    }

    companion object {
        const val TELEGRAM_MESSAGE_LIMIT = 4096
        const val SAFE_MESSAGE_LIMIT = 3900
        const val DEFAULT_DEBOUNCE_MS = 100L
    }
}
