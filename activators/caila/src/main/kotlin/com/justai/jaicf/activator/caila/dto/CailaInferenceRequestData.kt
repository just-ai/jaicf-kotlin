package com.justai.jaicf.activator.caila.dto

import kotlinx.serialization.Serializable

@Serializable
data class CailaAnalyzeRequestData(
    val data: CailaInferenceRequestData,
    val showAll: Boolean
) : java.io.Serializable {
    companion object {
        private const val serialVersionUID = 9073984233860769077L
    }
}

@Serializable
data class CailaInferenceRequestData(
    val phrase: CailaPhraseMarkupData,
    val knownSlots: List<CailaKnownSlotData>,
    val nBest: Int,
    val showDebugInfo: Boolean = false
) : java.io.Serializable {
    companion object {
        private const val serialVersionUID = 5610919149644712850L
    }
}

@Serializable
data class CailaPhraseMarkupData(
    val text: String,
    var entities: MutableList<CailaEntityMarkupData>
) : java.io.Serializable {
    companion object {
        private const val serialVersionUID = -6194136165197848082L
    }
}
