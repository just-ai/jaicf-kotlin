package com.justai.jaicf.slotfilling

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.Activator
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.reactions.Reactions

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
 *    slotFillingReactionsProcessor = object : SlotReactor {
 *        override fun canReact(name: String) = name == "Amount"
 *        override fun react(
 *              request: BotRequest,
 *              botContext: BotContext,
 *              reactions: Reactions,
 *              activatorContext: ActivatorContext?,
 *              slotName: String,
 *              prompts: List<String>
 *          ) {
 *              when (reactions) {
 *                  is TelephonyReactions -> reactions.telephony?.audio("https://example.com/amountQuestion.wav")
 *                  else -> reactions.say(prompts[0])
 *              }
 *          }
 *    })
 * ```
 *
 * @see [BotEngine]
 * */
interface SlotReactor {

    /**
     * Signals if this [SlotReactor] can react to filling of slot with specified [slotName].
     *
     * @param slotName of slot,
     * @return true if [SlotReactor] can react to filling of slot with specified [slotName].
     * */
    fun canReact(slotName: String): Boolean

    /**
     * Reacts to filling specified slot with [slotName].
     *
     * By default, [Activator]'s implementation of SlotFilling will react to filling specified slot,
     * like say random phrase from list of prompts. You can implement this method to provide custom reaction to filling slot with [slotName].
     *
     * @param request a current [BotRequest]
     * @param botContext current user's [BotContext]
     * @param reactions current user's request [Reactions]
     * @param activator initially stored [ActivatorContext] for current SlotFilling session. This context will be passed via [SlotFillingFinished] result to scenario when all slots are filled.
     * @param slotName of slot to be handled
     * @param prompts list of questions, asking user to fill slot, provided by activator.
     *
     * @see [Activator]
     * @see [SlotFillingResult]
     * */
    fun react(
        request: BotRequest,
        botContext: BotContext,
        reactions: Reactions,
        activator: ActivatorContext,
        slotName: String,
        prompts: List<String> = emptyList()
    )
}
