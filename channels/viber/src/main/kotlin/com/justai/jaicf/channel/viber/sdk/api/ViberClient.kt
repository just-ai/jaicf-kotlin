package com.justai.jaicf.channel.viber.sdk.api

import com.fasterxml.jackson.module.kotlin.readValue
import com.justai.jaicf.channel.viber.sdk.api.request.ApiRequest
import com.justai.jaicf.channel.viber.sdk.api.request.ApiResponse
import com.justai.jaicf.channel.viber.sdk.viberObjectMapper

open class ViberClient(val apiUrl: String, val httpClient: ViberHttpClient) {

    inline fun <T : ApiRequest, reified R : ApiResponse> sendRequest(
        endpoint: ViberHttpEndpoint,
        apiRequest: T,
        authToken: String
    ): R {
        val json = viberObjectMapper.writeValueAsString(apiRequest)
        val responseBody: String = httpClient.post(
            url = "$apiUrl${endpoint.uri}",
            requestBody = json,
            mapOf(
                VIBER_AUTH_TOKEN_HEADER to authToken,
                CONTENT_TYPE_HEADER_FIELD to "application/json"
            )
        )

        if (responseFailed(responseBody)) {
            throw ApiException(viberObjectMapper.readValue(responseBody))
        }
        return viberObjectMapper.readValue(responseBody)
    }

    fun responseFailed(responseBody: String) =
        viberObjectMapper.readValue<ApiResponse>(responseBody).status != 0

    companion object {
        const val VIBER_AUTH_TOKEN_HEADER = "X-Viber-Auth-Token"
        const val CONTENT_TYPE_HEADER_FIELD = "Content-Type"
    }
}

enum class ViberHttpEndpoint(val uri: String) {
    SET_WEBHOOK("/set_webhook"),
    SEND_MESSAGE("/send_message"),
    GET_ACCOUNT_INFO("/get_account_info"),
    GET_USER_DETAILS("/get_user_details"),
    GET_ONLINE_STATUS("/get_online");
}
