package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.dto.*
import com.justai.jaicf.channel.jaicp.dto.AudioReply
import com.justai.jaicf.channel.jaicp.dto.HangupReply
import com.justai.jaicf.channel.jaicp.dto.SwitchReply
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.reactions.Reactions

val Reactions.telephony
    get() = this as? TelephonyReactions

class TelephonyReactions(private val request: JaicpBotRequest) : JaicpReactions() {
    private val dialer by lazy { JaicpDialerAPI() }


    override fun audio(url: String): AudioReaction {
        replies.add(AudioReply(url.toUrl()))
        return AudioReaction.create(url)
    }

    fun hangup() {
        replies.add(HangupReply())
    }

    fun redial(redialData: JaicpDialerAPI.RedialData) {
        dialer.redial(redialData)
    }

    fun redial(fromTime: Long, toTime: Long?, maxAttempts: Int?) {
        dialer.redial(JaicpDialerAPI.RedialData(fromTime, toTime, maxAttempts = maxAttempts))
    }

    fun setResult(callResult: String?, callResultPayload: String?) {
        dialer.result(JaicpDialerAPI.CallResultData(callResult, callResultPayload))
    }

    fun report(value: String?, order: Int?) {
        dialer.report(JaicpDialerAPI.CallReportData(value, order))
    }

    fun report(tagPayload: String?, tagColor: String?) {
        dialer.tag(JaicpDialerAPI.CallTagData(tagPayload, tagColor))
    }

    fun transferCall(phoneNumber: String, sipHeaders: Map<String, String> = emptyMap()) {
        replies.add(SwitchReply(phoneNumber, sipHeaders))
    }
}