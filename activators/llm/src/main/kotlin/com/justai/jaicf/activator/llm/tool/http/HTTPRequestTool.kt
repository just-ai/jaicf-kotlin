package com.justai.jaicf.activator.llm.tool.http

import com.fasterxml.jackson.databind.node.ObjectNode
import com.justai.jaicf.activator.llm.LLMActivatorAPI
import com.justai.jaicf.activator.llm.tool.LLMTool
import com.justai.jaicf.activator.llm.tool.LLMToolCallContext
import com.justai.jaicf.activator.llm.tool.LLMToolFunction
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import java.lang.Thread.sleep
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.sequences.forEach


typealias RequestBuilder<T> = HttpRequestBuilder.(context: LLMToolCallContext<T>) -> Unit
typealias ResponseBuilder<T> = suspend LLMToolCallContext<T>.(response: HttpResponse) -> Any?

internal operator fun <T> RequestBuilder<T>.plus(builder: RequestBuilder<T>): RequestBuilder<T> = {
    invoke(this, it)
    builder.invoke(this, it)
}

private val DefaultRequestBuilder: RequestBuilder<*> = { ctx ->
    url(url.toString().let { url ->
        ctx.parameters.fold(url) { acc, param ->
            acc.replace("{${param.key}}", param.value.takeIf { it.isTextual }?.asText() ?: param.value.toString())
        }.also {
            this.url.parameters.clear()
        }
    })
}

internal val DefaultResponseBuilder: ResponseBuilder<*> = { response ->
    val body = response.bodyAsText()
    if (response.status.isSuccess()) {
        body
    } else {
        throw Exception("${response.status.value} - ${response.status.description}: $body")
    }
}

internal val LLMToolCallContext<*>.parameters
    get() = LLMTool.ArgumentsMapper.convertValue(call.arguments, ObjectNode::class.java)
        .fields().asSequence()
        .filter { it.value != null && !it.value.isNull }
        .toList()

internal fun HttpRequestBuilder.fillParameters(context: LLMToolCallContext<*>) {
    context.parameters.forEach { (key, value) ->
        parameter(key, if (value.isTextual) value.asText() else value)
    }
}

internal fun HttpRequestBuilder.fillJsonBody(context: LLMToolCallContext<*>) {
    contentType(ContentType.Application.Json)
    setBody(context.call.arguments)
}

fun <T> LLMToolCallContext<T>.httpRequest(
    url: String,
    client: HttpClient,
    requestBuilder: RequestBuilder<T>,
    responseBuilder: ResponseBuilder<T> = DefaultResponseBuilder
): LLMToolFunction<T> = {
    runBlocking {
        val response = client.request(url) {
            DefaultRequestBuilder.plus(requestBuilder)(this@httpRequest)
        }
        responseBuilder.invoke(this@httpRequest, response)
    }
}

fun <T> HttpClient.httpRequest(
    url: String,
    requestBuilder: RequestBuilder<T>,
    responseBuilder: ResponseBuilder<T> = DefaultResponseBuilder
): LLMToolFunction<T> = {
    httpRequest(url, this@httpRequest, requestBuilder, responseBuilder).invoke(this)
}

fun <T> httpRequest(
    url: String,
    requestBuilder: RequestBuilder<T>,
    responseBuilder: ResponseBuilder<T> = DefaultResponseBuilder
) = LLMActivatorAPI.get.httpClient.httpRequest(url, requestBuilder, responseBuilder)