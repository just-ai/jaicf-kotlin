package com.justai.jaicf.activator.rasa.test

import com.justai.jaicf.activator.Activator
import com.justai.jaicf.activator.rasa.RasaActivatorContext
import com.justai.jaicf.activator.rasa.RasaIntentActivator
import com.justai.jaicf.activator.rasa.api.RasaApi
import com.justai.jaicf.activator.rasa.api.RasaParseMessageRequest
import com.justai.jaicf.activator.rasa.rasa
import com.justai.jaicf.activator.selection.ActivationSelector
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.context.DialogContext
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RasaDeserializationTest {

    private fun api(response: String) = RasaApi("", httpClient = MockEngine { respond(response.toByteArray()) })

    private fun activator(response: String) = RasaIntentActivator.Factory(api(response)).create(
        Scenario {
            state("hello") {
                activators {
                    intent("Hello")
                }
            }
        }.model
    )

    private fun rasaResponse(entities: String) = """
        {
            "text": "Sample",
            "intents": [
                {
                    "name": "Hello",
                    "confidence": 0.5
                }
            ],
            "intent_ranking": [
                {
                    "name": "Hello",
                    "confidence": 0.5
                }
            ],
            $entities
        }
    """.trimIndent()

    private fun Activator.activate() = assertNotNull(
        activate(
            BotContext("", DialogContext()),
            QueryBotRequest("", ""),
            ActivationSelector.default
        )?.context as? RasaActivatorContext
    )

    @Test
    fun `Should deserialize entity`() {
        val ctx = activator(rasaResponse("""
            "entities": [
                {
                    "start": 0,
                    "end": 10,
                    "confidence": 0.5,
                    "value": "value",
                    "entity": "entity"
                }
            ]
        """.trimIndent()
        )).activate()

        val entity = assertNotNull(ctx.entities.find { it.entity == "entity" })
        assertEquals(0.5f, entity.confidence)
    }

    @Test
    fun `Should deserialize entity without confidence`() {
        val ctx = activator(rasaResponse("""
            "entities": [
                {
                    "start": 0,
                    "end": 10,
                    "value": "value",
                    "entity": "entity"
                }
            ]
        """.trimIndent()
        )).activate()

        val entity = assertNotNull(ctx.entities.find { it.entity == "entity" })
        assertEquals(null, entity.confidence)
    }

    @Test
    fun `Should store raw request in ActivatorContext`() {
        val ctx = activator(rasaResponse("""
            "entities": [
                {
                    "start": 0,
                    "end": 10,
                    "confidence_entity": 0.5,
                    "value": "value",
                    "entity": "entity"
                }
            ]
        """.trimIndent()
        )).activate()

        val entity = assertNotNull(ctx.entities.find { it.entity == "entity" })
        assertEquals(null, entity.confidence)
        assertEquals(
            0.5f,
            ctx.rawResponse["entities"]?.jsonArray
                ?.find { it.jsonObject["entity"]?.jsonPrimitive?.contentOrNull == "entity" }?.jsonObject
                ?.get("confidence_entity")?.jsonPrimitive?.floatOrNull
        )
    }
}