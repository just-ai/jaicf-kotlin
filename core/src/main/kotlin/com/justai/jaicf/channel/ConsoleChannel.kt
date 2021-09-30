package com.justai.jaicf.channel

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.logging.*
import com.justai.jaicf.test.reactions.TestReactions
import java.util.*

/**
 * A simple implementation of [BotChannel] that receives raw text requests from the console and prints a text responses back.
 * Creates [TestReactions] instance for every request.
 *
 * @param botApi a bot engine
 *
 * @see [BotApi]
 * @see [TestReactions]
 */
class ConsoleChannel(override val botApi: BotApi, private val clientId: String = UUID.randomUUID().toString()) : BotChannel {

    /**
     * Starts to receive requests from console
     *
     * @param startMessage an optional message that should be printed on startup
     */
    fun run(startMessage: String? = null) {
        if (!startMessage.isNullOrBlank()) {
            println("> $startMessage")
            process(startMessage)
        }

        while (true) {
            print("> ")
            val input = readLine()
            if (!input.isNullOrBlank()) {
                process(input)
            }
        }
    }

    private fun process(input: String) {
        val request = QueryBotRequest(clientId, input)
        val reactions = TestReactions()

        botApi.process(request, reactions, RequestContext.DEFAULT)

        render(reactions)
    }

    private fun render(reactions: TestReactions) {
        val answer = reactions.executionContext.reactions.joinToString("\n") {
            render(it).joinToString("\n  ", prefix = "< ")
        }

        println() // flush conversation logs
        println(answer)
    }

    private fun render(reaction: Reaction): List<String> = when (reaction) {
        is SayReaction -> reaction.text.split("\n")
        is ImageReaction -> listOf("${reaction.imageUrl} (image)")
        is AudioReaction -> listOf("${reaction.audioUrl} (audio)")
        is ButtonsReaction -> listOf(reaction.buttons.joinToString(" ") { "[$it]" })
        else -> listOf(reaction.toString())
    }
}