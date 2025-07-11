package com.justai.jaicf.activator.llm.tool.http

import com.justai.jaicf.activator.llm.LLMActivatorAPI
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

private val DefaultRequestBuilder: RequestBuilder<*> = { context ->
    method = HttpMethod.Post
    setBody(FormDataContent(Parameters.build {
        context.parameters.forEach { (key, value) ->
            append(key, if (value.isTextual) value.asText() else value.toString())
        }
    }))
}

fun <T> HttpClient.httpForm(
    url: String,
    requestBuilder: RequestBuilder<T> = {},
    responseBuilder: ResponseBuilder<T> = DefaultResponseBuilder
) = httpRequest(url, DefaultRequestBuilder + requestBuilder, responseBuilder)

fun <T> httpForm(
    url: String,
    requestBuilder: RequestBuilder<T> = {},
    responseBuilder: ResponseBuilder<T> = DefaultResponseBuilder
) = LLMActivatorAPI.get.httpClient.httpForm(url, requestBuilder, responseBuilder)