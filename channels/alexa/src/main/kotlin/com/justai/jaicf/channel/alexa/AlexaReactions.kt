package com.justai.jaicf.channel.alexa

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.model.interfaces.audioplayer.*
import com.amazon.ask.model.interfaces.display.Image
import com.amazon.ask.model.services.DefaultApiConfiguration
import com.amazon.ask.model.services.directive.*
import com.amazon.ask.services.ApacheHttpApiClient
import com.amazon.ask.util.JacksonSerializer
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.ResponseReactions
import com.justai.jaicf.logging.SayReaction

val Reactions.alexa
    get() = this as? AlexaReactions

class AlexaReactions(
    override val response: AlexaBotResponse,
    private val handlerInput: HandlerInput
) : ResponseReactions<AlexaBotResponse>(response) {

    private val speeches = mutableListOf<String>()

    private val directiveServiceClient = DirectiveServiceClient(
        DefaultApiConfiguration.builder()
            .withApiClient(ApacheHttpApiClient.standard())
            .withSerializer(JacksonSerializer())
            .withApiEndpoint(handlerInput.requestEnvelope.context.system.apiEndpoint)
            .withAuthorizationValue(handlerInput.requestEnvelope.context.system.apiAccessToken)
            .build()
    )

    override fun say(text: String): SayReaction {
        speeches.add(text)
        val speech = speeches.joinToString(" ")
        response.builder
            .withSpeech(speech)
            .withReprompt(speech)

        return SayReaction.create(text)
    }

    override fun audio(url: String): AudioReaction {
        return playAudio(url)
    }

    fun playAudio(
        url: String,
        token: String = url,
        behavior: PlayBehavior = PlayBehavior.REPLACE_ALL,
        offsetInMillis: Long = 0,
        previousToken: String = "",
        art: Image? = null,
        background: Image? = null,
        title: String? = null,
        subtitle: String? = null
    ): AudioReaction {

        val stream = Stream.builder()
            .withOffsetInMilliseconds(offsetInMillis)
            .withExpectedPreviousToken(previousToken)
            .withToken(token)
            .withUrl(url)
            .build()

        val meta = AudioItemMetadata.builder()
            .withArt(art)
            .withBackgroundImage(background)
            .withTitle(title)
            .withSubtitle(subtitle)
            .build()

        val audioItem = AudioItem.builder()
            .withStream(stream)
            .withMetadata(meta)
            .build()

        val playDirective = PlayDirective.builder()
            .withPlayBehavior(behavior)
            .withAudioItem(audioItem)
            .build()

        response.builder
            .addDirective(playDirective)
            .withShouldEndSession(true)

        return AudioReaction.create(url)
    }

    fun stopAudioPlayer() {
        response.builder.addAudioPlayerStopDirective()
    }

    fun endSession(text: String? = null) {
        text?.let { say(it) }

        response.builder
            .withShouldEndSession(true)
    }

    fun sendProgressiveResponse(text: String) = sendDirective(
        SpeakDirective.builder().withSpeech(text).build()
    )

    fun sendAPIResponse(data: Map<String, Any?>) {
        response.builder.withApiResponse(data)
    }

    fun sendDirective(directive: Directive) {
        val header = Header.builder()
            .withRequestId(handlerInput.requestEnvelope.request.requestId)
            .build()

        val directiveRequest = SendDirectiveRequest.builder()
            .withDirective(directive)
            .withHeader(header).build()

        directiveServiceClient.enqueue(directiveRequest)
    }
}