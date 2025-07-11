package com.justai.jaicf.activator.llm.tool.http

import com.justai.jaicf.activator.llm.LLMActivatorAPI
import io.ktor.client.*
import io.ktor.http.*

private val DefaultRequestBuilder: RequestBuilder<*> = { context ->
    method = HttpMethod.Put
    fillJsonBody(context)
}

fun <T> HttpClient.httpPut(
    url: String,
    requestBuilder: RequestBuilder<T> = {},
    responseBuilder: ResponseBuilder<T> = DefaultResponseBuilder
) = httpRequest(url, DefaultRequestBuilder + requestBuilder, responseBuilder)

fun <T> httpPut(
    url: String,
    requestBuilder: RequestBuilder<T> = {},
    responseBuilder: ResponseBuilder<T> = DefaultResponseBuilder
) = LLMActivatorAPI.get.httpClient.httpPut(url, requestBuilder, responseBuilder)