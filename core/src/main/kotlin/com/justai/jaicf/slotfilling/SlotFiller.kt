package com.justai.jaicf.slotfilling

import com.justai.jaicf.context.BotContext
import com.justai.jaicf.reactions.Reactions

/**
 * JAVADOC ME*/
interface SlotFiller {

    /**
     * JAVADOC ME*/
    fun canHandle(name: String): Boolean

    /**
     * JAVADOC ME*/
    fun handle(name: String, botContext: BotContext, reactions: Reactions)
}