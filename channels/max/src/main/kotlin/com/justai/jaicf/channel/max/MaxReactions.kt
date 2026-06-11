package com.justai.jaicf.channel.max

import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.channel.max.api.MaxBotApi
import com.justai.jaicf.channel.max.api.MaxMediaType
import com.justai.jaicf.channel.max.dto.MaxAttachmentRequest
import com.justai.jaicf.channel.max.dto.MaxButton
import com.justai.jaicf.channel.max.dto.MaxKeyboardPayload
import com.justai.jaicf.channel.max.dto.NewMessageBody
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions

val Reactions.max get() = this as? MaxReactions

/**
 * Reactions for the Max channel.
 */
class MaxReactions(
    val api: MaxBotApi,
    val request: MaxBotRequest,
    override val liveChatProvider: JaicpLiveChatProvider?
) : Reactions(), JaicpCompatibleAsyncReactions {

    private val chatId get() = request.chatId

    override fun say(text: String): SayReaction {
        api.sendMessage(chatId, NewMessageBody(text = text, format = "markdown"))
        return SayReaction.create(text)
    }

    fun say(text: String, inlineButtons: List<String>): SayReaction {
        api.sendMessage(
            chatId,
            NewMessageBody(
                text = text,
                format = "markdown",
                attachments = listOf(keyboard(inlineButtons.map { MaxButton.Callback(it, it) }))
            )
        )
        return SayReaction.create(text)
    }

    override fun buttons(vararg buttons: String): ButtonsReaction {
        api.sendMessage(
            chatId,
            NewMessageBody(attachments = listOf(keyboard(buttons.map { MaxButton.Callback(it, it) })))
        )
        return ButtonsReaction.create(buttons.asList())
    }

    fun requestContactButton(text: String): ButtonsReaction {
        api.sendMessage(
            chatId,
            NewMessageBody(attachments = listOf(keyboard(listOf(MaxButton.RequestContact(text)))))
        )
        return ButtonsReaction.create(listOf(text))
    }

    override fun image(url: String): ImageReaction {
        api.sendMedia(chatId, MaxMediaType.IMAGE, url, text = null)
        return ImageReaction.create(url)
    }

    override fun audio(url: String): AudioReaction {
        api.sendMedia(chatId, MaxMediaType.AUDIO, url, text = null)
        return AudioReaction.create(url)
    }

    fun sendVoice(url: String): AudioReaction = audio(url)

    fun sendDocument(url: String) {
        api.sendMedia(chatId, MaxMediaType.FILE, url, text = null)
    }

    // -------------------------------------------------------------------------
    // Keyboard builder helpers
    // -------------------------------------------------------------------------

    private fun keyboard(buttons: List<MaxButton>) =
        MaxAttachmentRequest.InlineKeyboard(MaxKeyboardPayload(buttons.map { listOf(it) }))
}
