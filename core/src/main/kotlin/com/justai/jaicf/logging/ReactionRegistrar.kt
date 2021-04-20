package com.justai.jaicf.logging

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.ExecutionContext

/**
 * Reactions logging methods holder.
 * */
interface ReactionRegistrar {

    var botContext: BotContext

    var executionContext: ExecutionContext

    fun registerReaction(reaction: Reaction) {
        executionContext.reactions.add(reaction)
    }

    fun SayReaction.Companion.create(text: String) =
        SayReaction(text, currentState).register()

    fun ImageReaction.Companion.create(imageUrl: String) =
        ImageReaction(imageUrl, currentState).register()

    fun ButtonsReaction.Companion.create(buttons: List<String>) =
        ButtonsReaction(buttons, currentState).register()

    fun GoReaction.Companion.create(path: String) =
        GoReaction(path, currentState).register()

    fun ChangeStateReaction.Companion.create(path: String) =
        ChangeStateReaction(path, currentState).register()

    fun AudioReaction.Companion.create(audioUrl: String) =
        AudioReaction(audioUrl, currentState).register()

    fun FileReaction.Companion.create(fileUrl: String) =
        FileReaction(fileUrl, currentState).register()

    fun LocationReaction.Companion.create(latitude: Float, longitude: Float) =
        LocationReaction(latitude, longitude, currentState).register()

    fun UrlReaction.Companion.create(url: String) =
        UrlReaction(url, currentState).register()

    fun VideoReaction.Companion.create(videoUrl: String) =
        VideoReaction(videoUrl, currentState).register()

    fun CarouselReaction.Companion.create(text: String, elements: List<CarouselReaction.Element>) =
        CarouselReaction(text, elements, currentState).register()

    fun DocumentReaction.Companion.create(url: String) =
        DocumentReaction(url, currentState).register()

    fun NewSessionReaction.Companion.create() =
        NewSessionReaction(currentState).register()

    fun EndSessionReaction.Companion.create() =
        EndSessionReaction(currentState).register()

    fun <T : Reaction> T.register() = apply {
        registerReaction(this)
    }
}

val ReactionRegistrar.currentState
    get() = botContext.dialogContext.currentState
