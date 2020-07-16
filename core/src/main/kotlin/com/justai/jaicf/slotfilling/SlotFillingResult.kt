package com.justai.jaicf.slotfilling

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.reactions.Reactions

/**
 * Set of available status for slotfilling.
 *
 * @see SlotFillingSkipped
 * @see SlotFillingFinished
 * @see SlotFillingInterrupted
 * @see SlotFillingInProgress
 * */
sealed class SlotFillingResult

/**
 * JAVADOC ME
 * */
object SlotFillingSkipped : SlotFillingResult()

/**
 * JAVADOC ME
 * */
object SlotFillingInProgress : SlotFillingResult()

/**
 * JAVADOC ME
 * */
object SlotFillingInterrupted : SlotFillingResult()

/**
 * JAVADOC ME
 * */
data class SlotFillingFinished(
    val activatorContext: ActivatorContext
) : SlotFillingResult()

