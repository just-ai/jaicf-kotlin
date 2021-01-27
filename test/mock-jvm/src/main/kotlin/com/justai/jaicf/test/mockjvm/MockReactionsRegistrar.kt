package com.justai.jaicf.test.mockjvm

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.logging.*
import com.justai.jaicf.model.state.StatePath
import com.justai.jaicf.reactions.Reactions
import io.mockk.Invocation

class MockReactionsRegistrar(
    private val reactions: Reactions
) : ReactionRegistrar {

    override var botContext: BotContext
        get() = reactions.botContext
        set(value) {}

    override var executionContext: ExecutionContext
        get() = reactions.executionContext
        set(value) {}

    internal val BotContext.currentStatePath
        get() = StatePath.parse(dialogContext.currentState)

    internal fun say(invocation: Invocation) =
        SayReaction.create(invocation.getFirstArgAsString())

    internal fun buttons(invocation: Invocation) =
        ButtonsReaction.create(invocation.getFirstArgAsList())

    internal fun image(invocation: Invocation) =
        ImageReaction.create(invocation.getFirstArgAsString())

    internal fun audio(invocation: Invocation) =
        AudioReaction.create(invocation.getFirstArgAsString())

    internal fun go(invocation: Invocation): GoReaction {
        val path = invocation.getFirstArgAsString()
        val dialogContext = botContext.dialogContext
        val currentState = botContext.currentStatePath
        val resolved = currentState.resolve(path).toString()
        dialogContext.nextState = resolved
        invocation.getSecondArg<String?>()?.let { callback ->
            dialogContext.backStateStack.push(currentState.resolve(callback).toString())
        }
        return GoReaction.create(resolved)
    }

    internal fun changeState(invocation: Invocation): ChangeStateReaction {
        val path = invocation.getFirstArgAsString()
        val dialogContext = botContext.dialogContext
        val currentState = StatePath.parse(dialogContext.currentState)
        val resolved = currentState.resolve(path).toString()
        dialogContext.nextContext = resolved
        invocation.getSecondArg<String?>()?.let { callback ->
            dialogContext.backStateStack.push(currentState.resolve(callback).toString())
        }
        return ChangeStateReaction.create(resolved)
    }

    internal fun goBack(invocation: Invocation): String? {
        val dialogContext = botContext.dialogContext
        val state = dialogContext.backStateStack.poll()
        invocation.getFirstArg<Any?>()?.let { botContext.result = it }
        return state
    }

    internal fun changeStateBack(invocation: Invocation): String? {
        val dialogContext = botContext.dialogContext
        val state = dialogContext.backStateStack.poll()
        invocation.getFirstArg<Any?>()?.let { botContext.result = it }
        return state
    }
}


@Suppress("UNCHECKED_CAST")
private fun Invocation.getFirstArgAsList(): List<String> = (this.args.first() as Array<out String>).toList()

@Suppress("UNCHECKED_CAST")
private fun Invocation.getFirstArgAsString(): String = this.args.first() as String

@Suppress("UNCHECKED_CAST")
private inline fun <reified T> Invocation.getSecondArg(): T? = this.args[1] as? T

@Suppress("UNCHECKED_CAST")
private inline fun <reified T> Invocation.getFirstArg(): T? = this.args[0] as? T