package com.justai.jaicf.channel.max

import com.justai.jaicf.channel.max.api.MaxBotApi
import com.justai.jaicf.channel.max.api.MaxMediaType
import com.justai.jaicf.channel.max.dto.MaxAttachmentRequest
import com.justai.jaicf.channel.max.dto.MaxButton
import com.justai.jaicf.channel.max.dto.MaxCallback
import com.justai.jaicf.channel.max.dto.MaxMessage
import com.justai.jaicf.channel.max.dto.MaxMessageBody
import com.justai.jaicf.channel.max.dto.MaxRecipient
import com.justai.jaicf.channel.max.dto.MaxTextFormat
import com.justai.jaicf.channel.max.dto.MaxUser
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.ExecutionContext
import com.justai.jaicf.context.RequestContext
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MaxReactionsTest {
    private val api = mockk<MaxBotApi>(relaxed = true)
    private val request = MaxTextRequest(
        MaxMessage(MaxUser(101), MaxRecipient(chatId = 202), body = MaxMessageBody(text = "x"))
    )

    private fun reactions(): MaxReactions {
        val r = MaxReactions(api, request, liveChatProvider = null)
        val botContext = BotContext("101")
        r.botContext = botContext
        r.executionContext = ExecutionContext(
            requestContext = RequestContext.DEFAULT,
            activationContext = null,
            botContext = botContext,
            request = request
        )
        return r
    }

    @Test fun `say sends a text message to the request chat and returns SayReaction`() {
        val r = reactions().say("hello")
        verify { api.sendMessage(202, match { it.text == "hello" }) }
        assertEquals("hello", r.text)
    }

    @Test fun `say sends plain text with no format by default`() {
        reactions().say("hi")
        verify { api.sendMessage(202, match { it.text == "hi" && it.format == null }) }
    }

    @Test fun `say with format sets the wire format value`() {
        reactions().say("hi", MaxTextFormat.HTML)
        verify { api.sendMessage(202, match { it.format == "html" }) }
    }

    @Test fun `say with inline buttons sends text plus a callback keyboard`() {
        reactions().say("pick", listOf("Yes", "No"))
        verify { api.sendMessage(202, match { b ->
            b.text == "pick" &&
                b.attachments?.filterIsInstance<MaxAttachmentRequest.InlineKeyboard>()?.singleOrNull()
                    ?.payload?.buttons?.flatten()?.all { it is MaxButton.Callback } == true
        }) }
    }

    @Test fun `buttons sends an inline keyboard of callback buttons`() {
        reactions().buttons("Yes", "No")
        verify { api.sendMessage(202, match { b ->
            val kb = b.attachments?.filterIsInstance<MaxAttachmentRequest.InlineKeyboard>()?.singleOrNull()
            kb != null && kb.payload.buttons.flatten().all { it is MaxButton.Callback }
        }) }
    }

    @Test fun `requestContactButton sends a request_contact keyboard`() {
        reactions().requestContactButton("Share your phone")
        verify { api.sendMessage(202, match { b ->
            b.attachments?.filterIsInstance<MaxAttachmentRequest.InlineKeyboard>()
                ?.single()?.payload?.buttons?.flatten()?.any { it is MaxButton.RequestContact } == true
        }) }
    }

    @Test fun `audio sends media of type audio and returns AudioReaction`() {
        val r = reactions().audio("https://x/a.mp3")
        verify { api.sendMedia(202, MaxMediaType.AUDIO, "https://x/a.mp3", null) }
        assertEquals("https://x/a.mp3", r.audioUrl)
    }

    @Test fun `answerCallback acknowledges the callback that triggered the request`() {
        val cb = MaxCallback(callbackId = "cb1", payload = "p", user = MaxUser(101))
        val callbackRequest = MaxCallbackRequest(
            cb, MaxMessage(MaxUser(101), MaxRecipient(chatId = 202), body = MaxMessageBody())
        )
        MaxReactions(api, callbackRequest, liveChatProvider = null).answerCallback("done")
        verify { api.answerCallback("cb1", notification = "done") }
    }

    @Test fun `sendDocument calls api sendMedia with type file`() {
        reactions().sendDocument("https://x/doc.pdf")
        verify { api.sendMedia(202, MaxMediaType.FILE, "https://x/doc.pdf", null) }
    }

    @Test fun `keyboard sends a mixed inline keyboard including a url button`() {
        reactions().keyboard("pick", listOf(MaxButton.Link("Site", "https://x"), MaxButton.Callback("Yes", "yes")))
        verify { api.sendMessage(202, match { b ->
            val row = b.attachments?.filterIsInstance<MaxAttachmentRequest.InlineKeyboard>()?.single()?.payload?.buttons?.flatten().orEmpty()
            row.any { it is MaxButton.Link && it.url == "https://x" } && row.any { it is MaxButton.Callback }
        }) }
    }
}
