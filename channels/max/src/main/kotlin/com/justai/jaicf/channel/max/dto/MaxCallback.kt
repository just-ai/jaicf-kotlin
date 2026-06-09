package com.justai.jaicf.channel.max.dto

/** Payload of a `message_callback` update — emitted when a user taps an inline button. */
data class MaxCallback(
    val callbackId: String,
    val payload: String? = null,
    val user: MaxUser,
    val timestamp: Long? = null
)
