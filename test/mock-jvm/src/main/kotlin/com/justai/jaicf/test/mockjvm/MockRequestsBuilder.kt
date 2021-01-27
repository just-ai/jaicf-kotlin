package com.justai.jaicf.test.mockjvm

import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.BotRequestType
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.test.model.ProcessResult
import io.mockk.every
import io.mockk.spyk

/**
 * Processes a query with mocked reactions
 *
 * @param query a raw text query to process
 * @return [ProcessResult] that can be used to assert result in sequence
 *
 * @see ProcessResult
 */
fun <B : BotRequest, R : Reactions> MockingTest<*, B, R>.query(query: String): ProcessResult {
//    useDefaultReactionsApi = true
    return process(request.withMockedInputAndType(query, BotRequestType.QUERY)) { }
}

/**
 * Processes a query with mocked channel reactions and reactions stub
 *
 * @param query a raw text query to process
 * @param body reactions stub
 * @return [ProcessResult] that can be used to assert result in sequence
 *
 * @see ProcessResult
 */
fun <B : BotRequest, R : Reactions> MockingTest<*, B, R>.query(
    query: String,
    body: MockReactionsBuilder<*, B, R>.() -> Unit
): ProcessResult =
    process(request.withMockedInputAndType(query, BotRequestType.QUERY), body)

fun <A : IntentActivatorContext, B : BotRequest, R : Reactions> MockingTest<A, B, R>.intent(
    activatorContext: A,
    body: MockReactionsBuilder<A, B, R>.() -> Unit
): ProcessResult {
    val intent = requireNotNull(activatorContext.intent)
    registerActivation(activatorContext)
    request.withMockedInputAndType(intent, type = BotRequestType.INTENT)
    return process(request, body)
}

fun <A : IntentActivatorContext, B : BotRequest, R : Reactions> MockingTest<A, B, R>.intent(
    activatorContext: A
): ProcessResult {
    val intent = requireNotNull(activatorContext.intent)
    registerActivation(activatorContext)
    request.withMockedInputAndType(intent, type = BotRequestType.INTENT)
    useDefaultReactionsApi = true
    return process(request) {}
}

fun <A : IntentActivatorContext, B : BotRequest, R : Reactions> MockingTest<A, B, R>.intent(intent: String): ProcessResult {
    useDefaultReactionsApi = true
    registerActivation(activator)
    request.withMockedInputAndType(intent, type = BotRequestType.INTENT)
    every { activator getProperty "intent" } answers { intent }
    return process(request) {}
}

fun <A : IntentActivatorContext, B : BotRequest, R : Reactions> MockingTest<A, B, R>.intent(
    intent: String,
    body: MockReactionsBuilder<A, B, R>.() -> Unit
): ProcessResult {
    registerActivation(activator)
    request.withMockedInputAndType(intent, type = BotRequestType.INTENT)
    every { activator getProperty "intent" } answers { intent }
    return process(request, body)
}

fun <A : IntentActivatorContext, B : BotRequest, R : Reactions> MockingTest<A, B, R>.intent(
    intent: String,
    activatorContext: A,
    body: MockReactionsBuilder<A, B, R>.() -> Unit
): ProcessResult {
    registerActivation(activatorContext)
    return intent(intent, body)
}

fun <A : ActivatorContext, B : BotRequest, R : Reactions> MockingTest<A, B, R>.event(event: String): ProcessResult {
    useDefaultReactionsApi = true
    return process(request.withMockedInputAndType(event, BotRequestType.EVENT)) { }
}

fun <A : ActivatorContext, B : BotRequest, R : Reactions> MockingTest<A, B, R>.event(
    input: String,
    body: MockReactionsBuilder<*, B, R>.() -> Unit
): ProcessResult =
    process(request.withMockedInputAndType(input, BotRequestType.EVENT), body)


@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
inline fun <A : ActivatorContext, reified B : BotRequest, R : Reactions> MockingTest<A, B, R>.request(
    request: B,
    noinline body: MockReactionsBuilder<A, B, R>.() -> Unit
): ProcessResult = process(spyk(request), body)


@Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
inline fun <A : ActivatorContext, reified B : BotRequest, R : Reactions> MockingTest<A, B, R>.request(
    request: B
): ProcessResult = process(spyk(request)) {}

private fun <B : BotRequest> B.withMockedInputAndType(input: String, type: BotRequestType) = apply {
    every { this@apply getProperty "input" } returns input
    every { this@apply getProperty "type" } returns type
}
