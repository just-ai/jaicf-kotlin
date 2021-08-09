package com.justai.jaicf.channel.jaicp.dto

import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
class JaicpDialerAPI {

    private var callResult: String? = null
    private var callResultPayload: String? = null
    private var reportData: MutableMap<String, CallReportData> = mutableMapOf()
    private var redial: RedialData? = null

    /**
     * Reports data to be stored in .xsls report.
     *
     * @param value - value in cell in .xsls report
     * @param order - optional order for column
     * */
    @Serializable
    internal class CallReportData(
        private val value: String?,
        private val order: Int?
    )

    /**
     * An object sent in response to schedule a redial.
     *
     * @property startDateTime unix timestamp (UTC-0 epoch milliseconds) to start attempting to redial a client
     * @property finishDateTime unix timestamp (UTC-0 epoch milliseconds) to end attempting to redial a client
     * @property allowedDays list of [DayOfWeek] allowed days
     * @property localTimeFrom local time interval start attempting to redial. E.g. 16:20. Property is deprecated
     * @property localTimeTo local time interval end attempting to redial. E.g. 23:59. Property is deprecated
     * @property maxAttempts max number of attempts to call client
     * @property retryIntervalInMinutes interval between redial attempts. Must not be less than 1
     * @property allowedTime local time intervals by day of a week
     * */
    @Serializable
    data class RedialData internal constructor(
        val startDateTime: Long? = null,
        val finishDateTime: Long? = null,
        val allowedDays: List<String> = emptyList(),
        @Deprecated("Field 'localTimeFrom' are deprecated, use 'allowedTime' instead")
        val localTimeFrom: String? = null,
        @Deprecated("Field 'localTimeTo' are deprecated, use 'allowedTime' instead")
        val localTimeTo: String? = null,
        val maxAttempts: Int? = null,
        val retryIntervalInMinutes: Int? = null,
        val allowedTime: AllowedTime? = null,
    ) {
        companion object {
            @Deprecated(
                "Parameters 'localTimeFrom' and 'localTimeTo' are deprecated, use 'create' method that accepts allowedTime",
                ReplaceWith(
                    "create(startDateTime, finishDateTime, allowedDays, AllowedTime(listOf(LocalTimeInterval(localTimeFrom, localTimeTo))), maxAttempts, retryIntervalInMinutes)",
                    "com.justai.jaicf.channel.jaicp.dto.*"
                )
            )
            fun create(
                startDateTime: Instant?,
                finishDateTime: Instant?,
                allowedDays: List<DayOfWeek> = emptyList(),
                localTimeFrom: String? = null,
                localTimeTo: String? = null,
                maxAttempts: Int? = null,
                retryIntervalInMinutes: Int? = null,
            ) = RedialData(
                startDateTime = startDateTime?.toEpochMilli(),
                finishDateTime = finishDateTime?.toEpochMilli(),
                allowedDays = allowedDays.mapToDialerDays(),
                localTimeFrom = localTimeFrom,
                localTimeTo = localTimeTo,
                retryIntervalInMinutes = retryIntervalInMinutes,
                maxAttempts = maxAttempts
            )

            fun create(
                startDateTime: Instant?,
                finishDateTime: Instant?,
                allowedDays: List<DayOfWeek> = emptyList(),
                allowedTime: AllowedTime? = null,
                maxAttempts: Int? = null,
                retryIntervalInMinutes: Int? = null
            ) = RedialData(
                startDateTime = startDateTime?.toEpochMilli(),
                finishDateTime = finishDateTime?.toEpochMilli(),
                allowedDays = allowedDays.mapToDialerDays(),
                allowedTime = allowedTime,
                retryIntervalInMinutes = retryIntervalInMinutes,
                maxAttempts = maxAttempts
            )
        }
    }

    @Deprecated(
        "Parameters 'localTimeFrom' and 'localTimeTo' are deprecated, use 'redial' method that accepts allowedTime",
        ReplaceWith(
            "redial(startDateTime, finishDateTime, allowedDays, AllowedTime(listOf(LocalTimeInterval(localTimeFrom, localTimeTo))), maxAttempts, retryIntervalInMinutes)",
            "com.justai.jaicf.channel.jaicp.dto.*"
        )
    )
    internal fun redial(
        startDateTime: Instant?,
        finishDateTime: Instant?,
        allowedDays: List<DayOfWeek> = emptyList(),
        localTimeFrom: String? = null,
        localTimeTo: String? = null,
        maxAttempts: Int? = null,
        retryIntervalInMinutes: Int? = null,
    ) = redial(
        RedialData(
            startDateTime = startDateTime?.toEpochMilli(),
            finishDateTime = finishDateTime?.toEpochMilli(),
            allowedDays = allowedDays.mapToDialerDays(),
            localTimeFrom = localTimeFrom,
            localTimeTo = localTimeTo,
            retryIntervalInMinutes = retryIntervalInMinutes,
            maxAttempts = maxAttempts
        )
    )

    internal fun redial(
        startDateTime: Instant?,
        finishDateTime: Instant?,
        allowedDays: List<DayOfWeek> = emptyList(),
        allowedTime: AllowedTime? = null,
        maxAttempts: Int? = null,
        retryIntervalInMinutes: Int? = null
    ) = redial(
        RedialData(
            startDateTime = startDateTime?.toEpochMilli(),
            finishDateTime = finishDateTime?.toEpochMilli(),
            allowedDays = allowedDays.mapToDialerDays(),
            allowedTime = allowedTime,
            retryIntervalInMinutes = retryIntervalInMinutes,
            maxAttempts = maxAttempts
        )
    )

    internal fun redial(redialData: RedialData) {
        checkStartFinishTime(redialData)
        checkLocalTime(redialData)
        checkAllowedTime(redialData)
        checkRetryAndInterval(redialData)
        redial = redialData
    }

    internal fun report(header: String, data: CallReportData) {
        reportData[header] = data
    }

    internal fun result(result: String?, resultPayload: String?) {
        callResult = result
        callResultPayload = resultPayload
    }
}

/**
 * Contains local time intervals by day of a week.
 *
 * @param mon list of time intervals on Monday
 * @param tue list of time intervals on Tuesday
 * @param wed list of time intervals on Wednesday
 * @param thu list of time intervals on Thursday
 * @param fri list of time intervals on Friday
 * @param sat list of time intervals on Saturday
 * @param sun list of time intervals on Sunday
 * @param default will be used if no interval is specified for the current day of the week
 */
@Serializable
data class AllowedTime(
    val default: List<LocalTimeInterval>? = null,
    val mon: List<LocalTimeInterval>? = null,
    val tue: List<LocalTimeInterval>? = null,
    val wed: List<LocalTimeInterval>? = null,
    val thu: List<LocalTimeInterval>? = null,
    val fri: List<LocalTimeInterval>? = null,
    val sat: List<LocalTimeInterval>? = null,
    val sun: List<LocalTimeInterval>? = null
)

internal val AllowedTime.intervals
    get() = listOf(default, mon, tue, wed, thu, fri, sat, sun)


/**
 * Local time interval attempting to redial
 *
 * @param localTimeFrom local time interval start attempting to redial. E.g. 16:20
 * @param localTimeTo local time interval end attempting to redial. E.g. 23:59
 * */
@Serializable
data class LocalTimeInterval(
    val localTimeFrom: String,
    val localTimeTo: String
)

private fun List<DayOfWeek>.mapToDialerDays(): List<String> = map {
    when (it) {
        DayOfWeek.MONDAY -> "mon"
        DayOfWeek.TUESDAY -> "tue"
        DayOfWeek.WEDNESDAY -> "wed"
        DayOfWeek.THURSDAY -> "thu"
        DayOfWeek.FRIDAY -> "fri"
        DayOfWeek.SATURDAY -> "sat"
        DayOfWeek.SUNDAY -> "sun"
    }
}

private fun checkStartFinishTime(data: JaicpDialerAPI.RedialData) {
    val start = data.startDateTime
    val finish = data.finishDateTime
    if (start != null && finish != null) {
        require(start < finish) {
            "The redial start time (startDateTime) must be less than redial finish time (finishDateTime)"
        }
    }
}

private fun checkLocalTime(data: JaicpDialerAPI.RedialData) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val start = data.localTimeFrom?.let { LocalTime.parse(it, formatter) }
    val end = data.localTimeTo?.let { LocalTime.parse(it, formatter) }
    if (start != null && end != null) {
        require(start.isBefore(end)) {
            "localTimeFrom must be before localTimeTo"
        }
    }
}

private fun checkAllowedTime(data: JaicpDialerAPI.RedialData) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    data.allowedTime?.intervals?.filterNotNull()
        ?.flatten()
        ?.forEach { interval ->
            val start = interval.localTimeFrom.let { LocalTime.parse(it, formatter) }
            val end = interval.localTimeTo.let { LocalTime.parse(it, formatter) }

            require(start.isBefore(end)) {
                "localTimeFrom must be before localTimeTo"
            }
        }
}

private fun checkRetryAndInterval(data: JaicpDialerAPI.RedialData) {
    data.retryIntervalInMinutes?.let {
        require(it >= 1) {
            "The retry interval in minutes must be a positive number. Given: $it"
        }
    }
    data.maxAttempts?.let {
        require(it in 1..50) {
            "The maximum number of attempts must be in range from 1 to 50 inclusive. Given: $it"
        }
    }
}
