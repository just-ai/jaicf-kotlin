package com.justai.jaicf.channel.viber.sdk.api

interface ViberHttpClient {
    fun post(url: String, requestBody: String, vararg headers: Pair<String, String>): String
}
