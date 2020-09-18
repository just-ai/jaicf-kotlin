package com.justai.jaicf.logging

import com.justai.jaicf.context.BotContext

/**
 * Reactions logging methods holder.
 * */
abstract class LoggingReactionRegistrar {

    abstract var botContext: BotContext

    internal abstract var loggingContext: LoggingContext

    protected fun registerReaction(reaction: LoggingReaction) {
        loggingContext.reactions.add(reaction)
    }

    protected fun SayReaction.Companion.register(text: String) =
        SayReaction(text, currentState).register()

    protected fun ImageReaction.Companion.register(imageUrl: String) =
        ImageReaction(imageUrl, currentState).register()

    protected fun AudioReaction.Companion.register(audioUrl: String) =
        AudioReaction(audioUrl, currentState).register()

    protected fun ButtonsReaction.Companion.register(buttons: List<String>) =
        ButtonsReaction(buttons, currentState).register()

    protected fun GoReaction.Companion.register(path: String) =
        GoReaction(path, currentState).register()

    protected fun ChangeStateReaction.Companion.register(path: String) =
        ChangeStateReaction(path, currentState).register()

    protected fun LoggingReaction.register() = registerReaction(this)

    protected val currentState: String
        get() = botContext.dialogContext.currentState

}