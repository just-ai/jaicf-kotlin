package com.justai.jaicf.activator.llm.tool.http

import com.justai.jaicf.activator.llm.LLMActivatorAPI
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod

private val DefaultRequestBuilder: RequestBuilder<*> = { context ->
    method = HttpMethod.Patch
    fillJsonBody(context)
}

fun <T> HttpClient.httpPatch(
    url: String,
    requestBuilder: RequestBuilder<T> = {},
    responseBuilder: ResponseBuilder<T> = DefaultResponseBuilder
) = httpRequest(url, DefaultRequestBuilder + requestBuilder, responseBuilder)

fun <T> httpPatch(
    url: String,
    requestBuilder: RequestBuilder<T> = {},
    responseBuilder: ResponseBuilder<T> = DefaultResponseBuilder
) = LLMActivatorAPI.get.httpClient.httpPatch(url, requestBuilder, responseBuilder)