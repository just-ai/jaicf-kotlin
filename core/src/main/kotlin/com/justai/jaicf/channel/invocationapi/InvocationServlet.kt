package com.justai.jaicf.channel.invocationapi

import com.justai.jaicf.helpers.logging.WithLogger
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * HttpServlet implementation that processes GET/POST requests to trigger request processing in [InvocableBotChannel].
 *
 * A helper extensions for Ktor framework with [InvocableBotChannel] routing.
 *
 * Usage example (with spring boot):
 * ```
 * @Configuration
 * @ServletComponentScan
 * class AppConfiguration {
 *
 *      @WebServlet("/telegram")
 *      class MyInvocationServlet : InvocationServlet(telegramChannel)
 * }
 * ```
 *
 * example requests:
 * curl -X POST {host}/invocation/telegram?clientId={clientId}&event=myEvent -d '{"key": "value"}'
 *
 * @see InvocableBotChannel
 * @see InvocationRequest
 * @see InvocationRequestType
 */
@Suppress("unchecked_cast")
open class InvocationServlet(
    private val channel: InvocableBotChannel
) : HttpServlet(), WithLogger {

    override fun doPost(req: HttpServletRequest?, resp: HttpServletResponse?) {
        req?.run { channel.processInvocation(req) }
    }

    override fun doGet(req: HttpServletRequest?, resp: HttpServletResponse?) {
        req?.run { channel.processInvocation(req) }
    }
}

/**
 * Processes invocation request from [InvocationServlet]
 * */
private fun InvocableBotChannel.processInvocation(req: HttpServletRequest) =
    processInvocation(InvocationQueryParams(req), req.inputStream.bufferedReader().readText())
