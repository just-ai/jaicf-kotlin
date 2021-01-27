package com.justai.jaicf.channel

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.test.reactions.TestReactions
import java.util.*

/**
 * A simple implementation of [BotChannel] that receives raw text requests from the console and prints a text responses back.
 * Supports only a [TestResponse].
 * Creates [TestReactions] instance for every request.
 *
 * @param botApi a bot engine
 *
 * @see [BotApi]
 * @see [TestReactions]
 */
class ConsoleChannel(override val botApi: BotApi) : BotChannel {

    private val clientId = UUID.randomUUID().toString()

    /**
     * Starts to receive requests from console
     *
     * @param startMessage an optional message that should be printed on startup
     */
    fun run(startMessage: String? = null) {
        startMessage?.let { process(startMessage) }
        while (true) {
            print("> ")
            val input = readLine()

            if (input.isNullOrBlank()) {
                continue
            }

            process(input)
        }
    }

    private fun process(input: String) {
        execute(input).replies.forEach { reply ->
            print("< ")
            println(reply)
        }
    }

    private fun execute(text: String): TestReactions {
        val request = QueryBotRequest(clientId, text)
        val reactions = TestReactions()

        botApi.process(request, reactions, RequestContext.DEFAULT)
        return reactions
    }
}