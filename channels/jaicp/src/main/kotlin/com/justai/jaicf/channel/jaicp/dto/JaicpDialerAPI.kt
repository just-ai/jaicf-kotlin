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
     * @property localTimeFrom local time interval start attempting to redial. E.g. 16:20
     * @property localTimeTo local time interval end attempting to redial. E.g. 23:59
     * @property retryIntervalInMinutes interval between redial attempts
     * @property maxAttempts max number of attempts to call client
     * */
    @Serializable
    data class RedialData internal constructor(
        val startDateTime: Long? = null,
        val finishDateTime: Long? = null,
        val allowedDays: List<String> = emptyList(),
        val localTimeFrom: String? = null,
        val localTimeTo: String? = null,
        val maxAttempts: Int? = null,
        val retryIntervalInMinutes: Int? = null
    ) {
        companion object {
            fun create(
                startDateTime: Instant?,
                finishDateTime: Instant?,
                allowedDays: List<DayOfWeek> = emptyList(),
                localTimeFrom: String? = null,
                localTimeTo: String? = null,
                maxAttempts: Int? = null,
                retryIntervalInMinutes: Int? = null
            ) = RedialData(
                startDateTime = startDateTime?.toEpochMilli(),
                finishDateTime = finishDateTime?.toEpochMilli(),
                allowedDays = allowedDays.mapToDialerDays(),
                localTimeFrom = localTimeFrom,
                localTimeTo = localTimeTo,
                retryIntervalInMinutes = retryIntervalInMinutes,
                maxAttempts = maxAttempts
            )
        }
    }

    internal fun redial(
        startDateTime: Instant?,
        finishDateTime: Instant?,
        allowedDays: List<DayOfWeek> = emptyList(),
        localTimeFrom: String? = null,
        localTimeTo: String? = null,
        maxAttempts: Int? = null,
        retryIntervalInMinutes: Int? = null
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

    internal fun redial(redialData: RedialData) {
        checkStartFinishTime(redialData)
        checkLocalTime(redialData)
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
    val st = data.startDateTime
    val fin = data.finishDateTime
    if (st != null && fin != null) {
        require(st < fin) {
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
            "localTimeFrom cannot be higher then localTimeTo"
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
