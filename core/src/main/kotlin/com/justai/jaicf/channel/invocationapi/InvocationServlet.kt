package com.justai.jaicf.channel.invocationapi

import com.justai.jaicf.helpers.logging.WithLogger
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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
        req?.run { channel.processExternalInvocation(req) }
    }

    override fun doGet(req: HttpServletRequest?, resp: HttpServletResponse?) {
        req?.run { channel.processExternalInvocation(req) }
    }
}

/**
 * Processes invocation request from [InvocationServlet]
 * */
private fun InvocableBotChannel.processExternalInvocation(req: HttpServletRequest) =
    processExternalInvocation(InvocationQueryParams(req), req.inputStream.bufferedReader().readText())
