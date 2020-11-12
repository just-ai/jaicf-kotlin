package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.dto.*
import com.justai.jaicf.channel.jaicp.dto.AudioReply
import com.justai.jaicf.channel.jaicp.dto.HangupReply
import com.justai.jaicf.channel.jaicp.dto.SwitchReply
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.reactions.Reactions
import java.time.Instant

val Reactions.telephony
    get() = this as? TelephonyReactions

class TelephonyReactions : JaicpReactions() {
    /**
     * Appends audio to the response.
     *
     * @param url of audio
     * */
    override fun audio(url: String): AudioReaction {
        replies.add(AudioReply(url.toUrl()))
        return AudioReaction.create(url)
    }

    /**
     * Hang up and ends call.
     * */
    fun hangup() {
        replies.add(HangupReply())
    }

    /**
     * Schedules a redial in outbound call campaign using [JaicpDialerAPI.RedialData].
     * */
    fun redial(redialData: JaicpDialerAPI.RedialData) = dialer.redial(redialData)

    /**
     * Schedules a redial in outbound call campaign.
     *
     * example usage:
     * ```
     * state("redial") {
     *    activators {
     *        regex("call me back")
     *    }
     *    action {
     *        reactions.say("Ok, I will call you in an hour.")
     *        reactions.telephony?.redial(
     *            fromTime = Instant.now().plusSeconds(3600),
     *            toTime = null,
     *            maxAttempts = 2
     *        )
     *    }
     * }
     * ```
     * @param fromTime unix timestamp (UTC-0) to start attempting to redial a client
     * @param toTime unix timestamp (UTC-0) to end attempting to redial a client
     * @param maxAttempts max number of attempts to call client
     * */
    fun redial(fromTime: Instant?, toTime: Instant?, maxAttempts: Int?) = dialer.redial(
        JaicpDialerAPI.RedialData(
            startDateTime = fromTime?.toEpochMilli(),
            finishDateTime = toTime?.toEpochMilli(),
            maxAttempts = maxAttempts
        )
    )

    /**
     * Sets result for call in outbound call campaign.
     *
     * @param callResult result of call
     * @param callResultPayload optional payload for call which will be stored in .xsls report.
     * */
    fun setResult(callResult: String?, callResultPayload: String? = null) =
        dialer.result(callResult, callResultPayload);

    /**
     * Reports custom property to be stored in .xsls report.
     *
     * Example usage:
     * ```
     * state("ReportAnyText") {
     *    activators {
     *        regex(.*)
     *    }
     *    action {
     *        reactions.telephony?.report("answer-for-some-question", request.input)
     *        reactions.say("Ok! I will remember what you have said")
     *    }
     * }
     * */
    fun report(header: String, value: String, order: Int? = null) =
        dialer.report(header, JaicpDialerAPI.CallReportData(value, order))

    /**
     * Transfers call to operator or other person.
     *
     * @param phoneNumber another person's phone number
     * */
    fun transferCall(phoneNumber: String, sipHeaders: Map<String, String> = emptyMap()) {
        replies.add(SwitchReply(phoneNumber, sipHeaders))
    }
}