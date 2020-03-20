package com.justai.jaicf.channel.yandexalice.api

import com.justai.jaicf.channel.yandexalice.api.storage.Image
import com.justai.jaicf.channel.yandexalice.api.storage.Images
import com.justai.jaicf.channel.yandexalice.api.storage.UploadedImage
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.JsonObject

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
            serializer = KotlinxSerializer(Json.nonstrict)
        }

        defaultRequest {
            header("Authorization", "OAuth $oauthToken")
        }
    }

    init {
        images.putAll(
            imageStorage.getOrPut(skillId) {
                listImages().map {it.origUrl to it.id}.toMap().toMutableMap()
            }
        )
    }

    fun getImageId(url: String) = images.getOrPut(url) { uploadImage(url).id }

    fun uploadImage(url: String): Image = runBlocking {
        client.post<UploadedImage>("$URL/skills/$skillId/images") {
            contentType(ContentType.Application.Json)
            body = JsonObject(mapOf("url" to JsonLiteral(url)))
        }.image
    }

    fun listImages(): List<Image> = runBlocking {
        client.get<Images>("$URL/skills/$skillId/images").images
    }
}