package com.justai.jaicf.channel.jaicp.reactions

import com.justai.jaicf.channel.jaicp.channels.TelephonyChannel
import com.justai.jaicf.channel.jaicp.dto.*
import com.justai.jaicf.channel.jaicp.dto.bargein.*
import com.justai.jaicf.helpers.http.toUrl
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.logging.currentState
import com.justai.jaicf.plugin.PathValue
import com.justai.jaicf.reactions.Reactions
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant

val Reactions.telephony
    get() = this as? TelephonyReactions


class TelephonyReactions(private val bargeInDefaultProps: BargeInProperties) : JaicpReactions() {

    internal var bargeIn: BargeInProperties? = null

    internal var bargeInInterrupt: BargeInResponse? = null

    companion object {
        private const val CURRENT_CONTEXT_PATH = "."
    }

    /**
     * Sends text to synthesis in telephony channel.
     * Allows to interrupt synthesis only by phrases which we can handle in scenario.
     *
     * @param text to synthesis speech from
     * @param bargeIn true to allow interruption and handle in in current dialog context
     * */
    fun say(text: String, bargeIn: Boolean) = when (bargeIn) {
        true -> say(text, CURRENT_CONTEXT_PATH)
        false -> say(text)
    }

    /**
     * Sends text to synthesis in telephony channel.
     * Allows to interrupt synthesis only by phrases which we can handle in scenario.
     *
     * @param text to synthesis speech from speech from
     * @param bargeInContext scenario context with states which should handle possible interruptions.
     * */
    fun say(text: String, @PathValue bargeInContext: String): SayReaction {
        ensureBargeInProps()
        replies.add(TextReply(text, state = currentState, bargeInReply = BargeInReplyData(bargeInContext, BargeInType.INTENT)))
        return SayReaction.create(text)
    }

    /**
     * Plays audio in a telephony call.
     *
     * @param url to play during call
     * */
    override fun audio(url: String): AudioReaction {
        replies.add(AudioReply(url.toUrl(), currentState))
        return AudioReaction.create(url)
    }

    /**
     * Plays audio in a telephony call.
     * Allows to barge in audio playback only by phrases which we can handle in scenario.
     *
     * @param url to play during call
     * @param bargeIn true to allow interruption and handle in in current dialog context
     * */
    fun audio(url: String, bargeIn: Boolean) = when (bargeIn) {
        true -> audio(url, CURRENT_CONTEXT_PATH)
        false -> audio(url)
    }

    /**
     * Plays audio in a telephony call.
     * Allows to barge in audio playback only by phrases which we can handle in scenario.
     *
     * @param url to play during call
     * @param bargeInContext scenario context with states which should handle possible interruptions.
     * */
    fun audio(url: String, @PathValue bargeInContext: String): AudioReaction {
        ensureBargeInProps()
        replies.add(AudioReply(url.toUrl(), bargeInReply = BargeInReplyData(bargeInContext, type = BargeInType.INTENT)))
        return AudioReaction.create(url)
    }

    /**
     * Hangs up and ends the call.
     * */
    fun hangup() {
        replies.add(HangupReply(currentState))
    }

    /**
     * Barge-in is speech synthesis interruption.
     *
     * If response uses barge-in by calling [say] or [audio] with arguments `bargeIn = true` or `bargeInContext = "..."`,
     *  calling this reaction will override [bargeInDefaultProps] from [TelephonyChannel.Factory].
     *
     * @param mode is [BargeInMode] to specify barge-in behaviour
     * @param trigger is [BargeInTrigger] to specify what triggers barge-in
     * @param noInterruptTimeMs minimal time in milliseconds for synthesis after which barge-in interruption can be triggered.
     * */
    fun bargeIn(mode: BargeInMode, trigger: BargeInTrigger, noInterruptTimeMs: Int) {
        bargeIn = BargeInProperties(mode, trigger, noInterruptTimeMs)
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
     * @param retryIntervalInMinutes interval between redial attempts. Must not be less than 1
     * */
    @Deprecated(
        "Parameters 'localTimeFrom' and 'localTimeTo' are deprecated, use 'redial' method that accepts allowedTime",
        ReplaceWith(
            "redial(startDateTime, finishDateTime, allowedDays, AllowedTime(listOf(LocalTimeInterval(localTimeFrom, localTimeTo))), maxAttempts, retryIntervalInMinutes)",
            "com.justai.jaicf.channel.jaicp.dto.*"
        )
    )
    fun redial(
        startDateTime: Instant? = null,
        finishDateTime: Instant? = null,
        allowedDays: List<DayOfWeek> = emptyList(),
        localTimeFrom: String? = null,
        localTimeTo: String? = null,
        maxAttempts: Int? = null,
        retryIntervalInMinutes: Int? = null,
    ) = dialer.redial(
        startDateTime = startDateTime,
        finishDateTime = finishDateTime,
        allowedDays = allowedDays,
        localTimeFrom = localTimeFrom,
        localTimeTo = localTimeTo,
        retryIntervalInMinutes = retryIntervalInMinutes,
        maxAttempts = maxAttempts
    )

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
     * @param allowedTime local time intervals by day of a week
     * @param maxAttempts max number of attempts to call client
     * @param retryIntervalInMinutes interval between redial attempts. Must not be less than 1
     * */
    fun redial(
        startDateTime: Instant? = null,
        finishDateTime: Instant? = null,
        allowedDays: List<DayOfWeek> = emptyList(),
        allowedTime: AllowedTime? = null,
        maxAttempts: Int? = null,
        retryIntervalInMinutes: Int? = null
    ) = dialer.redial(
        startDateTime,
        finishDateTime,
        allowedDays,
        allowedTime,
        maxAttempts,
        retryIntervalInMinutes
    )

    /**
     * Schedules a redial in outbound call campaign using [JaicpDialerAPI.RedialData].
     */
    fun redial(redialData: JaicpDialerAPI.RedialData) {
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
     * @param retryIntervalInMinutes interval between redial attempts. Must not be less than 1
     */
    @Deprecated(
        "Parameters 'localTimeFrom' and 'localTimeTo' are deprecated, use 'redial' method that accepts allowedTime",
        ReplaceWith(
            "redial(startRedialAfter, finishRedialAfter, allowedDays, AllowedTime(listOf(LocalTimeInterval(localTimeFrom, localTimeTo))), maxAttempts, retryIntervalInMinutes)",
            "com.justai.jaicf.channel.jaicp.dto.*"
        )
    )
    fun redial(
        startRedialAfter: Duration,
        finishRedialAfter: Duration,
        allowedDays: List<DayOfWeek> = emptyList(),
        localTimeFrom: String? = null,
        localTimeTo: String? = null,
        maxAttempts: Int? = null,
        retryIntervalInMinutes: Int? = null,
    ) {
        val currentTime = Instant.now()
        redial(
            startDateTime = currentTime.plus(startRedialAfter),
            finishDateTime = currentTime.plus(finishRedialAfter),
            allowedDays = allowedDays,
            localTimeFrom = localTimeFrom,
            localTimeTo = localTimeTo,
            maxAttempts = maxAttempts,
            retryIntervalInMinutes = retryIntervalInMinutes
        )
    }

    /**
     * Schedules a redial in outbound call campaign
     *
     * @param startRedialAfter a [Duration] amount after which redial will start
     * @param finishRedialAfter a [Duration] after which redial will finish
     * @param allowedDays list of [DayOfWeek] allowed days to call a client
     * @param allowedTime local time intervals by day of a week
     * @param maxAttempts max number of attempts to call client
     * @param retryIntervalInMinutes interval between redial attempts. Must not be less than 1
     */
    fun redial(
        startRedialAfter: Duration,
        finishRedialAfter: Duration,
        allowedDays: List<DayOfWeek> = emptyList(),
        allowedTime: AllowedTime? = null,
        maxAttempts: Int? = null,
        retryIntervalInMinutes: Int? = null
    ) {
        val currentTime = Instant.now()
        redial(
            currentTime.plus(startRedialAfter),
            currentTime.plus(finishRedialAfter),
            allowedDays,
            allowedTime,
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
        dialer.result(callResult, callResultPayload)

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
        replies.add(TelephonySwitchReply(phoneNumber = phoneNumber, headers = sipHeaders, state = currentState))
    }

    /**
     * Transfers call to operator or other person.
     *
     * @param reply another person's phone number
     * */
    fun transferCall(reply: TelephonySwitchReply) {
        replies.add(reply)
    }

    /**
     * Allows changing the default timeout (5 seconds) for the speechNotRecognized event in state.
     * The timeout duration must be specified in milliseconds as an integer between 1000 (1 second) and 20000 (20 seconds).
     * If you set a duration outside the range, the default timeout will be used.
     *
     * Example usage:
     * ```
     * state("BeforeSilence") {
     *    activators {
     *        regex(.*)
     *    }
     *    action {
     *        reactions.noInputTimeout(15000)
     *    }
     * }
     * ```
     * @param duration the duration of the timeout in milliseconds.
     * */
    fun noInputTimeout(duration: Int) = dialer.noInputTimeout(duration)

    /**
     * Allows to barge in speech synthesis or audio playback during handling bargeIn event.
     *
     * This reaction works only when manually handling bargeIn event.
     * In general case you should use [com.justai.jaicf.channel.jaicp.bargein.BargeInProcessor] to implement any custom bargeIn logic.
     *
     * @see com.justai.jaicf.channel.jaicp.bargein.BargeInProcessor
     * */
    fun allowBargeIn() {
        bargeInInterrupt = BargeInResponse(true)
    }

    override fun doBeforeCollect() {
        this.bargeIn ?: return
        replies.replaceAll { reply ->
            when (reply) {
                is TextReply -> if (reply.bargeInReply == null) reply.copy(bargeInReply = BargeInReplyData.IGNORE) else reply
                is AudioReply -> if (reply.bargeInReply == null) reply.copy(bargeInReply = BargeInReplyData.IGNORE) else reply
                else -> reply
            }

        }
    }

    private fun ensureBargeInProps() {
        bargeIn = bargeIn ?: bargeInDefaultProps
    }
}
