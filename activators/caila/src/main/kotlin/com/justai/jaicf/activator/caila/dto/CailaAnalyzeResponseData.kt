package com.justai.jaicf.activator.caila.dto

import kotlinx.serialization.Serializable

@Serializable
data class CailaAnalyzeResponseData(
    val markup: CailaMarkupData,
    val entitiesLookup: CailaPhraseMarkupData,
    val inference: CailaInferenceResultsData
) : java.io.Serializable

@Serializable
data class CailaInferenceResultsData(
    val phrase: CailaPhraseMarkupData,
    val variants: List<CailaInferenceResultData>
) : java.io.Serializable

@Serializable
data class CailaMarkupData(
    val source: String,
    val correctedText: String,
    val words: List<CailaWordData>
) : java.io.Serializable

@Serializable
data class CailaWordData(
    val startPos: Int,
    val endPos: Int,
    val source: String,
    val word: String,
    val punctuation: Boolean
) : java.io.Serializable
