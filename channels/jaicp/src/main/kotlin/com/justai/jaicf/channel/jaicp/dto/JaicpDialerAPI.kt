package com.justai.jaicf.channel.jaicp.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
class JaicpDialerAPI {

    private var callResultData: CallResultData? = null
    private var callTagData: CallTagData? = null
    private var callReportData: CallReportData? = null
    private var redialData: RedialData? = null

    /**
     * Один либо на звонок, либо на клиента.
     *
     * @property callResult - результат звонка, попадает в отчет об обзвоне из сценария. Прим. обонент перданул. Попадает в цсвху в поле "результат звонка". Также хранится в логах диалогов - результат звонка.
     * @property callResultPayload - что угодно json
     * */
    @Serializable
    data class CallResultData(
        private val callResult: String?,
        private val callResultPayload: JsonObject?
    )

    /**
     * Видно в отчете. Туда попадают всякие переменные из AL автоматом, плюс цвета.
     * Пишется в скрипте. Нужны для отдельных стоблцов в цсвхе  и в бублике.
     *
     * @property tagPayload - json value
     * @property tag - ключ
     * @property tagColor - в бублик
     *
     * */
    @Serializable
    data class CallTagData(
        private val tag: String,
        private val tagPayload: JsonObject?,
        private val tagColor: String?
    )

    /**
     * Не идет в бублик.
     *
     * @param header - ключ, название столбца в отчете
     * @param value - значене
     * @param order - порядок в отчете
     * */
    @Serializable
    class CallReportData(
        private val header: String,
        private val value: String?,
        private val order: Int?
    )


    /**
     * Хуйня для перезвона.
     *
     * @property startDateTime начало обзвона (instant)
     * @property finishDateTime конец обзвона (instant)
     * @property allowedDays список разрешенных дней
     * @property localTimeFrom начало интервала локального времени, в течение которого можно звонить. ФОрмат HH:MM
     * @property localTimeTo конец интервала локального времени, в течение которого можно звонить. ФОрмат HH:MM
     * @property retryIntervalInMinutes интервал в минутах между попытками
     * @property maxAttempts макс количетсво попыток
     * */
    @Serializable
    class RedialData(
        val startDateTime: Long?,
        val finishDateTime: Long?,
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
        redialData = data
    }

    internal fun report(data: CallReportData) {
        callReportData = data
    }

    internal fun tag(data: CallTagData) {
        callTagData = data
    }

    internal fun result(data: CallResultData) {
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
