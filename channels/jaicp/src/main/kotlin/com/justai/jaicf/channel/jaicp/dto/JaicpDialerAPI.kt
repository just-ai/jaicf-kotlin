package com.justai.jaicf.channel.jaicp.dto

import com.fasterxml.jackson.core.util.RequestPayload
import com.justai.jaicf.channel.jaicp.JSON
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
class JaicpDialerAPI {
    private var callResult: String? = null
    private var callResultPayload: String? = null
    private var reportData: MutableMap<String, CallReportData> = mutableMapOf()
    private var redial: RedialData? = null

    /**
     * Не идет в бублик.
     *
     * @param header - a column header in .xsls report
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
    data class RedialData(
        val startDateTime: Long? = null,
        val finishDateTime: Long? = null,
        val allowedDays: List<DayOfWeek> = emptyList(),
        val localTimeFrom: String? = null,
        val localTimeTo: String? = null,
        val retryIntervalInMinutes: Int? = null,
        val maxAttempts: Int? = null
    )

    internal fun redial(data: RedialData) {
        checkStartFinishTime(data)
        checkLocalTime(data)
        checkRetryAndInterval(data)
        redial = data
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

enum class DayOfWeek(val value: String) {
    MON("mon"),
    TUE("tue"),
    WED("wed"),
    THU("thu"),
    FRI("fri"),
    SAT("sat"),
    SUN("sun")
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
    val timeRegex = Regex("[0-9]{2}:[0-9]{2}")
    data.localTimeFrom?.let {
        if (!timeRegex.matches(it)) error("localTimeFrom should match pattern HH:MM, got $it")
    }
    data.localTimeTo?.let {
        if (!timeRegex.matches(it)) error("localTimeTo should match pattern HH:MM, got $it")
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
