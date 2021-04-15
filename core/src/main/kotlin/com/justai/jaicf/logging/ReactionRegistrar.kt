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

    fun AudioReaction.Companion.create(audioUrl: String) =
        AudioReaction(audioUrl, currentState).register()

    fun ButtonsReaction.Companion.create(buttons: List<String>) =
        ButtonsReaction(buttons, currentState).register()

    fun CarouselReaction.Companion.create(text: String, elements: List<CarouselReaction.Element>) =
        CarouselReaction(text, elements, currentState).register()

    fun GoReaction.Companion.create(path: String) =
        GoReaction(path, currentState).register()

    fun ChangeStateReaction.Companion.create(path: String) =
        ChangeStateReaction(path, currentState).register()

    fun <T : Reaction> T.register() = apply {
        registerReaction(this)
    }
}

val ReactionRegistrar.currentState
    get() = botContext.dialogContext.currentState
