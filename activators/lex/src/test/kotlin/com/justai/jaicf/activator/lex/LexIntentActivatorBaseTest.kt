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
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lexruntimev2.LexRuntimeV2Client
import software.amazon.awssdk.services.lexruntimev2.model.DialogActionType
import software.amazon.awssdk.services.lexruntimev2.model.IntentState
import software.amazon.awssdk.services.lexruntimev2.model.Message
import software.amazon.awssdk.services.lexruntimev2.model.MessageContentType
import software.amazon.awssdk.services.lexruntimev2.model.RecognizeTextRequest
import software.amazon.awssdk.services.lexruntimev2.model.RecognizeTextResponse
import software.amazon.awssdk.services.lexruntimev2.model.Slot
import software.amazon.awssdk.services.lexruntimev2.model.Value
import java.util.*

@ExtendWith(MockKExtension::class)
abstract class LexIntentActivatorBaseTest(scenario: Scenario) {
    private val client = mockk<LexRuntimeV2Client>()

    private val bot = BotEngine(
        scenario = scenario,
        activators = arrayOf(
            LexIntentActivator.Factory(
                LexConnector(
                    LexBotConfig(
                        "botId", "aliasId", Region.AP_NORTHEAST_1, Locale.US
                    ),
                    client
                )
            ),
            CatchAllActivator
        )
    )

    @BeforeEach
    fun init() = clearAllMocks()

    fun botTest(body: BotTest.() -> Any?) {
        BotTest(bot).apply { init() }.body()
    }

    fun respondWith(
        dialogActionType: DialogActionType,
        intentState: IntentState,
        intent: String? = null,
        responseMessage: String? = null,
        slotToElicit: String? = null,
        slots: Map<String, String?> = emptyMap()
    ) {
        val response = RecognizeTextResponse.builder().apply {
            sessionState {
                it.dialogAction { action ->
                    action.type(dialogActionType)
                    action.slotToElicit(slotToElicit)
                }
                it.intent { int ->
                    int.name(intent)
                    int.state(intentState)
                    int.slots(slots.mapValues { entry -> entry.value?.toSlot() })
                }
            }
            messages(
                Message.builder().apply {
                    contentType(MessageContentType.PLAIN_TEXT)
                    content(responseMessage)
                }.build()
            )
        }.build()

        every { client.recognizeText(any<RecognizeTextRequest>()) } returns response
    }

    fun respondClose(
        intent: String,
        responseMessage: String? = null,
        slots: Map<String, String?> = emptyMap()
    ) = respondWith(DialogActionType.CLOSE, IntentState.FULFILLED, intent, responseMessage, slots = slots)

    fun respondElicitSlot(
        intent: String,
        responseMessage: String? = null,
        slotToElicit: String,
        slots: Map<String, String?> = mapOf(slotToElicit to null)
    ) = respondWith(DialogActionType.ELICIT_SLOT, IntentState.IN_PROGRESS, intent, responseMessage, slotToElicit, slots)

    fun respondConfirm(
        intent: String,
        confirmationMessage: String? = null,
        slots: Map<String, String?> = emptyMap()
    ) = respondWith(
        DialogActionType.CONFIRM_INTENT,
        IntentState.IN_PROGRESS,
        intent,
        confirmationMessage,
        slots = slots
    )

    fun respondElicitIntent(responseMessage: String? = null) =
        respondWith(DialogActionType.ELICIT_INTENT, IntentState.FULFILLED, responseMessage = responseMessage)

    fun respondFailed(responseMessage: String? = null) =
        respondWith(DialogActionType.CLOSE, IntentState.FAILED, responseMessage = responseMessage)
}

private fun String.toSlot(): Slot =
    Slot.builder().value {
        Value.builder().apply {
            interpretedValue(this@toSlot)
            originalValue(this@toSlot)
            resolvedValues(this@toSlot)
        }.build()
    }.build()

