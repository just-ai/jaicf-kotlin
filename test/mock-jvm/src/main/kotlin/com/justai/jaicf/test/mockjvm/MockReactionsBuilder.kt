package com.justai.jaicf.test.mockjvm

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.test.TestDsl
import io.mockk.MockKMatcherScope
import kotlinx.atomicfu.atomic


/**
 * Creates a verification context for mocked reactions.
 * Records every call inside context to create a verifiable stub and mock reactions methods.
 * */
@Suppress("unused")
@TestDsl
class MockReactionsBuilder<A : ActivatorContext, B : BotRequest, R : Reactions>(
    private val mockingContext: MockingTest<A, B, R>,
) {
    private val expectStep = atomic(0)
    private val verificationStep = atomic(0)
    private fun nextStep() = expectStep.getAndIncrement()

    /**
     * Registers `say` reaction invocation. If reaction was not invoked, an error will be thrown.
     *
     * @param text expected text to be said in scenario
     * */
    fun MockReactionsBuilder<*, B, R>.say(text: String) = also {
        mockingContext.say(text, nextStep(), verificationStep)
    }

    /**
     * Registers `buttons` reaction invocation. If reaction was not invoked, an error will be thrown.
     *
     * @param buttons expected buttons sent from scenario
     * */
    fun MockReactionsBuilder<*, B, R>.buttons(buttons: List<String>) = also {
        mockingContext.buttons(buttons, nextStep(), verificationStep)
    }

    /**
     * Registers `buttons` reaction invocation. If reaction was not invoked, an error will be thrown.
     *
     * @param buttons expected buttons sent from scenario
     * */
    fun MockReactionsBuilder<*, B, R>.buttons(vararg buttons: String) = also {
        mockingContext.buttons(buttons.toList(), nextStep(), verificationStep)
    }

    /**
     * Registers `image` reaction invocation. If reaction was not invoked, an error will be thrown.
     *
     * @param url expected image url sent from scenario
     * */
    fun MockReactionsBuilder<*, B, R>.image(url: String) = also {
        mockingContext.image(url, nextStep(), verificationStep)
    }

    /**
     * Registers `audio` reaction invocation. If reaction was not invoked, an error will be thrown.
     *
     * @param url expected audio url sent from scenario
     * */
    fun MockReactionsBuilder<*, B, R>.audio(url: String) = also {
        mockingContext.audio(url, nextStep(), verificationStep)
    }

    /**
     * Registers `go` reaction invocation. If reaction was not invoked, an error will be thrown.
     *
     * @param state expected transition state from scenario
     * @param callbackState an optional callback state path
     * */
    fun MockReactionsBuilder<*, B, R>.go(state: String, callbackState: String? = null) = also {
        mockingContext.go(state, callbackState, nextStep(), verificationStep)
    }

    /**
     * Registers `go` reaction invocation. If reaction was not invoked, an error will be thrown.

     * @param state expected transition state from scenario
     * @param callbackState an optional callback state path
     * */
    fun MockReactionsBuilder<*, B, R>.changeState(state: String, callbackState: String? = null) = also {
        mockingContext.changeState(state, nextStep(), verificationStep)
    }

    /**
     * Registers a channel reaction invocation. If reaction was not invoked, an error will be thrown.
     *
     * Example usage:
     * ```
     * withChannel(telegram) {
     *  query("first") {
     *      calls { sendPhoto("photo-url.url") }
     *  }
     * }
     * ```
     * */
    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    inline fun <reified T : Any> MockReactionsBuilder<*, B, R>.calls(noinline body: @TestDsl R.() -> T) =
        also {
            mockingContext.registerChannelInvocation(nextStep(), verificationStep, body)
        }

    /**
     * Registers a reaction invocation inside [MockKMatcherScope] context with dynamic arguments.
     * Allows to use [MockKMatcherScope.any], [MockKMatcherScope.allAny] and other argument matchers.
     *
     * * Example usage:
     * ```
     * withChannel(telegram) {
     *  query("first") {
     *      calls { sendPhoto(any()) }
     *  }
     * }
     * ```
     *
     * @see [Reactions]
     * @see [MockKMatcherScope]
     * @return a mock of invoked reaction return value
     * */
    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    inline fun <reified T : Any> MockReactionsBuilder<*, B, R>.callsDynamic(noinline body: @TestDsl MockKMatcherScope.() -> T) =
        also { mockingContext.registerStubberInvocation(body) }

    /**
     * Registers invocation of dynamic method with answer. Must be used with [answers] method proceeding.
     * NOTE: Usage without answer method after this will cause error in runtime.
     *
     * Example usage:
     * ```
     * withChannel(facebook) {
     *  query("whoami") {
     *      callsDynamicWithAnswer { reactions.queryUserProfile() } answers { UserProfile(firstName = "test-user") }
     *  }
     * }
     * ```
     *
     * @see [Reactions]
     * @see [MockKMatcherScope]
     * @see answers
     * */
    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    inline fun <reified T : Any> MockReactionsBuilder<*, B, R>.callsDynamicWithAnswer(
        noinline body: @TestDsl MockKMatcherScope.() -> T
    ) = DynamicCallStubber(body)

    /**
     * Provides an answer for [callsDynamicWithAnswer] method.
     *
     * Example usage:
     * ```
     * withChannel(facebook) {
     *  query("whoami") {
     *      callsDynamicWithAnswer { reactions.queryUserProfile() } answers { UserProfile(firstName = "test-user") }
     *  }
     * }
     * ```
     *
     * @see [Reactions]
     * @see [MockKMatcherScope]
     * */
    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    inline infix fun <reified T : Any> DynamicCallStubber<T>.answers(noinline answerStub: @TestDsl DynamicCallStubber<T>.() -> T) =
        mockingContext.registerDynamicStubberInvocation(matcherStub, answerStub, this)
}