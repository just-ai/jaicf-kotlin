package com.justai.jaicf.slotfilling

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.BotEngine
import com.justai.jaicf.api.BotRequest

/**
 * Implementation of this interface enables reacting to filling slot in specified way.
 *
 * It can be useful, for example, if you want to create channel-specific slot filling reactions,
 * like using simple text question in messenger and pre-recorded .wav questions in telephony channel.
 *
 * Usage example:
 * ```
 * val bot = BotEngine(
 *    BotScenario.model,
 *    activators = arrayOf(cailaIntentActivator, CatchAllActivator),
 *    slotFillingReactionsProcessor = object : SlotFillingReactionsProcessor {
 *
 *        override fun canProcess(name: String) = name == "Amount"
 *
 *        override fun process(name: String, botContext: BotContext, reactions: Reactions, prompts: List<String>) {
 *            when (reactions) {
 *                is TelephonyReactions -> reactions.telephony?.audio("https://example.com/amountQuestion.wav")
 *                else -> reactions.say(prompts[0])
 *            }
 *        }
 *    })
 * ```
 *
 * @see [BotEngine]
 * */
interface SlotFillingReactionsProcessor {

    /**
     * Signals if this [SlotFillingReactionsProcessor] can process reactions to filling specified slot.
     *
     * @param name of slot,
     * @return true if slot filler can handle reaction for specified slot
     * */
    fun canProcess(name: String): Boolean

    /**
     * Processes reactions to filling slot with [name].
     *
     * @param name of slot to be handled
     * @param botContext current user's [BotContext]
     * @param reactions current user's request [Reactions]
     * @param prompts list of questions, asking user to fill slot, provided by activator.
     * */
    fun process(name: String, botContext: BotContext, reactions: Reactions, prompts: List<String> = emptyList())
}