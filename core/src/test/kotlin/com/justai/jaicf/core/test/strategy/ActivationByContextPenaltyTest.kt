package com.justai.jaicf.core.test.strategy

import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.activator.strategy.ActivationByContextPenalty
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DialogContext
import com.justai.jaicf.context.StrictActivatorContext
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.state.StatePath
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

internal class ActivationByContextPenaltyTest {
    private val strategy = ActivationByContextPenalty(0.2)

    private val currentNode = "/some/current/state"
    private val childNode = "/some/current/state/child"
    private val otherContextNode = "/another/target/state"
    private val rootNode = "/"
    private val siblingNode = "/some/current/sibling"

    private val bc = BotContext("", DialogContext().apply { currentContext = currentNode })

    @Test
    fun shouldRankStrictActivations() {
        val activations = listOf(
            Activation(childNode, StrictActivatorContext()),
            Activation(otherContextNode, StrictActivatorContext()),
            Activation(rootNode, StrictActivatorContext()),
            Activation(siblingNode, StrictActivatorContext())
        )

        val selected = strategy.selectActivation(bc, activations)
        assertEquals(childNode, selected.state)

        val ranked = strategy.rankActivationsByPenalty(
            activations,
            StatePath.parse(currentNode)
        )

        val predicted = ranked.first()
        assertEquals(1.0, predicted.adjustedConfidence)
        assertEquals(childNode, predicted.activation.state)

        val second = ranked[1]
        assertEquals(0.8, second.adjustedConfidence)
        assertEquals(siblingNode, second.activation.state)
    }

    @Test
    fun shouldRankActivationsWithConfidence() {
        val activations = listOf(
            Activation(childNode, IntentActivatorContext(0.6F, "")),
            Activation(otherContextNode, IntentActivatorContext(0.9F, "")),
            Activation(rootNode, IntentActivatorContext(0.7F, "")),
            Activation(siblingNode, IntentActivatorContext(0.8F, ""))
        )

        val selected = strategy.selectActivation(bc, activations)
        assertEquals(siblingNode, selected.state)

        val ranked = strategy.rankActivationsByPenalty(
            activations,
            StatePath.parse(currentNode)
        )

        val predicted = ranked.first()
        assertEquals(siblingNode, predicted.activation.state)
        assertEquals(0.64F, predicted.adjustedConfidence.toFloat())

        val second = ranked[1]
        assertEquals(childNode, second.activation.state)
        assertEquals(0.6F, second.adjustedConfidence.toFloat())
    }
}