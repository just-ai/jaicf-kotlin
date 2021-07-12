package com.justai.jaicf.context

import com.justai.jaicf.activator.ActivationContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.exceptions.BotException
import com.justai.jaicf.logging.ConversationLogger
import com.justai.jaicf.logging.Reaction

/**
 * This class will accumulate all execution information obtained during processing request.
 *
 * @param requestContext current channel request's context
 * @param activationContext selected activation context
 * @param botContext current client's bot context
 * @param request current client's request
 * @param reactions current channel provided reactions
 * @param input client's input
 * @param scenarioException an exception thrown from scenario
 * @param channelContext a storage for any context needed for channel or channel wrapper
 *
 * @see ConversationLogger
 * @see Reaction
 * */
data class ExecutionContext(
    val requestContext: RequestContext,
    var activationContext: ActivationContext?,
    val botContext: BotContext,
    val request: BotRequest,
    val firstState: String = botContext.dialogContext.currentState,
    val reactions: MutableList<Reaction> = mutableListOf(),
    val input: String = request.input,
    var scenarioException: BotException? = null,
    val isNewUser: Boolean = (botContext.temp[BotContextKeys.IS_NEW_USER_KEY] as? Boolean) ?: false,
    val channelContext: MutableMap<String, Any> = mutableMapOf()
)