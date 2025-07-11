package com.justai.jaicf.activator.llm.tool.http

import com.justai.jaicf.activator.llm.LLMActivatorAPI
import io.ktor.client.*
import io.ktor.http.*

private val DefaultRequestBuilder: RequestBuilder<*> = { context ->
    method = HttpMethod.Get
    fillParameters(context)
}

fun <T> HttpClient.httpGet(
    url: String,
    requestBuilder: RequestBuilder<T> = {},
    responseBuilder: ResponseBuilder<T> = DefaultResponseBuilder
) = httpRequest(url, DefaultRequestBuilder + requestBuilder, responseBuilder)

fun <T> httpGet(
    url: String,
    requestBuilder: RequestBuilder<T> = {},
    responseBuilder: ResponseBuilder<T> = DefaultResponseBuilder
) = LLMActivatorAPI.get.httpClient.httpGet(url, requestBuilder, responseBuilder)