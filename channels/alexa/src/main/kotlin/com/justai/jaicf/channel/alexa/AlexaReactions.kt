package com.justai.jaicf.channel.alexa

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.model.interfaces.alexa.presentation.apl.RenderDocumentDirective
import com.amazon.ask.model.interfaces.audioplayer.AudioItem
import com.amazon.ask.model.interfaces.audioplayer.AudioItemMetadata
import com.amazon.ask.model.interfaces.audioplayer.PlayBehavior
import com.amazon.ask.model.interfaces.audioplayer.PlayDirective
import com.amazon.ask.model.interfaces.audioplayer.Stream
import com.amazon.ask.model.interfaces.display.Image
import com.amazon.ask.model.services.DefaultApiConfiguration
import com.amazon.ask.model.services.directive.Directive
import com.amazon.ask.model.services.directive.DirectiveServiceClient
import com.amazon.ask.model.services.directive.Header
import com.amazon.ask.model.services.directive.SendDirectiveRequest
import com.amazon.ask.model.services.directive.SpeakDirective
import com.amazon.ask.request.RequestHelper
import com.amazon.ask.services.ApacheHttpApiClient
import com.amazon.ask.util.JacksonSerializer
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.EndSessionReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.ResponseReactions

val Reactions.alexa
    get() = this as? AlexaReactions

class AlexaReactions(
    override val response: AlexaBotResponse,
    val handlerInput: HandlerInput
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

    fun say(text: String, voice: String) =
        say("<voice name='$voice'>$text</voice>")

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

    fun endSession(text: String): EndSessionReaction {
        say(text)
        return endSession()
    }

    override fun endSession(): EndSessionReaction {
        response.builder.withShouldEndSession(true)
        return EndSessionReaction.create()
    }

    fun sendProgressiveResponse(text: String) = sendDirective(
        SpeakDirective.builder().withSpeech(text).build()
    )

    fun sendAPIResponse(data: Map<String, Any?>) {
        response.builder.withApiResponse(data)
    }

    fun sendDocument(token: String,
                     document: Map<String, Any>,
                     datasources: Map<String, Any> = emptyMap(),
                     sources: Map<String, Any> = emptyMap(),
                     packages: List<Any> = emptyList()
    ) {

        if (RequestHelper.forHandlerInput(handlerInput).supportedInterfaces.alexaPresentationAPL != null) {
            val directive = RenderDocumentDirective.builder()
                .withToken(token)
                .withDocument(document)
                .withDatasources(datasources)
                .withSources(sources)
                .withPackages(packages)
                .build()

            response.builder.addDirective(directive)
        }
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
