package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.dto.AudioReply
import com.justai.jaicf.channel.jaicp.dto.HangupReply
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.reactions.Reactions

val Reactions.telephony
    get() = this as? TelephonyReactions

class TelephonyReactions : JaicpReactions() {
    fun audio(url: String) {
        replies.add(AudioReply(url.toUrl()))
    }

    fun hangup(){
        replies.add(HangupReply())
    }
}