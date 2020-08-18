package com.justai.jaicf.slotfilling

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.reactions.Reactions

/**
 * Set of available results of slotfilling processing.
 *
 * @see SlotFillingSkipped
 * @see SlotFillingFinished
 * @see SlotFillingInterrupted
 * @see SlotFillingInProgress
 * */
sealed class SlotFillingResult

/**
 * This result indicates that either intent has no slots, or no prompts, or activator can not process it.
 *
 * @see [com.justai.jaicf.activator.Activator]
 * */
object SlotFillingSkipped : SlotFillingResult()

/**
 * This result indicates that intent started processing slotfilling,
 * prompted question to client and will prompt question for another slot.
 * 
 * @see [com.justai.jaicf.activator.Activator]
 * */
object SlotFillingInProgress : SlotFillingResult()

/**
 * This result indicates that slot filling is interrupted by activator-specific reasons.
 * If slot filling is interrupted, last client query will be processed in BotEngine.
 *
 * For example, SlotFillingInterrupted can be returned when client tried to fill slots too many times and failed it.
 *
 * @see [com.justai.jaicf.activator.Activator]
 * */
object SlotFillingInterrupted : SlotFillingResult()

/**
 * This result indicates that slot filling session is finished and returns activator context,
 * which will be passed to scenario.
 *
 * @param activatorContext initially stored activator context. New slots should be added to this context to be available in scenario.
 *
 * @see [com.justai.jaicf.activator.Activator]
 * @see [com.justai.jaicf.context.ActivatorContext]
 * */
data class SlotFillingFinished(
    val activatorContext: ActivatorContext
) : SlotFillingResult()

