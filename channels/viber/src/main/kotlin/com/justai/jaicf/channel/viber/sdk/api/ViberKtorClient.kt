package com.justai.jaicf.channel.viber.sdk.api

import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class ViberKtorClient(private val logLevel: LogLevel = LogLevel.INFO) : ViberHttpClient {

    private val httpClient = HttpClient {
        install(Logging) {
            level = logLevel
        }
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
            setBody(requestBody)
        }

        return response.bodyAsChannel().toInputStream().bufferedReader().use { it.readText() }
    }
}
