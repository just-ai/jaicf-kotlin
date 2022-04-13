package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.caila.client.CailaKtorClient
import com.justai.jaicf.activator.caila.slotfilling.CailaSlotFillingSettings
import com.justai.jaicf.activator.selection.ActivationSelector
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.builder.ScenarioGraphBuilder
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.context.manager.InMemoryBotContextManager
import com.justai.jaicf.model.activation.Activation
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.plugin.StateDeclaration
import com.justai.jaicf.plugin.StateName
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.slotfilling.SlotFillingFinished
import com.justai.jaicf.slotfilling.SlotReactor
import com.justai.jaicf.test.reactions.TestReactions
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class CailaActivatorBaseTest {
    abstract val scenario: Scenario

    private val cm = InMemoryBotContextManager
    private lateinit var clientId: String

    private var _reactions: TestReactions? = null
    private var _context: BotContext? = null
    private var request: BotRequest? = null

    val reactions: TestReactions get() = _reactions!!
    val context: BotContext get() = _context!!

    private val mockKtorEngine = MockEngine { request ->
        val method = request.url.encodedPath.substringAfterLast('/')
        val response = getCailaResponse(this@CailaActivatorBaseTest.request!!.input, method)
        respond(
            content = ByteReadChannel(response.use { it.readBytes() }),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }

    private val cailaHttpClientMock = CailaKtorClient("token", inferenceNBest = -1, engine = mockKtorEngine)

    @BeforeEach
    fun setUp() {
        clientId = UUID.randomUUID().toString()
        request = null
        _reactions = null
        _context = loadContext()
    }

    val Activation.intentContext get() = assertNotNull(context.caila)
    val SlotFillingFinished.intentContext get() = assertNotNull(activatorContext.caila)

    val Activation.entityContext get() = assertNotNull(context.cailaEntity)
    val SlotFillingFinished.entityContext get() = assertNotNull(activatorContext.cailaEntity)

    fun test(
        confidenceThreshold: Double = 0.2,
        maxSlotRetries: Int = 2,
        stopOnAnyIntent: Boolean = false,
        stopOnAnyIntentThreshold: Double = 1.0,
        body: CailaIntentActivator.() -> Unit
    ): Unit = CailaIntentActivator(
        scenario.model,
        CailaNLUSettings(
            "token",
            confidenceThreshold = confidenceThreshold,
            cailaSlotFillingSettings = CailaSlotFillingSettings(
                maxSlotRetries, stopOnAnyIntent, stopOnAnyIntentThreshold
            )
        ),
        cailaHttpClientMock
    ).run(body)

    fun Activator.activate(request: BotRequest, botContext: BotContext = context) =
        activate(botContext, request, ActivationSelector.default)

    fun Activator.mustActivate(request: BotRequest, botContext: BotContext = context) =
        assertNotNull(activate(botContext, request, ActivationSelector.default))

    fun Activator.mustNotActivate(request: BotRequest, botContext: BotContext = context) =
        assertNull(activate(botContext, request, ActivationSelector.default))

    fun CailaIntentActivator.recognizeIntent(request: BotRequest) = recogniseIntent(context, request)

    fun Activator.fillSlots(
        request: BotRequest,
        reactions: Reactions = createReactions(),
        botContext: BotContext = context,
        activation: Activation? = null,
        slotReactor: SlotReactor? = null
    ) = fillSlots(request, reactions, botContext, activation?.context, slotReactor)

    fun Activation.assertCaila(body: CailaIntentActivatorContext.() -> Unit) = intentContext.body()

    fun SlotFillingFinished.assertCaila(body: CailaIntentActivatorContext.() -> Unit) = intentContext.body()

    inline fun <reified T> Any?.assertType(): T = run {
        assertTrue(this is T)
        this
    }

    fun CailaIntentActivatorContext.assertSlot(name: String, value: String) {
        val slot = assertNotNull(slots[name])
        assertEquals(value, slot)
    }

    private fun createReactions() = mockk<TestReactions>(relaxed = true).also { _reactions = it }

    fun saveContext(botContext: BotContext) =
        InMemoryBotContextManager.saveContext(botContext, QueryBotRequest(clientId, ""), null, RequestContext.DEFAULT)

    fun loadContext(): BotContext =
        InMemoryBotContextManager.loadContext(QueryBotRequest(clientId, ""), RequestContext.DEFAULT)

    fun exchangeContext(botContext: BotContext) = saveContext(botContext).run { loadContext() }

    fun query(input: String) = QueryBotRequest(clientId, input).also {
        request = it
        _context = _context?.let(::exchangeContext) ?: loadContext()
    }

    @StateDeclaration
    fun ScenarioGraphBuilder<*, *>.intent(@StateName name: String) = state(name) { activators { intent(name) } }

    @StateDeclaration
    fun ScenarioGraphBuilder<*, *>.entity(@StateName name: String) = state(name) { activators { cailaEntity(name) } }

    private fun getCailaResponse(query: String, method: String) =
        javaClass.getResourceAsStream("/caila_responses/$method/${query.replace(" ", "_").toLowerCase()}.json")!!
}