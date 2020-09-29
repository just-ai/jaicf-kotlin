package com.justai.jaicf.core.test.activation

import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.activator.selection.ActivationByConfidence
import com.justai.jaicf.activator.selection.ActivationByContextPenalty
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DialogContext
import com.justai.jaicf.context.StrictActivatorContext
import com.justai.jaicf.core.test.BaseTest
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.state.ActivationTransition
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

internal class ActivationSelectorTest : BaseTest() {
    private val selectorByContext = ActivationByContextPenalty(0.2)
    private val selectorByConfidence = ActivationByConfidence()

    private val currentNode = "/some/current/state"
    private val sameState = transitionToState(currentNode)
    private val child = transitionToState("/some/current/state/child")
    private val otherContext = transitionToState("/another/target/state")
    private val root = transitionToState("/")
    private val sibling = transitionToState("/some/current/sibling")
    private val indirectChild = transitionToState("/some/current/state/child/some/substate")

    private val bc = BotContext("", DialogContext().apply { currentContext = currentNode })

    @Test
    fun `should rank strict activations`() {
        val activations = listOf(
            Activation(child, StrictActivatorContext()),
            Activation(sameState, StrictActivatorContext()),
            Activation(otherContext, StrictActivatorContext()),
            Activation(root, StrictActivatorContext()),
            Activation(sibling, StrictActivatorContext()),
            Activation(indirectChild, StrictActivatorContext())
        )

        val selected = selectorByContext.selectActivation(bc, activations)
        assertEquals(child, selected.transition)

        val ranked = selectorByContext.rankActivationsByPenalty(activations)
        val predicted = ranked.first()
        assertEquals(child, predicted.activation.transition)
        assertEquals(1.0, predicted.adjustedConfidence)

        val second = ranked[1]
        assertEquals(indirectChild, second.activation.transition)
        assertEquals(1.0, second.adjustedConfidence)

        val third = ranked[2]
        assertEquals(sameState, third.activation.transition)
        assertEquals(0.8, third.adjustedConfidence)

        val forth = ranked[3]
        assertEquals(sibling, forth.activation.transition)
        assertEquals(0.8, forth.adjustedConfidence)
    }

    @Test
    fun `should rank activations with confidence and context`() {
        val activations = listOf(
            Activation(child, IntentActivatorContext(0.6F, "")),
            Activation(otherContext, IntentActivatorContext(0.9F, "")),
            Activation(root, IntentActivatorContext(0.7F, "")),
            Activation(sibling, IntentActivatorContext(0.8F, "")),
            Activation(indirectChild, IntentActivatorContext(0.8F, ""))
        )

        step("Check strict activations. Expect transition to child state.")
        val selected = selectorByContext.selectActivation(bc, activations)
        assertEquals(indirectChild, selected.transition)

        val ranked = selectorByContext.rankActivationsByPenalty(activations)
        val predicted = ranked.first()
        assertEquals(indirectChild, predicted.activation.transition)
        assertEquals(0.8F, predicted.adjustedConfidence.toFloat())

        val second = ranked[1]
        assertEquals(sibling, second.activation.transition)
        assertEquals(0.64F, second.adjustedConfidence.toFloat())
    }

    @Test
    fun `should rank activations only with confidence`() {
        val activations = listOf(
            Activation(child, IntentActivatorContext(0.6F, "")),
            Activation(otherContext, IntentActivatorContext(0.9F, "")),
            Activation(root, IntentActivatorContext(0.7F, "")),
            Activation(sibling, IntentActivatorContext(0.8F, "")),
            Activation(indirectChild, IntentActivatorContext(0.8F, ""))
        )

        step("Check activation selection by confidence. Indirect child (transition by fromState modifier) should be selected.")
        val selected = selectorByConfidence.selectActivation(bc, activations)
        assertEquals(indirectChild, selected.transition)
    }

    private fun transitionToState(toState: String) = ActivationTransition(
        fromState = currentNode,
        toState = toState
    )
}