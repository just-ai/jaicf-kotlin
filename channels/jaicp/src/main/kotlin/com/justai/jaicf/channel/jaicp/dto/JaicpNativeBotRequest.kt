package com.justai.jaicf.channel.jaicp.dto

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotRequestType
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface JaicpNativeBotRequest : BotRequest {
    val jaicp: JaicpBotRequest
}

interface TelephonyBotRequest : JaicpNativeBotRequest {
    /**
     * Caller returns caller’s phone number.
     */
    val caller: String?
        get() = jaicp.rawRequest["caller"]?.jsonPrimitive?.content

    /**
     * Returns a SIP trunk. SIP trunk is the virtual version of an analog phone line.
     */
    val trunk: String?
        get() = jaicp.rawRequest["extension"]?.jsonPrimitive?.content

    /**
     * Returns an object with data linked with the current client’s phone number.
     * This data can be provided for each number when adding a phone number to a call campaign.
     */
    val calleePayload: JsonObject?
        get() = jaicp.rawRequest.jsonObject["originateData"]?.jsonObject?.get("payload")?.jsonObject

    /**
     * Returns the call campaign schedule that was set during the campaign creation.
     * Returned object has two properties, allowedDays and allowedTime
     */
    val campaignSchedule: Schedule?
        get() = jaicp.rawRequest.jsonObject["originateData"]?.jsonObject?.get("callScenarioData")
            ?.jsonObject?.get("schedule")?.jsonObject?.get("campaignSchedule")?.jsonObject
            ?.let(JSON::decodeFromJsonElement)

    /**
     * Returns the history of completed and available call attempts for the current phone number.
     * Returns an object of [DialHistory] having information about call attempts.
     *
     * @see [DialHistory]
     */
    val dialHistory: DialHistory?
        get() = jaicp.rawRequest.jsonObject["originateData"]?.jsonObject?.get("callScenarioData")
            ?.jsonObject?.get("callAttemptsHistory")?.let(JSON::decodeFromJsonElement)

    /**
     * Returns the dial schedule of current phone number. Returned object has two properties, allowedDays and allowedTime.
     * Works only when the call was created using the Calls API with a custom schedule of allowed call time intervals.
     */
    val dialSchedule: Schedule?
        get() = jaicp.rawRequest.jsonObject["originateData"]?.jsonObject?.get("callScenarioData")
            ?.jsonObject?.get("schedule")?.jsonObject?.get("dialSchedule")?.jsonObject
            ?.let(JSON::decodeFromJsonElement)

    /**
     * Returns a token for downloading call recordings made in the current project.
     * The Record calls toggle in the phone channel settings must be set to active so that call recordings become available for download.
     */
    val audioToken: String?
        get() = jaicp.data?.jsonObject?.get("resterisk")?.jsonObject?.get("callRecordsDownloadData")
            ?.jsonObject?.get("audioToken")?.jsonObject?.get("token")?.jsonPrimitive?.content

    /**
     *  Returns the path to the current call recording. Returned string containing the relative path to the file on
     *  the server where the project is hosted: The Record calls toggle in the phone channel settings must be set to
     *  active so that call recordings become available for download.
     *  The absolute call recording file URL conforms to the following template: https://{host}/restapi/download/{projectId}/recordings/call/{callRecordingPath}
     */
    val callRecordingPath: String?
        get() = jaicp.data?.jsonObject?.get("resterisk")?.jsonObject?.get("callRecordingFile")?.jsonPrimitive?.content

    /**
     * When the `onCallNotConnected` event is triggered, it returns the reason the call failed.
     * It allows to determine the reason the bot could not reach the client. It returns a string: `BUSY` or `NO_ANSWER`.
     */
    val callNotConnectedReason: String?
        get() = jaicp.data?.jsonObject?.get("resterisk")?.jsonObject?.get("callNotConnectedData")?.jsonObject
            ?.get("reason")?.jsonPrimitive?.content

    /**
     * Returns an URL for downloading the current call recording. Returned string with the call recording download URL.
     * The Record calls toggle in the phone channel settings must be set to active so that call recordings become available for download.
     *
     * @param sessionId id of current session. You can receive sessionId as property of ActionContext.
     */
    fun getCallRecordingFullUrl(sessionId: String): String? =
        jaicp.data?.jsonObject?.get("resterisk")?.jsonObject?.get("callRecordsDownloadData")
            ?.jsonObject?.get("downloadUrl")?.jsonPrimitive?.content?.replace("{sessionId}", sessionId)


    companion object {
        fun create(jaicp: JaicpBotRequest): TelephonyBotRequest = when (jaicp.type) {
            BotRequestType.QUERY -> TelephonyQueryRequest(jaicp)
            BotRequestType.EVENT -> jaicp.rawRequest["bargeInIntentStatus"]
                ?.let { TelephonyBargeInRequest(jaicp, JSON.decodeFromJsonElement(it)) }
                ?: TelephonyEventRequest(jaicp)
            BotRequestType.INTENT -> error("Jaicp intent events are not supported")
        }
    }
}

@Serializable
data class Schedule(
    val allowedDays: List<String>? = null,
    val allowedTime: AllowedTime? = null,
)

/**
 * Contains the history of completed and available call attempts for the current phone number for the current series and for the phone number.
 * Several call attempts in a row form a series. If the bot reaches the customer and schedules a new call using the
 * `redial` method, the series is reset, and new call attempts are made during a new series.
 *
 * @property completedAttempts is the number of call attempts already made during the current series.
 * If a new series is scheduled from the script, the counter is reset to 0.
 *
 * @property availableAttempts is the number of call attempts available during the current series.
 * This property can be set when creating a call campaign, can be added to a campaign through the Calls API, and from a script using the `redial` method.
 *
 * @property completedAttemptsToPhone is the total number of all call attempts made for the phone number. It never gets reset.
 *
 * @property availableAttemptsToPhone is the maximum number of call attempts allowed for the phone number.
 */
@Serializable
data class DialHistory(
    var completedAttempts: Int,
    var completedAttemptsToPhone: Int,
    var availableAttempts: Int,
    var availableAttemptsToPhone: Int
)

data class TelephonyQueryRequest(
    override val jaicp: JaicpBotRequest,
) : TelephonyBotRequest, QueryBotRequest(jaicp.clientId, jaicp.input)

data class TelephonyEventRequest(
    override val jaicp: JaicpBotRequest,
) : TelephonyBotRequest, EventBotRequest(jaicp.clientId, jaicp.input)

data class TelephonyBargeInRequest internal constructor(
    override val jaicp: JaicpBotRequest,
    val bargeInRequest: BargeInRequest,
) : TelephonyBotRequest, QueryBotRequest(jaicp.clientId, bargeInRequest.recognitionResult.text) {

    val transition = bargeInRequest.bargeInTransition.transition
}

interface ChatWidgetBotRequest : JaicpNativeBotRequest {
    companion object {
        fun create(jaicp: JaicpBotRequest) = when (jaicp.type) {
            BotRequestType.QUERY -> ChatWidgetQueryRequest(jaicp)
            BotRequestType.EVENT -> ChatWidgetEventRequest(jaicp)
            BotRequestType.INTENT -> error("Jaicp intent events are not supported")
        }
    }
}

data class ChatWidgetQueryRequest(
    override val jaicp: JaicpBotRequest,
) : ChatWidgetBotRequest, QueryBotRequest(jaicp.clientId, jaicp.input)

data class ChatWidgetEventRequest(
    override val jaicp: JaicpBotRequest,
) : ChatWidgetBotRequest, EventBotRequest(jaicp.clientId, jaicp.input)

interface ChatApiBotRequest : JaicpNativeBotRequest {
    companion object {
        fun create(jaicp: JaicpBotRequest) = when (jaicp.type) {
            BotRequestType.QUERY -> ChatApiQueryRequest(jaicp)
            BotRequestType.EVENT -> ChatApiEventRequest(jaicp)
            BotRequestType.INTENT -> error("Jaicp intent events are not supported")
        }
    }
}

data class ChatApiQueryRequest(
    override val jaicp: JaicpBotRequest,
) : ChatApiBotRequest, QueryBotRequest(jaicp.clientId, jaicp.input)

data class ChatApiEventRequest(
    override val jaicp: JaicpBotRequest,
) : ChatApiBotRequest, EventBotRequest(jaicp.clientId, jaicp.input)

val BotRequest.telephony
    get() = this as? TelephonyBotRequest

val BotRequest.chatapi
    get() = this as? ChatApiBotRequest

val BotRequest.chatwidget
    get() = this as? ChatWidgetBotRequest

internal val BotRequest.jaicp
    get() = this as? JaicpBotRequest

internal val BotRequest.jaicpNative
    get() = this as? JaicpNativeBotRequest

val BotRequest.bargeIn
    get() = this as? TelephonyBargeInRequest
