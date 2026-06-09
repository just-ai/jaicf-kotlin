package com.justai.jaicf.channel.max.dto

data class MaxUser(
    val userId: Long,
    val name: String? = null,
    val username: String? = null,
    val isBot: Boolean = false
)

data class MaxRecipient(
    val chatId: Long,
    val chatType: String? = null,
    val userId: Long? = null
)

data class MaxMessageBody(
    val mid: String? = null,
    val seq: Long? = null,
    val text: String? = null,
    val attachments: List<MaxAttachment>? = null
)

data class MaxMessage(
    val sender: MaxUser? = null,
    val recipient: MaxRecipient,
    val timestamp: Long? = null,
    val body: MaxMessageBody
)
