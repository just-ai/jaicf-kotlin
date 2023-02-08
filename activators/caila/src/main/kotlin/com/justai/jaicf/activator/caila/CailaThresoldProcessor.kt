package com.justai.zb.scenarios.engine

import com.justai.jaicf.activator.caila.CailaNLUSettings
import com.justai.jaicf.activator.caila.dto.CailaInferenceResultData
import kotlin.math.max

class CailaThresholdProcessor(
    settings: CailaNLUSettings
) {
    private val patternThreshold = settings.intentThresholds.patterns ?: settings.confidenceThreshold
    private val phrasesThreshold = settings.intentThresholds.phrases ?: settings.confidenceThreshold

    //  Due to different thresholds for patters and phrases result.confidence can be changed after this method
    //
    //  For example, if intent has patterns score 0.6 and phrases score 0.5, initial confidence will be 0.6.
    //  If pattern threshold >= 0.6 and phrases threshold <= 0.5, intent confidence should change to phrases score
    fun applyThresholds(result: CailaInferenceResultData): CailaInferenceResultData? {
        var patternConfidence = result.weights?.patterns ?: 0.0
        var phrasesConfidence = result.weights?.phrases ?: 0.0
        if (patternConfidence == 0.0 && phrasesConfidence == 0.0) {
            // that means that custom thresholds are not applicable (External NLU most likely), so we need to use confidence as score
            return if (result.confidence > phrasesThreshold) result else null
        }
        var updatedResult = result;
        if (result.weights != null) {
            if (result.weights.patterns < patternThreshold) {
                patternConfidence = 0.0
            }
            if (result.weights.phrases < phrasesThreshold) {
                phrasesConfidence = 0.0
            }
            updatedResult = result.copy(confidence = max(patternConfidence, phrasesConfidence))
        }
        return if (updatedResult.confidence > 0.0) updatedResult else null
    }
}