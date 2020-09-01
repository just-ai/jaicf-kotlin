package com.justai.jaicf.context

import com.justai.jaicf.channel.http.HttpBotRequest

/**
 * An additional details of request.
 * Every channel could create this class instance parsing some request's meta-data.
 */
open class RequestContext(
    /**
     * Indicates if there is a new session with user.
     * Bot engine cleans all session data from [BotContext] if this flag is set.
     */
    open val newSession: Boolean,
    open val httpBotRequest: HttpBotRequest?
) {
    companion object Factory {
        val DEFAULT: RequestContext
            get() = RequestContext(newSession = false, httpBotRequest = null)

        fun fromHttp(httpBotRequest: HttpBotRequest) = RequestContext(newSession = false, httpBotRequest = httpBotRequest)
    }
}