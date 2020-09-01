package com.justai.jaicf.channel

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.api.TextResponse
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.reactions.TextReactions
import java.util.*

/**
 * A simple implementation of [BotChannel] that receives raw text requests from the console and prints a text responses back.
 * Supports only a [TextResponse].
 * Creates [TextReactions] instance for every request.
 *
 * @param botApi a bot engine
 *
 * @see [BotApi]
 * @see [TextReactions]
 */
class ConsoleChannel(override val botApi: BotApi): BotChannel {

    private val clientId = UUID.randomUUID().toString()

    /**
     * Starts to receive requests from console
     *
     * @param startMessage an optional message that should be printed on startup
     */
    fun run(startMessage: String? = null) {
        startMessage?.let { process(startMessage) }
        while(true) {
            print("> ")
            val input = readLine()

            if (input.isNullOrBlank()) {
                continue
            }

            process(input)
        }
    }

    private fun process(input: String) {
        execute(input)?.let {
            it.split("\n").forEach { reply ->
                print("< ")
                println(reply)
            }
        }
    }

    private fun execute(text: String): String? {
        val request = QueryBotRequest(clientId, text)
        val reactions = TextReactions(TextResponse())

        botApi.process(request, reactions, RequestContext.DEFAULT)
        return reactions.response.text
    }
}