package com.justai.jaicf.channel.jaicp.dto

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotRequestType
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.dto.bargein.BargeInRequest
import com.justai.jaicf.channel.jaicp.logging.internal.SessionManager
import com.justai.jaicf.context.ActionContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface JaicpNativeBotRequest : BotRequest {
    val jaicp: JaicpBotRequest
}

interface TelephonyBotRequest : JaicpNativeBotRequest {
    val caller: String?
        get() = jaicp.rawRequest["caller"]?.jsonPrimitive?.content
    val trunk: String?
        get() = jaicp.rawRequest["extension"]?.jsonPrimitive?.content
    val calleePayload: JsonObject?
        get() = jaicp.rawRequest.jsonObject["originateData"]?.jsonObject?.get("payload")?.jsonObject
    val campaignSchedule: JsonObject?
        get() = jaicp.rawRequest.jsonObject["originateData"]?.jsonObject?.get("callScenarioData")
            ?.jsonObject?.get("schedule")?.jsonObject?.get("campaignSchedule")?.jsonObject
    val dialSchedule: JsonObject?
        get() = jaicp.rawRequest.jsonObject["originateData"]?.jsonObject?.get("callScenarioData")
            ?.jsonObject?.get("schedule")?.jsonObject?.get("dialSchedule")?.jsonObject
    val audioToken: String?
        get() = jaicp.data?.jsonObject?.get("resterisk")?.jsonObject?.get("callRecordsDownloadData")
            ?.jsonObject?.get("audioToken")?.jsonObject?.get("token")?.jsonPrimitive?.content
    val callRecordingPath: String?
        get() = jaicp.data?.jsonObject?.get("resterisk")?.jsonObject?.get("callRecordingFile")?.jsonPrimitive?.content

    fun getCallRecordingFullUrl(context: ActionContext<*, *, *>): String? =
        jaicp.data?.jsonObject?.get("resterisk")?.jsonObject?.get("callRecordsDownloadData")
            ?.jsonObject?.get("downloadUrl")?.jsonPrimitive?.content?.let {
                SessionManager.get(context.reactions.executionContext).getOrCreateSessionId().let { sessionData ->
                    it.replace("{sessionId}", sessionData.sessionId)
                }
            }

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
