package com.justai.jaicf.channel.jaicp.dto

import com.justai.jaicf.channel.jaicp.JSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
abstract class Reply(val type: String) {
    abstract fun serialized(): String
}

@Serializable
data class TextReply(
    val text: String,
    val markup: String? = null,
    val tts: String? = null,
    val state: String? = null
) : Reply("text") {
    override fun serialized() = JSON.encodeToString(serializer(), this)
}

@Serializable
data class Button(
    val text: String,
    val transition: String? = null
)

@Serializable
data class ButtonsReply(
    val buttons: List<Button>,
    val state: String? = null
) : Reply("buttons") {
    constructor(button: Button) : this(arrayListOf(button))

    override fun serialized() = JSON.encodeToString(serializer(), this)
}

@Serializable
data class ImageReply(
    val imageUrl: String,
    val text: String? = null,
    val state: String? = null
) : Reply("image") {
    override fun serialized() = JSON.encodeToString(serializer(), this)
}

@Serializable
data class AudioReply(
    val audioUrl: String,
    val state: String? = null
) : Reply("audio") {

    override fun serialized() = JSON.encodeToString(serializer(), this)
}

@Serializable
class HangupReply(val state: String? = null) : Reply("hangup") {
    override fun serialized() = JSON.encodeToString(serializer(), this)
}

/**
 * An object with parameters to customize transfer call in telephony channel.
 *
 * @param phoneNumber - a phone number to transfer call to.
 * @param headers - SIP headers sent with INVITE message.
 * @param transferBotId - JAICP bot identifier to transfer call to. This bot must be a telephony bot created in JAICP Console.
 * @param continueCall - whether to return call back to bot after transfer. True to return call back.
 * @param continueRecording - whether to continue recording after transfer. True to continue recording after transfer.
 *  */
@Serializable
class TelephonySwitchReply(
    val phoneNumber: String? = null,
    val headers: Map<String, String> = emptyMap(),
    @SerialName("transferChannel")
    val transferBotId: String? = null,
    val continueCall: Boolean? = null,
    val continueRecording: Boolean? = null
) : Reply("switch") {
    override fun serialized() = JSON.encodeToString(serializer(), this)
}

/**
 * An object with parameters to customize switch to livechat operator.
 *
 * @param firstMessage - a message to send to operator when livechat started. Default value is last user message.
 * @param closeChatPhrases - a list of commands called to return back to bot.
 * @param appendCloseChatButton - true to append buttons from [closeChatPhrases] list. False not to append. Default false.
 * @param ignoreOffline - to ignore whether any operators are online. Default false.
 * @param oneTimeMessage - to send text from [firstMessage] to operator but not to execute switch. Conversation with bot will continue. Default false.
 * @param destination - a group of operators to switch conversation to.
 * @param lastMessage - a message sent to operator when user ended conversation with any of [closeChatPhrases]
 * @param attributes - a json with data sent to operator when livechat started.
 * @param hiddenAttributes - a json with data sent to operator channel.
 * @param sendMessagesToOperator - true to send conversation history to operator.
 * @param sendMessageHistoryAmount - amount of last conversation history messages to send to operator.
 *
 * @see com.justai.jaicf.channel.jaicp.reactions.switchToLiveChat
 * */
@Serializable
data class LiveChatSwitchReply(
    val firstMessage: String? = null,
    val closeChatPhrases: List<String> = emptyList(),
    val appendCloseChatButton: Boolean = false,
    val ignoreOffline: Boolean = false,
    val oneTimeMessage: Boolean = false,
    val destination: String? = null,
    val lastMessage: String? = null,
    val attributes: JsonObject? = null,
    val hiddenAttributes: JsonObject? = null,
    val sendMessagesToOperator: Boolean = false,
    val sendMessageHistoryAmount: Int? = null
) : Reply("switch") {
    override fun serialized() = JSON.encodeToString(serializer(), this)
}