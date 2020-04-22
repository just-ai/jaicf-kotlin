package com.justai.jaicf.activator.caila.client

import com.justai.jaicf.activator.caila.dto.CailaInferenceResults
import com.justai.jaicf.activator.caila.dto.EntitiesLookupResults

interface CailaHttpClient {
    fun simpleInference(query: String): CailaInferenceResults?
    fun entitiesLookup(query: String): EntitiesLookupResults?
}