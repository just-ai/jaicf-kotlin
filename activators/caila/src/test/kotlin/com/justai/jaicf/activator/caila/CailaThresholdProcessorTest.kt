package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.caila.dto.CailaInferenceResultData
import com.justai.jaicf.activator.caila.dto.CailaIntentData
import com.justai.jaicf.activator.caila.dto.InferenceResultWeights
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CailaThresholdProcessorTest {

    @ParameterizedTest
    @MethodSource("provideApplyThresholdsTestCaseArguments")
    fun applyThresholdsTest(
        caseName: String,
        expectedConfidence: Double?,  // if null - no result expected
        intentConfidence: Double?,
        intentPatternsScore: Double?,
        intentPhrasesScore: Double?,
        patternsThreshold: Double?,
        phrasesThreshold: Double?
    ) {
        val settings: CailaNLUSettings = createTestConfiguration(null, patternsThreshold, phrasesThreshold)
        val inferenceResult = createTestInferenceResult(intentConfidence, intentPatternsScore, intentPhrasesScore)

        val actualResult = CailaThresholdProcessor(settings).applyThresholds(inferenceResult)

        if (expectedConfidence == null) {
            assertNull(actualResult)
        } else {
            assertEquals(expectedConfidence, actualResult!!.confidence, "Error in case: $caseName")
        }
    }

    companion object {
        private fun createTestConfiguration(
            confidenceThreshold: Double?,
            patternsThreshold: Double?,
            phrasesThreshold: Double?
        ): CailaNLUSettings {
            var settings = CailaNLUSettings(accessToken = "fakeToken")
            if (confidenceThreshold != null) {
                settings = settings.copy(confidenceThreshold = confidenceThreshold)
            }
            if (patternsThreshold != null || phrasesThreshold != null) {
                settings = settings.copy(intentThresholds = IntentThresholds(phrasesThreshold, patternsThreshold))
            }
            return settings
        }

        private fun createTestInferenceResult(
            intentConfidence: Double?,
            intentPatternsScore: Double?,
            intentPhrasesScore: Double?
        ): CailaInferenceResultData {
            var inferenceResult = CailaInferenceResultData(
                intent = CailaIntentData(
                    id = 0L,
                    path = "fakeIntentPath",
                    answer = null,
                    customData = null,
                    slots = null
                ),
                confidence = (intentConfidence ?: 0.0),
                slots = null,
                debug = null
            )
            if (intentPatternsScore != null || intentPhrasesScore != null) {
                inferenceResult = inferenceResult.copy(
                    weights = InferenceResultWeights(intentPatternsScore ?: 0.0, intentPhrasesScore ?: 0.0)
                )
            }
            return inferenceResult
        }

        @JvmStatic
        fun provideApplyThresholdsTestCaseArguments() =
            listOf(
                Arguments.of(
                    "no result expected - zero confidence result",
                    null, 0.0, null, null, null, null
                ),
                Arguments.of(
                    "no weights. Phrases threshold should be used. Intent should not be filtered out",
                    0.33, 0.33, null, null, 0.4, 0.3
                ),
                Arguments.of(
                    "no weights. Phrases threshold should be used. Intent should be filtered out",
                    null, 0.33, null, null, 0.3, 0.4
                ),
                Arguments.of(
                    "Zero pattern weight. Phrases weight higher that threshold",
                    0.5, 0.5, 0.0, 0.5, 0.2, 0.2
                ),
                Arguments.of(
                    "Zero pattern weight. Phrases weight lower that threshold",
                    null, 0.5, 0.0, 0.5, 0.2, 0.9
                ),
                Arguments.of(
                    "Zero phrases weight. Pattern weight higher that threshold",
                    0.5, 0.5, 0.5, 0.0, 0.2, 0.2
                ),
                Arguments.of(
                    "Zero phrases weight. Pattern weight lower that threshold",
                    null, 0.5, 0.5, 0.0, 0.9, 0.2
                ),
                Arguments.of(
                    "Has all weights. No thresholds applied",
                    0.5, 0.5, 0.5, 0.3, 0.2, 0.2
                ),
                Arguments.of(
                    "Has all weights. Lower phrases score threshold applied",
                    0.5, 0.5, 0.5, 0.3, 0.2, 0.4
                ),
                Arguments.of(
                    "Has all weights. Lower patterns score threshold applied",
                    0.5, 0.5, 0.3, 0.5, 0.4, 0.2
                ),
                Arguments.of(
                    "Has all weights. Higher phrases score threshold applied",
                    0.3, 0.5, 0.3, 0.5, 0.2, 0.6
                ),
                Arguments.of(
                    "Has all weights. Higher patterns score threshold applied",
                    0.3, 0.5, 0.5, 0.3, 0.6, 0.2
                ),
                Arguments.of(
                    "Has all weights. All threshold applied",
                    null, 0.5, 0.3, 0.5, 0.4, 0.6
                )
            )
    }
}