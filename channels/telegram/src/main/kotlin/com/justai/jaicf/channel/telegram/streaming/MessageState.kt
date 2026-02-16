package com.justai.jaicf.channel.telegram.streaming

/**
 * Represents the state of a message being streamed to Telegram.
 *
 * @property text the current accumulated text of the message
 * @property messageId the Telegram message ID once the message has been sent (null before first send)
 * @property debouncer the debouncer instance used to batch updates to this message
 */
data class MessageState(
    val text: StringBuilder,
    var messageId: Long?,
    val debouncer: MessageDebouncer
)
