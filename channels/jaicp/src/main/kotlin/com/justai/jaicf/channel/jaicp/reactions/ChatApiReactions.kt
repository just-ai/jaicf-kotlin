package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.dto.JaicpBotRequest
import com.justai.jaicf.reactions.Reactions

val Reactions.chatapi
    get() = this as? ChatApiReactions

class ChatApiReactions(private val request: JaicpBotRequest) : JaicpReactions()