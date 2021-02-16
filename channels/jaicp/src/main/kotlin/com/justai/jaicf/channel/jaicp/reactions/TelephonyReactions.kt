package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.dto.*
import com.justai.jaicf.channel.jaicp.dto.bargein.*
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.Reactions
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant

val Reactions.telephony
    get() = this as? TelephonyReactions


class TelephonyReactions : JaicpReactions() {

    internal var bargeIn: SimpleBargeInData? = null

    internal var bargeInInterrupt: BargeInInterruptData? = null

    override fun audio(url: String): AudioReaction {
        replies.add(AudioReply(url.toUrl()))
        return AudioReaction.create(url)
    }

    fun say(text: String, interruptInContext: String = "."): SayReaction {
        replies.add(TextReply(text, bargeInReply = BargeInReplyData(interruptInContext)))
        return SayReaction.create(text)
    }

    fun say(text: String, interruptable: Boolean, interruptionContext: String = "."): SayReaction {
        if (interruptable) {
            return say(text, interruptionContext)
        }
        replies.add(TextReply(text))
        return SayReaction.create(text)
    }

    /**
     * Hangs up and ends the call.
     * */
    fun hangup() {
        replies.add(HangupReply())
    }

    /**
     * Barge-in is speech synthesis interruption. This reaction enables barge-in for current response.
     *
     * @param mode is [BargeInMode] to specify barge-in behaviour
     * @param trigger is [BargeInTrigger] to specify what triggers barge-in
     * @param noInterruptTimeMs minimal time in milliseconds for synthesis after which barge-in interruption can be triggered.
     * */
    fun bargeIn(mode: BargeInMode, trigger: BargeInTrigger, noInterruptTimeMs: Int) {
        bargeIn = SimpleBargeInData(mode, trigger, noInterruptTimeMs)
    }

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
     *            startDateTime = Instant.now().plusSeconds(3600),
     *            maxAttempts = 2
     *        )
     *    }
     * }
     * ```
     * @param startDateTime unix timestamp (UTC-0 epoch milliseconds) to start attempting to redial a client
     * @param finishDateTime unix timestamp (UTC-0 epoch milliseconds) to end attempting to redial a client
     * @param allowedDays list of [DayOfWeek] allowed days to call a client
     * @param localTimeFrom local time interval start attempting to redial. E.g. 16:20
     * @param localTimeTo local time interval end attempting to redial. E.g. 23:59
     * @param maxAttempts max number of attempts to call client
     * @param retryIntervalInMinutes interval between redial attempts
     * */
    fun redial(
        startDateTime: Instant? = null,
        finishDateTime: Instant? = null,
        allowedDays: List<DayOfWeek> = emptyList(),
        localTimeFrom: String? = null,
        localTimeTo: String? = null,
        maxAttempts: Int? = null,
        retryIntervalInMinutes: Int? = null
    ) = dialer.redial(
        startDateTime,
        finishDateTime,
        allowedDays,
        localTimeFrom,
        localTimeTo,
        maxAttempts,
        retryIntervalInMinutes
    )

    /**
     * Schedules a redial in outbound call campaign using [JaicpDialerData.RedialData].
     */
    fun redial(redialData: JaicpDialerData.RedialData) {
        dialer.redial(redialData)
    }

    /**
     * Schedules a redial in outbound call campaign
     *
     * @param startRedialAfter a [Duration] amount after which redial will start
     * @param finishRedialAfter a [Duration] after which redial will finish
     * @param allowedDays list of [DayOfWeek] allowed days to call a client
     * @param localTimeFrom local time interval start attempting to redial. E.g. 16:20
     * @param localTimeTo local time interval end attempting to redial. E.g. 23:59
     * @param maxAttempts max number of attempts to call client
     * @param retryIntervalInMinutes interval between redial attempts
     */
    fun redial(
        startRedialAfter: Duration,
        finishRedialAfter: Duration,
        allowedDays: List<DayOfWeek> = emptyList(),
        localTimeFrom: String? = null,
        localTimeTo: String? = null,
        maxAttempts: Int? = null,
        retryIntervalInMinutes: Int? = null
    ) {
        val currentTime = Instant.now()
        redial(
            currentTime.plus(startRedialAfter),
            currentTime.plus(finishRedialAfter),
            allowedDays,
            localTimeFrom,
            localTimeTo,
            maxAttempts,
            retryIntervalInMinutes
        )
    }

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
        dialer.report(header, JaicpDialerData.CallReportData(value, order))

    /**
     * Transfers call to operator or other person.
     *
     * @param phoneNumber another person's phone number
     * */
    fun transferCall(phoneNumber: String, sipHeaders: Map<String, String> = emptyMap()) {
        replies.add(TelephonySwitchReply(phoneNumber = phoneNumber, headers = sipHeaders))
    }

    /**
     * Transfers call to operator or other person.
     *
     * @param reply another person's phone number
     * */
    fun transferCall(reply: TelephonySwitchReply) {
        replies.add(reply)
    }

    fun allowInterrupt() {
        bargeInInterrupt = BargeInInterruptData(true)
    }
}
