package com.justai.jaicf.activator.selection

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.helpers.logging.WithLogger
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.activation.ActivationSelector
import com.justai.jaicf.model.state.StatePath

/**
 * This activation selector calculates penalty for each activation,
 * applies penalties for every activation and selects single activation with highest adjusted confidence.
 *
 * Penalty increases with difference level between current state and activation target state.
 * Difference level is a number of transitions from current state to activation target state.
 *
 * @property stepUpPenaltyBase penalty base for context stepUp.
 * @see calculatePenalty method with formula
 * */
class ActivationByContextPenalty(
    private val stepUpPenaltyBase: Double = 0.2
) : ActivationSelector(), WithLogger {

    internal data class ActivationWithScore(val activation: Activation, val adjustedConfidence: Double)

    /**
     * @param botContext a current user's [BotContext]
     * @param activations list of all available activations
     * @return the most relevant [Activation]
     *
     * @see Activation
     * @see ActivationSelector
     * @see com.justai.jaicf.BotEngine
     */
    override fun selectActivation(botContext: BotContext, activations: List<Activation>): Activation {
        val currentState = StatePath.parse(botContext.dialogContext.currentContext).resolve(".")
        val activationsByPenalty = rankActivationsByPenalty(activations, currentState)
        return activationsByPenalty.first().activation.also {
            logActivationResults(activationsByPenalty)
        }
    }

    private fun logActivationResults(activationsByPenalty: List<ActivationWithScore>) = activationsByPenalty.take(5)
        .joinToString("\n") { "to ${it.activation.state} with confidence ${it.adjustedConfidence}" }
        .let { logger.debug("Possible transitions:\n$it") }

    /**
     * Applies context difference penalty for each activation.
     *
     * @param activations a list of possible activations
     * @param currentState a state to find activations from
     *
     * @return list of [ActivationWithScore], where score = activation.confidence * penalty
     * */
    internal fun rankActivationsByPenalty(
        activations: List<Activation>,
        currentState: StatePath
    ): List<ActivationWithScore> {
        return activations.mapNotNull { activation ->
            activation.state?.let { targetState ->
                val statesDiff = calculateStatesDifference(StatePath.parse(targetState), currentState)
                val changeContextPenalty = calculatePenalty(statesDiff)
                val score = activation.context.confidence * changeContextPenalty
                ActivationWithScore(activation, score)
            }
        }.sortedByDescending { it.adjustedConfidence }
    }

    /**
     * Calculates penalty from [similarityLevel] number of transitions.
     * e.g.. when similarity level
     * 0 - 1,
     * 1 - 0.8
     * 2 - 0.7
     * 3 - 0.63
     * 4 - 0.58
     *
     * @param similarityLevel number of transitions between contexts
     * @return 1 - x - x/2 - x/3 - x/4 - ... x/(l+1)
     */
    private fun calculatePenalty(similarityLevel: Int) =
        (0 until similarityLevel).fold(1.0) { penalty, level ->
            penalty - stepUpPenaltyBase / (level + 1)
        }

}

