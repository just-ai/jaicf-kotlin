package com.justai.jaicf.test.mockjvm

import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.logging.ExecutionContext
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.ResponseReactions
import com.justai.jaicf.test.BotTest
import com.justai.jaicf.test.TestIntentActivator
import com.justai.jaicf.test.model.ProcessResult
import io.mockk.*
import kotlinx.atomicfu.AtomicInt


class MockingTest<A : ActivatorContext, B : BotRequest, R : Reactions>(
    @PublishedApi
    internal val testClass: BotTest,
    val request: B,
    val reactions: R,
    val activator: A,
) {
    var enforceStrictOrder: Boolean = true
    var useDefaultReactionsApi: Boolean = true

    @PublishedApi
    internal val verificationCalls: MutableMap<Int, R.() -> Any> = mutableMapOf()

    @PublishedApi
    internal val matcherCalls: MutableList<MockKMatcherScope.() -> Any> = mutableListOf()

    @PublishedApi
    internal val registrar = MockReactionsRegistrar(reactions)

    internal fun say(text: String, step: Int, verificationStep: AtomicInt) {
        verificationCalls[step] = { reactions.say(text) }
        every { reactions.say(text) } answers {
            ensureOrder(verificationStep, step)
            registrar.say(invocation)
        }
    }

    internal fun buttons(buttons: List<String>, step: Int, verificationStep: AtomicInt) {
        verificationCalls[step] = { reactions.buttons(*buttons.toTypedArray()) }
        every { reactions.buttons(*buttons.toTypedArray()) } answers {
            ensureOrder(verificationStep, step)
            registrar.buttons(invocation)
        }
    }

    internal fun image(url: String, step: Int, verificationStep: AtomicInt) {
        verificationCalls[step] = { reactions.image(url) }
        every { reactions.image(url) } answers {
            ensureOrder(verificationStep, step)
            registrar.image(invocation)

        }
    }

    internal fun audio(url: String, step: Int, verificationStep: AtomicInt) {
        verificationCalls[step] = { reactions.audio(url) }
        every { reactions.audio(url) } answers {
            ensureOrder(verificationStep, step)
            registrar.audio(invocation)
        }
    }

    internal fun go(path: String, callbackState: String?, step: Int, verificationStep: AtomicInt): Unit {
        verificationCalls[step] = { reactions.go(path, callbackState) }
        every { reactions.go(path, callbackState) } answers {
            ensureOrder(verificationStep, step)
            registrar.go(invocation)
        }
    }

    internal fun changeState(path: String, step: Int, verificationStep: AtomicInt) {
        verificationCalls[step] = { reactions.changeState(path) }
        every { reactions.changeState(path) } answers {
            ensureOrder(verificationStep, step)
            registrar.changeState(invocation)
        }
    }

    internal fun <T : IntentActivatorContext> registerActivation(activator: T) {
        testClass.withBotContext {
            client[TestIntentActivator.ACTIVATOR_VALUE_KEY] = activator
        }
    }


    @PublishedApi
    internal inline fun <reified T : Any> registerChannelInvocation(
        step: Int,
        verificationStep: AtomicInt,
        noinline body: R.() -> T,
    ) {
        verificationCalls[step] = body
        every { reactions.body() } answers {
            ensureOrder(verificationStep, step)
            mockk()
        }
    }

    @PublishedApi
    internal inline fun <reified T : Any> registerStubberInvocation(noinline body: MockKMatcherScope.() -> T) {
        every { body() } answers { mockk() }
        matcherCalls.add(body)
    }

    @PublishedApi
    internal inline fun <reified T : Any> registerDynamicStubberInvocation(
        noinline matcherStub: MockKMatcherScope.() -> T,
        noinline answerStub: DynamicCallStubber<T>.() -> T,
        stubber: DynamicCallStubber<T>
    ) {
        every(matcherStub) answers { answerStub(stubber) }
        matcherCalls.add(matcherStub)
    }

    @PublishedApi
    internal fun ensureOrder(verificationStep: AtomicInt, expectedStep: Int) {
        if (enforceStrictOrder) {
            val act = verificationStep.getAndIncrement()
            if (act != expectedStep) {
                error("Invalid reactions order")
            }
        }
    }

    private fun <R : Reactions> R.addDefaultMethods() {
        if (useDefaultReactionsApi) {
            every { reactions.say(any()) } answers { registrar.say(invocation) }
            every { reactions.audio(any()) } answers { registrar.audio(invocation) }
            every { reactions.image(any()) } answers { registrar.image(invocation) }
            every { reactions.buttons(*anyVararg()) } answers { registrar.buttons(invocation) }
            every { reactions.go(any(), any()) } answers { registrar.go(invocation) }
            every { reactions.changeState(any(), any()) } answers { registrar.changeState(invocation) }

            every { reactions.goBack(any()) } answers {
                registrar.goBack(invocation)
                    ?.also { state -> go(state) }
            }

            every { reactions.changeStateBack(any()) } answers {
                registrar.changeStateBack(invocation)
                    ?.also { state -> changeState(state) }
            }
        }
    }

    internal fun process(
        request: BotRequest,
        body: MockReactionsBuilder<A, B, R>.() -> Unit
    ): ProcessResult = with(MockReactionsBuilder(this).body()) {
        // 1. add backing fields for mocks
        reactions.addContextsBackingFields()
        reactions.addDefaultMethods()
        reactions.addNullResponse()
        request.withMockedClientId(testClass.clientId)

        // 2. process request with reactions
        val processResult = testClass.process(request)

        // 3. verify reactions methods are called
        verificationCalls.values.forEach { method -> verify { reactions.method() } }
        matcherCalls.forEach { method -> verify { method() } }

        // 4. clean data before next run
        verificationCalls.clear()
        reactions.clearMocks()
        reactions.addContextsBackingFields()
        processResult
    }
}

private fun <R : Reactions> R.clearMocks() = clearMocks(this)

private fun <R : Reactions> R.addContextsBackingFields() {
    mockWithBackingField<ExecutionContext>(this, "executionContext")
    mockWithBackingField<BotContext>(this, "botContext")
}

private inline fun <reified T : Any> mockWithBackingField(obj: Any, propName: String) {
    every { obj setProperty propName value any<T>() } propertyType T::class answers { fieldValue = value }
    every { obj getProperty propName } answers { fieldValue }
}

private fun <B : BotRequest> B.withMockedClientId(clientId: String) = apply {
    every { this@apply getProperty "clientId" } answers { clientId }
}

private fun <R : Reactions> R.addNullResponse() = (this as? ResponseReactions<*>)?.let { reactions ->
    every { reactions getProperty "response" } answers { null }
}

class DynamicCallStubber<T>(internal val matcherStub: MockKMatcherScope.() -> T)