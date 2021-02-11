package com.justai.jaicf.channel.invocationapi

import io.ktor.request.*
import io.ktor.util.*
import javax.servlet.http.HttpServletRequest


/**
 * @property clientId a recipient, chat or channel identifier from a concrete channel implementation.
 * @property input query or event sent to invoke channel activation.
 * @property type a [InvocationRequestType] of [InvocationRequest].
 * */
internal class InvocationQueryParams(queryParamsMap: Map<String, List<String>>) {
    private val event: String? = queryParamsMap["event"]?.firstOrNull()
    private val query: String? = queryParamsMap["query"]?.firstOrNull()

    constructor(request: ApplicationRequest) : this(request.queryParameters.toMap())

    @Suppress("UNCHECKED_CAST")
    constructor(request: HttpServletRequest) : this(request.parameterMap.map { (k, v) ->
        (k as String) to (v as Array<out String>).toList()
    }.toMap())

    val type: InvocationRequestType = when {
        event != null -> InvocationRequestType.EVENT
        query != null -> InvocationRequestType.QUERY
        else -> error("event or query must be specified in query parameters")
    }

    val input = when (type) {
        InvocationRequestType.EVENT -> requireNotNull(event)
        InvocationRequestType.QUERY -> requireNotNull(query)
    }

    val clientId: String = requireNotNull(queryParamsMap["clientId"]?.firstOrNull()) {
        "clientId path variable must be specified for gateway call"
    }
}

/**
 * Type of invocation request
 * */
internal enum class InvocationRequestType {
    EVENT, QUERY;
}