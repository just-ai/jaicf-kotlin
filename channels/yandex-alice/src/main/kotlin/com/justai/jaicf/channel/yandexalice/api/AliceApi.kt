package com.justai.jaicf.channel.yandexalice.api

import com.justai.jaicf.channel.yandexalice.JSON
import com.justai.jaicf.channel.yandexalice.api.storage.Image
import com.justai.jaicf.channel.yandexalice.api.storage.Images
import com.justai.jaicf.channel.yandexalice.api.storage.UploadedImage
import com.justai.jaicf.helpers.http.withTrailingSlash
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private val client = HttpClient(CIO) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(JSON)
    }

    install(Logging) {
        level = LogLevel.INFO
    }
}

class AliceApi(
    private val oauthToken: String,
    private val skillId: String,
    apiUrl: String,
) {
    private val apiUrl: String = apiUrl.withTrailingSlash(false)

    companion object {
        private val imageStorage = mutableMapOf<String, MutableMap<String, String>>()
    }

    private val images = mutableMapOf<String, String>()

    init {
        images.putAll(
            imageStorage.getOrPut(skillId) {
                listImages().associate { it.origUrl to it.id }.toMutableMap()
            }
        )
    }

    fun getImageId(url: String) = images.getOrPut(url) { uploadImage(url).id }

    internal fun getImageUrl(id: String) = images.entries.find { id == it.value }?.key

    fun uploadImage(url: String): Image = runBlocking {
        client.post("$apiUrl/skills/$skillId/images") {
            contentType(ContentType.Application.Json)
            header("Authorization", "OAuth $oauthToken")
            setBody(JsonObject(mapOf("url" to JsonPrimitive(url))))
        }.body<UploadedImage>().image
    }.also { image ->
        imageStorage[skillId]?.put(image.origUrl, image.id)
    }

    fun listImages(): List<Image> = runBlocking {
        client.get("$apiUrl/skills/$skillId/images") {
            header("Authorization", "OAuth $oauthToken")
        }.body<Images>().images
    }
}
