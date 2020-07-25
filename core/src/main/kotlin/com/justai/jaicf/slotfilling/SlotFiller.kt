package com.justai.jaicf.slotfilling

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.BotEngine

/**
 * This interface can be implemented to create a special way to handle filling some slots.
 * It can be useful, for example, if you want to create channel-specific slot filling,
 * like using simple text question in messenger and pre-recorded .wav questions in telephony channel.
 *
 * @see [BotEngine]
 *
 * */
interface SlotFiller {

    /**
     * Tells if it's possible to fill custom this slot with your own code.
     *
     * @param name - name of slot to be filled
     * @return true if can handle, no if can not
     * */
    fun canHandle(name: String): Boolean

    /**
     * Handles process of filling specific slot using current request's channel reactions
     *
     * @param name - name of slot to handle
     * @param botContext current user's [BotContext]
     * @param reactions current user's request [Reactions]
     * */
    fun handle(name: String, botContext: BotContext, reactions: Reactions)
}