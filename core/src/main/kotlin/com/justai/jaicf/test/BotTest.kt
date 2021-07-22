package com.justai.jaicf.test

import com.justai.jaicf.BotEngine
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.IntentBotRequest
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.test.context.TestRequestContext
import com.justai.jaicf.test.model.ProcessResult
import com.justai.jaicf.test.reactions.TestReactions
import com.justai.jaicf.test.reactions.answer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import java.util.*

/**
 * Main abstraction for every bot test. Contains helper methods for bot behaviour testing.
 * Executes each request to the bot with [TestRequestContext].
 * Each action block of the scenario runs in [com.justai.jaicf.test.context.TestActionContext] context.
 * Once this test doesn't involve a channel, you have to create [Reactions] instance or use [TestReactions] by default.
 *
 * Usage example:
 *
 * ```
 * class HelloWorldBotTest: BotTest(helloWorldBot) {
 *
 *   @Test
 *   fun `NLU extracts the name from a raw query`() {
 *     withCurrentContext("/helper")
 *     query("my name is john") returnsResult "John"
 *   }
 * }
 * ```
 *
 * @param bot a configured [BotEngine] instance to be tested
 * @see TestReactions
 * @see TestRequestContext
 * @see com.justai.jaicf.test.context.TestActionContext
 */
open class BotTest(private val bot: BotEngine) {

    internal lateinit var botContext: BotContext
    internal lateinit var reactions: Reactions
    lateinit var clientId: String

    private var requestContext: TestRequestContext = TestRequestContext(false)

    @BeforeEach
    fun init() {
        withClientId(UUID.randomUUID().toString())
        reactions = TestReactions()
        requestContext = TestRequestContext()
    }

    private fun saveBotContext() {
        bot.defaultContextManager.saveContext(botContext, null, null, requestContext)
    }

    /**
     * Sets up reactions for this test
     * @param reactions reactions to use
     * @see [Reactions]
     */
    fun withReactions(reactions: Reactions) {
        this.reactions = reactions
    }

    /**
     * Sets up a current client identifier
     * @param clientId client ID
     */
    fun withClientId(clientId: String) {
        botContext = bot.defaultContextManager.loadContext(EventBotRequest(clientId, ""), requestContext)
        this.clientId = clientId
    }

    /**
     * Starts a new session.
     * In fact just creates a new instance of [TestRequestContext] with newSession flag set to true.
     * It also copies random numbers queue from previous context.
     */
    fun withNewSession() {
        requestContext = TestRequestContext(newSession = true).also {
            it.randomNumbers.addAll(requestContext.randomNumbers)
        }
    }

    /**
     * Sets up a [BotContext] data
     * @param context a code block that initializes some [BotContext] variables
     */
    fun withBotContext(context: BotContext.() -> Unit) {
        context.invoke(botContext)
        saveBotContext()
    }

    /**
     * Sets up a current dialogue context path.
     * Useful if you would like to test some particular dialogue state.
     *
     * @param path a context path to set
     */
    fun withCurrentContext(path: String) {
        botContext.dialogContext.currentContext = path
        saveBotContext()
    }

    /**
     * Sets up a current dialogue state path
     * Useful if you would like to test some particular dialogue state.
     *
     * @param path a state path to set
     */
    fun withCurrentState(path: String) {
        botContext.dialogContext.currentState = path
        botContext.dialogContext.currentContext = path
        saveBotContext()
    }

    /**
     * Initialises a queue of numbers for smart random generator
     * @param numbers a collection of numbers to be used by smartRandom function
     */
    fun withRandomNumbers(vararg numbers: Int) {
        requestContext.randomNumbers.clear()
        requestContext.randomNumbers.addAll(numbers.toList())
    }

    /**
     * Sets up arbitrary named variables that can be used in runInTest code block of [com.justai.jaicf.test.context.TestActionContext]
     * @param vars a map of named variables
     */
    fun withVariables(vars: Map<String, Any?>) {
        requestContext.variables.putAll(vars)
    }

    /**
     * Sets up arbitrary named variables that can be used in runInTest code block of [com.justai.jaicf.test.context.TestActionContext]
     * @param vars a collection named variables
     */
    fun withVariables(vararg vars: Pair<String, Any?>) = withVariables(vars.toMap())

    /**
     * Sets up a current back state of scenario
     * @param path a path of back state
     */
    fun withBackState(path: String) {
        botContext.dialogContext.backStateStack.push(path)
        saveBotContext()
    }

    /**
     * Processes a [BotRequest]
     * @param request a [BotRequest] instance
     * @return [ProcessResult] that can be used to assert result in sequence
     *
     * @see ProcessResult
     */
    fun process(request: BotRequest): ProcessResult {
        bot.process(request, reactions, requestContext = requestContext)
        botContext = bot.defaultContextManager.loadContext(request, requestContext)
        reactions.executionContext.scenarioException?.let {
            throw it
        }
        return ProcessResult(botContext, reactions)
    }

    /**
     * A helper method that processes an intent
     * @param intent a recognised intent to process
     * @return [ProcessResult] that can be used to assert result in sequence
     *
     * @see ProcessResult
     */
    fun intent(intent: String) = process(IntentBotRequest(botContext.clientId, intent))

    /**
     * A helper method that processes an event
     * @param event an event name to process
     * @return [ProcessResult] that can be used to assert result in sequence
     *
     * @see ProcessResult
     */
    fun event(event: String) = process(EventBotRequest(botContext.clientId, event))

    /**
     * A helper method that processes a raw text query
     * @param query a raw text query to process
     * @return [ProcessResult] that can be used to assert result in sequence
     *
     * @see ProcessResult
     */
    fun query(query: String) = process(QueryBotRequest(botContext.clientId, query))

    /**
     * Asserts text response in the case if reactions is [TestReactions]
     * @param response an expected raw text
     */
    fun isTextResponse(response: String) {
        assertEquals(response, reactions.answer)
    }

    /**
     * Asserts the current state of dialogue
     * @param state an expected state path
     */
    fun isState(state: String) {
        assertEquals(state, botContext.dialogContext.currentState)
    }

    /**
     * Asserts the next state of dialogue
     * @param state an expected state path
     */
    fun isNextState(state: String) {
        assertEquals(state, botContext.dialogContext.nextState)
    }

    /**
     * Asserts the current context of dialogue
     * @param context an expected context path
     */
    fun isContext(context: String) {
        assertEquals(context, botContext.dialogContext.currentContext)
    }

    /**
     * Asserts the next context of dialogue
     * @param context an expected context path
     */
    fun isNextContext(context: String) {
        assertEquals(context, botContext.dialogContext.nextContext)
    }

    /**
     * Asserts the result returned from sub-scenario
     * @param expectedValue an expected value of the returned result
     */
    fun isResult(expectedValue: Any?) {
        assertEquals(expectedValue, botContext.result)
    }

    /**
     * Asserts the current [BotContext] content
     * @param context a code block that generates an expected properties of [BotContext] to be asserted with current one
     */
    fun isBotContext(context: BotContext.() -> Unit) {
        val expected = BotContext(botContext.clientId, botContext.dialogContext)
        context.invoke(expected)
        expected.client.isNotEmpty().let {
            assertEquals(expected.client, botContext.client)
        }
        expected.session.isNotEmpty().let {
            assertEquals(expected.session, botContext.session)
        }
        expected.temp.isNotEmpty().let {
            assertEquals(expected.temp, botContext.temp)
        }
    }
}