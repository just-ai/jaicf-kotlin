package com.justai.jaicf.slotfilling

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.BotEngine
import com.justai.jaicf.api.BotRequest

/**
 * This interface can be implemented to create a special way to handle filling some slots.
 * It can be useful, for example, if you want to create channel-specific slot filling reactions,
 * like using simple text question in messenger and pre-recorded .wav questions in telephony channel.
 *
 * @see [BotEngine]
 *
 * */
interface SlotFiller {

    /**
     * Signals if this slot filler can handle a particular slot.
     *
     * @param name of slot to be handled
     * @return true if slot filler can handle reaction for specified slot
     * */
    fun canHandle(name: String): Boolean

    /**
     * Handles process of filling specific slot using current request's channel reactions.
     *
     * @param name of slot to be handled
     * @param botContext current user's [BotContext]
     * @param reactions current user's request [Reactions]
     * */
    fun handle(name: String, botContext: BotContext, reactions: Reactions)
}