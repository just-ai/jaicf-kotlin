package com.justai.jaicf.activator.caila.client

import com.justai.jaicf.activator.caila.dto.CailaAnalyzeResponseData
import com.justai.jaicf.activator.caila.dto.CailaEntitiesLookupResults
import com.justai.jaicf.activator.caila.dto.CailaInferenceResultData
import com.justai.jaicf.helpers.http.toUrl

interface CailaHttpClient {
    val url: String
    val accessToken: String
    val inferenceNBest: Int

    fun simpleInference(query: String): CailaInferenceResultData?

    fun entitiesLookup(query: String): CailaEntitiesLookupResults?

    fun analyze(query: String): CailaAnalyzeResponseData?
}

val CailaHttpClient.inferenceUrl: String
    get() = "$url/$accessToken/nlu/inference".toUrl()

val CailaHttpClient.entitiesLookupUrl: String
    get() = "$url/$accessToken/nlu/entities".toUrl()

val CailaHttpClient.analyzeUrl: String
    get() = "$url/$accessToken/nlu/analyze".toUrl()
