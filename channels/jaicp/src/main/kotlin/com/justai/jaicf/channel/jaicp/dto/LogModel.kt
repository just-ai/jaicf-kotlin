package com.justai.jaicf.channel.jaicp.dto

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.activator.catchall.CatchAllActivatorContext
import com.justai.jaicf.activator.event.EventActivatorContext
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.activator.regex.RegexActivatorContext
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.logging.toEpochMillis
import com.justai.jaicf.context.StrictActivatorContext
import com.justai.jaicf.reactions.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.time.OffsetDateTime
import java.time.ZoneId


@Serializable
internal class LogModel private constructor(
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
    val channelType: String
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
            fun fromActivation(activationContext: ActivationContext?) = NlpInfo(
                nlpClass = activationContext?.activation?.state,
                ruleType = activationContext?.activator?.name,
                rule = when (val ctx = activationContext?.activation?.context) {
                    is RegexActivatorContext -> ctx.pattern.pattern()
                    is EventActivatorContext -> ctx.event
                    is CatchAllActivatorContext -> "catchAll"
                    is IntentActivatorContext -> ctx.intent
                    else -> null
                },
                confidence = when (val ctx = activationContext?.activation?.context) {
                    is StrictActivatorContext -> 1.0
                    is IntentActivatorContext -> ctx.confidence.toDouble()
                    else -> null
                },
                fromState = "" // TODO
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
                requestData = req.data.jsonObject,
                data = req.data.jsonObject
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
            fun create(reactions: List<Reaction>, activationContext: ActivationContext?): ResponseData {
                val nlpInfo = NlpInfo.fromActivation(activationContext)
                return ResponseData(
                    answer = buildAnswer(reactions),
                    replies = buildReplies(reactions),
                    sessionId = null,
                    confidence = nlpInfo.confidence,
                    nlpClass = nlpInfo.nlpClass
                )
            }

            private fun buildAnswer(reactions: List<Reaction>) = reactions
                .filterIsInstance<SayReaction>()
                .joinToString(separator = "\n\n")

            private fun buildReplies(reactions: List<Reaction>): List<JsonElement?> = reactions.mapNotNull { r ->
                when (r) {
                    is SayReaction -> JSON.toJson(TextReply.serializer(), TextReply(text = r.text, state = r.state))
                    is ImageReaction -> JSON.toJson(
                        ImageReply.serializer(),
                        ImageReply(imageUrl = r.imageUrl, state = r.state)
                    )
                    is AudioReaction -> JSON.toJson(
                        AudioReply.serializer(),
                        AudioReply(audioUrl = r.audioUrl, state = r.state)
                    )
                    is ButtonsReaction -> JSON.toJson(
                        ButtonsReply.serializer(),
                        ButtonsReply(buttons = r.buttons.map { Button(it) }, state = r.state)
                    )
                    else -> null
                }
            }
        }
    }

    companion object Factory {
        fun fromRequest(
            jaicpBotRequest: JaicpBotRequest,
            reactions: MutableList<Reaction>,
            activationContext: ActivationContext?,
            input: String
        ): LogModel {
            val currentTimeUTC = OffsetDateTime.now(ZoneId.of("UTC")).toEpochMillis()
            val request = Request.fromRequest(jaicpBotRequest, input)
            val user = User.fromRequest(jaicpBotRequest)
            val nlp = NlpInfo.fromActivation(activationContext)
            val response = ResponseData.create(reactions, activationContext)

            return LogModel(
                botId = jaicpBotRequest.botId,
                bot = jaicpBotRequest.botId,
                channel = jaicpBotRequest.channelType,
                userId = jaicpBotRequest.channelUserId,
                questionId = jaicpBotRequest.questionId,
                request = request,
                query = input,
                timestamp = currentTimeUTC,
                processingTime = currentTimeUTC - jaicpBotRequest.startProcessingTime,
                answer = response.answer,
                channelType = jaicpBotRequest.channelType,
                nlpInfo = nlp,
                response = Response(response),
                user = user
            )
        }
    }
}


