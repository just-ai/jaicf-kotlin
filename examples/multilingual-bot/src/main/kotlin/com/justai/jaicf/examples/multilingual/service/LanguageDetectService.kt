package com.justai.jaicf.examples.multilingual.service

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.justai.jaicf.examples.multilingual.MainBot
import com.justai.jaicf.examples.multilingual.util.HttpClient
import com.justai.jaicf.examples.multilingual.util.Jackson
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

object LanguageDetectService {
    private const val endpoint = "https://app.jaicp.com/cailapub/api/caila/p/${MainBot.accessToken}/nlu/detectlanguage"

    fun detectLanguage(input: String): SupportedLanguage? = runBlocking {
        try {
            val body = HttpClient.post<String>(endpoint) {
                body = input.toDetectLanguageRequest().stringify()
                contentType(ContentType.parse("application/json"))
            }
            SupportedLanguage.valueOf(Jackson.readValue(body, jacksonTypeRef<List<String>>()).first())
        } catch (e: Exception) {
            null
        }
    }
}

private data class DetectLanguageRequestData(
    val documents: List<String>,
) {
    fun stringify(): String = Jackson.writeValueAsString(this)
}

private fun String.toDetectLanguageRequest() = DetectLanguageRequestData(listOf(this))

enum class SupportedLanguage {
    ru, en;
}
