package com.justai.jaicf.channel.telegram.streaming

import com.justai.jaicf.helpers.logging.WithLogger
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.request.EditMessageText
import com.pengrad.telegrambot.request.SendMessage
import java.util.stream.Stream

/**
 * Handles streaming of messages for Telegram channel.
 * Manages message updates, throttling, and splitting of long messages.
 */
internal class TelegramStreamingHandler(
    private val api: TelegramBot,
    private val chatId: Long,
    private val config: StreamConfig,
    private val onMessageSent: (Message) -> Unit
) : WithLogger {

    companion object {
        private const val TELEGRAM_MESSAGE_LIMIT = 4096
        private const val SAFE_MESSAGE_LIMIT = 3900
    }

    // Accumulates all text for the final return value
    private val fullTextBuffer = StringBuilder()

    // Accumulates text for the current message only
    private val currentMessageBuffer = StringBuilder()

    private var currentMessageId: Int? = null
    private var lastUpdateTime = 0L

    /**
     * Process a stream of text chunks and update Telegram messages in real-time.
     */
    fun processStream(stream: Stream<String>): String {
        sendInitialMessage()

        stream.forEach { chunk ->
            appendChunk(chunk)
        }

        finalizeCurrentMessage()
        return fullTextBuffer.toString()
    }

    private fun sendInitialMessage() {
        val message = sendMessage(config.initialPlaceholder)
        currentMessageId = message.messageId()
        currentMessageBuffer.clear()
    }

    private fun appendChunk(chunk: String) {
        fullTextBuffer.append(chunk)

        // Check if adding this chunk would exceed the limit
        if (currentMessageBuffer.length + chunk.length > SAFE_MESSAGE_LIMIT) {
            // Finalize current message WITHOUT this chunk
            finalizeCurrentMessage()

            // Start new message
            val message = sendMessage(config.initialPlaceholder)
            currentMessageId = message.messageId()

            // Clear buffer and start with the current chunk
            currentMessageBuffer.clear()
            currentMessageBuffer.append(chunk)

            lastUpdateTime = System.currentTimeMillis()
        } else {
            // Normal append - chunk fits in current message
            currentMessageBuffer.append(chunk)
            val currentTime = System.currentTimeMillis()

            if (shouldUpdateMessage(currentTime)) {
                updateCurrentMessage()
                lastUpdateTime = currentTime
            }
        }
    }

    private fun shouldUpdateMessage(currentTime: Long): Boolean {
        return currentTime - lastUpdateTime >= config.updateIntervalMs
    }

    private fun finalizeCurrentMessage() {
        currentMessageId?.let { msgId ->
            val textToSend = currentMessageBuffer.toString()
            if (textToSend.isNotEmpty() && textToSend != config.initialPlaceholder) {
                updateMessage(msgId, textToSend)
            }
        }
    }

    private fun updateCurrentMessage() {
        currentMessageId?.let { msgId ->
            val text = currentMessageBuffer.toString()
            if (text.isNotEmpty() && text != config.initialPlaceholder) {
                updateMessage(msgId, text)
            }
        }
    }

    private fun sendMessage(text: String): Message {
        var req = SendMessage(chatId, text)
        config.parseMode?.let { req = req.parseMode(it) }

        val response = api.execute(req)
        val message = response.message()
            ?: throw IllegalStateException("Failed to send message for streaming")

        onMessageSent(message)
        return message
    }

    private fun updateMessage(messageId: Int, text: String) {
        try {
            var req = EditMessageText(chatId, messageId, text)
            config.parseMode?.let { req = req.parseMode(it) }

            api.execute(req)
        } catch (e: Exception) {
            handleUpdateError(messageId, text, e)
        }
    }

    private fun handleUpdateError(messageId: Int, text: String, error: Exception) {
        val errorMsg = error.message?.lowercase() ?: ""

        when {
            "message is not modified" in errorMsg -> {
                // Ignore - text hasn't changed, this is normal
            }
            "message to edit not found" in errorMsg -> {
                logger.warn("Message $messageId was deleted by user, stopping streaming updates")
            }
            "message can't be edited" in errorMsg -> {
                logger.warn("Message $messageId is too old to edit (>48 hours)")
            }
            "can't parse" in errorMsg -> {
                retryWithoutParseMode(messageId, text)
            }
            else -> {
                logger.error("Error updating streamed message $messageId: ${error.message}")
            }
        }
    }

    private fun retryWithoutParseMode(messageId: Int, text: String) {
        try {
            api.execute(EditMessageText(chatId, messageId, text))
        } catch (retryException: Exception) {
            logger.error("Failed to update message $messageId even without parse mode: ${retryException.message}")
        }
    }
}
