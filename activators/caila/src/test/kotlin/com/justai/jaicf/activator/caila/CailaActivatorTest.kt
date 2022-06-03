package com.justai.jaicf.activator.caila

import com.justai.jaicf.builder.Scenario
import com.justai.jaicf.slotfilling.SlotFillingFinished
import com.justai.jaicf.slotfilling.SlotFillingInProgress
import com.justai.jaicf.slotfilling.SlotFillingInterrupted
import io.mockk.Called
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.RepetitionInfo
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CailaActivatorTest : CailaActivatorBaseTest() {
    override val scenario = Scenario {
        intent("Hello")
        intent("Order")

        state("onlyHello", modal = true) {
            intent("Hello")
        }

        state("onlyOrder", modal = true) {
            intent("Order")
        }

        state("entity", modal = true) {
            entity("Pizza")
            entity("duckling.time")
        }
    }

    @Nested
    inner class IntentActivationTest {

        @Test
        fun `Should recognize all intents above confidence threshold`() = test(confidenceThreshold = 0.0) {
            recognizeIntent(query("hello")).run {
                assertTrue(any { it.intent == "Hello" })
                assertTrue(any { it.intent == "Order" })
            }
        }

        @Test
        fun `Should not recognize intents below confidence threshold`() = test(confidenceThreshold = 0.3) {
            recognizeIntent(query("order")).run {
                assertTrue(any { it.intent == "Order" })
                assertFalse(any { it.intent == "Hello" })
            }
        }

        @Test
        fun `Should activate intent Hello above confidence threshold`() = test(confidenceThreshold = 0.3) {
            context.dialogContext.currentContext = "/onlyHello"
            mustActivate(query("hello")).run {
                assertEquals("Hello", intentContext.intent)
                assertEquals("/onlyHello/Hello", state)
            }
        }

        @Test
        fun `Should activate intent Order above confidence threshold`() = test(confidenceThreshold = 0.3) {
            context.dialogContext.currentContext = "/onlyOrder"
            mustActivate(query("order")).run {
                assertEquals("Order", intentContext.intent)
                assertEquals("/onlyOrder/Order", state)
            }
        }


        @Test
        fun `Should not activate intent Hello below confidence threshold`() = test(confidenceThreshold = 0.3) {
            context.dialogContext.currentContext = "/onlyOrder"
            mustNotActivate(query("hello"))
        }

        @Test
        fun `Should not activate intent Order below confidence threshold`() = test(confidenceThreshold = 0.3) {
            context.dialogContext.currentContext = "/onlyHello"
            mustNotActivate(query("order"))
        }

        @Test
        fun `Should activate most confident intent Hello`() = test(confidenceThreshold = 0.0) {
            mustActivate(query("hello")).run {
                assertEquals("Hello", intentContext.intent)
                assertEquals("/Hello", state)
            }
        }

        @Test
        fun `Should activate most confident intent Order`() = test(confidenceThreshold = 0.0) {
            mustActivate(query("order")).run {
                assertEquals("Order", intentContext.intent)
                assertEquals("/Order", state)
            }
        }
    }

    @Nested
    inner class SlotFillingTest {

        @Test
        fun `Should not start slotfilling for Hello`() = test {
            val activation = mustActivate(query("hello"))
            fillSlots(query("hello"), activation = activation).assertType<SlotFillingFinished>().assertCaila {
                assertEquals("Hello", intent)
            }
        }

        @Test
        fun `Should start slotfilling for Order without all slots`() = test {
            val activation = mustActivate(query("order"))
            fillSlots(query("order"), activation = activation).assertType<SlotFillingInProgress>()
        }

        @Test
        fun `Should answer with slot prompt on slotfilling in progress`() = test {
            val activation = mustActivate(query("order 10 am"))
            fillSlots(query("order 10 am"), activation = activation).assertType<SlotFillingInProgress>()

            verify { reactions.say("what do you want to order?") }
        }

        @Test
        fun `Should not answer with slot prompt on slotfilling finished`() = test {
            val activation = mustActivate(query("order pizza 10 am"))
            fillSlots(query("order pizza 10 am"), activation = activation).assertType<SlotFillingFinished>()

            verify { reactions wasNot Called }
        }

        @Test
        fun `Should start slotfilling for Order without required slots`() = test {
            val activation = mustActivate(query("order 10 am"))
            fillSlots(query("order 10 am"), activation = activation).assertType<SlotFillingInProgress>()
        }

        @Test
        fun `Should not start slotfilling for Order with all slots`() = test {
            val activation = mustActivate(query("order pizza 10 am"))
            fillSlots(query("order pizza 10 am"), activation = activation).assertType<SlotFillingFinished>().assertCaila {
                assertEquals("Order", intent)
                assertSlots("pizza" to "pizza", "time" to "10 am")
                assertEntities("Pizza" to "pizza", "duckling.time" to "10 am")
            }
        }

        @Test
        fun `Should not start slotfilling for Order with all required slots`() = test {
            val activation = mustActivate(query("order pizza"))
            fillSlots(query("order pizza"), activation = activation).assertType<SlotFillingFinished>().assertCaila {
                assertEquals("Order", intent)
                assertSlots("pizza" to "pizza")
                assertEntities("Pizza" to "pizza")
            }
        }

        @Test
        fun `Should fill all required slots`() = test {
            val activation = mustActivate(query("order"))
            fillSlots(query("order"), activation = activation).assertType<SlotFillingInProgress>()
            fillSlots(query("order 10 am")).assertType<SlotFillingInProgress>()
            fillSlots(query("order pizza 10 am")).assertType<SlotFillingFinished>().assertCaila {
                assertEquals("Order", intent)
                assertSlots("pizza" to "pizza", "time" to "10 am")
                assertEntities("Pizza" to "pizza", "duckling.time" to "10 am")
            }
        }

        @Test
        fun `Should fill all required slots 2`() = test {
            val activation = mustActivate(query("order 10 am"))
            fillSlots(query("order 10 am"), activation = activation).assertType<SlotFillingInProgress>()
            fillSlots(query("order pizza 10 am")).assertType<SlotFillingFinished>().assertCaila {
                assertEquals("Order", intent)
                assertSlots("pizza" to "pizza", "time" to "10 am")
                assertEntities("Pizza" to "pizza", "duckling.time" to "10 am")
            }
        }

        @Test
        fun `Should stop slotfilling after max retries`() = test(stopOnAnyIntent = false, maxSlotRetries = 3) {
            val activation = mustActivate(query("order"))
            fillSlots(query("order"), activation = activation).assertType<SlotFillingInProgress>()
            fillSlots(query("order 10 am")).assertType<SlotFillingInProgress>()
            fillSlots(query("hello")).assertType<SlotFillingInProgress>()
            fillSlots(query("hello")).assertType<SlotFillingInterrupted>()
        }

        @Test
        fun `Should interrupt slot filling if enabled with big confidence`() = test(stopOnAnyIntent = true, stopOnAnyIntentThreshold = 0.7) {
            val activation = mustActivate(query("order"))
            fillSlots(query("order"), activation = activation).assertType<SlotFillingInProgress>()
            fillSlots(query("hello")).assertType<SlotFillingInterrupted>()
        }

        @Test
        fun `Should not interrupt slot filling if enabled with small confidence`() = test(stopOnAnyIntent = true, stopOnAnyIntentThreshold = 0.9) {
            val activation = mustActivate(query("order"))
            fillSlots(query("order"), activation = activation).assertType<SlotFillingInProgress>()
            fillSlots(query("hello")).assertType<SlotFillingInProgress>()
        }

        @Test
        fun `Should not interrupt slot filling if disabled`() = test(stopOnAnyIntent = false) {
            val activation = mustActivate(query("order"))
            fillSlots(query("order"), activation = activation).assertType<SlotFillingInProgress>()
            fillSlots(query("hello")).assertType<SlotFillingInProgress>()
        }

        @RepeatedTest(4)
        fun `Should interrupt slot filling on start`(repetition: RepetitionInfo) = test(stopOnAnyIntent = false) {
            val starts = arrayOf("/start", "/START", "/Start", "/StArT")
            val activation = mustActivate(query("order"))
            fillSlots(query("order"), activation = activation).assertType<SlotFillingInProgress>()
            fillSlots(query(starts[repetition.currentRepetition - 1])).assertType<SlotFillingInterrupted>()
        }
    }

    @Nested
    inner class EntitiesActivationTest {

        @Test
        fun `Should activate entity time`() = test {
            context.dialogContext.currentContext = "/entity"
            mustActivate(query("order 10 am")).run {
                assertEquals("/entity/duckling.time", state)
                assertEquals("duckling.time", entityContext.entity)
                assertEquals("10 am", entityContext.text)
                assertEquals("10 am", entityContext.value)
            }
        }

        @Test
        fun `Should activate entity pizza`() = test {
            context.dialogContext.currentContext = "/entity"
            mustActivate(query("order pizza")).run {
                assertEquals("/entity/Pizza", state)
                assertEquals("Pizza", entityContext.entity)
                assertEquals("pizza", entityContext.text)
                assertEquals("pizza", entityContext.value)
            }
        }

        @Test
        fun `Should not activate if no entity`() = test {
            context.dialogContext.currentContext = "/entity"
            mustNotActivate(query("hello"))
        }
    }
}
