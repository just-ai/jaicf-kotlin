package com.justai.jaicf.channel.viber.sdk.api

import io.ktor.client.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class ViberKtorClient(private val logLevel: LogLevel = LogLevel.INFO) : ViberHttpClient {

    private val httpClient = HttpClient {
        Logging(Logger.DEFAULT, logLevel)
    }

    override fun post(url: String, requestBody: String, headers: Map<String, String>) =
        runBlocking(Dispatchers.IO) {
            doPost(url, requestBody, headers)
        }

    private suspend fun doPost(
        url: String,
        requestBody: String,
        headers: Map<String, String>
    ): String {

        val response: HttpResponse = httpClient.post(url) {
            headers.forEach { header(it.key, it.value) }
            body = requestBody
        }

        return response.content.toInputStream().bufferedReader().use { it.readText() }
    }
}
