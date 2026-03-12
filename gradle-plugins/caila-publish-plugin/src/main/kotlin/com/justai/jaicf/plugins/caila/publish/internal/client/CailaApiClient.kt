package com.justai.jaicf.plugins.caila.publish.internal.client

import com.justai.jaicf.plugins.caila.publish.model.GetImageResponse
import com.justai.jaicf.plugins.caila.publish.model.GetModelResponse
import com.justai.jaicf.plugins.caila.publish.model.PublishImageRequestDto
import com.justai.jaicf.plugins.caila.publish.model.PublishImageResponseDto
import com.justai.jaicf.plugins.caila.publish.model.PublishModelRequestDto
import com.justai.jaicf.plugins.caila.publish.model.S3CredentialsResponseDto
import com.justai.jaicf.plugins.caila.publish.model.WizardPublishModelRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking

class CailaApiClient(
    private val accessToken: String,
    val baseUrl: String,
    val httpClient: HttpClient,
) {
    suspend fun publishImage(accountId: Int, body: PublishImageRequestDto): PublishImageResponseDto {
        return httpClient.post("$baseUrl/account/$accountId/image") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    fun publishImageBlocking(accountId: Int, body: PublishImageRequestDto): PublishImageResponseDto {
        return runBlocking {
            publishImage(accountId, body)
        }
    }

    suspend fun publishModel(accountId: Int, body: PublishModelRequestDto) {
        httpClient.post("$baseUrl/account/$accountId/model") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    fun publishModelBlocking(accountId: Int, body: PublishModelRequestDto) {
        runBlocking {
            publishModel(accountId, body)
        }
    }

    suspend fun publishModelWizard(accountId: Int, body: WizardPublishModelRequestDto) {
        httpClient.post("$baseUrl/wizard/account/$accountId/model") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    fun publishModelWizardBlocking(accountId: Int, body: WizardPublishModelRequestDto) {
        runBlocking {
            publishModelWizard(accountId, body)
        }
    }

    fun getImagesBlocking(accountId: Int): GetImageResponse {
        return runBlocking {
            httpClient.get("$baseUrl/account/$accountId/image-v2") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                }
                contentType(ContentType.Application.Json)
            }.body()
        }
    }

    fun deleteImageBlocking(accountId: Int, imageId: Int) {
        return runBlocking {
            httpClient.delete("$baseUrl/account/$accountId/image/$imageId") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                }
                contentType(ContentType.Application.Json)
            }
        }
    }

    fun deleteModelBlocking(accountId: Int, modelId: Int) {
        return runBlocking {
            httpClient.delete("$baseUrl/account/$accountId/model/$modelId") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                }
                contentType(ContentType.Application.Json)
            }
        }
    }

    fun getModelBlocking(accountId: Int): GetModelResponse {
        return runBlocking {
            httpClient.get("$baseUrl/account/$accountId/model") {
                parameter("onlyMy", true)
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                }
                contentType(ContentType.Application.Json)
            }.body()
        }
    }

    /**
     * Fetches S3 credentials for the account.
     * Returns null if credentials are not available or if the request fails.
     */
    suspend fun getS3Credentials(accountId: Int): S3CredentialsResponseDto {
        return httpClient.get("$baseUrl/account/$accountId/s3") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            contentType(ContentType.Application.Json)
        }.body()
    }

    /**
     * Fetches S3 credentials for the account (blocking version).
     * Returns null if credentials are not available or if the request fails.
     * This is an optional feature - if it fails, the plugin continues without S3.
     */
    fun getS3CredentialsBlocking(accountId: Int): S3CredentialsResponseDto? {
        return runBlocking {
            try {
                getS3Credentials(accountId)
            } catch (e: Exception) {
                null
            }
        }
    }
}
