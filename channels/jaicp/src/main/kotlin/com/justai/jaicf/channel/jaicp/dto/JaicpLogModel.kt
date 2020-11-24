package com.justai.jaicf.channel.jaicp.dto

import com.justai.jaicf.activator.catchall.CatchAllActivatorContext
import com.justai.jaicf.activator.event.EventActivatorContext
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.activator.regex.RegexActivatorContext
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.logging.JaicpConversationSessionData
import com.justai.jaicf.channel.jaicp.toJson
import com.justai.jaicf.context.StrictActivatorContext
import com.justai.jaicf.logging.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json


@Serializable
internal data class JaicpLogModel private constructor(
    val bot: String,
    val channel: String,
    val userId: String,
    val user: User?,
    val questionId: String,
    val request: Request,
    val nlpInfo: NlpInfo,
    val response: Response,
    val timestamp: Long,
    val processingTime: Long,
    var query: String?,
    val botId: String,
    var answer: String?,
    val channelType: String,
    val sessionId: String,
    val isNewSession: Boolean
) {
    @Serializable
    data class NlpInfo(
        val fromState: String,
        val nlpClass: String?,
        val confidence: Double?,
        val rule: String?,
        val ruleType: String?
    ) {
        companion object Factory {
            fun create(lc: LoggingContext) = NlpInfo(
                nlpClass = lc.activationContext?.activation?.state,
                ruleType = lc.activationContext?.activator?.name,
                rule = when (val ctx = lc.activationContext?.activation?.context) {
                    is RegexActivatorContext -> ctx.pattern.pattern()
                    is EventActivatorContext -> ctx.event
                    is CatchAllActivatorContext -> "catchAll"
                    is IntentActivatorContext -> ctx.intent
                    else -> null
                },
                confidence = when (val ctx = lc.activationContext?.activation?.context) {
                    is StrictActivatorContext -> 1.0
                    is IntentActivatorContext -> ctx.confidence.toDouble()
                    else -> null
                },
                fromState = lc.firstState
            )
        }
    }

    @Serializable
    data class Request(
        val type: String,
        var query: String?,
        val requestData: JsonObject,
        val data: JsonObject
    ) {
        companion object Factory {
            fun fromRequest(
                req: JaicpBotRequest,
                input: String
            ) = Request(
                type = req.type.toString().toLowerCase(),
                query = input,
                requestData = req.data?.jsonObject ?: json { },
                data = req.data?.jsonObject ?: json { }
            )
        }
    }

    @Serializable
    data class Response(
        val responseData: ResponseData
    )

    @Serializable
    data class User(
        val id: String? = null,
        val firstName: String? = null,
        val lastName: String? = null,
        val username: String? = null
    ) {
        companion object Factory {
            fun fromRequest(br: JaicpBotRequest) = JSON.parse(this.serializer(), br.userFrom.toString())
        }
    }

    @Serializable
    data class ResponseData(
        val nlpClass: String?,
        val confidence: Double?,
        val replies: List<JsonElement?>,
        val answer: String,
        val sessionId: String?
    ) {
        companion object Factory {
            fun create(lc: LoggingContext): ResponseData {
                val nlpInfo = NlpInfo.create(lc)
                return ResponseData(
                    answer = buildAnswer(lc.reactions),
                    replies = buildReplies(lc.reactions),
                    sessionId = null,
                    confidence = nlpInfo.confidence,
                    nlpClass = nlpInfo.nlpClass
                )
            }

            private fun buildAnswer(reactions: List<Reaction>) = reactions
                .filterIsInstance<SayReaction>()
                .joinToString(separator = "\n\n")

            private fun buildReplies(reactions: List<Reaction>) =
                reactions.toReplies().map { it.serialized().toJson() }
        }
    }

    companion object Factory {
        fun fromRequest(
            jaicpBotRequest: JaicpBotRequest,
            loggingContext: LoggingContext,
            session: JaicpConversationSessionData
        ): JaicpLogModel {
            val currentTimeUTC = System.currentTimeMillis()
            val request = Request.fromRequest(jaicpBotRequest, loggingContext.input)
            val user = User.fromRequest(jaicpBotRequest)
            val nlp = NlpInfo.create(loggingContext)
            val response = ResponseData.create(loggingContext)

            return JaicpLogModel(
                botId = jaicpBotRequest.botId,
                bot = jaicpBotRequest.botId,
                channel = jaicpBotRequest.channelType,
                userId = jaicpBotRequest.channelUserId,
                questionId = jaicpBotRequest.questionId,
                request = request,
                query = loggingContext.input,
                timestamp = currentTimeUTC,
                processingTime = currentTimeUTC - jaicpBotRequest.startProcessingTime,
                answer = response.answer,
                channelType = jaicpBotRequest.channelType,
                nlpInfo = nlp,
                response = Response(response),
                user = user,
                sessionId = session.sessionId,
                isNewSession = session.isNewSession
            )
        }
    }
}

private fun List<Reaction>.toReplies() = mapNotNull { r ->
    when (r) {
        is SayReaction -> TextReply(r.text, state = r.fromState)
        is ImageReaction -> ImageReply(r.imageUrl, state = r.fromState)
        is AudioReaction -> AudioReply(audioUrl = r.audioUrl, state = r.fromState)
        is ButtonsReaction -> ButtonsReply(buttons = r.buttons.map { Button(it) }, state = r.fromState)
        else -> null
    }
}


