package com.justai.jaicf.test.reactions


import com.justai.jaicf.context.ExecutionContext
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.Reactions

/**
 * A simple [Reactions] implementation which stores all reaction to [ExecutionContext].
 */
class TestReactions : Reactions() {

    internal val answer
        get() = executionContext.reactions.filterIsInstance<SayReaction>()
            .joinToString(separator = "\n") { it.text }
            .trimIndent()

    internal val replies
        get() = executionContext.reactions.filterIsInstance<SayReaction>().map { it.text }

    override fun say(text: String): SayReaction {
        return SayReaction.create(text)
    }

    override fun image(url: String) = super.image(url).register()

    override fun buttons(vararg buttons: String) = super.buttons(*buttons).register()

    override fun audio(url: String) = super.audio(url).register()
}

val Reactions.test
    get() = this as? TestReactions

internal val Reactions.answer: String
    get() = executionContext.reactions.filterIsInstance<SayReaction>()
        .joinToString(separator = "\n") { it.text }
        .trimIndent()

internal val Reactions.replies: List<String>
    get() = executionContext.reactions.filterIsInstance<SayReaction>().map { it.text }
