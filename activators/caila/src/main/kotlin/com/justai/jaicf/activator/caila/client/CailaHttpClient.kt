package com.justai.jaicf.activator.caila.client

import com.justai.jaicf.activator.caila.dto.CailaInferenceResults
import com.justai.jaicf.activator.caila.dto.EntitiesLookupResults
import com.justai.jaicf.helpers.http.toUrl

interface CailaHttpClient {
    val url: String
    val accessToken: String

    fun simpleInference(query: String): CailaInferenceResults?
    fun entitiesLookup(query: String): EntitiesLookupResults?
}

val CailaHttpClient.inferenceUrl: String
    get() = "$url/$accessToken/nlu/inference".toUrl()

val CailaHttpClient.entitiesLookupUrl: String
    get() = "$url/$accessToken/nlu/entities".toUrl()
