package com.justai.jaicf.activator.lex

import com.justai.jaicf.BotEngine
import com.justai.jaicf.activator.catchall.CatchAllActivator
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.test.BotTest
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.awssdk.services.lexruntime.model.DialogState
import software.amazon.awssdk.services.lexruntime.model.PostTextResponse

@ExtendWith(MockKExtension::class)
abstract class LexIntentActivatorBaseTest(scenario: Scenario) {
    private val connector = mockk<LexConnector>()

    private val bot = BotEngine(
        model = scenario.model,
        activators = arrayOf(
            LexIntentActivator.Factory(connector),
            CatchAllActivator
        )
    )

    @BeforeEach
    fun init() = clearAllMocks()

    fun botTest(body: BotTest.() -> Any?) {
        BotTest(bot).apply { init() }.body()
    }

    fun respondWith(
        dialogState: DialogState,
        intent: String? = null,
        nluConfidence: Float? = null,
        responseMessage: String? = null,
        slotToElicit: String? = null,
        slots: Map<String, String?> = emptyMap()
    ) {
        val response = PostTextResponse.builder()
            .dialogState(dialogState)
            .intentName(intent)
            .nluIntentConfidence { it.score(nluConfidence?.toDouble()) }
            .message(responseMessage)
            .slotToElicit(slotToElicit)
            .slots(slots)
            .build()

        every { connector.postText(any(), any()) } returns response
    }

    fun respondReadyForFulfillment(
        intent: String,
        nluConfidence: Float = 1f,
        responseMessage: String? = null,
        slots: Map<String, String?> = emptyMap()
    ) = respondWith(DialogState.READY_FOR_FULFILLMENT, intent, nluConfidence, responseMessage, slots = slots)

    fun respondElicitSlot(
        intent: String,
        nluConfidence: Float = 1f,
        responseMessage: String? = null,
        slotToElicit: String,
        slots: Map<String, String?> = mapOf(slotToElicit to null)
    ) = respondWith(DialogState.ELICIT_SLOT, intent, nluConfidence, responseMessage, slotToElicit, slots)

    fun respondElicitIntent(responseMessage: String? = null) =
        respondWith(DialogState.ELICIT_INTENT, responseMessage = responseMessage)

    fun respondFailed(responseMessage: String? = null) =
        respondWith(DialogState.FAILED, responseMessage = responseMessage)
}