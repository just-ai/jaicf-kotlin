package com.justai.jaicf.api

/**
 * A base interface for every response from JAICF bot.
 */
interface BotResponse

/**
 * Simple response with raw text
 *
 * @property text a text of the response
 */
open class TextResponse(
    var text: String? = null
) : BotResponse