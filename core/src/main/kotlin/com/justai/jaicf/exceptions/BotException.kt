package com.justai.jaicf.exceptions

import com.justai.jaicf.activator.Activator

/**
 * Base class for all exception thrown from BotEngine
 * */
abstract class BotException : RuntimeException() {
    abstract val currentState: String
}

/**
 * An exception thrown when transition from [currentState] to [targetState] failed because [targetState] does not exist.
 * */
data class NoStateFoundException(
    override val currentState: String,
    val targetState: String
) : BotException() {

    override val message: String = "No transition possible found from state $currentState to $targetState"
}

/**
 * A runtime exception thrown from BotEngine.
 * */
open class BotExecutionException(
    open val exception: Exception,
    override val currentState: String
) : BotException()

/**
 * A runtime exception thrown during activation
 * */
data class ActivationException(
    override val exception: Exception,
    override val currentState: String,
    val activator: Activator
) : BotExecutionException(exception, currentState) {

    override val message: String =
        "Failed to execute activation from state $currentState with activator ${activator.name}"
}

/**
 * A runtime exception thrown from action-block
 * */
data class ActionException(
    override val exception: Exception,
    override val currentState: String,
) : BotExecutionException(exception, currentState) {

    override val message: String = "Failed to execute action on state $currentState"
}

val BotException.scenarioCause: Exception
    get() = (this as? BotExecutionException)?.exception ?: this