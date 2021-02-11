package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions

val Reactions.chatapi
    get() = this as? ChatApiReactions

class ChatApiReactions : JaicpReactions(), JaicpCompatibleAsyncReactions