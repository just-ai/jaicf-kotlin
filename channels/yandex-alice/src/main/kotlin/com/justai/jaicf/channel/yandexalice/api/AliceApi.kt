package com.justai.jaicf.channel.yandexalice.api

import com.justai.jaicf.channel.yandexalice.JSON
import com.justai.jaicf.channel.yandexalice.api.storage.Image
import com.justai.jaicf.channel.yandexalice.api.storage.Images
import com.justai.jaicf.channel.yandexalice.api.storage.UploadedImage
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class AliceApi(
    oauthToken: String,
    private val skillId: String
) {

    companion object {
        private const val URL = "https://dialogs.yandex.net/api/v1"
        private val imageStorage = mutableMapOf<String, MutableMap<String, String>>()
    }

    private val images = mutableMapOf<String, String>()

    private val client = HttpClient(CIO) {
        expectSuccess = true

        install(JsonFeature) {
            serializer = KotlinxSerializer(JSON)
        }

        defaultRequest {
            header("Authorization", "OAuth $oauthToken")
        }
    }

    init {
        images.putAll(
            imageStorage.getOrPut(skillId) {
                listImages().map { it.origUrl to it.id }.toMap().toMutableMap()
            }
        )
    }

    fun getImageId(url: String) = images.getOrPut(url) { uploadImage(url).id }

    fun uploadImage(url: String): Image = runBlocking {
        client.post<UploadedImage>("$URL/skills/$skillId/images") {
            contentType(ContentType.Application.Json)
            body = JsonObject(mapOf("url" to JsonPrimitive(url)))
        }.image
    }.also { image ->
        imageStorage[skillId]?.put(image.origUrl, image.id)
    }

    fun listImages(): List<Image> = runBlocking {
        client.get<Images>("$URL/skills/$skillId/images").images
    }
}