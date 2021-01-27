package com.justai.jaicf.test.mockjvm

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.channel.BotChannel
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.generic.ActivatorTypeToken
import com.justai.jaicf.generic.ChannelTypeToken
import com.justai.jaicf.generic.ContextTypeToken
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.test.BotTest
import com.justai.jaicf.test.reactions.TestReactions
import io.mockk.mockkClass
import io.mockk.spyk

/**
 * Creates a [MockingTest] to test scenario with specific [BotChannel] implementation
 *
 * @param token channel type token
 * @param body verification scope body
 * */
fun <B : BotRequest, R : Reactions> BotTest.withChannel(
    token: ChannelTypeToken<B, R>,
    body: MockingTest<*, B, R>.() -> Unit,
) = withContext(
    request = mockkClass(token.requestType),
    reactions = mockkClass(token.reactionsType),
    activator = mockkClass(IntentActivatorContext::class),
    body = body
)

/**
 * Creates a [MockingTest] to test scenario with specific [BotChannel] and [Activator] implementations
 *
 * @param token context type token, which consists of [ChannelTypeToken] and [ActivatorTypeToken]
 * @param body verification scope body
 * */
fun <A : ActivatorContext, B : BotRequest, R : Reactions> BotTest.withContext(
    token: ContextTypeToken<A, B, R>,
    body: MockingTest<A, B, R>.() -> Unit,
) = withContext(
    request = mockkClass(token.requestType),
    reactions = mockkClass(token.reactionsType),
    activator = mockkClass(token.activatorType),
    body = body
)

/**
 * Creates a [MockingTest] to test scenario with specific [Activator] implementation
 *
 * @param token an [ActivatorTypeToken]
 * @param body verification scope body
 * */
fun <A : ActivatorContext> BotTest.withActivation(
    token: ActivatorTypeToken<A>,
    body: MockingTest<A, *, *>.() -> Unit,
) = withContext(
    request = mockkClass(BotRequest::class),
    reactions = spyk(TestReactions()),
    activator = mockkClass(token.activatorType),
    body = body
)

private fun <A : ActivatorContext, B : BotRequest, R : Reactions> BotTest.withContext(
    request: B,
    reactions: R,
    activator: A,
    body: MockingTest<A, B, R>.() -> Unit,
) {
    withReactions(reactions)
    MockingTest(this, request, reactions, activator).body()
    withReactions(TestReactions())
}