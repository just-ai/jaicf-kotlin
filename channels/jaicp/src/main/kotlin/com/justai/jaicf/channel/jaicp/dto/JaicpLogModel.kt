package com.justai.jaicf.channel.jaicp.dto

import com.justai.jaicf.activator.catchall.CatchAllActivatorContext
import com.justai.jaicf.activator.event.EventActivatorContext
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.activator.regex.RegexActivatorContext
import com.justai.jaicf.channel.jaicp.JSON
import com.justai.jaicf.channel.jaicp.logging.internal.SessionData
import com.justai.jaicf.channel.jaicp.toJson
import com.justai.jaicf.context.ExecutionContext
import com.justai.jaicf.context.StrictActivatorContext
import com.justai.jaicf.exceptions.BotException
import com.justai.jaicf.exceptions.scenarioCause
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.CarouselReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.Reaction
import com.justai.jaicf.logging.SayReaction
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject


@Suppress("DataClassPrivateConstructor")
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
    val isNewSession: Boolean,
    val isNewUser: Boolean,
    val channelData: JsonObject?,
    val exception: String? = null
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
            fun create(executionContext: ExecutionContext) = NlpInfo(
                nlpClass = executionContext.activationContext?.activation?.state,
                ruleType = executionContext.activationContext?.activator?.name,
                rule = when (val ctx = executionContext.activationContext?.activation?.context) {
                    is RegexActivatorContext -> ctx.pattern.pattern()
                    is EventActivatorContext -> ctx.event
                    is CatchAllActivatorContext -> "catchAll"
                    is IntentActivatorContext -> ctx.intent
                    else -> null
                },
                confidence = when (val ctx = executionContext.activationContext?.activation?.context) {
                    is StrictActivatorContext -> 1.0
                    is IntentActivatorContext -> ctx.confidence.toDouble()
                    else -> null
                },
                fromState = executionContext.firstState
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
                requestData = req.data?.jsonObject ?: buildJsonObject { },
                data = req.data?.jsonObject ?: buildJsonObject { }
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
            fun fromRequest(br: JaicpBotRequest) = JSON.decodeFromString(this.serializer(), br.userFrom.toString())
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
            fun create(executionContext: ExecutionContext): ResponseData {
                val nlpInfo = NlpInfo.create(executionContext)
                val replies = buildReplies(executionContext.reactions)

                executionContext.scenarioException?.toReply()?.let {
                    replies.add(JSON.encodeToJsonElement(ErrorReply.serializer(), it))
                }
                return ResponseData(
                    answer = buildAnswer(executionContext.reactions),
                    replies = replies,
                    sessionId = null,
                    confidence = nlpInfo.confidence,
                    nlpClass = nlpInfo.nlpClass
                )
            }

            private fun buildAnswer(reactions: List<Reaction>) = reactions
                .filterIsInstance<SayReaction>()
                .joinToString(separator = "\n\n")

            private fun buildReplies(reactions: List<Reaction>): MutableList<JsonElement> =
                reactions.toReplies().map { it.serialized().toJson() }.toMutableList()
        }
    }

    companion object Factory {
        fun fromRequest(
            jaicpBotRequest: JaicpBotRequest,
            executionContext: ExecutionContext,
            session: SessionData
        ): JaicpLogModel {
            val currentTimeUTC = System.currentTimeMillis()
            val request = Request.fromRequest(jaicpBotRequest, executionContext.input)
            val user = User.fromRequest(jaicpBotRequest)
            val nlp = NlpInfo.create(executionContext)
            val response = ResponseData.create(executionContext)

            return JaicpLogModel(
                botId = jaicpBotRequest.botId,
                bot = jaicpBotRequest.botId,
                channel = jaicpBotRequest.channelType,
                userId = jaicpBotRequest.channelUserId,
                questionId = jaicpBotRequest.questionId,
                request = request,
                query = executionContext.input,
                timestamp = currentTimeUTC,
                processingTime = currentTimeUTC - jaicpBotRequest.startProcessingTime,
                answer = response.answer,
                channelType = jaicpBotRequest.channelType,
                nlpInfo = nlp,
                response = Response(response),
                user = user,
                sessionId = session.sessionId,
                isNewSession = session.isNewSession,
                isNewUser = executionContext.isNewUser,
                channelData = jaicpBotRequest.channelData,
                exception = executionContext.scenarioException?.scenarioCause?.stackTraceToString()
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
        is CarouselReaction -> r.toReply()
        else -> null
    }
}.toMutableList()

private fun CarouselReaction.toReply() = CarouselReply(
    title,
    elements.map {
        CarouselReply.Element(
            title = it.title,
            button = it.buttons.firstOrNull()?.text ?: "",
            description = it.description,
            imageUrl = it.imageUrl,
            buttonRedirectUrl = it.buttons.firstOrNull()?.url
        )
    }
)

private fun BotException.toReply() = ErrorReply(scenarioCause.stackTraceToString(), currentState, "")
