package com.justai.jaicf.channel.jaicp.dto

import com.justai.jaicf.channel.jaicp.JSON
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
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
     * An object sent to schedule a redial.
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
    internal data class RedialData(
        val startDateTime: Long? = null,
        val finishDateTime: Long? = null,
        val allowedDays: List<String> = emptyList(),
        val localTimeFrom: String? = null,
        val localTimeTo: String? = null,
        val maxAttempts: Int? = null,
        val retryIntervalInMinutes: Int? = null
    )

    internal fun redial(
        startDateTime: Instant?,
        finishDateTime: Instant?,
        allowedDays: List<DayOfWeek> = emptyList(),
        localTimeFrom: String? = null,
        localTimeTo: String? = null,
        maxAttempts: Int? = null,
        retryIntervalInMinutes: Int? = null
    ) {
        val redialData = RedialData(
            startDateTime = startDateTime?.toEpochMilli(),
            finishDateTime = finishDateTime?.toEpochMilli(),
            allowedDays = allowedDays.mapToDialerDays(),
            localTimeFrom = localTimeFrom,
            localTimeTo = localTimeTo,
            retryIntervalInMinutes = retryIntervalInMinutes,
            maxAttempts = maxAttempts
        )

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

    internal fun getApiResponse(): JsonElement {
        return JSON.toJson(serializer(), this)
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
        if (st > fin) {
            error("startDateTime must be less than finishDateTime")
        }
    }
}


private fun checkLocalTime(data: JaicpDialerAPI.RedialData) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val start = data.localTimeFrom?.let {
        formatter.parse(it)
    }
    val end = data.localTimeTo?.let {
        formatter.parse(it)
    }
    if (start != null && end != null) {
        if (LocalTime.from(start).isAfter(LocalTime.from(end))) {
            error("localTimeFrom cannot be higher then localTimeTo")
        }
    }

}

private fun checkRetryAndInterval(data: JaicpDialerAPI.RedialData) {
    fun numberInRange(value: Int, min: Int, max: Int) = value in min..max
    data.retryIntervalInMinutes?.let {
        if (!numberInRange(it, 1, Int.MAX_VALUE)) {
            error("retryIntervalInMinutes must be a positive integer, got $it")
        }
    }
    data.maxAttempts?.let {
        if (!numberInRange(it, 1, 50)) {
            error("maxAttempts must be a positive integer, got $it")
        }
    }
}
