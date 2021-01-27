package com.justai.jaicf.test.mockjvm.tests

import com.justai.jaicf.channel.telegram.telegram
import com.justai.jaicf.model.scenario.Scenario
import com.justai.jaicf.test.ScenarioTest
import com.justai.jaicf.test.mockjvm.query
import com.justai.jaicf.test.mockjvm.withChannel
import io.mockk.MockKException
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


private val tgScenario = object : Scenario() {
    init {
        state("defaultMethods") {
            activators {
                regex("defaultMethods")
            }
            action(telegram) {
                reactions.say("I'm in state with default methods test")
                reactions.say("dynamic")
                reactions.image("test")
                reactions.audio("test")
                reactions.sayRandom("arg1", "arg2")
                reactions.sayRandom(listOf("arg1", "arg2"))
                reactions.go("child")
            }

            state("child") {
                activators {
                    regex("child")
                }
                action {
                    reactions.say("i'm in child")
                    reactions.goBack("result")
                    reactions.go("child")
                }

                state("child") {
                    activators {
                        regex("child")
                    }
                    action {
                        reactions.say("i'm in child")
                    }
                }
            }
        }

        state("nativeMethodMixin") {
            activators {
                regex("nativeMethodMixin")
            }
            action(telegram) {
                reactions.say("i'm in nativeMethodMixin")
                reactions.sendMessage("native call")
            }
        }
    }
}

class DefaultsTest : ScenarioTest(tgScenario.model) {

    @Test
    fun `should answer to common methods with default reactions api`() = withChannel(telegram) {
        query("defaultMethods").run {
            goesToState("/defaultMethods")
            visitsState("/defaultMethods/child")
            endsWithState("/defaultMethods/child/child")
            hasAnswer("I'm in state with default methods test")
        }
    }

    @Test
    fun `should ignore native call`() = withChannel(telegram) {
        query("nativeMethodMixin") goesToState "/nativeMethodMixin"
    }

    @Test
    fun `should raise error with invalid native call content`() = withChannel(telegram) {
        assertThrows<AssertionError> {
            query("nativeMethodMixin") {
                calls { sendMessage("invalid text which should cause error") }
            } goesToState "/nativeMethodMixin"
        }
    }

    @Test
    fun `should mix native calls with default reactions api`() = withChannel(telegram) {
        query("nativeMethodMixin") {
            calls { sendMessage("native call") }
        } goesToState "/nativeMethodMixin"
    }
}

