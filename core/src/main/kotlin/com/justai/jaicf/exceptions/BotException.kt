package com.justai.jaicf.exceptions

import com.justai.jaicf.activator.Activator

abstract class BotException : RuntimeException() {
    abstract val currentState: String
}

data class NoStateFoundException(
    override val currentState: String,
    val targetState: String
) : BotException() {

    override val message: String = "No transition possible found from state $currentState to $targetState"
}

open class BotExecutionException(
    open val exception: Throwable,
    override val currentState: String
) : BotException()

data class ActivationException(
    override val exception: Exception,
    override val currentState: String,
    val activator: Activator
) : BotExecutionException(exception, currentState) {

    override val message: String =
        "Failed to execute activation from state $currentState with activator ${activator.name}"
}

data class ActionException(
    override val exception: Throwable,
    override val currentState: String,
) : BotExecutionException(exception, currentState) {

    override val message: String = "Failed to execute action on state $currentState"
}