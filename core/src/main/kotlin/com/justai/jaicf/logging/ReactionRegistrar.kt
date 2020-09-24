package com.justai.jaicf.logging

import com.justai.jaicf.context.BotContext

/**
 * Reactions logging methods holder.
 * */
abstract class ReactionRegistrar {

    abstract var botContext: BotContext

    abstract var loggingContext: LoggingContext

    protected fun registerReaction(reaction: Reaction) {
        loggingContext.reactions.add(reaction)
    }

    protected fun SayReaction.Companion.create(text: String) =
        SayReaction(text, currentState).register()

    protected fun ImageReaction.Companion.create(imageUrl: String) =
        ImageReaction(imageUrl, currentState).register()

    protected fun AudioReaction.Companion.create(audioUrl: String) =
        AudioReaction(audioUrl, currentState).register()

    protected fun ButtonsReaction.Companion.create(buttons: List<String>) =
        ButtonsReaction(buttons, currentState).register()

    protected fun GoReaction.Companion.create(path: String) =
        GoReaction(path, currentState).register()

    protected fun ChangeStateReaction.Companion.create(path: String) =
        ChangeStateReaction(path, currentState).register()

    protected fun <T : Reaction> T.register() = apply {
        registerReaction(this)
    }

    protected val currentState: String
        get() = botContext.dialogContext.currentState
}