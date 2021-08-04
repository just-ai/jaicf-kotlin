package com.justai.jaicf.activator.caila.dto

import kotlinx.serialization.Serializable

@Serializable
data class CailaAnalyzeResponseData(
    val markup: CailaMarkupData,
    val entitiesLookup: CailaPhraseMarkupData,
    val inference: CailaInferenceResultsData
) : java.io.Serializable {
    companion object {
        private const val serialVersionUID = -7944621135486361310L
    }
}

@Serializable
data class CailaInferenceResultsData(
    val phrase: CailaPhraseMarkupData,
    val variants: List<CailaInferenceResultData>
) : java.io.Serializable {
    companion object {
        private const val serialVersionUID = -8632267852430180094L
    }
}

@Serializable
data class CailaMarkupData(
    val source: String,
    val correctedText: String,
    val words: List<CailaWordData>
) : java.io.Serializable {
    companion object {
        private const val serialVersionUID = -2819797379889298421L
    }
}

@Serializable
data class CailaWordData(
    val startPos: Int,
    val endPos: Int,
    val source: String,
    val word: String,
    val punctuation: Boolean
) : java.io.Serializable {
    companion object {
        private const val serialVersionUID = -5443803721832092446L
    }
}
