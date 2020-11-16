package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.reactions.Reactions

val Reactions.chatapi
    get() = this as? ChatApiReactions

class ChatApiReactions : JaicpReactions()