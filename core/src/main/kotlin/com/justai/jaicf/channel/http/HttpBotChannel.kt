package com.justai.jaicf.channel.http

import com.justai.jaicf.channel.BotChannel

/**
 * A base interface for every channel that works through the HTTP.
 * It receives a raw string body and responds with raw string response.
 * The body and response should be de-serialized and serialized accordingly by the particular implementation.
 * The most common use-case for channels that accept JSON encoded body via POST request and return a JSON body in reply.
 * Every implementation could be used through Ktor httpBotRouting extension or [HttpBotChannelServlet]
 */
interface HttpBotChannel: BotChannel {
    /**
     * Processes a channel-related request to the bot and returns channel-related response.
     * This method should de-serialize request and serialize response using some tool according to the expected request and response format.
     *
     * @param request a received HTTP request
     * @return a serialized channel-related response. Should return null if the request cannot be processed.
     * @see HttpBotRequest
     */
    fun process(request: HttpBotRequest): HttpBotResponse
}
