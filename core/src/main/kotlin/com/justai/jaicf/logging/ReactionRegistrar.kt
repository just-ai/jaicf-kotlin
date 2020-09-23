package com.justai.jaicf.logging

import com.justai.jaicf.context.BotContext
import java.rmi.registry.Registry

/**
 * Reactions logging methods holder.
 * */
abstract class ReactionRegistrar {

    abstract var botContext: BotContext

    abstract var loggingContext: LoggingContext

    protected fun registerReaction(reaction: Reaction) {
        loggingContext.reactions.add(reaction)
    }

    protected fun SayReaction.Companion.createAndRegister(text: String) =
        SayReaction(text, currentState).register()

    protected fun ImageReaction.Companion.createAndRegister(imageUrl: String) =
        ImageReaction(imageUrl, currentState).register()

    protected fun AudioReaction.Companion.createAndRegister(audioUrl: String) =
        AudioReaction(audioUrl, currentState).register()

    protected fun ButtonsReaction.Companion.createAndRegister(buttons: List<String>) =
        ButtonsReaction(buttons, currentState).register()

    protected fun GoReaction.Companion.createAndRegister(path: String) =
        GoReaction(path, currentState).register()

    protected fun ChangeStateReaction.Companion.createAndRegister(path: String) =
        ChangeStateReaction(path, currentState).register()

    protected fun <T : Reaction> T.register() = apply {
        registerReaction(this)
    }

    protected val currentState: String
        get() = botContext.dialogContext.currentState
}