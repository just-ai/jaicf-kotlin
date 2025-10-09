package com.justai.jaicf.channel.telegram

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Aggregates consecutive text messages from the same user with a debounce delay.
 * When a user sends multiple messages in quick succession (e.g., long message split by Telegram),
 * this aggregator collects them and processes as a single combined message.
 *
 * @property waitTimeMs the delay in milliseconds to wait for additional messages before processing
 * @property scope the coroutine scope for managing aggregation jobs
 */
class UserMessageAggregator(
    private val waitTimeMs: Long = DEFAULT_WAIT_TIME_MS,
    private val scope: CoroutineScope
) {
    private val mutex = Mutex()
    private val pendingMessages = mutableMapOf<Long, PendingUserMessages>()

    /**
     * Adds a text message to the aggregation queue for a specific chat.
     * If this is the first message from this chat, starts a timer.
     * If messages are already pending, adds to the buffer and resets the timer.
     *
     * @param chatId the Telegram chat ID
     * @param messageText the text of the message to aggregate
     * @param onProcess callback to execute when aggregation is complete with the combined text
     */
    fun addMessage(
        chatId: Long,
        messageText: String,
        onProcess: suspend (String) -> Unit
    ) {
        scope.launch {
            mutex.withLock {
                val pending = pendingMessages.getOrPut(chatId) {
                    PendingUserMessages(mutableListOf(), onProcess)
                }

                pending.messages.add(messageText)
                pending.job?.cancel()

                pending.job = scope.launch {
                    delay(waitTimeMs)
                    processMessages(chatId)
                }
            }
        }
    }

    private suspend fun processMessages(chatId: Long) {
        val pending = mutex.withLock {
            pendingMessages.remove(chatId)
        } ?: return

        val combinedText = pending.messages.joinToString("\n")
        pending.onProcess(combinedText)
    }

    /**
     * Immediately processes all pending messages for all chats.
     * Useful for cleanup or shutdown scenarios.
     */
    suspend fun flushAll() {
        val chatsToFlush = mutex.withLock {
            pendingMessages.keys.toList()
        }

        chatsToFlush.forEach { chatId ->
            val pending = mutex.withLock {
                pendingMessages.remove(chatId)?.also { it.job?.cancel() }
            }

            pending?.let {
                val combinedText = it.messages.joinToString("\n")
                it.onProcess(combinedText)
            }
        }
    }

    private data class PendingUserMessages(
        val messages: MutableList<String>,
        val onProcess: suspend (String) -> Unit,
        var job: Job? = null
    )

    companion object {
        const val DEFAULT_WAIT_TIME_MS = 500L
    }
}
