package com.justai.jaicf.channel.jaicp.dto

import kotlinx.serialization.Serializable

@Serializable
class JaicpDialerAPI {

    private var callResultData: CallResultData? = null
    private var callTagData: CallTagData? = null
    private var callReportData: CallReportData? = null
    private var redialData: RedialData? = null

    @Serializable
    data class CallResultData(
        private val callResult: String?,
        private val callResultPayload: String?
    )

    @Serializable
    class CallTagData(
        private val tagPayload: String?,
        private val tagColor: String?
    )

    @Serializable
    class CallReportData(
        private val value: String?,
        private val order: Int?
    )

    @Serializable
    class RedialData(
        private val startDateTime: Long?,
        private val finishDateTime: Long?,
        private val allowedDays: List<DayOfWeek> = emptyList(),
        private val localTimeFrom: String? = null,
        private val localTimeTo: String? = null,
        private val retryIntervalInMinutes: Int? = null,
        private val maxAttempts: Int? = null
    )

    internal fun redial(data: RedialData) {
        DialerApiValidator.validate(data)
        redialData = data
    }

    internal fun report(data: CallReportData) {
        DialerApiValidator.validate(data)
        callReportData = data
    }

    internal fun tag(data: CallTagData) {
        DialerApiValidator.validate(data)
        callTagData = data
    }

    internal fun result(data: CallResultData) {
        DialerApiValidator.validate(data)
        callResultData = data
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

private object DialerApiValidator {
    fun validate(reportData: JaicpDialerAPI.CallReportData) {
    }

    fun validate(redialData: JaicpDialerAPI.RedialData) {

    }

    fun validate(callTagData: JaicpDialerAPI.CallTagData) {

    }

    fun validate(resultData: JaicpDialerAPI.CallResultData) {

    }
}